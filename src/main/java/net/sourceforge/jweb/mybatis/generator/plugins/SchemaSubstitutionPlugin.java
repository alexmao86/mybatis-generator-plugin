package net.sourceforge.jweb.mybatis.generator.plugins;

import java.lang.reflect.Field;
import java.util.List;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

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
	
	@Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        InnerClass criteria = null;
        // first, find the Criteria inner class
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
                criteria = innerClass;
                break;
            }
        }
        if (criteria == null) {
            // can't find the inner class for some reason, bail out.
            return true;
        }
        
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "subQueryClause")); //$NON-NLS-1$

        //method name
        method.setName("andGenericSubquery");
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        method.addBodyLine("addCriterion(subQueryClause);");
        method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$

        criteria.addMethod(method);

        return true;
    }
}
