package net.sourceforge.jweb.maven.mojo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import net.sourceforge.jweb.annotation.Application;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
/**
 * scan annotation for net.sourceforge.jweb.annotaion.Application on class path
 * register all web application entry to database
 * @author alex
 *
 */
@Mojo(name = "register", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class ApplicationRegistrationMojo extends AbstractMojo {
	@Parameter(property = "driver", required = true)
	private String driver;
	@Parameter(property = "url", required = true)
	private String url;
	@Parameter(property = "username", required = false, defaultValue="")
	private String username;
	@Parameter(property = "password", required = false, defaultValue="")
	private String password;
	@Parameter(property = "insert", required = false, defaultValue="insert into s_application (id,parent_id,application_key,application_name,description,href,icon_class,last_modify,sort_key) values (?,?,?,?,?,?,?,?,?)")
	private String insert;
	@Parameter(property = "purge", required = false, defaultValue="delete from s_application")
	private String purge;
	@Parameter(property = "packagePrefix", required = false, defaultValue="")
	private String packagePrefix;
	/**
	 * Properties for scan
	 */
	@Parameter(property = "properties", required = false)
	private Properties properties;
	
	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		Connection connection=null;
		try {
			System.out.println("PWD"+password);
			Class.forName(driver);
			if(username==null){
				connection=DriverManager.getConnection(url);
			}
			else{
				connection=DriverManager.getConnection(url, username, password==null?"":password);
			}

			PreparedStatement purgeSql=connection.prepareStatement(purge);
			purgeSql.execute();
			purgeSql.close();
			
			PreparedStatement insertSql=connection.prepareStatement(insert);
			
			ConfigurationBuilder builder=new ConfigurationBuilder();
			builder.setScanners(new TypeAnnotationsScanner());
			if(packagePrefix.isEmpty()){
				getLog().warn( "You did not setup one clear scan prefix, so it is going to scan the whole classpath, this is very slow or nothing will be scan");
			}
			else {
				builder.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packagePrefix)));
				builder.setUrls(ClasspathHelper.forPackage(packagePrefix));
			}
			Reflections reflections = builder.build();
			
			//get all spring mvc annotated class
			Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Controller.class, false);
			if(annotated.isEmpty()){
				getLog().warn("no web method found, is there any configuration mistake?");
			}
			for(Class<?> clazz:annotated){
				String baseUrl=getServletPath(clazz);
				Set<Method> webMethods = ReflectionUtils.getAllMethods(
						clazz,
						ReflectionUtils.withModifier(Modifier.PUBLIC)
				);
				
				for(Method method:webMethods){
					String url = getServletPath(method);
					if(url.isEmpty()) continue;
					
					getLog().info("Find web method: "+baseUrl+url);
					
					Application app=method.getAnnotation(Application.class);
					
					if(app==null){
						getLog().error("no application config found, please add@Application to mapped method, ignored");
						continue;
					}

					insertSql.clearParameters();
					int i=1;
					insertSql.setInt(i++, app.id());
					insertSql.setInt(i++, app.parentId());
					insertSql.setString(i++, app.key());
					insertSql.setString(i++, app.name());
					insertSql.setString(i++, app.desc());
					insertSql.setString(i++, baseUrl+url);
					insertSql.setString(i++, app.icon());
					insertSql.setDate(i++, new java.sql.Date(System.currentTimeMillis()));
					insertSql.setInt(i++, app.sort());
					insertSql.execute();
				}
			}
		} catch (ClassNotFoundException e) {
			getLog().error(e.getMessage());
			e.printStackTrace();
			return;
		}
		catch (SQLException e) {
			getLog().error(e.getMessage());
			e.printStackTrace();
			return;
		}finally {
			if(connection!=null) {
				try {
					connection.close();
				} catch (SQLException e) {
					getLog().warn("close connection failed");
				}
			}
		}
	}

	private String getServletPath(Method method) {
		String path="";
		if(path.isEmpty()){
			RequestMapping clazzMapping=method.getAnnotation(RequestMapping.class);
			if(clazzMapping!=null)path=clazzMapping.value()[0];
		}
		
		if(path.isEmpty()){
			GetMapping getMapping=method.getAnnotation(GetMapping.class);
			if(getMapping!=null)path=getMapping.value()[0];
		}
		
		if(path.isEmpty()){
			PostMapping postMapping=method.getAnnotation(PostMapping.class);
			if(postMapping!=null)path=postMapping.value()[0];
		}
		
		if(path.isEmpty()){
			DeleteMapping deleteMapping=method.getAnnotation(DeleteMapping.class);
			if(deleteMapping!=null)path=deleteMapping.value()[0];
		}
		
		if(path.isEmpty()){
			PutMapping deleteMapping=method.getAnnotation(PutMapping.class);
			if(deleteMapping!=null)path=deleteMapping.value()[0];
		}
		
		return path;
	}
	private String getServletPath(Class<?> clazz) {
		String path="";
		if(path.isEmpty()){
			RequestMapping clazzMapping=clazz.getAnnotation(RequestMapping.class);
			if(clazzMapping!=null)path=clazzMapping.value()[0];
		}
		
		if(path.isEmpty()){
			GetMapping getMapping=clazz.getAnnotation(GetMapping.class);
			if(getMapping!=null)path=getMapping.value()[0];
		}
		
		if(path.isEmpty()){
			PostMapping postMapping=clazz.getAnnotation(PostMapping.class);
			if(postMapping!=null)path=postMapping.value()[0];
		}
		
		if(path.isEmpty()){
			DeleteMapping deleteMapping=clazz.getAnnotation(DeleteMapping.class);
			if(deleteMapping!=null)path=deleteMapping.value()[0];
		}
		
		if(path.isEmpty()){
			PutMapping deleteMapping=clazz.getAnnotation(PutMapping.class);
			if(deleteMapping!=null)path=deleteMapping.value()[0];
		}
		
		return path;
	}
}
