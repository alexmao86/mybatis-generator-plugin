/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.jweb.maven.minify;

import jargs.gnu.CmdLineParser;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Offical YUICompressor will ro System.exit(0); if error happens, in maven plugin,
 * I do not want to see this, this class is changed version of YUICompressor, if error happens,
 * rather java process will not exit than ignore minifier on file.
 * 
 * @author maoanapex88@163.com
 *
 */
public class YUICompressorNoExit {

    public static void main(String args[]) throws Exception{
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option typeOpt = parser.addStringOption("type");
        CmdLineParser.Option versionOpt = parser.addBooleanOption('V', "version");
        CmdLineParser.Option verboseOpt = parser.addBooleanOption('v', "verbose");
        CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
        CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
        CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
        CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
        CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
        CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
        CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");
        CmdLineParser.Option mungemapFilenameOpt = parser.addStringOption('m', "mungemap");

        Reader in = null;
        Writer out = null;
        Writer mungemap = null;

        try {

            parser.parse(args);

            Boolean help = (Boolean) parser.getOptionValue(helpOpt);
            if (help != null && help.booleanValue()) {
                usage();
                //System.exit(0);
            }

            Boolean version = (Boolean) parser.getOptionValue(versionOpt);
            if (version != null && version.booleanValue()) {
                version();
                //System.exit(0);
            }

            boolean verbose = parser.getOptionValue(verboseOpt) != null;

            String charset = (String) parser.getOptionValue(charsetOpt);
            if (charset == null || !Charset.isSupported(charset)) {
                // charset = System.getProperty("file.encoding");
                // if (charset == null) {
                //     charset = "UTF-8";
                // }

                // UTF-8 seems to be a better choice than what the system is reporting
                charset = "UTF-8";


                if (verbose) {
                    System.err.println("\n[INFO] Using charset " + charset);
                }
            }

            int linebreakpos = -1;
            String linebreakstr = (String) parser.getOptionValue(linebreakOpt);
            if (linebreakstr != null) {
            	linebreakpos = Integer.parseInt(linebreakstr, 10);
            }

            String typeOverride = (String) parser.getOptionValue(typeOpt);
            if (typeOverride != null && !typeOverride.equalsIgnoreCase("js") && !typeOverride.equalsIgnoreCase("css")) {
                usage();
                //System.exit(1);
            }

            boolean munge = parser.getOptionValue(nomungeOpt) == null;
            boolean preserveAllSemiColons = parser.getOptionValue(preserveSemiOpt) != null;
            boolean disableOptimizations = parser.getOptionValue(disableOptimizationsOpt) != null;

            String[] fileArgs = parser.getRemainingArgs();
            List<String> files = Arrays.asList(fileArgs);
            if (files.isEmpty()) {
                if (typeOverride == null) {
                    usage();
                  //  System.exit(1);
                }
                files = new java.util.ArrayList<String>();
                files.add("-"); // read from stdin
            }

            String output = (String) parser.getOptionValue(outputFilenameOpt);
            String pattern[];
            if(output == null) {
                pattern = new String[0];
            } else if (output.matches("(?i)^[a-z]\\:\\\\.*")){ // if output is with something like c:\ dont split it
                pattern = new String[]{output};
            } else {
                pattern = output.split(":");
            }
            
            String mungemapFilename = (String) parser.getOptionValue(mungemapFilenameOpt);
            if (mungemapFilename != null) {
                mungemap = new OutputStreamWriter(new FileOutputStream(mungemapFilename), charset);
            }

            Iterator<String> filenames = files.iterator();
            while(filenames.hasNext()) {
                String inputFilename = (String)filenames.next();
                String type = null;
                try {
                    if (inputFilename.equals("-")) {

                        in = new InputStreamReader(System.in, charset);
                        type = typeOverride;

                    } else {

                        if ( typeOverride != null ) {
                            type = typeOverride;
                        }
                        else {
                            int idx = inputFilename.lastIndexOf('.');
                            if (idx >= 0 && idx < inputFilename.length() - 1) {
                                type = inputFilename.substring(idx + 1);
                            }
                        }

                        if (type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
                            usage();
                            //System.exit(1);
                        }

                        in = new InputStreamReader(new FileInputStream(inputFilename), charset);
                    }

                    String outputFilename = output;
                    // if a substitution pattern was passed in
                    if (pattern.length > 1 && files.size() > 0) {
                        outputFilename = inputFilename.replaceFirst(pattern[0], pattern[1]);
                    }

                    if (type.equalsIgnoreCase("js")) {

                    	final String localFilename = inputFilename;

                        JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

                            public void warning(String message, String sourceName,
                                    int line, String lineSource, int lineOffset) {
                                System.err.println("\n[WARNING] in " + localFilename);
                                if (line < 0) {
                                    System.err.println("  " + message);
                                } else {
                                    System.err.println("  " + line + ':' + lineOffset + ':' + message);
                                }
                            }

                            public void error(String message, String sourceName,
                                    int line, String lineSource, int lineOffset) {
                                System.err.println("[ERROR] in " + localFilename);
                                if (line < 0) {
                                    System.err.println("  " + message);
                                } else {
                                    System.err.println("  " + line + ':' + lineOffset + ':' + message);
                                }
                            }

                            public EvaluatorException runtimeError(String message, String sourceName,
                                    int line, String lineSource, int lineOffset) {
                                error(message, sourceName, line, lineSource, lineOffset);
                                return new EvaluatorException(message);
                            }
                        });

                        // Close the input stream first, and then open the output stream,
                        // in case the output file should override the input file.
                        in.close(); in = null;

                        if (outputFilename == null) {
                            out = new OutputStreamWriter(System.out, charset);
                        } else {
                            out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
                            if (mungemap != null) {
                                mungemap.write("\n\nFile: "+outputFilename+"\n\n");
                            }
                        }
                        compressor.compress(out, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);

                    } else if (type.equalsIgnoreCase("css")) {
                        CssCompressor compressor = new CssCompressor(in);
                        // Close the input stream first, and then open the output stream,
                        // in case the output file should override the input file.
                        in.close(); in = null;
                        if (outputFilename == null) {
                            out = new OutputStreamWriter(System.out, charset);
                        } else {
                            out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
                        }
                        compressor.compress(out, linebreakpos);
                    }

                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            if (mungemap !=null) {
                try {
                    mungemap.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void version() {
        System.err.println("@VERSION@");
    }
    private static void usage() {
        System.err.println(
                "YUICompressor Version: @VERSION@\n"

                        + "\nUsage: java -jar yuicompressor-@VERSION@.jar [options] [input file]\n"
                        + "\n"
                        + "Global Options\n"
                        + "  -V, --version             Print version information\n"
                        + "  -h, --help                Displays this information\n"
                        + "  --type <js|css>           Specifies the type of the input file\n"
                        + "  --charset <charset>       Read the input file using <charset>\n"
                        + "  --line-break <column>     Insert a line break after the specified column number\n"
                        + "  -v, --verbose             Display informational messages and warnings\n"
                        + "  -m <file>                 Place a mapping of munged identifiers to originals in this file\n\n"
                        + "  -o <file>                 Place the output into <file>. Defaults to stdout.\n"
                        + "                            Multiple files can be processed using the following syntax:\n"
                        + "                            java -jar yuicompressor.jar -o '.css$:-min.css' *.css\n"
                        + "                            java -jar yuicompressor.jar -o '.js$:-min.js' *.js\n\n"

                        + "JavaScript Options\n"
                        + "  --nomunge                 Minify only, do not obfuscate\n"
                        + "  --preserve-semi           Preserve all semicolons\n"
                        + "  --disable-optimizations   Disable all micro optimizations\n\n"

                        + "If no input file is specified, it defaults to stdin. In this case, the 'type'\n"
                        + "option is required. Otherwise, the 'type' option is required only if the input\n"
                        + "file extension is neither 'js' nor 'css'.");
    }
}