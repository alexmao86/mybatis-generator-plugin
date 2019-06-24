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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import net.sourceforge.jweb.maven.util.XMLUtil;

/**
 * plugin util for transform w3c.DOM and mybatis generator DOM
 * 
 * events
 * 
 * @author Alex
 *
 */
public class PluginUtil {
	private PluginUtil(){
		
	}
	
	public static Method getMethod(Interface unit, String... nameCandidates) {
		for(Method method:unit.getMethods()) {
			for(String name:nameCandidates) {
				if(method.getName().equals(name)){
					return method;
				}
			}
		}
		return null;
	}
	
	public static Method getMethod(TopLevelClass unit, String... nameCandidates) {
		for(Method method:unit.getMethods()) {
			for(String name:nameCandidates) {
				if(method.getName().equals(name)){
					return method;
				}
			}
		}
		return null;
	}
	
	public static org.w3c.dom.Element getElement(org.w3c.dom.Document dom, String... xpathCandidates) throws XPathExpressionException{
		for(String xpath:xpathCandidates) {
			XPathExpression exp=XMLUtil.compile(xpath);
			org.w3c.dom.Element templateElement = (org.w3c.dom.Element)exp.evaluate(dom, XPathConstants.NODE);
			if(templateElement!=null) {
				return templateElement;
			}
		}
		return null;
	}
	
	public static Method clone(Method template) {
		return new Method(template);
	}
	
    public static Attribute cloneAttribute(Node node){
    	return new Attribute(node.getNodeName(), node.getNodeValue());
    }
    
    public static org.mybatis.generator.api.dom.xml.Element cloneElement(Node w3cEl){
    	return cloneElement(w3cEl, null);
    }
    
    public static org.mybatis.generator.api.dom.xml.Element cloneElement(Node w3cEl, String idAttrValue){
    	if(!(w3cEl instanceof org.w3c.dom.Element)){
    		return new TextElement(w3cEl.getNodeValue());
    	}
    	XmlElement root=new XmlElement(w3cEl.getNodeName());
    	Queue<ElementPeer> queue=new LinkedList<ElementPeer>();
    	queue.offer(new ElementPeer(root, w3cEl));
    	while(!queue.isEmpty()){
    		ElementPeer cur=queue.poll();
    		NamedNodeMap attrs = cur.w3cElement.getAttributes();
    		if(attrs!=null){
    			for(int i=0;i<attrs.getLength();i++){
    				Node attr=attrs.item(i);
    				if(cur.mybatisElement==root && idAttrValue!=null && "id".equals(attr.getNodeName())){//Ϊ��ڵ��޸�id����
    					cur.mybatisElement.addAttribute(new Attribute("id", idAttrValue));
    				}
    				else{
    					cur.mybatisElement.addAttribute(PluginUtil.cloneAttribute(attr));
    				}
    			}
    		}
    		
    		NodeList children = cur.w3cElement.getChildNodes();
    		if(children!=null){
    			for(int i=0;i<children.getLength();i++){
    				Node child=children.item(i);
    				if(child instanceof Text){
    					String text=child.getTextContent();
    					text=text.trim();
    					if(text.length()>0)cur.mybatisElement.addElement(new TextElement(text.trim()));
    				}
    				else if(child instanceof Comment){
    					String text = child.getNodeValue().trim();
    					if(text.length()>0) cur.mybatisElement.addElement(new TextElement("<!--"+new Date()+"\n"+text+"-->"));
    				}
    				else if(child instanceof org.w3c.dom.Element){
    					XmlElement mybatisChild=new XmlElement(child.getNodeName());
    					cur.mybatisElement.addElement(mybatisChild);
    					queue.offer(new ElementPeer(mybatisChild, (org.w3c.dom.Element)child));
    				}
    			}
    		}
    	}
    	return root;
    }
    /**
	 * find first child element of given name
	 * @param element
	 * @param name
	 * @return
	 */
    public static XmlElement findFirst(XmlElement element, String name){
		Queue<XmlElement> queue=new LinkedList<XmlElement>();
		queue.offer(element);
		
		while(!queue.isEmpty()){
			XmlElement top=queue.poll();
			if(name.equals(top.getName())){
				return top;
			}
			
			for(org.mybatis.generator.api.dom.xml.Element ch:top.getElements()){
				if(ch instanceof XmlElement){
					queue.offer((XmlElement)ch);
				}
			}
		}
		
		return null;
	}
	
	public static List<XmlElement> findAll(XmlElement element, String name){
		List<XmlElement> ret=new ArrayList<XmlElement>();
		
		Queue<XmlElement> queue=new LinkedList<XmlElement>();
		queue.offer(element);
		
		while(!queue.isEmpty()){
			XmlElement top=queue.poll();
			if(name.equals(top.getName())){
				ret.add(top);
			}
			
			for(org.mybatis.generator.api.dom.xml.Element ch:top.getElements()){
				if(ch instanceof XmlElement){
					queue.offer((XmlElement)ch);
				}
			}
		}
		
		return ret;
	}
   
    private final static class ElementPeer {
    	XmlElement mybatisElement;
    	Node w3cElement;
		public ElementPeer(XmlElement mybatisElement, Node w3cElement) {
			super();
			this.mybatisElement = mybatisElement;
			this.w3cElement = w3cElement;
		}
    }
    /**
     * replace first child name of nodeName with textElement
     * @param e
     * @param nodeName
     * @param textElement
     */
	public static boolean replaceFirst(XmlElement e, String nodeName, TextElement textElement) {
		int i=0;
		for(i=0;i<e.getElements().size();i++) {
			Element ch=e.getElements().get(i);
			if(ch instanceof XmlElement) {
				String name=((XmlElement) ch).getName();
				if(name.equals(nodeName)) {
					break;
				}
			}
		}
		if(i>=0&&i<e.getElements().size()) {
			e.getElements().set(i, textElement);
			return true;
		}
		return false;
	}
}
