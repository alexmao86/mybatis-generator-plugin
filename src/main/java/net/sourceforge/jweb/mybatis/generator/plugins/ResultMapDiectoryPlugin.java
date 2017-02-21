package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.IntrospectedColumn;
/**
 * add result map to directory
 * @author alex
 *
 */
public class ResultMapDiectoryPlugin extends PluginAdapter {
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, final IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType(FullyQualifiedJavaType.getNewMapInstance());
		topLevelClass.addImportedType(FullyQualifiedJavaType.getNewHashMapInstance());
		
		Field field=new Field(){
			public String getFormattedContent(int indentLevel) {
				StringBuilder builder = new StringBuilder();
				
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("private static final Map<String, String> resultMap=new HashMap<String, String>();\n");
				
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("static{\n");
				
				for(IntrospectedColumn c : introspectedTable.getAllColumns()){
					OutputUtilities.javaIndent(builder, indentLevel);
					OutputUtilities.javaIndent(builder, indentLevel);
					builder.append("resultMap.put(\"").append(c.getJavaProperty()).append("\",\"").append(c.getActualColumnName()).append("\");\n");
				}
				
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("}\n");
				
				return builder.toString();
			}
		};
		topLevelClass.addField(field);
		
		Method method = new Method(){
			public String getFormattedContent(int indentLevel, boolean interfaceMethod) {
				if(interfaceMethod) return "";
				
				StringBuilder builder = new StringBuilder();
				
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("public static String mappingRealColumn(String javaProperty){\n");
				
				OutputUtilities.javaIndent(builder, indentLevel);
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("return resultMap.get(javaProperty);\n");
				
				OutputUtilities.javaIndent(builder, indentLevel);
				builder.append("}\n");
				
				return builder.toString();
			}
		};
		topLevelClass.addMethod(method);
		return true;
	}
	
	
}
