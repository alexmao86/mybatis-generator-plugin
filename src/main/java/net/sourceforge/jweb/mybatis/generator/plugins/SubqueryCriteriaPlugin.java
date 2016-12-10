package net.sourceforge.jweb.mybatis.generator.plugins;
/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.List;

import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * Typically, dealing with user/role table for selecting users with role SQL: <br>
 * <i>select * from user where id in (select user_id as id from user_role where role_id=?)</i>
 * Offical Generator generated code, you need to do as follow:
 * <pre>
 * UserRoleExample e=new UserRoleExample();
 * e.createCriteria().andRoleIdEqual(?);
 * List&lt;UserRole&gt; userRoles=userRoleMapper.selectByExample(e);
 * 
 * List&lt;Integer&gt; idList=new ArrayList&lt;Integer&gt;(userRoles.size());
 * for(UserRole ur:userRoles) idList.add(ur.getUserId());
 * 
 * UserExample e1=new UserExample();
 * e1.createCriteria().andIdIn(idList);
 * userMapper.selectByExample(e1);
 * </pre>
 * Look, it is boring, right?! so SubqueryCriteriaPlugin can enhance this as:
 * <pre>
 * UserExample e1=new UserExample();
 * e1.createCriteria().andGenericSubquery("id in (select user_id as id from user_role where role_id=roleId)");
 * </pre>
 * @author maoanapex88@163.com
 *
 */
public class SubqueryCriteriaPlugin extends PluginAdapter {
	/**
     * 
     */
    public SubqueryCriteriaPlugin() {
        super();
    }

    public boolean validate(List<String> warnings) {
        return true;
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
        
        //���һ��ͨ�õ��Ӳ�ѯ��䣬һ���Զ�����䣬����: ... and subQuery
        //subQuery �� user_id in (select ..... )

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
