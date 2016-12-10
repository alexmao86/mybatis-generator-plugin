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
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.sourceforge.jweb.maven.util.XMLUtil;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * In my experience, The generated code can not meet requirement perfectly, so you need to insert your own code in generated
 * code. in RAD, the database(Domain) may change during development frequently, so when regenerating code, you must be very careful to maintain
 * your piece of code in pojo and mapper xml. so UserDefinedXMLMapperPlugin can hold your part of babatis code and xml.<br>
 * 
 * Usage:
 * <pre>
 * &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.UserDefinedXMLMapperPlugin"&gt;
 * &lt;property name="baseDirectory" value="base directory"/&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 
 * If baseDirectory is not set, plugin will use current, i.e. new File("").getAbsolutePath();
 * if set and start with /, then plugin will consider you are using absolute path, in Windows, please use absolute path starts with /,
 * for example, /C:\\a\\b.
 * if set and not start with /, then plugin will consider you are using relative path, i.e. new File("").getAbsolutePath()+ baseDirectory
 * 
 * In your generatorConfig.xml table node, add:
 * <pre>
 * &lt;table ...&gt;
 * &lt;!-- your mapper file, you can use any tag name as root --&gt;
 * &lt;property name="UserDefinedXMLMapper" value="file name"/&gt;
 * 
 * &lt;!-- interface will insert to Mapper interface --&gt;
 * &lt;property name="xxxInterface" value="public Domain selectByProductId(int productId);"/&gt;
 * 
 * &lt;!-- Implemention will insert to Mapper class if generating IMPL --&gt;
 * &lt;property name="xxxImplement" value="public Domain selectByProductId(int productId){return sqlClient.queryForList(...', '...');}"/&gt;
 * &lt;/table&gt;
 * </pre>
 * Your defined xml mapper can use any tag name as root, NOTE, no xml validation.
 * 
 * @author maoanapex88@163.com
 *
 */
public class UserDefinedXMLMapperPlugin extends PluginAdapter {
	private final static Log LOG=LogFactory.getLog(UserDefinedXMLMapperPlugin.class);
	private String baseDirectory;
	
	public boolean validate(List<String> warnings) {
		Properties properties=this.getProperties();
		if(properties==null){
			baseDirectory=new File("").getAbsolutePath();
			LOG.warn("[UserDefinedXMLMapperPlugin] You did not set baseDirectory, so use current directory default: "+baseDirectory);
		}
		else {
			String value=properties.getProperty("baseDirectory");
			if(value==null){
				baseDirectory=new File("").getAbsolutePath();
				LOG.warn("[UserDefinedXMLMapperPlugin] You did not set baseDirectory, so use current directory default: "+baseDirectory);
			}
			else {
				if(value.startsWith("/")){
					File f = new File(value);
					if(f.exists()){
						baseDirectory=f.getAbsolutePath();
						LOG.debug("[UserDefinedXMLMapperPlugin] You set baseDirectory, "+baseDirectory);
					}
					else {
						LOG.warn("[UserDefinedXMLMapperPlugin] You set baseDirectory, but it is not exist, validation failed on: "+baseDirectory);
						return false;
					}
				}
				else {
					String currentBase=new File("").getAbsolutePath();
					if(!currentBase.endsWith("/")) currentBase=currentBase+"/";
					baseDirectory=currentBase+value;
					
					LOG.debug("[UserDefinedXMLMapperPlugin] Your real baseDirectory, "+baseDirectory);
				}
			}
		}
		if(!baseDirectory.endsWith("/")) baseDirectory=baseDirectory+"/";
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) return true;
		
		for(Entry entry:properties.entrySet()){
			String key=entry.getKey().toString();
			if(key.endsWith("Interface")){
				if(interfaze!=null)interfaze.addMethod(new LiterialMethod(introspectedTable.getTableConfiguration().getTableName(), entry.getValue().toString()));
			}
			else if(key.endsWith("Import")){
				if(interfaze!=null)interfaze.addImportedType(new FullyQualifiedJavaType(entry.getValue().toString()));
				if(topLevelClass!=null)topLevelClass.addImportedType(new FullyQualifiedJavaType(entry.getValue().toString()));
			}
			else if(key.endsWith("Implement")){
				if(topLevelClass!=null){
					topLevelClass.addMethod(new LiterialMethod(introspectedTable.getTableConfiguration().getTableName(), entry.getValue().toString()));
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) return true;
		
		String mapper=properties.getProperty("UserDefinedXMLMapper");
		if(mapper==null) return true;
		
		File mapperFile=new File(baseDirectory+mapper);
		
		if(!mapperFile.exists()){
			LOG.warn(mapper+" not exists, ignored");
			return true;
		}
		
		Document generatorDom=getDocumentInstance(sqlMap);
		
		try {
			org.w3c.dom.Document doc=XMLUtil.createDocument(mapperFile);
			
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			if(nodeList!=null){
				for(int i=0;i<nodeList.getLength();i++){
					Node node = nodeList.item(i);
					Element xmlElement = PluginUtil.cloneElement(node);
					if(generatorDom!=null){
						generatorDom.getRootElement().addElement(xmlElement);
					}
					else {
						System.out.println("Can not get Document");
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error when parse customer mapper file", e);
			//e.printStackTrace();
		}
		
		return true;
	}
	
	private Document getDocumentInstance(final GeneratedXmlFile sqlMap){
		try {
			Field f=sqlMap.getClass().getDeclaredField("document");
			f.setAccessible(true);
			return (Document)f.get(sqlMap);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	//�������Զ��巽����
	public class LiterialMethod extends Method {
		private String literial;
		private String tableName;

		public LiterialMethod(String tableName, String literial) {
			super();
			this.literial = literial;
			this.tableName = tableName;
		}
		public final String getLiterial() {
			return literial;
		}
		public String getFormattedContent(int indentLevel, boolean interfaceMethod) {
			StringBuilder sb = new StringBuilder();
		    OutputUtilities.javaIndent(sb, indentLevel);
		    sb.append("/**\n");
		    
		    OutputUtilities.javaIndent(sb, indentLevel);
		    sb.append(" * This method was generated by UserDefinedXMLMapperPlugin\n");
		    
		    OutputUtilities.javaIndent(sb, indentLevel);
		    sb.append(" * This method corresponds to the database table ").append(tableName).append("\n");
		    
		    OutputUtilities.javaIndent(sb, indentLevel);
		    sb.append(" */\n");
		    
		    OutputUtilities.javaIndent(sb, indentLevel);
		    sb.append(this.getLiterial());
			return sb.toString();
		}
	}
}
