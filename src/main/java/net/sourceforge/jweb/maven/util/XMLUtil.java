package net.sourceforge.jweb.maven.util;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
 * 
 * XML util for DOM operation, WITHOUT VALIDATION!!!
 * @author maoanapex88@163.com
 *
 */
public class XMLUtil {
	private static DocumentBuilderFactory factory = null;
	private static DocumentBuilder builder = null;
	private static TransformerFactory tff;//
	private static ErrorHandler handler = null;
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	
	static {
		try {
			factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			builder = factory.newDocumentBuilder();
			tff = TransformerFactory.newInstance();
			builder.setEntityResolver(new EntityResolver(){
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					ByteArrayInputStream byteStream=new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
					return new InputSource(byteStream);
				}});
			handler = new ErrorHandler() {
				public void warning(SAXParseException e) throws SAXException {
				}
				public void fatalError(SAXParseException e) throws SAXException {
				}
				public void error(SAXParseException e) throws SAXException {
				}
			};
			builder.setErrorHandler(handler);
			xPathFactory=XPathFactory.newInstance();
			xPath=xPathFactory.newXPath();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	private static final Object lock=new Object();
	public static void call() {
	}

	private XMLUtil() {
	}
	public static Document newDocument(){
		// handler.clear();
		synchronized(lock){
			return builder.newDocument();
		}
	}
	/**
	 * create org.w3c.dom.Document from file
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(File file) throws Exception {
		// handler.clear();
		synchronized(lock){
			return builder.parse(file);
		}
	}
	/**
	 * create org.w3c.dom.Document from reader
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(Reader reader) throws Exception {
		// handler.clear();
		synchronized(lock){
			return builder.parse(new InputSource(reader));
		}
	}
	
	/**
	 * create org.w3c.dom.Document from input stream
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(InputStream inputStream)
			throws Exception {
		// handler.clear();
		synchronized(lock){
			return builder.parse(inputStream);
		}
	}

	/**
	 * create org.w3c.dom.Document from uri
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(String uri) throws Exception {
		// handler.clear();
		URL url = new URL(uri);
		synchronized(lock){
			return builder.parse(url.openStream());
		}
	}
	/**
	 * create org.w3c.dom.Document from URL
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Document createDocument(URL url) throws Exception {
		// handler.clear();
		synchronized(lock){
			return builder.parse(url.openStream());
		}
	}

	/**
	 * read root
	 * @param document
	 * @return
	 */
	public static Element readRoot(Document document) {
		return document.getDocumentElement();
	}

	/**
	 * read text
	 * @param element
	 * @return
	 */
	public static String readText(Node element) {
		if (element == null)
			return "";
		else
			return element.getTextContent();
	}

	/**
	 * read trimed text, if element not found return empty string
	 * @param element
	 * @return
	 */
	public static String readTrimedText(Node element) {
		if (element == null)
			return "";
		else
			return element.getTextContent().trim();
	}
	/**
	 * read attribute value as string, if attr not found return null
	 * @param element
	 * @param attributeName
	 * @return
	 */
	public static String readAttribute(Node element, String attributeName) {
		if (element == null)
			return null;
		NamedNodeMap attributes = element.getAttributes();
		if (attributes == null)
			return null;
		Node value = attributes.getNamedItem(attributeName);
		if (value == null)
			return null;
		return value.getTextContent();
	}
	/**
	 * read first element from given node as parent by given name, if not found return null
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public static Element readFirstChild(Node parentNode, String nodeName) {
		if (parentNode != null) {
			NodeList children = parentNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeName().equals(nodeName))
					return (Element) children.item(i);
			}
		}
		return null;
	}

	/**
	 * read elements as List from given node as parent by given name, if not found return list of zero length
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public static List<Element> readChildren(Node parentNode, String nodeName) {
		ArrayList<Element> ret = new ArrayList<Element>();
		NodeList children = parentNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equals(nodeName))
				ret.add((Element) children.item(i));
		}
		return ret;
	}
	/**
	 * read all elements as List from given node as parent, if not found return list of zero length
	 * @param parentNode
	 * @param nodeName
	 * @return
	 */
	public static List<Element> readChildren(Node parentNode) {
		ArrayList<Element> ret = new ArrayList<Element>();
		NodeList children = parentNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element)
				ret.add((Element) children.item(i));
		}
		return ret;
	}
	/**
	 * save doc to file
	 * @param doc
	 * @param filepath
	 */
	public static void transfer(Document doc, String filepath) {
		try {
			Transformer tf = tff.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(filepath);
			tf.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
	/**
	 * save file to stream
	 * @param doc
	 * @param out
	 */
	public static void transfor(Document doc, OutputStream out) {
		try {
			Transformer tf = tff.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(out);
			tf.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
	/**
	 * print document to string
	 * @param doc
	 * @return
	 * @throws TransformerException
	 */
	public static String toString(Document doc) throws TransformerException{
		Transformer tf = tff.newTransformer();
		StringWriter writer = new StringWriter();
		tf.transform(new DOMSource(doc), new StreamResult(writer));
		String output = writer.getBuffer().toString();
		return output;
	}
	/**
	 * compile one xpath expression
	 * @param expression
	 * @return
	 * @throws XPathExpressionException
	 */
	public static XPathExpression compile(String expression) throws XPathExpressionException {
		return xPath.compile(expression);
	}
}
