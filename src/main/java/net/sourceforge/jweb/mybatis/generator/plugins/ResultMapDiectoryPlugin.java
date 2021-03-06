package net.sourceforge.jweb.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
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
		
		Field field=new Field();
		field.setFinal(true);
		field.setStatic(true);
		field.setName("resultMap");
		FullyQualifiedJavaType mapType=FullyQualifiedJavaType.getNewMapInstance();
		mapType.addTypeArgument(new FullyQualifiedJavaType("java.lang.String"));
		mapType.addTypeArgument(new FullyQualifiedJavaType("java.lang.String"));
		field.setType(mapType);
		field.setInitializationString("new HashMap<String, String>()");
		field.setVisibility(JavaVisibility.PRIVATE);
		
		InitializationBlock block=new InitializationBlock(true);
		for (IntrospectedColumn c : introspectedTable.getAllColumns())
        {
			String line=new StringBuilder().append("resultMap.put(\"").append(c.getJavaProperty()).append("\",\"").append(c.getActualColumnName()).append("\");").toString();
			block.addBodyLine(line);
        }
		topLevelClass.addInitializationBlock(block);
		
		topLevelClass.addField(field);
		
		Method method = new Method();

		method.setVisibility(JavaVisibility.PUBLIC);
		method.setFinal(true);
		method.setStatic(true);
		method.setReturnType(new FullyQualifiedJavaType("java.lang.String"));
		method.setName("mappingRealColumn");
		method.addParameter(new Parameter(new FullyQualifiedJavaType("java.lang.String"),"javaProperty"));
		method.addBodyLine("return resultMap.get(javaProperty);");
		
		topLevelClass.addMethod(method);
		
		/*
	    enum Column{
	        A("","");
	        private final String name;
	
	        Column(String name, String order) {
	            this.name = name;
	            this.order = order;
	        }
	        public String asc(){
	            return this.name+" asc";
	        }
	        public  String desc(){
	            return this.name+" desc";
	        }
	    }
		 */
		InnerEnum columnEnum=new InnerEnum(new FullyQualifiedJavaType(topLevelClass.getType().getFullyQualifiedNameWithoutTypeParameters()+".Column"));
		columnEnum.setVisibility(JavaVisibility.PUBLIC);
		topLevelClass.addInnerEnum(columnEnum);
		
		Field columnName=new Field();
		columnName.setFinal(true);
		columnName.setStatic(false);
		columnName.setVisibility(JavaVisibility.PRIVATE);
		columnName.setName("actualTableColumnName");
		columnName.setType(FullyQualifiedJavaType.getStringInstance());
		columnEnum.addField(columnName);
		
		Method contructor=new Method();
		contructor.setConstructor(true);
		contructor.setName("Column");
		contructor.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
		contructor.addBodyLine("this.actualTableColumnName=name;");
		columnEnum.addMethod(contructor);
		
		Method name=new Method();
		name.setVisibility(JavaVisibility.PUBLIC);
		name.setName("col");
		name.setReturnType(FullyQualifiedJavaType.getStringInstance());
		name.addBodyLine("return this.actualTableColumnName;");
		columnEnum.addMethod(name);
		
		Method asc=new Method();
		asc.setVisibility(JavaVisibility.PUBLIC);
		asc.setName("asc");
		asc.setReturnType(FullyQualifiedJavaType.getStringInstance());
		asc.addBodyLine("return this.actualTableColumnName+\" asc \";");
		columnEnum.addMethod(asc);
		
		Method desc=new Method();
		desc.setVisibility(JavaVisibility.PUBLIC);
		desc.setName("desc");
		desc.setReturnType(FullyQualifiedJavaType.getStringInstance());
		desc.addBodyLine("return this.actualTableColumnName+\" desc\";");
		columnEnum.addMethod(desc);
		for (IntrospectedColumn c : introspectedTable.getAllColumns())
        {
			String enumConstant=new StringBuilder().append(c.getJavaProperty()).append("(\"").append(c.getActualColumnName()).append("\")").toString();
			columnEnum.addEnumConstant(enumConstant);
        }
		
		Method niceOrderBy=new Method();
		niceOrderBy.addJavaDocLine("/*\nGenerated by ResultMapDirectoryPlugin");
		niceOrderBy.addJavaDocLine("demo: example.setOrderBy("+topLevelClass.getType().getShortName()+".Column.xxx.asc() or desc() )\n*/");
		
		niceOrderBy.setName("orderBy");
		niceOrderBy.setVisibility(JavaVisibility.PUBLIC);
		niceOrderBy.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "orders", true));
		niceOrderBy.addBodyLine("StringBuilder builder=new StringBuilder();");
		niceOrderBy.addBodyLine("for(int i=0;i<orders.length;i++){");
		niceOrderBy.addBodyLine("	builder.append(orders[i]).append(\" \");");
		niceOrderBy.addBodyLine("}");
		niceOrderBy.addBodyLine("if(builder.length()>0){this.setOrderByClause(builder.toString());}");
		topLevelClass.addMethod(niceOrderBy);
		
		return true;
	}
	
	
}
