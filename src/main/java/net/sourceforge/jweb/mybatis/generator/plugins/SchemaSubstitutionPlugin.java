package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

public class SchemaSubstitutionPlugin  extends PluginAdapter {
	private final static Log LOG=LogFactory.getLog(SchemaSubstitutionPlugin.class);
	private static final String RECUR_MARK = "__SchemaSubstitutionPlugin__recur__mark__";

	public boolean validate(List<String> paramList) {
		return true;
	}

	public void initialized(IntrospectedTable introspectedTable) {
		if(!isPluginEnabled(introspectedTable)){
			return ;
		}
		
		final FullyQualifiedTable fqt = introspectedTable.getFullyQualifiedTable();
		try {
			java.lang.reflect.Field isf=fqt.getClass().getDeclaredField("runtimeTableName");
			isf.setAccessible(true);
			Object value=isf.get(fqt);
			if(value!=null){
				String strValue = value.toString();
				isf.set(fqt, "${namespace}"+strValue);
			}
			
			java.lang.reflect.Field rsf=fqt.getClass().getDeclaredField("introspectedTableName");
			rsf.setAccessible(true);
			value=rsf.get(fqt);
			if(value!=null){
				String strValue = value.toString();
				rsf.set(fqt, "${namespace}"+strValue);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		//this is one duplicated invocation, the purpose is to trigger calling calculateXmlAttributes, 
		//final to ullyQualifiedTable.getFullyQualifiedTableNameAtRuntime()
		Object mark=introspectedTable.getAttribute(RECUR_MARK);
		if(mark==null){
			introspectedTable.setAttribute(RECUR_MARK, RECUR_MARK);
			introspectedTable.initialize();
		}
	}
	
	@Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(!isPluginEnabled(introspectedTable)){
			return true;
		}
		insertFieldWithAccessor(topLevelClass);
        
        return true;
    }

	
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(!isPluginEnabled(introspectedTable)){
			return true;
		}
		insertFieldWithAccessor(topLevelClass);
        
        return true;
	}

	private void insertFieldWithAccessor(TopLevelClass topLevelClass) {
		//add namespace attribute
		Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName("namespace");
        field.setInitializationString("\"\"");
        field.addJavaDocLine("/**namespace used as schema indicator*/");
        topLevelClass.addField(field);

        //setter
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setNamespace");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "ns"));
        method.addBodyLine("this.namespace = ns;");
        topLevelClass.addMethod(method);
        
        //getter
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("getNamespace");
        method.addBodyLine("return this.namespace;");
        topLevelClass.addMethod(method);
	}
	private boolean isPluginEnabled(final IntrospectedTable ist){
		Properties properties=ist.getTableConfiguration().getProperties();
		if(properties==null) return false;
		String enable=properties.getProperty("SchemaSubstitutionPluginEnabled");
		
		if("false".equalsIgnoreCase(enable)){
			LOG.debug("SchemaSubstitutionPlugin disabled for table: "+ist.getTableConfiguration().getTableName());
			return false;
		}
		
		return true;
	}
}
