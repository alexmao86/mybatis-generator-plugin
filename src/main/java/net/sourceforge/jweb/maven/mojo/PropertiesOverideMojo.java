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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * override properties config file mojo, use #{...} as substitution
 * <br>
 * <i>Usage: mvn maven-jweb-plugin:overwrite</i>
 * @goal overwrite
 * @author maoanapex88@163.com
 *
 */
public class PropertiesOverideMojo extends AbstractMojo{
	public static final String DOT = ".";
	/**
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	private File builddir;
	/**
	 * Location of the built artifact
	 * @parameter expression="${project.build.finalName}
	 * @required
	 */
	private String finalName;
	/**
	 * Location of the built artifact
	 * @parameter expression="${project.packaging}
	 * @required
	 */
	private String packing;
	/**
	 * @parameter
	 */
	private boolean disabled=false;
	/**
	 * file extensions for replacement, default .properties and .xml
	 * @parameter
	 */
	private String[] extensions;
	private Set<String> extensionSet;
	/**
	 * the profile to be used
	 * @parameter
	 */
	private String profile="";
	/**
	 * the encoding to be used
	 * @parameter
	 */
	private String encoding="UTF-8";
	/**
	 * entry array for replacement, use key=value
	 * @parameter
	 */
	private String[] entries;
	private Map<String, String> replacements;

	private void processConfiguration() {
		extensionSet=new HashSet<String>();
		if(extensions!=null){
			for(String ext:extensions) extensionSet.add(ext);
		}
		else {
			extensionSet.add(".properties");
			extensionSet.add(".xml");
			this.getLog().info("Replace default .properties and .xml file.");
		}
		
		replacements=new HashMap<String, String>();
		if(entries!=null){
			for(String entry:entries){
				String[] kv=entry.split("=");
				if(kv.length!=2){
					this.getLog().warn(entry+" is not correct entry, ignored. Please use k=v format.");
					continue;
				}
				
				if(kv[0].isEmpty()){
					this.getLog().warn(entry+" is not correct entry, ignored. Please use k=v format.");
					continue;
				}
				if(profile==null||profile.isEmpty()){
					replacements.put(kv[0], kv[1]);
					continue;
				}
				
				if(kv[0].startsWith(profile+".")){
					replacements.put(kv[0].substring(profile.length()+1), kv[1]);
					continue;
				}
			}
			this.getLog().info("Properties:");
			for(Entry<String, String> entry:replacements.entrySet()){
				this.getLog().info(entry.getKey()+"="+entry.getValue());
			}
		}
	}
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(disabled){
			this.getLog().info("plugin was disabled");
			return ;
		}
		processConfiguration();
		if(replacements.isEmpty()){
			this.getLog().info("Nothing to replace with");
			return ;
		}
		
		String name=this.builddir.getAbsolutePath()+File.separator+this.finalName+"."+this.packing;//the final package
		this.getLog().debug("final artifact: "+name);// the final package
		
		try {
			File finalWarFile=new File(name);
			File tempFile = File.createTempFile(finalWarFile.getName(), null);
			tempFile.delete();//check deletion
			boolean renameOk=finalWarFile.renameTo(tempFile);
			if (!renameOk) {
			    getLog().error("Can not rename file, please check.");
			    return ;
			}
			
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(finalWarFile));
			ZipFile zipFile = new ZipFile(tempFile);
			Enumeration<? extends ZipEntry> entries=zipFile.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry=entries.nextElement();
				if(acceptMime(entry)){
					getLog().info("applying replacements for "+entry.getName());
					InputStream inputStream=zipFile.getInputStream(entry);
					String src=IOUtils.toString(inputStream, encoding);
					//do replacement
					for(Entry<String, String> e : replacements.entrySet()){
						src=src.replaceAll("#\\{"+e.getKey()+"}", e.getValue());
					}
					out.putNextEntry(new ZipEntry(entry.getName()));
					IOUtils.write(src, out, encoding);
					inputStream.close();
				}
				else {
					//not repalce, just put entry back to out zip
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
			}
			zipFile.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean acceptMime(ZipEntry entry) {
		for(String ext:extensionSet){
			if(entry.getName().endsWith(ext)){
				return true;
			}
		}
		return false;
	}
	
	/*public static void main(String[] args) {
		System.out.println("#{abc} world.".replaceAll("#\\{abc}", "hello"));
	}*/
}
