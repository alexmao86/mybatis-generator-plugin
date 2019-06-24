package net.sourceforge.jweb.mybatis.generator.plugins;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import net.sourceforge.jweb.maven.util.XMLUtil;

/**
 * Adds "selectOneByExample" method to the appropriate Mapper interface returning exactly one object instance.<br/>
 * Example configuration:<br/>
 * <tt>
 * <pre>
 * &lt;generatorConfiguration&gt;
 *  &lt;context ...&gt;
 *
 *      &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.SelectOneByExamplePlugin"/&gt;
 *      ...
 *
 *  &lt;/context&gt;
 * &lt;/generatorConfiguration&gt;
 * </pre>
 * </tt>
 * <br/> Properties:<br/> <ul> <li><strong>methodToGenerate</strong> (optional) : the name of the method to generate.
 * Default: <strong>selectOneByExample</strong></li> <li><strong>excludeClassNamesRegexp</strong> (optional): classes to
 * exclude from generation as regular expression. Default: none</li> </ul>
 * 
 * 2019-05-23 enhance selectOne for JDK8, added selectOneSafely();
 */
public class SelectOneByExamplePlugin extends PluginAdapter {
	private final static Log LOG=LogFactory.getLog(SelectOneByExamplePlugin.class);
	
    private Config config;
    private boolean generateSelectOneWithBLOBs=true;
    private boolean generateSelectOneWithoutBLOBs=true;

    public boolean validate(List<String> warnings) {
        if (this.config == null){
        	this.config = new Config(getProperties());
        }
        return true;
    }
    
    @Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(config.supportJDK8) {
			interfaze.addImportedType(new FullyQualifiedJavaType("org.slf4j.LoggerFactory"));
			List<Method> appendMethods=new ArrayList<Method>(2);
			for(Method method : interfaze.getMethods()) {
				if(method.getName().equals(config.methodToGenerate)) {
					Method newMethod=PluginUtil.clone(method);
					
					newMethod.setName(config.methodToGenerate+"Safely");
					newMethod.setDefault(true);
					
					String line1="List<%s> list=this.selectByExample(%s);";
					String line2="if(list.isEmpty()) return null;";
					String line3="if(list.size()>1) {LoggerFactory.getLogger(%s.class).info(\"select one retured {} rows\",list.size());}";
					String line4="return list.get(0);";
					newMethod.addBodyLine(String.format(line1, method.getReturnType().getShortName().replaceFirst("WithBLOBs", ""), method.getParameters().get(0).getName()) );
					newMethod.addBodyLine(line2);
					newMethod.addBodyLine(String.format(line3, interfaze.getType().getShortName()));
					newMethod.addBodyLine(line4);
					appendMethods.add(newMethod);
					
				}
				else if(method.getName().equals(config.methodToGenerate+"WithBLOBs")) {
					Method newMethod=PluginUtil.clone(method);
					newMethod.setName(config.methodToGenerate+"WithBLOBsSafely");
					newMethod.setDefault(true);
					
					String line1="List<%s> list=this.selectByExampleWithBLOBs(%s);";
					String line2="if(list.isEmpty()) return null;";
					String line3="if(list.size()>1) {LoggerFactory.getLogger(%s.class).info(\"select one returned {} rows\",list.size());}";
					String line4="return list.get(0);";
					newMethod.addBodyLine(String.format(line1, method.getReturnType().getShortName(), method.getParameters().get(0).getName()) );
					newMethod.addBodyLine(line2);
					newMethod.addBodyLine(String.format(line3, interfaze.getType().getShortName()));
					newMethod.addBodyLine(line4);
					appendMethods.add(newMethod);
				}
			}
			for(Method method:appendMethods){
				interfaze.addMethod(method);
			}
		}
		return true;
	}

	@Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    	String xmlStr=document.getFormattedContent();
    	StringReader reader=new StringReader(xmlStr);
    	try {
    		org.w3c.dom.Document dom=XMLUtil.createDocument(reader);
			if(generateSelectOneWithBLOBs){
				XPathExpression exp=XMLUtil.compile("//select[@id='selectByExampleWithBLOBs']");
				Object nodeObj = exp.evaluate(dom, XPathConstants.NODESET);
				if(nodeObj!=null){
					NodeList nodes=(NodeList)nodeObj;
					if(nodes.getLength()==1){
						Element selectEl=(Element)nodes.item(0);
						document.getRootElement().addElement(new TextElement("<!-- generated by SelectOneByExamplePlugin "+new Date()+" -->"));
						org.mybatis.generator.api.dom.xml.XmlElement selectOneXmlEl=(org.mybatis.generator.api.dom.xml.XmlElement)PluginUtil.cloneElement(selectEl, config.methodToGenerate+"WithBLOBs");
						if(config.forceSelectOne) {
							selectOneXmlEl.addElement(new TextElement("limit 1"));
						}
						document.getRootElement().addElement(selectOneXmlEl);
					}
					else if(nodes.getLength()>1){
						LOG.warn("[WARN] find more than one selectByExampleWithBLOBs element, this is ambitious for SelectOneByExamplePlugin, ignored");
					}
				}
	    	}
	    	if(generateSelectOneWithoutBLOBs){
	    		XPathExpression exp=XMLUtil.compile("//select[@id='selectByExample']");
	    		Object nodeObj = exp.evaluate(dom, XPathConstants.NODESET);
				if(nodeObj!=null){
					NodeList nodes=(NodeList)nodeObj;
					if(nodes.getLength()==1){
						Element selectEl=(Element)nodes.item(0);
						document.getRootElement().addElement(new TextElement("<!-- generated by SelectOneByExamplePlugin "+new Date()+" -->"));
						org.mybatis.generator.api.dom.xml.XmlElement selectOneXmlEl=(org.mybatis.generator.api.dom.xml.XmlElement)PluginUtil.cloneElement(selectEl, config.methodToGenerate);
						if(config.forceSelectOne) {
							selectOneXmlEl.addElement(new TextElement("limit 1"));
						}
						document.getRootElement().addElement(selectOneXmlEl);
						
					}
					else if(nodes.getLength()>1){
						LOG.warn("[WARN] find more than one selectByExample element, this is ambitious for SelectOneByExamplePlugin, ignored");
					}
				}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        return true;
    }
    
    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        if ((generateSelectOneWithBLOBs=!config.shouldExclude(interfaze.getType()))){
        	interfaze.addMethod(generateSelectOneByExample(method, introspectedTable, true));
        }

        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,IntrospectedTable introspectedTable) {
        if ((generateSelectOneWithoutBLOBs=!config.shouldExclude(interfaze.getType()))){
        	interfaze.addMethod(generateSelectOneByExample(method, introspectedTable, false));
        }
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if ((generateSelectOneWithBLOBs=!config.shouldExclude(topLevelClass.getType()))){
        	topLevelClass.addMethod(generateSelectOneByExample(method, introspectedTable, true));
        }
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if ((generateSelectOneWithoutBLOBs=!config.shouldExclude(topLevelClass.getType()))){
        	topLevelClass.addMethod(generateSelectOneByExample(method, introspectedTable, false));
        }
        return true;
    }
    private Method generateSelectOneByExample(Method method, IntrospectedTable introspectedTable, boolean withBLOBs) {
        Method m = new Method(config.methodToGenerate+(withBLOBs?"WithBLOBs":""));
        m.setVisibility(method.getVisibility());
        FullyQualifiedJavaType returnType = introspectedTable.getRules().calculateAllFieldsClass();
        m.setReturnType(returnType);
        List<String> annotations = method.getAnnotations();
        for (String a : annotations) {
            m.addAnnotation(a);
        }
        List<Parameter> params = method.getParameters();
        for (Parameter p : params) {
            m.addParameter(p);
        }
        context.getCommentGenerator().addGeneralMethodComment(m, introspectedTable);
        return m;
    }

    private static final class Config extends BasePluginConfig {
        private static final String methodToGenerateKey = "methodToGenerate";
        private static final String forceSelectOneKey = "forceSelectOne";
        private static final String jdk8EnableKey = "supportJDK8";

        private String methodToGenerate;
        private boolean forceSelectOne;//add limit 1 to make sure select one
        private boolean supportJDK8;//suport jdk8 default method in interface
        
        protected Config(Properties props) {
            super(props);
            this.methodToGenerate = props.getProperty(methodToGenerateKey, "selectOneByExample");
            this.forceSelectOne=Boolean.parseBoolean(props.getProperty(forceSelectOneKey, "true"));
            this.supportJDK8=Boolean.parseBoolean(props.getProperty(jdk8EnableKey, "true"));
        }
    }
   
}
