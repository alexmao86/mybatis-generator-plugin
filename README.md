# mybatis-generator-plugin
<b>Mybatis</b>(http://blog.mybatis.org/) is one fantastic ORM tool for Java, I like it very much because of its lightweight and smart way to do ORM. There is also one <b>mybatis generator</b>In official release, generator helps generating standard java code as you project's DAO layer. <br>

This project is plugin suite for mybatis generator. What is mybatis generator, please see http://www.mybatis.org/generator/. The generator is handy to use, but there are still some not handy parts:
<ul>
<li><b>Subquery</b> I want to execute sql like this, select c1, c2, ..., cn from table where <b>c1 in (select cc1 as c1 from table 1 where cc2 = 1)</b>, there is no generated code source for this. Using offical generator, you will write code like this:<br>
<pre>
YourOtherDomainExample e=new YourOtherDomainExample();
e.createCriteria().andCc2Equals(1);
List yourOtherDomainList = YourOtherDomainMapper.selectByExample(e);

//then constructe one list of id
list idList=new ArrayList(yourOtherDomainList.size());
for(YourOtherDomain d:yourOtherDomainList) idList.add(d.getCc1());

//use IdInList query
YourDomainExample e1=new YourDomainExample();
e1.createCriteria().andC1In(idList);
YourDomainMapper.selectByExample(e1);

</pre>
<pre>
&lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.SubqueryCriteriaPlugin"/&gt; DO it
</pre>
add this plugin to your generatorConfig.xml, then you will get 
<pre>
YourDomainExample{
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
<li>
<b>SelectOneByExample</b>, default generatored code does not have select single one Object by example, 
<pre>
&lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.SelectOneByExamplePlugin"/&gt; DO it
</pre>
In your DAO/Mapper code, you will see:
<pre>

 /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table domain
     *
     * @mbggenerated
     */
    Domain selectOneByExample(DomainExample example);
</pre>
</li>
<li>
<b>Advanced Clause Example</b>, default mybatis generator sql statement style is: "select columns from table where <b>(a and b and c ...) OR (d and e and f ... ) OR (... and ...)</b>", in some case, this is really inconvenient, for example, you want you sql as follow:<br>
select columns from table where <b>(a or b) and c</b>, to suit mybatis generator, you must use its equivalent form - <br>
select columns from table where <b>(a and c) or (b and c)</b>, if this is complex, it is hard to transform it.
<pre>
&lt;plugin type="net.sourceforge.jweb.mybatis.generator.plugins.AdvancedWhereClausePlugin"/&gt; DO it
</pre>
You will see
<pre>
YourDomainExample{
...
 protected abstract static class GeneratedCriteria {
   ...
       public Criteria andDomainClumnxxxx(.....) {
            ......
            return (Criteria) this;
        }
        public Criteria <b>orDomainClumnxxxx</b>(.....) {
            ......
            return (Criteria) this;
        }
   ...
 }
}
</pre>
</li>
<li>
Some annotations plugin
<pre>
net.sourceforge.jweb.mybatis.generator.plugins.OptionsAnnotationPlugin
net.sourceforge.jweb.mybatis.generator.plugins.CacheAnnotationPlugin
net.sourceforge.jweb.mybatis.generator.plugins.ModelBuilderPlugin
</pre>
</li>
</ul>
<pre>
&lt;repository&gt;
    &lt;id&gtlSonatype OSS Snapshot Repository&lt;/id&gt;
    &lt;url&gtlhttp://oss.sonatype.org/content/repositories/snapshots&lt;/url&gt;
&lt;/repository&gt;
&lt;repository&gt;
    &lt;id&gtlSonatype OSS Release Repository&lt;/id&gt;
    &lt;url&gtlhttps://oss.sonatype.org/service/local/staging/deploy/maven2&lt;/url&gt;
&lt;/repository&gt;
&lt;dependency&gt;
  &lt;groupId&gt;com.github.alexmao86&lt;/groupId&gt;
  &lt;artifactId&gt;jweb-maven-plugin&lt;/artifactId&gt;
  &lt;version&gt;1.0&lt;/version&gt;
&lt;/dependency&gt;
</pre>
