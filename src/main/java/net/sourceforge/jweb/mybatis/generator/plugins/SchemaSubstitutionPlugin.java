package net.sourceforge.jweb.mybatis.generator.plugins;

import java.lang.reflect.Field;
import java.util.List;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

public class SchemaSubstitutionPlugin  extends PluginAdapter {
	private static final String RECUR_MARK = "__SchemaSubstitutionPlugin__recur__mark__";

	public boolean validate(List<String> paramList) {
		return true;
	}

	public void initialized(IntrospectedTable introspectedTable) {
		final FullyQualifiedTable fqt = introspectedTable.getFullyQualifiedTable();
		try {
			Field isf=fqt.getClass().getDeclaredField("runtimeTableName");
			isf.setAccessible(true);
			Object value=isf.get(fqt);
			if(value!=null){
				String strValue = value.toString();
				isf.set(fqt, "${namespace}"+strValue);
			}
			
			Field rsf=fqt.getClass().getDeclaredField("introspectedTableName");
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
	
	
}
