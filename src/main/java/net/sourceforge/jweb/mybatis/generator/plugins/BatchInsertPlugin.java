package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;

/**
 * 实现批量插入， 生成xml的片段参考
 * https://stackoverflow.com/questions/17563463/mybatis-insert-list-values
 * 
 * 
 * 
 * Mapper中方法签名格式 int insertInBatch(List<YourDomain> list); int
 * insertSelectiveInBatch(List<YourDomain> list);
 * 
 * <p>
 * 针对<PRE>
 * &lt;table tableName="xxx" domainObjectName="xxx"&gt;
 *  &lt;generatedKey column="id" sqlStatement="SELECT UUID_SHORT()" identity="false" /&gt;
 * &lt;/table&gt;
 * </PRE>
 * 会将<b>sqlStatement</b>的值直接作为values部分该column对应的值；　前提是identity=false
 * </p>
 * 
 * <p>
 * 本插件为简单基础版的批量插入,自行拼接。
 * 若需要更复杂强大的插件可以参考: https://github.com/itfsw/mybatis-generator-plugin
 * </p>
 * @author maoanapex88@163.com alexmao86
 * @author dailey.yet@outlook.com
 *
 */
public class BatchInsertPlugin extends PluginAdapter {

	private static final String METHOD_BATCH_INSERT = "insertInBatch";

	@Override
	public boolean validate(List<String> arg0) {
		return true;
	}

	/**
	 * 生成xml的片段参考
	 * https://stackoverflow.com/questions/17563463/mybatis-insert-list-values
	 * 手动写mybatis的document比较麻烦，可以利用PluginUtil进行clone
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		final String separator = ", ";
		XmlElement batchInsertElement = new XmlElement("insert");
		batchInsertElement.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
		batchInsertElement.addAttribute(new Attribute("parameterType", "list"));
		StringBuffer insertSB = new StringBuffer();
		StringBuffer valuesSB = new StringBuffer();
		insertSB.append("insert into ").append(introspectedTable.getFullyQualifiedTableNameAtRuntime()).append(" (");
		valuesSB.append(" ( ");
		Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
		GeneratedKey gk = introspectedTable.getGeneratedKey();
		while (iter.hasNext()) {
			IntrospectedColumn introspectedColumn = iter.next();
			if (introspectedColumn.isIdentity()) {
				// cannot set values on identity fields
				continue;
			}
			insertSB.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			if (gk != null && gk.getColumn().equals(introspectedColumn.getActualColumnName())
					&& gk.getRuntimeSqlStatement() != null && !gk.isIdentity()) {
				//若配置表时定义了generateKey属性，且满足特定条件，key值将为sqlStament表达式
				valuesSB.append("(" + gk.getRuntimeSqlStatement() + ")");
			} else {
				valuesSB.append("#{item." + introspectedColumn.getJavaProperty() + "}");
			}
			if (iter.hasNext()) {
				insertSB.append(separator); // $NON-NLS-1$
				valuesSB.append(separator); // $NON-NLS-1$
			}
		}
		int lastSep = insertSB.lastIndexOf(separator);
		if (lastSep == (insertSB.length() - separator.length())) {
			insertSB.delete(lastSep, separator.length());
		}
		insertSB.append(" ) values");
		lastSep = valuesSB.lastIndexOf(separator);
		if (lastSep == (valuesSB.length() - separator.length())) {
			valuesSB.delete(lastSep, separator.length());
		}
		valuesSB.append(" ) ");
		TextElement insertHeadElement = new TextElement(insertSB.toString());
		batchInsertElement.addElement(insertHeadElement);
		XmlElement forEachElement = new XmlElement("foreach");
		batchInsertElement.addElement(forEachElement);
		forEachElement.addAttribute(new Attribute("collection", "list"));
		forEachElement.addAttribute(new Attribute("item", "item"));
		forEachElement.addAttribute(new Attribute("separator", ","));
		TextElement valusElement = new TextElement(valuesSB.toString());
		forEachElement.addElement(valusElement);
		document.getRootElement().addElement(batchInsertElement);
		return true;
	}

	/**
	 * Mapper client生成后插入接口,中方法签名格式 int insertInBatch(List<YourDomain> list); int
	 * insertSelectiveInBatch(List<YourDomain> list);
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		interfaze.addImportedType(FullyQualifiedJavaType.getNewListInstance());
		FullyQualifiedJavaType rescordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		interfaze.addImportedType(rescordType);
		Method method = new Method(METHOD_BATCH_INSERT);
		interfaze.addMethod(method);
		method.setReturnType(new FullyQualifiedJavaType("int"));
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
		listType.addTypeArgument(rescordType);
		method.addParameter(new Parameter(listType, "list"));
		return true;
	}
}
