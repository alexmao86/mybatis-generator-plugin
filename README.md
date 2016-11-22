# mybatis-generator-plugin
<b>Mybatis</b>(http://blog.mybatis.org/) is one fantastic ORM tool for Java, I like it very much because of its lightweight and smart way to do ORM. There is also one <b>mybatis generator</b>In official release, generator helps generating standard java code as you project's DAO layer. <br>

This project is plugin suite for mybatis generator. What is mybatis generator, please see http://www.mybatis.org/generator/. The generator is handy to use, but there are still some not handy parts:
<ul>
<li><b>Subquery</b> I want to execute sql like this, select c1, c2, ..., cn from table where <b>c1 in (select cc1 as c1 from table 1 where cc2 = ?)</b>, there is no generated code sourde for this.<br>
<pre>
&lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.AdvancedWhereClausePlugin"/&gt; DO it
</pre>
add this plugin to your generatorConfig.xml, then you will get 
<pre>YourDomainExample{
...
 protected abstract static class GeneratedCriteria {
   ...
   Criteria has andGenericSubquery(String subquery){
   ...
   }
   ...
 }
}
Usage:
YourDomainExample e=new YourDomainExample();
e.createCriteria().andGenericSubquery("c1 in (select cc1 as c1 from table 1 where cc2 = 10)");
mapper.selectByExample(e);
</pre>
</li>
</ul>
