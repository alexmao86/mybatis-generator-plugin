package net.sourceforge.jweb.mybatis.generator.plugins;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

public class SchemaSubstitutionPlugin  extends PluginAdapter {
	public boolean validate(List<String> paramList) {
		return true;
	}

	public void initialized(IntrospectedTable introspectedTable) {
		final FullyQualifiedTable fqt = introspectedTable.getFullyQualifiedTable();
		
		//use proxy method to intercept and change runtime table name
		introspectedTable.setFullyQualifiedTable((FullyQualifiedTable)Proxy.newProxyInstance(
						fqt.getClass().getClassLoader(), 
						new Class[]{FullyQualifiedTable.class}, 
						new InvocationHandler(){
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								String methodName = method.getName();
								if("getFullyQualifiedTableNameAtRuntime".equals(methodName)){
									Object fullyQualifiedTableNameAtRuntime = method.invoke(fqt, args);
									if(fullyQualifiedTableNameAtRuntime!=null){
										String modified = fullyQualifiedTableNameAtRuntime.toString();
										int dot=modified.lastIndexOf(".");
										if(dot==-1) {
											return "${schema}."+modified;
										}
										else {
											return modified.substring(0, dot) +"${schema}"+ modified.substring(dot);
										}
									}
									return fullyQualifiedTableNameAtRuntime;
								}
								
								return method.invoke(fqt, args);
							}
						}//end of InvocationHandler inner class
					));//proxy end，end setFullyQualifiedTable
		
		//this is one duplicated invocation, the purpose is to trigger calling calculateXmlAttributes, final to ullyQualifiedTable.getFullyQualifiedTableNameAtRuntime()
		introspectedTable.initialize();
	}
}
