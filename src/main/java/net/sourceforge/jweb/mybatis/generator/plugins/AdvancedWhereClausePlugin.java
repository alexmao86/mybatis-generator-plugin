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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;


/**
 * Offical generator using SQL statement style as: "select xxx from table WHERE <b>(a and b) or (c and d) or (...)</b>", here a, b, c, d are Criterion, (...) is Criteria objects,
 * then use OR to join ordered Criteria. If your SQL is "select ... from table where (a or b) and c", you must use its equivalent to suit generator
 * code: select ... from table where <b>(a and c) or (b and c)</b>.<br>
 * 
 * So advanced where clause plugin extends default domain example with AND OR together, configurate it as:
 * <pre>
 * &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.AdvancedWhereClausePlugin"&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 
 * <pre>
 * In your generatorConfig.xml table node, add:
 * &lt;table ...&gt;
 * &lt;property name="AdvancedWhereClausePluginEnabled" value="true/false"/&gt;
 * &lt;/table&gt;
 * If not set, it will be enabled by default.
 * </pre>
 *
 * <pre>
 * Then coding like:
 * YourDomainExample e=new YourDomainExample();
 * e.createCriteria().andXXXX().orYYYY()....
 * </pre>
 * Configuration:
 * <table border="1">
 * 	<tr><td>Key</td><td>Value</td></tr>
 * 	<tr><td>AdvancedWhereClausePluginEnabled</td><td>true/false</td></tr>
 * </table>
 * 
 * @author maoanapex88@163.com
 */
public class AdvancedWhereClausePlugin extends PluginAdapter {
	private final static Log LOG=LogFactory.getLog(AdvancedWhereClausePlugin.class);
	public boolean validate(List<String> warnings) {
		return true;
	}
	/**
	 * when model example class generated, plugin will add below code to source
	 * <pre>
	 *  public Criteria and(){
	 *  	...
	 *  }
	 *  public void and(Criteria criteria) {
	 *  	....
	 *  }
	 * In Criteria add:
	 *  private String criteriaLogical = "or";
	 *  public void setCriterionLogical(String logic) {
     *    	this.criterionLogical = logic;
     *  }
     *  public void setCriteriaLogical(String logic) {
     *     	this.criteriaLogical = logic;
     * 	}
     * </pre>
	 */
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) return true;
		
		String enable=properties.getProperty("AdvancedWhereClausePluginEnabled");
		
		if("false".equalsIgnoreCase(enable)){
			LOG.debug("AdvancedWhereClausePlugin disabled for table: "+introspectedTable.getTableConfiguration().getTableName());
			return true;
		}
		
		InnerClass generatedCriteria = null;
        // first, find the Criteria inner class
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
                generatedCriteria= innerClass;
                break;
            }
        }

        if (generatedCriteria== null) {
            // can't find the inner class for some reason, bail out.
            return true;
        }
        
        /***/
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        //method name
        method.setName("and");
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addBodyLine("Criteria criteria = createCriteriaInternal();");
        method.addBodyLine("oredCriteria.add(criteria);");
        method.addBodyLine("criteria.setCriteriaLogical(\"and\");");
        method.addBodyLine("return criteria;");
        topLevelClass.addMethod(method);
        
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("and");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getCriteriaInstance(), "criteria"));
        method.addBodyLine("oredCriteria.add(criteria);");
        method.addBodyLine("criteria.setCriteriaLogical(\"and\");");
        topLevelClass.addMethod(method);
        /*******************************end***************************************/
        
        
        /*
         advaned where clause predicate logic, can be and or or. RESERVED!
         private String criteriaLogical = "or";
        */
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName("criteriaLogical");
        field.setInitializationString("\"or\"");
        field.addJavaDocLine("/**advaned where clause predicate logic, can be and or or.*/");
        generatedCriteria.addField(field);
        /*
         public void setCriteriaLogical(String logic) {
            this.criteriaLogical = logic;
         }
         */
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "logic")); //$NON-NLS-1$
        method.setName("setCriteriaLogical");
        method.addBodyLine("this.criteriaLogical = logic;");
        generatedCriteria.addMethod(method);
        
        //getter
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("getCriteriaLogical");
        method.addBodyLine("return this.criteriaLogical;");
        generatedCriteria.addMethod(method);
        
        // second, find the Criteria inner class
        InnerClass criterionInnerClass=null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("Criterion".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
            	criterionInnerClass = innerClass;
                break;
            }
        }
        
        /*
         advaned where clause predicate logic, can be and or or. RESERVED!
         private String criterionLogical = "and";
        */
        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName("criterionLogical");
        field.setInitializationString("\"and\"");
        field.addJavaDocLine("/**advaned where clause predicate logic, can be and or or. RESERVED!*/");
        criterionInnerClass.addField(field);
        
        /*
         * public void setCriterionLogical(String logic) {
            	this.criterionLogical = logic;
        	}
         */
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "logic")); //$NON-NLS-1$
        method.setName("setCriterionLogical");
        method.addBodyLine("this.criterionLogical = logic;");
        criterionInnerClass.addMethod(method);
        
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("getCriterionLogical");
        method.addBodyLine("return this.criterionLogical;");
        criterionInnerClass.addMethod(method);
        
        List<Method> methods=generatedCriteria.getMethods();
        List<Method> ItList=new ArrayList<Method>(methods);
        for(Method m:ItList){
        	String methodName=m.getName();
        	if(methodName.startsWith("and")&&m.getReturnType().equals(FullyQualifiedJavaType.getCriteriaInstance())){
        		Method orMethod=generateOrMethod(m);
        		generatedCriteria.addMethod(orMethod);
        		List<String> bodyLines=orMethod.getBodyLines();
        		bodyLines.add(bodyLines.size()-1, "criteria.get(criteria.size()-1).setCriterionLogical(\"or\");");
        	}
        }
		return true;
	}
	
	private Method generateOrMethod(Method m) {
		Method or=new Method();
		or.setName(m.getName().replaceFirst("and", "or"));
		or.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		or.setVisibility(JavaVisibility.PUBLIC);
		or.getParameters().addAll(m.getParameters());
		or.getBodyLines().addAll(m.getBodyLines());
		
		return or;
	}
	
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) return true;
		
		String enable=properties.getProperty("AdvancedWhereClausePluginEnabled");
		
		if("false".equalsIgnoreCase(enable)){
			LOG.debug("AdvancedWhereClausePlugin disabled for table: "+introspectedTable.getTableConfiguration().getTableName());
			return true;
		}
		
		XmlElement foreach=PluginUtil.findFirst(element, "foreach");
		if(foreach!=null){
			List<Attribute> attrs=foreach.getAttributes();
			for(int i=0;i<attrs.size();i++){
				Attribute attr=attrs.get(i);
				if("separator".equals(attr.getName())){
					attrs.remove(i);
					break;
				}
			}
			foreach.addAttribute(new Attribute("index", "idx"));
		}
		else {
			LOG.warn("can not find elment <foreach>, ignored");
		}
		
		//use <if test="idx > 0">${criteria.criteriaLogical}</if>
		XmlElement ife=PluginUtil.findFirst(element, "if");
		if(ife!=null){
			XmlElement criteriaLogic=new XmlElement("if");
			criteriaLogic.addAttribute(new Attribute("test", "idx > 0"));
			criteriaLogic.addElement(new TextElement("${criteria.criteriaLogical}"));
			ife.addElement(0, criteriaLogic);
		}
		else {
			LOG.warn("can not find elment <if>, ignored");
		}
		
		XmlElement trim=PluginUtil.findFirst(element, "trim");
		if(trim!=null){
			List<Attribute> attrs=trim.getAttributes();
			for(int i=0;i<attrs.size();i++){
				Attribute attr=attrs.get(i);
				if("prefixOverrides".equals(attr.getName())){
					attrs.remove(i);
					break;
				}
			}
			trim.addAttribute(new Attribute("prefixOverrides", "and |or"));
		}
		else {
			LOG.warn("can not find elment <trim>, ignored");
		}
		
		List<XmlElement> whens=PluginUtil.findAll(element, "when");
		for(XmlElement when:whens){
			List<Element> whenChildren=when.getElements();
			if(whenChildren==null||whenChildren.isEmpty()) {
				continue;
			}
			
			String text = whenChildren.iterator().next().getFormattedContent(0);
			
			if(text!=null){
				if(whenChildren.size()==1){
					whenChildren.clear();
				}
				else {
					whenChildren.remove(0);
				}
				when.addElement(0, new TextElement(text.replaceFirst("and", "\\$\\{criterion\\.criterionLogical\\}")));
			}
		}
		
		return true;
	}
	
}
