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

import net.sourceforge.jweb.maven.mojo.MinifyMojo;
/**
 * 
 * One simple static factory to create Minifier Instance.
 * It returns,
 * <ul>
 *  <li>net.sourceforge.jweb.maven.minify.FreeMarkerMinifier for .ftl freemarker file</li>
 *  <li>net.sourceforge.jweb.maven.minify.YUICompressorMinifier for javascript file</li>
 *  <li>net.sourceforge.jweb.maven.minify.YUICompressorMinifier for css file</li>
 *  </ul>
 * return one dummy minifier for others
 * 
 * @author maoanapex88@163.com
 *
 */
public class MinifierFactory {
	public static Minifier create(final MinifyMojo mojo, String extension){
		AbstractMinifier ins= null;
		if("ftl".equalsIgnoreCase(extension)){
			ins= new FreeMarkerMinifier();
		}
		else if("js".equalsIgnoreCase(extension)){
			ins=new YUICompressorMinifier();
		}
		else if("css".equalsIgnoreCase(extension)){
			ins=new YUICompressorMinifier();
		}
		else {
			return new AbstractMinifier() {
				public void minify(File f) throws IOException {
					mojo.getLog().warn("unsupported mime type to minify");
				}
			};
		}

		ins.setLog(mojo.getLog());
		ins.setMinifyMojo(mojo);
		
		return ins;
	}
}
