package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
/**
 * change selectByExampleWithRowBounds' parameter RowBounds to Page. becuase my design of mybatis interceptor
 * for paging used Page which extends RowBounds holding paging information.
 * @author maoanapex88@163.com
 *
 */
public class WithRowBoundsSignatureChangePlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if(interfaze!=null){
			//first remove import org.apache.ibatis.session.RowBounds
			Set<FullyQualifiedJavaType> types=interfaze.getImportedTypes();
			FullyQualifiedJavaType remove=null;
			for(FullyQualifiedJavaType type:types){
				if(type.getFullyQualifiedName().equals("org.apache.ibatis.session.RowBounds")){
					remove = type;
					break;
				}
			}
			if(remove!=null){
				types.remove(types);
			}
			
			//then change selectByExampleXXXWithRowbounds' parameter RowBounds to Page
			List<Method> methods=interfaze.getMethods();
			for(Method method:methods){
				if(!method.getName().endsWith("WithRowbounds")){
					continue;
				}
				List<Parameter> parameters = method.getParameters();
				int i=0;
				for(;i<parameters.size();i++){
					Parameter param=parameters.get(i);
					if(param.getType().getFullyQualifiedName().equals("org.apache.ibatis.session.RowBounds")){
						break;
					}
				}
				parameters.remove(i);
				parameters.add(new Parameter(new FullyQualifiedJavaType("net.sourceforge.orm.mybatis.Page"), "rowBounds"));
			}
		}
		
		if(topLevelClass!=null){
			//first remove import org.apache.ibatis.session.RowBounds
			Set<FullyQualifiedJavaType> types=topLevelClass.getImportedTypes();
			FullyQualifiedJavaType remove=null;
			for(FullyQualifiedJavaType type:types){
				if(type.getFullyQualifiedName().equals("org.apache.ibatis.session.RowBounds")){
					remove = type;
					break;
				}
			}
			if(remove!=null){
				types.remove(types);
			}
			
			//then change selectByExampleXXXWithRowbounds' parameter RowBounds to Page
			List<Method> methods=topLevelClass.getMethods();
			for(Method method:methods){
				if(!method.getName().endsWith("WithRowbounds")){
					continue;
				}
				List<Parameter> parameters = method.getParameters();
				int i=0;
				for(;i<parameters.size();i++){
					Parameter param=parameters.get(i);
					if(param.getType().getFullyQualifiedName().equals("org.apache.ibatis.session.RowBounds")){
						break;
					}
				}
				parameters.remove(i);
				parameters.add(new Parameter(new FullyQualifiedJavaType("net.sourceforge.orm.mybatis.Page"), "rowBounds"));
			}
		}
		
		return true;
	}
	
}
