package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.plugins.RowBoundsPlugin;

/**
 * PagePlugin is replacement of org.mybatis.generator.plugins.RowBoundsPlugin.
 * change selectByExampleWithRowBounds' parameter RowBounds to Page. becuase my
 * design of mybatis interceptor for paging used Page which extends RowBounds
 * holding paging information.
 * 
 * Usage:
 * <pre>
 * &lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.PagePlugin"&gt;
 * &lt;property name="useRowBounds" value="true/false"/&gt;&lt;--true to use old org.mybatis.generator.plugins.RowBoundsPlugin --&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 
 * <pre>
 * &lt;table ...&gt;
 * &lt;!-- your mapper file, you can use any tag name as root --&gt;
 * &lt;--true to use old org.mybatis.generator.plugins.RowBoundsPlugin in one table level --&gt;
 * &lt;property name="PagePluginUseRowBounds" value="true/false"/&gt;
 * &lt;/table&gt;
 * </pre>
 * @author maoanapex88@163.com
 *
 */
public class PagePlugin extends RowBoundsPlugin {
	private FullyQualifiedJavaType pageBound;
	private boolean useOldRowBounds=false;
	
	public boolean validate(List<String> warnings) {
		System.out.println("\tＲＥＡＤ　ｂｅｆｏｒｅ　ｕｓｅ*****************************************");
		System.out.print("\tnet.sourceforge.jweb.mybatis.generator.plugins.PagePlugin");
		System.out.println(" is replacement of org.mybatis.generator.plugins.RowBoundsPlugin");
		System.out.println("\tPagePlugin changes client methods ");
		System.out.println("\tList<Domain> selectByExampleWithBLOBsWithRowbounds(DomainExample example, RowBounds rowBounds);");
		System.out.println("\tList<Domain> selectByExampleWithRowbounds(DomainExample example, RowBounds rowBounds);");
		System.out.println("\tto");
		System.out.println("\tList<Domain> selectByExampleWithBLOBsWithRowbounds(DomainExample example, Page rowBounds);");
		System.out.println("\tList<Domain> selectByExampleWithRowbounds(DomainExample example, Page rowBounds);");
		System.out.println("\tPage class is in below maven artifact");
		System.out.println("\t  <!-- https://mvnrepository.com/artifact/com.github.alexmao86/jweb-common -->");
		System.out.println("\t	<dependency>");
		System.out.println("\t	    <groupId>com.github.alexmao86</groupId>");
		System.out.println("\t	    <artifactId>jweb-common</artifactId>");
		System.out.println("\t	    <version>???</version>");
		System.out.println("\t	</dependency>");

		Properties properties=this.getProperties();
		if(properties!=null){
			String value=properties.getProperty("useRowBounds");
			useOldRowBounds = "true".equalsIgnoreCase(value);
		}
		return super.validate(warnings);
	}
	
	public PagePlugin() {
		super();
		this.pageBound = new FullyQualifiedJavaType("net.sourceforge.orm.mybatis.Page");
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		boolean useOldRowBoundsTableLevel = "true".equalsIgnoreCase(properties.getProperty("PagePluginUseRowBounds"));
		if(useOldRowBounds||useOldRowBoundsTableLevel){
			return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
		}
		else {
			if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
				copyAndAddMethod(method, interfaze);
			}
		}
		return true;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		Properties properties=introspectedTable.getTableConfiguration().getProperties();
		boolean useOldRowBoundsTableLevel = "true".equalsIgnoreCase(properties.getProperty("PagePluginUseRowBounds"));
		if(useOldRowBounds||useOldRowBoundsTableLevel){
			return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
		}
		else {
			if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3) {
				copyAndAddMethod(method, interfaze);
			}
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
