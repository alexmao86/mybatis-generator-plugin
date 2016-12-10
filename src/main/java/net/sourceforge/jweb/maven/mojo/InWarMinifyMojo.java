package net.sourceforge.jweb.maven.mojo;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sourceforge.jweb.maven.minify.YUICompressorNoExit;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * InWarMinifyMojo can mini js and css in war package, it can work through your final war package and minifier js, css and freemarker files.
 * <br>
 * <i>Usage: mvn maven-jweb-plugin:warmini</i>
 * @author maoanapex88@163.com
 * @goal warmini
 */
public class InWarMinifyMojo extends MinifyMojo {
	/**
	 * @parameter
	 */
	private boolean disabled=false;
	private DecimalFormat decimalFormat=new DecimalFormat("0.00");
	private NumberFormat numberFormat=NumberFormat.getPercentInstance();
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(disabled) return ;
		processConfiguration();
		String name=this.getBuilddir().getAbsolutePath()+File.separator+this.getFinalName()+"."+this.getPacking();
		this.getLog().info(name);
		MinifyFileFilter fileFilter=new MinifyFileFilter();
		int counter=0;
		try {
			File finalWarFile=new File(name);
			File tempFile = File.createTempFile(finalWarFile.getName(), null);
			tempFile.delete();//check deletion
			boolean renameOk=finalWarFile.renameTo(tempFile);
			if (!renameOk) {
		        getLog().error("Can not rename file, please check.");
		    }
			
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(finalWarFile));
			ZipFile zipFile = new ZipFile(tempFile);
			Enumeration<? extends ZipEntry> entries=zipFile.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry=entries.nextElement();
				//no compress, just transfer to war
				if(!fileFilter.accept(entry)){
					getLog().debug("nocompress entry: "+entry.getName());
					out.putNextEntry(entry);
					InputStream inputStream=zipFile.getInputStream(entry);
					byte[] buf=new byte[512];
					int len=-1;
		            while ((len = inputStream.read(buf)) > 0) {
		                out.write(buf, 0, len);
		            }
		            inputStream.close();
					continue;
				}
				
				File sourceTmp=new File(FileUtils.getUserDirectoryPath()+File.separator+".mvntmp"+File.separator+counter+".tmp");
				File destTmp=new File(FileUtils.getUserDirectoryPath()+File.separator+".mvntmp"+File.separator+counter+".min.tmp");
				FileUtils.writeStringToFile(sourceTmp, "");
				FileUtils.writeStringToFile(destTmp, "");
				
				//assemble arguments
				String [] provied=getYuiArguments();
				int length = (provied==null?0:provied.length);
				length+=5;
				int i=0;
				
				String[] ret=new String[length];
				
				ret[i++]="--type";
				ret[i++]=(entry.getName().toLowerCase().endsWith(".css")?"css":"js");
				
				if(provied!=null){
					for(String s:provied){
						ret[i++]=s;
					}
				}
				
				ret[i++]=sourceTmp.getAbsolutePath();
				ret[i++]="-o";
				ret[i++]=destTmp.getAbsolutePath();
				
				try {
					InputStream in=zipFile.getInputStream(entry);
					FileUtils.copyInputStreamToFile(in, sourceTmp);
					in.close();
					
					YUICompressorNoExit.main(ret);
				} catch (Exception e) {
					this.getLog().warn("compress error, this file will not be compressed:"+buildStack(e));
					FileUtils.copyFile(sourceTmp, destTmp);
				}
				
				out.putNextEntry(new ZipEntry(entry.getName()));
				InputStream compressedIn=new FileInputStream(destTmp);
				byte[] buf=new byte[512];
				int len=-1;
	            while ((len = compressedIn.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	            compressedIn.close();
	            
	            String sourceSize=decimalFormat.format(sourceTmp.length()*1.0d/1024)+" KB";
				String destSize=decimalFormat.format(destTmp.length()*1.0d/1024)+" KB";
				getLog().info("compressed entry:"+entry.getName()+" ["+sourceSize+" ->"+destSize+"/"+numberFormat.format(1-destTmp.length()*1.0d/sourceTmp.length())+"]");
				
				counter++;
			}
			zipFile.close();
			out.close();
			
			FileUtils.cleanDirectory(new File(FileUtils.getUserDirectoryPath()+File.separator+".mvntmp"));
			FileUtils.forceDelete(new File(FileUtils.getUserDirectoryPath()+File.separator+".mvntmp"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	class MinifyFileFilter{
		public boolean accept(ZipEntry e) {
			if(e.isDirectory()) return false;
			return getMimes().contains(getEntryType(e))&&matchPath(e);
		}
	}
	private String getEntryType(ZipEntry e) {
		String result = null;
		String fname = e.getName();
		int index = fname.lastIndexOf(".");
		if (index > 0) {
			String type = fname.substring(index + 1);
			result = type.toLowerCase();
		}
		return result;
	}

	private boolean matchPath(ZipEntry e) {
		if(getExPatterns().isEmpty()) return true;
		for(String p:getExPatterns()){
			String path=e.getName();
			if(getMatcher().match(p, path)) {
				return false;
			}
		}
		return true;
	}

	public static String buildStack(Throwable t) {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(arrayOutputStream);
		t.printStackTrace(printStream);
		String ret = arrayOutputStream.toString();
		try {
			arrayOutputStream.close();
			printStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
