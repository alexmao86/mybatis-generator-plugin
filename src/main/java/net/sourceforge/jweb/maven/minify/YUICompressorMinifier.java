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
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * Yahoo UI compressor minifier wrapper for invoking 
 * @see net.sourceforge.jweb.maven.minify.YUICompressorNoExit
 * @author maoanapex88@163.com
 *
 */
public class YUICompressorMinifier extends AbstractMinifier {
	public void minify(File f) {
		File saveTo=getSaveFile(f, "");
		
		String parent=saveTo.getParent();
		String pname=saveTo.getName().substring(0, saveTo.getName().indexOf("."));
		String sname=saveTo.getName().substring(saveTo.getName().lastIndexOf("."));
		String finalName=parent+File.separator+pname+this.getMinifyMojo().getMiniPrefix()+sname;
		File finalFile=new File(finalName);
		try {
			FileUtils.writeStringToFile(finalFile, "");
			
			String [] provied=this.getMinifyMojo().getYuiArguments();
			int length = (provied==null?0:provied.length);
			length+=5;
			int i=0;
			
			String[] ret=new String[length];
			
			ret[i++]="--type";
			ret[i++]=(saveTo.getName().toLowerCase().endsWith(".css")?"css":"js");
			
			if(provied!=null){
				for(String s:provied){
					ret[i++]=s;
				}
			}
			
			ret[i++]=f.getAbsolutePath();
			ret[i++]="-o";
			ret[i++]=finalName;
			StringBuilder builder=new StringBuilder();
			builder.append("yuicompressor ");
			for(String s:ret){
				builder.append(s).append(" ");
			}
			this.getLog().debug(builder);
			
			YUICompressorNoExit.main(ret);
		} catch (Exception e) {
			//e.printStackTrace();
			this.getLog().warn(e);
			this.getLog().info("minifier will copy source only");
			try {
				FileUtils.copyFile(f, finalFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
