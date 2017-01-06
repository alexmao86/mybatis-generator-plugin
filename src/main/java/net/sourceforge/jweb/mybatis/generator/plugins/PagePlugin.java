package net.sourceforge.jweb.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.plugins.RowBoundsPlugin;

/**
 * change selectByExampleWithRowBounds' parameter RowBounds to Page. becuase my
 * design of mybatis interceptor for paging used Page which extends RowBounds
 * holding paging information.
 * 
 * @author maoanapex88@163.com
 *
 */
public class PagePlugin extends RowBoundsPlugin {
	private FullyQualifiedJavaType pageBound;

	public PagePlugin() {
		super();
		this.pageBound = new FullyQualifiedJavaType("net.sourceforge.orm.mybatis.Page");
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
			copyAndAddMethod(method, interfaze);
		}
		return true;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
			copyAndAddMethod(method, interfaze);
		}
		return true;
	}

	private void copyAndAddMethod(Method method, Interface interfaze) {
		Method newMethod = new Method(method);
		newMethod.setName(method.getName() + "WithRowbounds");
		newMethod.addParameter(new Parameter(this.pageBound, "rowBounds"));
		interfaze.addMethod(newMethod);
		interfaze.addImportedType(this.pageBound);
	}
}
