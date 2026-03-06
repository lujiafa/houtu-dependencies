package io.github.lujiafa.houtu.util.common;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmlUtils {

	/**
	 * @Title toXmlWithJAXB
	 * @Description 使用JAXB方式生成xml
	 * @param obj
	 * @param charset
	 * @throws JAXBException
	 * @return String
	 */
	public static String toXmlWithJAXB(Object obj, Charset charset) throws JAXBException {
		if (obj == null) {
			throw new IllegalArgumentException("parameter obj must cannot be null.");
		}
		JAXBContext context = JAXBContext.newInstance(obj.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, charset == null?StandardCharsets.UTF_8.name():charset.name());
		StringWriter writer = new StringWriter();
		marshaller.marshal(obj, writer);
		return writer.toString();
	}
	
	/**
	 * @Title toXml
	 * @Description 对象转一级xml字符串
	 * @param obj
	 * @return String
	 */
	public static String toXml(Object obj) {
		return toXml(obj, null);
	}
	
	/**
	 * @Title toXml
	 * @Description 对象转一级xml字符串，
	 * @param obj bean对象
	 * @param charset 字符集
	 * @return String
	 */
	public static String toXml(Object obj, Charset charset) {
		Assert.notNull(obj, "parameter obj cannot be null.");
		Map<String, String> tmap = null;
		if (obj instanceof Map map) {
			tmap = MapUtils.toStringMap(map);
		} else {
			tmap = IntrospectorUtils.toStringMap(obj);
		}
		return toXml(tmap, charset);
	}
	
	/**
	 * @Title toXml
	 * @Description map转一级xml字符串
	 * @param stringMap 集合
	 * @return String
	 */
	public static String toXml(Map<String, String> stringMap) {
		return toXml(stringMap, null);
	}

	/**
	 * @Title toXml
	 * @Description Map<String, String>转一级xml字符串
	 * @param stringMap 集合
	 * @param charset 字符集
	 * @return String
	 */
	public static String toXml(Map<String, String> stringMap, Charset charset) {
		//charset = StringUtils.isEmpty(charset) ? StandardCharsets.UTF_8.name() : charset;
		Document document = new Document();
		Element rootElement = new Element("xml");
		if (stringMap != null) {
			Iterator<String> iterator = stringMap.keySet().iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				if (name == null) {
					continue;
				}
				String value = stringMap.get(name);
				Element element = new Element(name);
				if (value != null) {
					element.setText(value);
				}
				rootElement.addContent(element);
			}
		}
		document.setRootElement(rootElement);
		Format format = Format.getPrettyFormat();
		format.setEncoding(charset==null?StandardCharsets.UTF_8.name() : charset.name());
		XMLOutputter docWriter = new XMLOutputter(format);
		return docWriter.outputString(document);
	}
	
	/**
	 * @Title:parseToMap
	 * @Description 解析一级xml格式数据到map集合
	 * @param xmlBytes 一级xml格式数据
	 * @return Map<String,String>
	 */
	public static Map<String, String> parseToMap(byte[] xmlBytes) {
		return parseToMap(new String(xmlBytes, StandardCharsets.UTF_8));
	}

	/**
	 * @Title:parseToMap
	 * @Description 解析一级xml格式数据到map集合
	 * @param xml 一级xml格式字符串
	 * @return Map<String,String>
	 */
	public static Map<String, String> parseToMap(String xml) {
		try {
			Map<String, String> map = new LinkedHashMap<String, String>();
			if (!StringUtils.hasLength(xml)) {
				return map;
			}
			SAXBuilder builder = new SAXBuilder();
			builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
			builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
			builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			Document document = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	        Element rootElement = document.getRootElement();
	        List<Element> elements = rootElement.getChildren();
	        for (Element element : elements) {
	        	map.put(element.getName(), element.getText());
			}
	        return map;
		} catch (JDOMException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}