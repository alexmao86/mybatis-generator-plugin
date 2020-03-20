package net.sourceforge.jweb.mybatis.generator.plugins;

import java.io.StringReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

import net.sourceforge.jweb.maven.util.XMLUtil;
/**
 * 满足某些情况下，只查询某些列的情况.
 * 使用方法：<br>
 * <pre>
 * &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.ColumnSubsetPlugin"&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 插件会生成
 * <ul>
 * <li>selectByExampleWithSpecifiedColumns</li>
 * <li>如果使用了selectOnePlugin, 则有 selectOneByExampleWithSpecifiedColumns和selectOneByExampleWithSpecifiedColumnsSafely</li>
 * <li>在mapper中：如果使用了PagePlugin, 则有 selectByExampleWithRowboundsWithSpecifiedColumns</li>
 * <li>在mapper中：selectByPrimaryKeyWithSpecifiedColumns</li>
 * <li>在xml中： 上述接口对应的xml select 片段</li>
 * <li>在Example中： 增加columns字段</li>
 * </ul>
 * 动态列名的用法, XXXExample.setColumns(....);<br>
 * <h3>预定义列表</h3>
 * 在table中增加property, 每一条property配置对应一条语句，其中“subset.”是固定前缀。<br>
 * 例如一个表只需要查询user_name字段，则<br>
 * <pre>
 * &lt;table ...
 * &lt;property name="subset.UserNameOnly" value="user_name"/&gt;
 * &lt;/table ...
 * </pre>
 * 本配置会增加生成：
 * <ul>
 * <li>在mapper中：selectByExampleUserNameOnly</li>
 * <li>在mapper中：如果使用了selectOnePlugin, 则有 selectOneByExampleUserNameOnly和selectOneByExampleUserNameOnlySafely</li>
 * <li>在mapper中：如果使用了PagePlugin, 则有 selectByExampleWithRowboundsUserNameOnly</li>
 * <li>在mapper中：selectByPrimaryKeyUserNameOnly</li>
 * <li>在xml中： 有UserNameOnly_Column_list和上述接口对应的xml select 片段</li>
 * </ul>
 * @author maoanapex88@163.com alexmao86
 *
 */
public class ColumnSubsetPlugin extends PluginAdapter {
	private final static String PREFIX="subset.";
	private final static Log LOG=LogFactory.getLog(ColumnSubsetPlugin.class);
	@Override
	public boolean validate(List<String> arg0) {
		LOG.debug("ColumnSubsetPlugin does no validate");
		return true;
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties!=null) {
			for(Object keyObj:properties.keySet()) {
				String key=keyObj.toString();
				if(!key.startsWith(PREFIX)) {
					continue;
				}
				key=key.substring(PREFIX.length()).trim();
				if(key.isEmpty()) {
					continue;
				}
				//考虑到withBlobs的情况，方法模板使用withBlobs。因为列最终由用户控制
				//生成对应的selectByExampleXXX
				Method templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBs", "selectByExample");
				if(templateMethod!=null) {
					Method selectSubset=PluginUtil.clone(templateMethod);
					selectSubset.setName("selectByExample"+key);
					interfaze.addMethod(selectSubset);
				}
				
				//生成对应的selectOneByExampleXXX
				templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBs","selectOneByExample");
				if(templateMethod!=null) {
					LOG.debug("generate selectOneByExample+Subset");
					Method selectSubset=PluginUtil.clone(templateMethod);
					selectSubset.setName("selectOneByExample"+key);
					interfaze.addMethod(selectSubset);
				}
				
				//生成对应的selectByExampleWithRowboundsXXX
				templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBsWithRowbounds", "selectByExampleWithRowbounds");
				if(templateMethod!=null) {
					LOG.debug("generate selectByExampleWithRowbounds+Subset");
					Method selectSubset=PluginUtil.clone(templateMethod);
					selectSubset.setName("selectByExampleWithRowbounds"+key);
					interfaze.addMethod(selectSubset);
				}
				
				//生成对应的selectByPrimaryKeyXXX
				templateMethod=PluginUtil.getMethod(interfaze, "selectByPrimaryKey");
				if(templateMethod!=null) {
					LOG.debug("generate selectByPrimaryKey+Subset");
					Method selectSubset=PluginUtil.clone(templateMethod);
					selectSubset.setName("selectByPrimaryKey"+key);
					interfaze.addMethod(selectSubset);
				}
				
				//生成对应的selectOneByExampleXXXSafely
				templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBsSafely","selectOneByExampleSafely");
				if(templateMethod!=null) {
					Method selectSubset=PluginUtil.clone(templateMethod);
					selectSubset.setName("selectOneByExample"+key+"Safely");
					String line1="List<%s> list=this.selectByExample%s(%s);";
//					selectSubset.getBodyLines().set(0, String.format(line1, introspectedTable.getTableConfiguration().getDomainObjectName(), key, selectSubset.getParameters().get(0).getName()));
					selectSubset.getBodyLines().set(0, String.format(line1, introspectedTable.getRules().calculateAllFieldsClass().getShortName(), key, selectSubset.getParameters().get(0).getName()));
					interfaze.addMethod(selectSubset);
				}
			}
		}
		
		//考虑到withBlobs的情况，方法模板使用withBlobs。因为列最终由用户控制
		//生成对应的selectByExampleXXX
		Method templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBs", "selectByExample");
		if(templateMethod!=null) {
			Method selectSubset=PluginUtil.clone(templateMethod);
			selectSubset.setName("selectByExampleWithSpecifiedColumns");
			interfaze.addMethod(selectSubset);
			
			//对应的变长参数签名方法
			Method varargSelectSubset=PluginUtil.clone(templateMethod);
			varargSelectSubset.setDefault(true);
			varargSelectSubset.getBodyLines().clear();
			varargSelectSubset.setName("selectByExampleWithSpecifiedColumns");
			varargSelectSubset.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns", true));
			varargSelectSubset.addBodyLine("StringBuilder builder=new StringBuilder();");
			varargSelectSubset.addBodyLine("for(int i=0;i<columns.length;i++){");
			varargSelectSubset.addBodyLine("	builder.append(columns[i]).append(\",\");");
			varargSelectSubset.addBodyLine("}");
			varargSelectSubset.addBodyLine("builder.deleteCharAt(builder.length()-1);");
			varargSelectSubset.addBodyLine(selectSubset.getParameters().get(0).getName()+".setColumns(builder.toString());");
			
			StringBuilder returnStatement=new StringBuilder();
			returnStatement.append("return this.").append(selectSubset.getName())
			.append("(");
			for(int i=0;i<selectSubset.getParameters().size();i++) {
				Parameter p=selectSubset.getParameters().get(i);
				returnStatement.append(p.getName()).append(",");
			}
			if(selectSubset.getParameters().size()>0) {
				returnStatement.deleteCharAt(returnStatement.length()-1);
			}
			returnStatement.append(");");
			
			varargSelectSubset.addBodyLine(returnStatement.toString());
			interfaze.addMethod(varargSelectSubset);
		}
		
		//生成对应的selectOneByExampleXXX
		templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBs","selectOneByExample");
		if(templateMethod!=null) {
			LOG.debug("generate selectOneByExample+Subset");
			Method selectSubset=PluginUtil.clone(templateMethod);
			selectSubset.setName("selectOneByExampleWithSpecifiedColumns");
			interfaze.addMethod(selectSubset);
			
			//对应的变长参数签名方法
			Method varargSelectSubset=PluginUtil.clone(templateMethod);
			varargSelectSubset.setDefault(true);
			varargSelectSubset.getBodyLines().clear();
			varargSelectSubset.setName("selectOneByExampleWithSpecifiedColumns");
			varargSelectSubset.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns", true));
			varargSelectSubset.addBodyLine("StringBuilder builder=new StringBuilder();");
			varargSelectSubset.addBodyLine("for(int i=0;i<columns.length;i++){");
			varargSelectSubset.addBodyLine("	builder.append(columns[i]).append(\",\");");
			varargSelectSubset.addBodyLine("}");
			varargSelectSubset.addBodyLine("builder.deleteCharAt(builder.length()-1);");
			varargSelectSubset.addBodyLine(selectSubset.getParameters().get(0).getName()+".setColumns(builder.toString());");
			
			StringBuilder returnStatement=new StringBuilder();
			returnStatement.append("return this.").append(selectSubset.getName())
			.append("(");
			for(int i=0;i<selectSubset.getParameters().size();i++) {
				Parameter p=selectSubset.getParameters().get(i);
				returnStatement.append(p.getName()).append(",");
			}
			if(selectSubset.getParameters().size()>0) {
				returnStatement.deleteCharAt(returnStatement.length()-1);
			}
			returnStatement.append(");");
			
			varargSelectSubset.addBodyLine(returnStatement.toString());
			interfaze.addMethod(varargSelectSubset);
		}
		
		//生成对应的selectByExampleWithRowboundsXXX
		templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBsWithRowbounds", "selectByExampleWithRowbounds");
		if(templateMethod!=null) {
			LOG.debug("generate selectByExampleWithRowbounds+Subset");
			Method selectSubset=PluginUtil.clone(templateMethod);
			selectSubset.setName("selectByExampleWithRowboundsWithSpecifiedColumns");
			interfaze.addMethod(selectSubset);
			
			//对应的变长参数签名方法
			Method varargSelectSubset=PluginUtil.clone(templateMethod);
			varargSelectSubset.setDefault(true);
			varargSelectSubset.getBodyLines().clear();
			varargSelectSubset.setName("selectByExampleWithRowboundsWithSpecifiedColumns");
			varargSelectSubset.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns", true));
			varargSelectSubset.addBodyLine("StringBuilder builder=new StringBuilder();");
			varargSelectSubset.addBodyLine("for(int i=0;i<columns.length;i++){");
			varargSelectSubset.addBodyLine("	builder.append(columns[i]).append(\",\");");
			varargSelectSubset.addBodyLine("}");
			varargSelectSubset.addBodyLine("builder.deleteCharAt(builder.length()-1);");
			varargSelectSubset.addBodyLine(selectSubset.getParameters().get(0).getName()+".setColumns(builder.toString());");
			
			StringBuilder returnStatement=new StringBuilder();
			returnStatement.append("return this.").append(selectSubset.getName())
			.append("(");
			for(int i=0;i<selectSubset.getParameters().size();i++) {
				Parameter p=selectSubset.getParameters().get(i);
				returnStatement.append(p.getName()).append(",");
			}
			if(selectSubset.getParameters().size()>0) {
				returnStatement.deleteCharAt(returnStatement.length()-1);
			}
			returnStatement.append(");");
			
			varargSelectSubset.addBodyLine(returnStatement.toString());
			interfaze.addMethod(varargSelectSubset);
		}
		
		//生成对应的selectByPrimaryKeyXXX
		templateMethod=PluginUtil.getMethod(interfaze, "selectByPrimaryKey");
		if(templateMethod!=null) {
			LOG.debug("generate selectByPrimaryKey+Subset");
			Method selectSubset=PluginUtil.clone(templateMethod);
			selectSubset.getParameters().get(0).addAnnotation("@Param(\""+selectSubset.getParameters().get(0).getName()+"\")");
			
			Parameter columnsParam=new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns");
			columnsParam.addAnnotation("@Param(\"columns\")");
			selectSubset.addParameter(columnsParam);
			
			selectSubset.setName("selectByPrimaryKeyWithSpecifiedColumns");
			interfaze.addMethod(selectSubset);
			
			//对应的变长参数签名方法
			Method varargSelectSubset=PluginUtil.clone(templateMethod);
			varargSelectSubset.getBodyLines().clear();
			varargSelectSubset.setDefault(true);
			varargSelectSubset.setName("selectByPrimaryKeyWithSpecifiedColumns");
			varargSelectSubset.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns", true));
			varargSelectSubset.addBodyLine("StringBuilder builder=new StringBuilder();");
			varargSelectSubset.addBodyLine("for(int i=0;i<columns.length;i++){");
			varargSelectSubset.addBodyLine("	builder.append(columns[i]).append(\",\");");
			varargSelectSubset.addBodyLine("}");
			varargSelectSubset.addBodyLine("builder.deleteCharAt(builder.length()-1);");
			
			StringBuilder returnStatement=new StringBuilder();
			returnStatement.append("return this.").append(selectSubset.getName())
			.append("(");
			for(int i=0;i<selectSubset.getParameters().size()-1;i++) {
				Parameter p=selectSubset.getParameters().get(i);
				returnStatement.append(p.getName()).append(",");
			}
			returnStatement.append("builder.toString()");
			returnStatement.append(");");
			
			varargSelectSubset.addBodyLine(returnStatement.toString());
			interfaze.addMethod(varargSelectSubset);
		}
		
		//生成对应的selectOneByExampleXXXSafely
		templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBsSafely","selectOneByExampleSafely");
		if(templateMethod!=null) {
			Method selectSubset=PluginUtil.clone(templateMethod);
			selectSubset.setName("selectOneByExampleWithSpecifiedColumnsSafely");
			String line1="List<%s> list=this.selectByExampleWithSpecifiedColumns(%s);";
			// change from introspectedTable.getFullyQualifiedTable().getDomainObjectName() to introspectedTable.getRules().calculateAllFieldsClass().getShortName()
            selectSubset.getBodyLines().set(0, String.format(line1, introspectedTable.getRules().calculateAllFieldsClass().getShortName(), selectSubset.getParameters().get(0).getName()));
			interfaze.addMethod(selectSubset);
			
			//对应的变长参数签名方法
			Method varargSelectSubset=PluginUtil.clone(templateMethod);
			varargSelectSubset.getBodyLines().clear();
			varargSelectSubset.setDefault(true);
			varargSelectSubset.setName("selectOneByExampleWithSpecifiedColumnsSafely");
			varargSelectSubset.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns", true));
			varargSelectSubset.addBodyLine("StringBuilder builder=new StringBuilder();");
			varargSelectSubset.addBodyLine("for(int i=0;i<columns.length;i++){");
			varargSelectSubset.addBodyLine("	builder.append(columns[i]).append(\",\");");
			varargSelectSubset.addBodyLine("}");
			varargSelectSubset.addBodyLine("builder.deleteCharAt(builder.length()-1);");
			varargSelectSubset.addBodyLine(selectSubset.getParameters().get(0).getName()+".setColumns(builder.toString());");
			
			StringBuilder returnStatement=new StringBuilder();
			returnStatement.append("return this.").append(selectSubset.getName())
			.append("(");
			for(int i=0;i<selectSubset.getParameters().size();i++) {
				Parameter p=selectSubset.getParameters().get(i);
				returnStatement.append(p.getName()).append(",");
			}
			if(selectSubset.getParameters().size()>0) {
				returnStatement.deleteCharAt(returnStatement.length()-1);
			}
			returnStatement.append(");");
			
			varargSelectSubset.addBodyLine(returnStatement.toString());
			interfaze.addMethod(varargSelectSubset);
		}
		return true;
	}
	@Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		String xmlStr=document.getFormattedContent();
    	StringReader reader=new StringReader(xmlStr);
    	
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) {
			properties=new Properties();
		}
		try {
    		org.w3c.dom.Document dom=XMLUtil.createDocument(reader);
			for(Entry<Object, Object> entry:properties.entrySet()) {
				String key=entry.getKey().toString();
				String value=entry.getValue().toString();
				if(!key.startsWith(PREFIX)) {
					continue;
				}
				key=key.substring(PREFIX.length()).trim();
				if(key.isEmpty()) {
					continue;
				}
				
				//生成列明sql
				XmlElement columnEl=new XmlElement("sql");
				columnEl.addAttribute(new Attribute("id", key+"_Column_list"));
				columnEl.addElement(new TextElement(value.trim()));
				document.getRootElement().addElement(4, columnEl);
				
	    		//生成selectByExampleXXX
				org.w3c.dom.Element templateElement = PluginUtil.getElement(dom, "//select[@id='selectByExampleWithBLOBs']", "//select[@id='selectByExample']");
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExample"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成selectByExampleXXX
				templateElement = PluginUtil.getElement(dom, "//select[@id='selectOneByExampleWithBLOBs']", "//select[@id='selectOneByExample']");
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectOneByExample"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成对应的selectByExampleWithRowboundsXXX
				templateElement = PluginUtil.getElement(dom, "//select[@id='selectByExampleWithBLOBsWithRowbounds']", "//select[@id='selectByExampleWithRowbounds']");
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExampleWithRowbounds"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成对应的selectByPrimaryKeyXXX
				templateElement = PluginUtil.getElement(dom, "//select[@id='selectByPrimaryKey']");
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByPrimaryKey"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
			}
			
			//生成自定义列名的sql
			//生成selectByExampleXXX
			org.w3c.dom.Element templateElement = PluginUtil.getElement(dom, "//select[@id='selectByExample']", "//select[@id='selectByExample']");
			if(templateElement!=null) {
				XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExampleWithSpecifiedColumns");
				PluginUtil.replaceFirst(e, "include", new TextElement("${columns}"));
				document.getRootElement().addElement(e);
			}
			
			//生成selectByExampleXXX
			templateElement = PluginUtil.getElement(dom, "//select[@id='selectOneByExampleWithBLOBs']", "//select[@id='selectOneByExample']");
			if(templateElement!=null) {
				XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectOneByExampleWithSpecifiedColumns");
				PluginUtil.replaceFirst(e, "include", new TextElement("${columns}"));
				document.getRootElement().addElement(e);
			}
			
			//生成对应的selectByExampleWithRowboundsXXX
			templateElement = PluginUtil.getElement(dom, "//select[@id='selectByExampleWithBLOBsWithRowbounds']", "//select[@id='selectByExampleWithRowbounds']");
			if(templateElement!=null) {
				XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExampleWithRowboundsWithSpecifiedColumns");
				PluginUtil.replaceFirst(e, "include", new TextElement("${columns}"));
				document.getRootElement().addElement(e);
			}
			
			//生成对应的selectByPrimaryKeyXXX
			templateElement = PluginUtil.getElement(dom, "//select[@id='selectByPrimaryKey']");
			if(templateElement!=null) {
				XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByPrimaryKeyWithSpecifiedColumns");
				PluginUtil.replaceFirst(e, "include", new TextElement("${columns}"));
				document.getRootElement().addElement(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	//在模型生成是，增加columns配置
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, final IntrospectedTable introspectedTable) {
		Field field=new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setFinal(false);
		field.setStatic(false);
		field.setType(FullyQualifiedJavaType.getStringInstance());
		field.setName("columns");
		field.setInitializationString("\"ID\"");//default select id from ...
		topLevelClass.addField(field);
		
		Method getter=new Method();
		getter.setName("getColumns");
		getter.setVisibility(JavaVisibility.PUBLIC);
		getter.setReturnType(FullyQualifiedJavaType.getStringInstance());
		getter.addBodyLine("return this.columns;");
		topLevelClass.addMethod(getter);
		
		Method setter=new Method();
		setter.setName("setColumns");
		setter.setVisibility(JavaVisibility.PUBLIC);
		setter.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "columns"));
		setter.addBodyLine("this.columns=columns;");
		topLevelClass.addMethod(setter);
		
		Method varargSetter=new Method();
		varargSetter.addJavaDocLine("/**use Column produced by ResultMapDiectoryPlugin*/");
		varargSetter.setName("setColumns");
		varargSetter.setVisibility(JavaVisibility.PUBLIC);
		varargSetter.addParameter(new Parameter(new FullyQualifiedJavaType(topLevelClass.getType().getFullyQualifiedName()+".Column"), "columns", true));
		varargSetter.addBodyLine("StringBuilder builder=new StringBuilder();");
		varargSetter.addBodyLine("for(int i=0;i<columns.length;i++){");
		varargSetter.addBodyLine("	builder.append(columns[i].col()).append(\",\");");
		varargSetter.addBodyLine("}");
		varargSetter.addBodyLine("builder.deleteCharAt(builder.length()-1);");
		varargSetter.addBodyLine("this.columns=builder.toString();");
		topLevelClass.addMethod(varargSetter);
		
		return true;
	}

	private void changeColumnList(XmlElement e, String id) {
		for(Element child:e.getElements()) {
			if(!(child instanceof XmlElement)) {
				continue;
			}
			XmlElement xmlChild=(XmlElement)child;
			if(!xmlChild.getName().equals("include")) {
				continue;
			}
			xmlChild.getAttributes().clear();
			xmlChild.addAttribute(new Attribute("refid", id));
		}
	}
}
