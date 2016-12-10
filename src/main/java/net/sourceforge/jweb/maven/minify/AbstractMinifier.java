package net.sourceforge.jweb.maven.minify;
import java.io.File;

import net.sourceforge.jweb.maven.mojo.MinifyMojo;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.logging.Log;
/*
 * 
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
/**
 * The abstract minifier, defined the shared save file method
 * 
 * @author maoanapex88@163.com
 *
 */
public abstract class AbstractMinifier implements Minifier{
	private Log log;
	private MinifyMojo mojo;

	public Log getLog() {
		return log;
	}

	protected void setLog(Log log) {
		this.log = log;
	}
	protected void setMinifyMojo(MinifyMojo mojo){
		this.mojo=mojo;
	}
	public MinifyMojo getMinifyMojo(){
		return mojo;
	}
	/**
	 * get file instance for minifier from maven style project context
	 * @param source file source
	 * @param folder container folder
	 * @return
	 */
	public File getSaveFile(File source, String folder) {
		String sourcePath=source.getAbsolutePath();
		if(mojo.getResources()!=null){
			for(Resource r:mojo.getResources()){
				if(sourcePath.startsWith(r.getDirectory())){
					String base=mojo.getBuilddir().getAbsolutePath();
					String sub=sourcePath.replace(r.getDirectory(), "");
					return new File(base+folder+sub);
				}
			}
		}
		
		if(mojo.getTestResources()!=null){
			for(Resource r:mojo.getTestResources()){
				if(sourcePath.startsWith(r.getDirectory())){
					String base=mojo.getBuilddir().getAbsolutePath();
					String sub=sourcePath.replace(r.getDirectory(), "");
					return new File(base+folder+sub);
				}
			}
		}
		
		if(sourcePath.startsWith(mojo.getSourcedir().getAbsolutePath())){
			String base=mojo.getBuilddir().getAbsolutePath();
			String sub=sourcePath.replace(mojo.getSourcedir().getAbsolutePath(), "");
			return new File(base+folder+sub);
		}
		
		if(sourcePath.startsWith(mojo.getTestSourcedir().getAbsolutePath())){
			String base=mojo.getBuilddir().getAbsolutePath();
			String sub=sourcePath.replace(mojo.getTestSourcedir().getAbsolutePath(), "");
			return new File(base+folder+sub);
		}
		
		if(sourcePath.contains(File.separator+mojo.getWebapp()+File.separator)){
			String base=mojo.getBuilddir().getAbsolutePath()+File.separator;
			String sub=sourcePath.substring(sourcePath.indexOf(mojo.getWebapp()) + mojo.getWebapp().length());
			return new File(base+File.separator+mojo.getFinalName()+folder+sub);
		}
		
		if(sourcePath.startsWith(mojo.getBasedir().getAbsolutePath())){
			String base=mojo.getBuilddir().getAbsolutePath();
			String sub=sourcePath.replace(mojo.getBasedir().getAbsolutePath(), "");
			return new File(base+folder+sub);
		}
		
		return null;
	}
}
