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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.jweb.maven.util.AntPathMatcher;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;

/**
 * 
 * base Mojo
 * @author maoanapex88@163.com
 *
 */
public abstract class AbstractMinifyMojo extends AbstractMojo{
	public static final String DOT = ".";
	
	/**
	 * @parameter expression="${project.basedir}"
	 * @required
	 * @readonly
	 */
	protected File basedir;
	
	/**
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 * @readonly
	 */
	protected File sourcedir;
	/**
	 * @parameter expression="${project.build.testSourceDirectory}"
	 * @required
	 * @readonly
	 */
	protected File testSourcedir;
	/**
	 * @parameter expression="${project.resources}"
	 * @required
	 * @readonly
	 */
	protected List<Resource> resources;
	/**
	 * @parameter expression="${project.testResources}"
	 * @required
	 * @readonly
	 */
	protected List<Resource> testResources;
	/**
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected File builddir;
	/**
	 * Location of the built artifact
	 * @parameter expression="${project.build.finalName}
	 * @required
	 */
	protected String finalName;
	/**
	 * Location of the built artifact
	 * @parameter expression="${project.packaging}
	 * @required
	 */
	protected String packing;
	/**
	 * @parameter
	 */
	protected String[] minifies;
	
	/**
	 * @parameter
	 */
	protected String miniPrefix="";
	
	/**
	 * @parameter
	 */
	protected String webapp="webapp";
	
	protected Set<String> mimes;
	/**
	 * @parameter
	 */
	protected String[] excludes;
	
	/**
	 * @parameter
	 */
	protected String[] yuiArguments;
	
	protected Set<String> exPatterns;

	protected AntPathMatcher matcher=new AntPathMatcher();
	
	protected void processConfiguration() {
		//matcher.setPathSeparator(File.separator);
		getLog().debug("builddir:"+builddir);
		mimes=new HashSet<String>();
		if(minifies!=null){
			for(String include:minifies){
				mimes.add(include);
			}
		}
		exPatterns=new HashSet<String>();
		if(excludes!=null){
			for(String exclude:excludes){
				exPatterns.add(exclude);
			}
		}
	}
	//----
	public File getBasedir() {
		return basedir;
	}

	public static String getDot() {
		return DOT;
	}

	public File getSourcedir() {
		return sourcedir;
	}

	public File getTestSourcedir() {
		return testSourcedir;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public List<Resource> getTestResources() {
		return testResources;
	}

	public File getBuilddir() {
		return builddir;
	}

	public String[] getMinifies() {
		return minifies;
	}

	public Set<String> getMimes() {
		return mimes;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public Set<String> getExPatterns() {
		return exPatterns;
	}

	public AntPathMatcher getMatcher() {
		return matcher;
	}
	public String getFinalName() {
		return finalName;
	}

	public String getPacking() {
		return packing;
	}

	public String getWebapp() {
		return webapp;
	}

	public String[] getYuiArguments() {
		return yuiArguments;
	}

	public String getMiniPrefix() {
		return miniPrefix;
	}
}
