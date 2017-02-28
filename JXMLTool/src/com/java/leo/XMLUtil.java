package com.java.leo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLUtil {
	public static <T> T converyToJavaBean(String xmlStr, Class<T> c){
		T t = null;
		try {
			JAXBContext context = JAXBContext.newInstance(c);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			t = (T) unmarshaller.unmarshal(new StringReader(xmlStr));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return t;
	}
	
	public  static <T> T converyToJavaBean(JAXBContext context, String xmlStr, Class<T> c){
		JAXBElement<T> t = null;
		try {
			Unmarshaller unmarshaller =  context.createUnmarshaller();
			t = (JAXBElement<T>) unmarshaller.unmarshal((new StreamSource(new StringReader(xmlStr))),c);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return t.getValue();
	}
	
	public static String convertToXml(Object obj){
		String result = null;
		Marshaller marshaller = null;
		JAXBContext context = null;
		try {
			context = JAXBContext.newInstance(obj.getClass());
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
			
			StringWriter writer = new StringWriter();
			marshaller.marshal(obj, writer);
			result = writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public static String convertToXml(JAXBContext context, Object obj){
		String result = null;
		Marshaller marshaller = null;
		try {
			if (null == context) {
				context = JAXBContext.newInstance(obj.getClass());
			}	
			
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
			
			StringWriter writer = new StringWriter();
			//用于彻底删除多余的 命名空间前缀
			XMLFilterImpl filter = new XMLFilterImpl(){
				private  boolean isRootElement = true;
				@Override			 
				public void startDocument () throws SAXException{
					super.startDocument();
				}
				
				@Override
			    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException {
					if(this.isRootElement) {
						this.isRootElement = false;
						if(!uri.equals("")  ){
							localName = localName + " xmlns=\"" +uri+"\"";
						}
						
						AttributesImpl newAttributesImpl = new AttributesImpl();
						for(int i = 0; i< atts.getLength(); i++){
							if(! atts.getQName(i).contains("xmlns")){
								newAttributesImpl.addAttribute("", atts.getLocalName(i), atts.getLocalName(i), atts.getType(i), atts.getValue(i));
							}
						}
						
						super.startElement("", localName, localName, newAttributesImpl);	
					}
					else{
						AttributesImpl newAttributesImpl = new AttributesImpl();
						for(int i = 0; i < atts.getLength(); i++){
							newAttributesImpl.addAttribute("", atts.getLocalName(i), atts.getLocalName(i), atts.getType(i), atts.getValue(i));
						}
						super.startElement("", localName, localName, newAttributesImpl);	
					}
			    }
				
				@Override
			    public void endElement (String uri, String localName, String qName) throws SAXException {
					super.endElement(uri, localName, localName );
			    }				
				
				@Override
			    public void startPrefixMapping (String prefix, String uri) throws SAXException {
			      //  if(this.rootNamespace!=null) uri = this.rootNamespace;
			       // if(!this.ignoreNamespace) super.startPrefixMapping("", uri);
			    }
			};
			
			//需要dom4j
			filter.setContentHandler(new XMLWriter(writer,new OutputFormat()));
			marshaller.marshal(obj, filter);
			result = writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static boolean validateXmlFileByXsd(String xmlFile, File xsdFile){
		boolean flag = false;
		try {
			String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
	        Schema schema = schemaFactory.newSchema(xsdFile);
	        // unmarshaller.setSchema(schema);
	        Validator validator = schema.newValidator();
	        InputSource inputSource = new InputSource(xmlFile);
	        Source source = new SAXSource(inputSource);
	        validator.validate(source);
	        flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	public static boolean validateXmlByXsd(String xmlStr, File xsdFile){
		boolean flag = false;
		try {
			String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	        SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
	        Schema schema = schemaFactory.newSchema(xsdFile);
	        // unmarshaller.setSchema(schema);
	        Validator validator = schema.newValidator();
	        ByteArrayInputStream input=new ByteArrayInputStream(xmlStr.getBytes("utf-8"));
			InputSource inputSource = new InputSource(input);
	        Source source = new SAXSource(inputSource);
	        validator.validate(source);
	        flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	public static String formatXML(String inputXML) throws Exception{
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(inputXML));
		String requestXML = null;
		XMLWriter writer = null;
		if(document != null){
			try{
				StringWriter stringWriter = new StringWriter();
				OutputFormat format = new OutputFormat(" ", true);
				format.setEncoding("utf-8");//gb2312
				writer = new XMLWriter(stringWriter, format);
				writer.write(document);
				writer.flush();
				requestXML = stringWriter.getBuffer().toString();
			}finally{
				if(writer != null){
					try{
						writer.close();
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		}
		return requestXML;
	}
	
	public static String formatRequestXML(String inputXML){
		String outputXML = inputXML;
		inputXML = inputXML.replaceAll("[\\s&&[^\r\n]]*(?:[\r\n][\\s&&[^\r\n]]*)+", "");
		Pattern p = Pattern.compile("(.*)<!\\[CDATA\\[(.*)\\]\\]>(.*)");
		Matcher m = p.matcher(inputXML);
		String tempXML = "";
		if(m.matches()){
			String inXML = m.group(2);
			try{
				inXML = formatXML(m.group(2));
			}catch(Exception e) {
				e.printStackTrace();
			}
			inXML = inXML.replaceAll("\n\n", "\n");
			tempXML = m.group(1)+"<![CDATA["+inXML+"]]>"+m.group(3);
			try{
				outputXML = formatXML(tempXML);
				outputXML = outputXML.replaceAll("\n\n", "\n");
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		return outputXML;
	}
	
	public static void main(String[] args) {
		Book book=new Book();
		book.setName("哈利波特之火焰杯");
		book.setPageSize(1238);
		System.out.println(book);
		
		String xml=convertToXml(book);
		System.out.println(xml);
		System.out.println(validateXmlByXsd(xml, new File("GBook.xsd")));
		
		Book book2=converyToJavaBean(xml, Book.class);
		System.out.println(book2.toString() +" "+book2.getName());
		
		xml=convertToXml(null, book);
		System.out.println(xml);
		try {
			System.out.println(formatXML(xml));
			System.out.println(formatRequestXML(xml));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(validateXmlFileByXsd("GBook.out.xml", new File("GBook.xsd")));
		System.out.println(validateXmlFileByXsd("2GBook.out.xml", new File("GBook.xsd")));
	}
}
