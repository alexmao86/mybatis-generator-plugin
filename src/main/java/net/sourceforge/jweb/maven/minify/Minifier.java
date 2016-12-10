package net.sourceforge.jweb.maven.minify;
import java.io.File;
import java.io.IOException;

import net.sourceforge.jweb.maven.mojo.MinifyMojo;

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
 * 
 * The Minifier interface, minifier is used for creating -mini.js, -mini.css.
 * @see net.sourceforge.jweb.maven.mojo.InWarMinifyMojo
 * @see net.sourceforge.jweb.maven.mojo.MinifyMojo
 * @author maoanapex88@163.com
 *
 */
public interface Minifier {
	/**
	 * minify one file source
	 * @param f
	 * @throws IOException
	 */
	public void minify(File f) throws IOException;
	public Log getLog();
	/**
	 * minifier mojo delegate
	 * @return instance of minifier
	 */
	public MinifyMojo getMinifyMojo();
}
