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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jweb.maven.minify.Minifier;
import net.sourceforge.jweb.maven.minify.MinifierFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal mini
 * Mojo for minifier one file
 * <br>
 * <i>Usage: mvn maven-jweb-plugin:mini</i>
 */
public class MinifyMojo extends AbstractMinifyMojo {
	public void execute() throws MojoExecutionException, MojoFailureException {
		processConfiguration();
		if(mimes.isEmpty()){
			getLog().warn("minify do nothing, did you forget include file types configuration in your pom?");
			return ;
		}
		
		try {
			List<File> minifyFiles=new ArrayList<File>();
			
			collectFiles(minifyFiles, basedir);
			for(File f:minifyFiles){
				minifyFile(f, basedir);
			}
		} catch (IOException e) {
			e.printStackTrace();
			getLog().error(e.getMessage());
		}
	}

	private void minifyFile(File f, File base) throws IOException {
		
		long start=System.currentTimeMillis();
		Minifier minifier=MinifierFactory.create(this, getFileType(f));
		minifier.minify(f);
		long end = System.currentTimeMillis();
		getLog().info("minify "+f.getAbsolutePath()+", time costs "+(end-start)+" ms");
	}

	private void collectFiles(final List<File> collected, File base) throws IOException {
		getLog().debug("search files for minifying in:"+base.getAbsolutePath());
		if(base.getAbsolutePath().startsWith(this.getBuilddir().getAbsolutePath())){
			return ;
		}
		if (base.isFile()) {
			if (new MinifyFileFilter().accept(base)) {
				collected.add(base);
				getLog().debug("add file"+base.getAbsolutePath()+"  to minify list");
			}
		} else {
			File[] files=base.listFiles(new MinifyFileFilter());
			if(files==null) return ;
			for (File f : files) {
				collectFiles(collected, f);
			}
		}
	}
	
	private class MinifyFileFilter implements java.io.FileFilter {
		public boolean accept(File pathname) {
			if(pathname.isDirectory()) return false;
			return mimes.contains(getFileType(pathname))&&matchPath(pathname);
		}

	}
	private String getFileType(File file) {
		String result = null;
		String fname = file.getName();
		int index = fname.lastIndexOf(DOT);
		if (index > 0) {
			String type = fname.substring(index + 1);
			result = type.toLowerCase();
		}
		return result;
	}

	private boolean matchPath(File pathname) {
		if(exPatterns.isEmpty()) return true;
		for(String p:exPatterns){
			String path=pathname.getAbsolutePath().replace(basedir.getAbsolutePath(), "");
			if(matcher.match(p, path)) {
				return false;
			}
		}
		return true;
	}
	
	
}
