package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;

/**
 * 实现批量插入， 生成xml的片段参考
 * https://stackoverflow.com/questions/17563463/mybatis-insert-list-values
 * 
 * Mapper中方法签名格式 int insertInBatch(List<YourDomain> list); int
 * insertSelectiveInBatch(List<YourDomain> list);
 * 
 * @author maoanapex88@163.com alexmao86
 *
 */
public class BatchSelectPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 生成xml的片段参考
	 * https://stackoverflow.com/questions/17563463/mybatis-insert-list-values
	 * 手动写mybatis的document比较麻烦，可以利用PluginUtil进行clone
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	/**
	 * Mapper client生成后插入接口,中方法签名格式 
	 * int insertInBatch(List<YourDomain> list);
	 * int insertSelectiveInBatch(List<YourDomain> list);
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}
}
