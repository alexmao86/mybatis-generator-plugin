package net.sourceforge.jweb.mybatis.generator.plugins;
import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
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
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * Provides annotations to the generated mapper(Dao) or Model(Pojo), annotations can be added on Model class, model class fields and Mappers
 * interface.<br>
 * <p/>
 * Example configuration:<br/>
 * <tt><pre>
 * &lt;generatorConfiguration&gt;
 *  &lt;context ...&gt;
 * <p/>
 *      &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.AnnotationsPlugin"&gt;
 *          ...
 * <p/>
 *      &lt;/plugin&gt;
 *      ...
 * <p/>
 *  &lt;/context&gt;
 * &lt;/generatorConfiguration&gt;
 * </pre></tt>
 * This plugin  will find special properties under table/property tag<br/>
 * <h3>annotation config for Mapper</h3>
 * <ol>
 * <li><strong>name</strong> format as Mapper.imports.??? is class imports for Mapper, ??? can be anything</li>
 * <li><strong>value</strong> qualified class names using comma splitting</li>
 * </ol>
 * <ol>
 * <li><strong>name</strong> format as Mapper.annotations.???, ??? can be anything</li>
 * <li><strong>value</strong> annotation content</li>
 * </ol>
 * 
 * <h3>annotation config for Model class</h3>
 * <ol>
 * <li><strong>name</strong> format as Model.imports.??? is class imports for table's model, ??? can be anything</li>
 * <li><strong>value</strong> qualified class names using comma splitting</li>
 * </ol>
 * <ol>
 * <li><strong>name</strong> format as Model.annotations.???, ??? can be anything</li>
 * <li><strong>value</strong> annotation content</li>
 * </ol>
 * 
 * <h3>annotation config for model field, getter and setter</h3>
 * <ol>
 * <li><strong>name</strong> format as Field.${column name}.annotations.???, ??? can be all: annotation will be added to field/getter/setter, field: only field; setter: only setter; getter: only getter</li>
 * <li><strong>value</strong> annotation content on field</li>
 * </ol>
 *
 */
public class AnnotationsPlugin extends PluginAdapter {
	private final static Log LOG=LogFactory.getLog(AnnotationsPlugin.class);
    /**
     * {@inheritDoc}
     */
    public boolean validate(List<String> warnings) {
    	LOG.debug("AnnotationsPlugin does no validate");
        return true;
    }

    /**
     * {@inheritDoc} when xxxMapper generated
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    	Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) {
			System.out.println("properties for "+interfaze+" not found");
			return true;
		}
		properties.forEach((k,v)->{
			String key=k.toString();
			if(key.startsWith("Mapper.imports.")) {
				String imports[]=v.toString().split(",");
				for(String i:imports) {
					if(!i.trim().isEmpty()) {
						interfaze.addImportedType(new FullyQualifiedJavaType(i.trim()));
					}
				}
			}
			else if(key.startsWith("Mapper.annotations.")) {
				interfaze.addAnnotation(v.toString().trim());
			}
		});
		
        return true;
    }
    
    /**
     * {@inheritDoc} when model class generated
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    	Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) {
			System.out.println("properties for "+topLevelClass+" not found");
			return true;
		}
		properties.forEach((k,v)->{
			String key=k.toString();
			if(key.startsWith("Model.imports.")) {
				String imports[]=v.toString().split(",");
				for(String i:imports) {
					if(!i.trim().isEmpty()) {
						topLevelClass.addImportedType(new FullyQualifiedJavaType(i.trim()));
					}
				}
			}
			else if(key.startsWith("Model.annotations.")) {
				topLevelClass.addAnnotation(v.toString().trim());
			}
		});
		
        return true;
    }

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) {
			System.out.println("properties for "+topLevelClass+"@"+field+" not found");
			return true;
		}
		properties.forEach((k,v)->{
			String key=k.toString();
			if(key.startsWith("Field."+introspectedColumn.getActualColumnName()+".annotations.")) {
				if(key.endsWith(".all")) {
					field.addAnnotation(v.toString().trim());
					Method m=getGetterMethodByName(topLevelClass, introspectedColumn.getJavaProperty());
					if(m!=null) {
						m.addAnnotation(v.toString().trim());
					}
					m=getsetterMethodByName(topLevelClass, introspectedColumn.getJavaProperty());
					if(m!=null) {
						m.addAnnotation(v.toString().trim());
					}
				}
				else if(key.endsWith(".field")) {
					field.addAnnotation(v.toString().trim());
				}
				else if(key.endsWith(".getter")) {
					Method m=getGetterMethodByName(topLevelClass, introspectedColumn.getJavaProperty());
					if(m!=null) {
						m.addAnnotation(v.toString().trim());
					}
				}
				else if(key.endsWith(".setter")) {
					Method m=getsetterMethodByName(topLevelClass, introspectedColumn.getJavaProperty());
					if(m!=null) {
						m.addAnnotation(v.toString().trim());
					}
				}
			}
		});
		return true;
	}
	
	private Method getGetterMethodByName(TopLevelClass topLevelClass, String fieldName) {
		String methodPrefix=fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
		for(Method m:topLevelClass.getMethods()) {
			if(m.getName().equals("is"+methodPrefix)||m.getName().equals("get"+methodPrefix)) {
				return m;
			}
		}
		return null;
	}
	private Method getsetterMethodByName(TopLevelClass topLevelClass, String fieldName) {
		String methodPrefix=fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
		for(Method m:topLevelClass.getMethods()) {
			if(m.getName().equals("set"+methodPrefix)) {
				return m;
			}
		}
		return null;
	}
}
