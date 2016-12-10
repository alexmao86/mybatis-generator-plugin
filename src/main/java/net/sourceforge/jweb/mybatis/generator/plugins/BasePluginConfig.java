package net.sourceforge.jweb.mybatis.generator.plugins;
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
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.Properties;
import java.util.regex.Pattern;

public abstract class BasePluginConfig {

    private static final String excludeClassNamesRegexpKey = "excludeClassNamesRegexp";

    private Pattern excludeClassNamesRegexp;

    protected BasePluginConfig(Properties props) {
        String regexp = props.getProperty(excludeClassNamesRegexpKey, null);
        if (regexp != null)
            this.excludeClassNamesRegexp = Pattern.compile(regexp);
    }

    boolean shouldExclude(FullyQualifiedJavaType type) {
        return this.shouldExclude(type.getFullyQualifiedName());
    }

    boolean shouldExclude(String className) {
        return excludeClassNamesRegexp != null && excludeClassNamesRegexp.matcher(className).matches();
    }
}
