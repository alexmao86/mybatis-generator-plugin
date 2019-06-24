package net.sourceforge.jweb.mybatis.generator.plugins;

import java.io.StringReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
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
 * 然后在table中增加property, 每一条property配置对应一条语句，其中“subset.”是固定前缀。<br>
 * 例如一个表只需要查询user_name字段，则<br>
 * <pre>
 * &lt;table ...
 * &lt;property name="subset.UserNameOnly" value="user_name"/&gt;
 * &lt;/table ...
 * </pre>
 * 本配置会生成：
 * <ul>
 * <li>在mapper中：selectByExampleUserNameOnly </li>
 * <li>在mapper中：如果使用了selectOnePlugin, 则有 selectOneByExampleUserNameOnly和selectOneByExampleUserNameOnlySafely</li>
 * <li>在mapper中：如果使用了PagePlugin, 则有 selectByExampleWithRowboundsUserNameOnly</li>
 * <li>在mapper中：selectByPrimaryKeyUserNameOnly</li>
 * 
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
		if(properties==null) {
			LOG.debug("Missing properties, for example <properties name=\"subset.prefix\" value=\"col0, col1,..\"/>");
			return true;
		}
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
			Method templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBs");
			if(templateMethod==null) {
				System.out.println("try selectByExample method");
				templateMethod=PluginUtil.getMethod(interfaze, "selectByExample");
			}
			if(templateMethod!=null) {
				LOG.debug("generate selectByExample+Subset");
				Method selectSubset=PluginUtil.clone(templateMethod);
				selectSubset.setName("selectByExample"+key);
				interfaze.addMethod(selectSubset);
			}
			
			//生成对应的selectOneByExampleXXX
			templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBs");
			if(templateMethod==null) {
				LOG.debug("try selectOneByExampleWithBLOBs method");
				templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExample");
			}
			if(templateMethod!=null) {
				LOG.debug("generate selectOneByExample+Subset");
				Method selectSubset=PluginUtil.clone(templateMethod);
				selectSubset.setName("selectOneByExample"+key);
				interfaze.addMethod(selectSubset);
			}
			
			//生成对应的selectByExampleWithRowboundsXXX
			templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithBLOBsWithRowbounds");
			if(templateMethod==null) {
				LOG.debug("try selectByExampleWithRowbounds method");
				templateMethod=PluginUtil.getMethod(interfaze, "selectByExampleWithRowbounds");
			}
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
			templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleWithBLOBsSafely");
			if(templateMethod==null) {
				LOG.debug("try selectOneByExampleSafely method");
				templateMethod=PluginUtil.getMethod(interfaze, "selectOneByExampleSafely");
			}
			if(templateMethod!=null) {
				Method selectSubset=PluginUtil.clone(templateMethod);
				selectSubset.setName("selectOneByExample"+key+"Safely");
				String line1="List<%s> list=this.selectByExample%s(%s);";
				selectSubset.getBodyLines().set(0, String.format(line1, introspectedTable.getTableConfiguration().getDomainObjectName(), key, selectSubset.getParameters().get(0).getName()));
				interfaze.addMethod(selectSubset);
			}
		}
		return true;
	}
	@Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		if(properties==null) {
			LOG.debug("Missing properties, for example <properties name=\"subset.prefix\" value=\"col0, col1,..\"/>");
			return true;
		}
		String xmlStr=document.getFormattedContent();
    	StringReader reader=new StringReader(xmlStr);
    	
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
				XPathExpression exp=XMLUtil.compile("//select[@id='selectByExampleWithBLOBs']");
				org.w3c.dom.Element templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				if(templateElement==null) {
					exp=XMLUtil.compile("//select[@id='selectByExample']");
					templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				}
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExample"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成selectByExampleXXX
				exp=XMLUtil.compile("//select[@id='selectOneByExampleWithBLOBs']");
				templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				if(templateElement==null) {
					exp=XMLUtil.compile("//select[@id='selectOneByExample']");
					templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				}
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectOneByExample"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成对应的selectByExampleWithRowboundsXXX
				exp=XMLUtil.compile("//select[@id='selectByExampleWithBLOBsWithRowbounds']");
				templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				if(templateElement==null) {
					exp=XMLUtil.compile("//select[@id='selectByExampleWithRowbounds']");
					templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				}
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByExampleWithRowbounds"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
				
				//生成对应的selectByPrimaryKeyXXX
				exp=XMLUtil.compile("//select[@id='selectByPrimaryKey']");
				templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
				if(templateElement!=null) {
					XmlElement e=(XmlElement)PluginUtil.cloneElement(templateElement, "selectByPrimaryKey"+key);
					changeColumnList(e, key+"_Column_list");
					document.getRootElement().addElement(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    	
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
