package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;
import java.util.Properties;

import org.apache.shiro.util.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * 在和spring整合时，需要全用xml或者全注解方式，不支持混合方式，因此，这里提供生成xml的插件。
 * 插件检测table中的相关配置，参考dtd文件，property加前缀cachexml.
 * 
 * cachexml.enable: true/false means enable cache
 * 
 * 目前只支持增加cache 结点，不在insert,update,delete,select上增加属性
 * @author maoanapex88@163.com alexmao86
 *
 */
public class CacheXmlPlugin extends PluginAdapter{
	private final static Log LOG=LogFactory.getLog(CacheXmlPlugin.class);
	@Override
	public boolean validate(List<String> arg0) {
		LOG.debug("CacheXmlPlugin does no validate");
        return true;
	}
	
	//增加 serialiable 接口实现
	@Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType("java.io.Serializable");
		topLevelClass.addSuperInterface(new FullyQualifiedJavaType("java.io.Serializable"));
		
		Field serializationIdField=new Field();
		serializationIdField.setName("serialVersionUID");
		serializationIdField.setStatic(true);
		serializationIdField.setFinal(true);
		serializationIdField.setType(new FullyQualifiedJavaType("long"));
		serializationIdField.setVisibility(JavaVisibility.PRIVATE);
		serializationIdField.setInitializationString("1L");
		topLevelClass.addField(serializationIdField);
		
		return true;
	}
	
	//插入cache xml
	@Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		
		if(properties==null) {
			return true;
		}
		boolean enable=Boolean.parseBoolean(properties.getProperty("cachexml.enable", "false"));
		if(!enable) {
			return true;
		}
		XmlElement cacheEl=new XmlElement("cache");
		
		String type=properties.getProperty("cachexml.type");
		if(StringUtils.hasText(type)) {
			cacheEl.addAttribute(new Attribute("type", type));
		}
		String eviction=properties.getProperty("cachexml.eviction");
		if(StringUtils.hasText(eviction)) {
			cacheEl.addAttribute(new Attribute("eviction", eviction));
		}
		String flushInterval=properties.getProperty("cachexml.flushInterval");
		if(StringUtils.hasText(flushInterval)) {
			cacheEl.addAttribute(new Attribute("flushInterval", flushInterval));
		}
		String size=properties.getProperty("cachexml.size");
		if(StringUtils.hasText(size)) {
			cacheEl.addAttribute(new Attribute("size", flushInterval));
		}
		String readOnly=properties.getProperty("cachexml.readOnly");
		if(StringUtils.hasText(readOnly)) {
			cacheEl.addAttribute(new Attribute("readOnly", readOnly));
		}
		String blocking=properties.getProperty("cachexml.blocking");
		if(StringUtils.hasText(blocking)) {
			cacheEl.addAttribute(new Attribute("blocking", blocking));
		}
		document.getRootElement().addElement(0, cacheEl);
		return true;
	}
}
