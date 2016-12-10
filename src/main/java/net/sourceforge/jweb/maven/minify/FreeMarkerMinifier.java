package net.sourceforge.jweb.maven.minify;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * Freemarker file minifier, the minifier here is removing duplicate blanks and make text in one line.
 * the core execution here is regex replace "[\\s]{1,}" with empty string.
 * 
 * It read .ftl files from your web app's /WEB-INF/classes/
 * @author maoanapex88@163.com
 *
 */
public class FreeMarkerMinifier extends AbstractMinifier{

	public void minify(File f) throws IOException {
		File saveTo=getSaveFile(f, "/classes/");
		
		this.getLog().info("save to"+saveTo.getAbsolutePath());
		
		Writer out=new FileWriter(saveTo);
		Reader in=new FileReader(f);
		char[] buf = new char[1024];
		int length =-1;
		StringBuilder builder=new StringBuilder();
		while((length=in.read(buf))!= -1){
			builder.append(buf, 0, length);
			
		}
		out.write(builder.toString().replaceAll("[\\s]{1,}", " "));
		
		in.close();
		out.close();
	}
}
