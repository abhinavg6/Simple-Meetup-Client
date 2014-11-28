package com.sapient.meetupclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.DOMBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

/**
 * A simple JDOM based client for Meetup groups and events API
 * 
 * @author abhinavg6
 *
 */
public class MeetupJDOMClient {

	private static final Logger logger = LogManager
			.getLogger(MeetupJDOMClient.class);

	private String apiKey;
	private String apiToken;
	private String uri;

	public void initClient(String apiKey, String apiToken, String uri) {
		this.apiKey = apiKey;
		this.apiToken = apiToken;
		this.uri = uri;
	}

	/**
	 * Get a list of groups for the topic of interest
	 */
	public Map<Integer, String> getSeedData(String resourcePath,
			String resourceAttrs) {
		Map<Integer, String> seedData = new HashMap<Integer, String>();

		// Get all group items from the XML
		List<Element> elements = getElementsFromXML(resourcePath,
				resourceAttrs, "//items/item");
		for (Element element : elements) {
			Integer groupId = null;
			String groupName = null;

			List<Element> children = element.getChildren();
			for (Element childElement : children) {
				if (childElement.getName().equalsIgnoreCase("id")) {
					// Get the group id
					groupId = Integer.parseInt(childElement.getValue());
				}
				if (childElement.getName().equalsIgnoreCase("name")) {
					// Get the group name
					groupName = childElement.getValue();
				}
			}

			// Add each group id and name to be returned as seed data
			if ((null != groupId) || (null != groupName)) {
				seedData.put(groupId, groupName);
			}
		}
		return seedData;
	}

	/**
	 * Get a list of past and upcoming events for a group
	 */
	public Map<String, List<DataTuple<String, String>>> getDataTable(
			String resourcePath, String resourceAttrs) {
		Map<String, List<DataTuple<String, String>>> dataTable = new HashMap<String, List<DataTuple<String, String>>>();

		// Get all event items from the XML
		List<Element> elements = getElementsFromXML(resourcePath,
				resourceAttrs, "//items/item");
		for (Element element : elements) {
			List<DataTuple<String, String>> tuples = new ArrayList<DataTuple<String, String>>();
			String eventId = null;

			List<Element> children = element.getChildren();
			for (Element childElement : children) {
				if (childElement.getName().equalsIgnoreCase("id")) {
					// Get the event id
					eventId = childElement.getValue();
				}
				if (childElement.getName().equalsIgnoreCase("status")) {
					// Add the event status tuple
					tuples.add(new DataTuple<String, String>("status",
							childElement.getValue()));
				}
				if (childElement.getName().equalsIgnoreCase("name")) {
					// Add the event name tuple
					tuples.add(new DataTuple<String, String>("name",
							childElement.getValue()));
				}
				if (childElement.getName().equalsIgnoreCase("venue")) {
					List<Element> grandChildren = childElement.getChildren();
					// Get the event venue
					for (Element grandChild : grandChildren) {
						if (grandChild.getName().equalsIgnoreCase("city")) {
							// Add the venue city tuple
							tuples.add(new DataTuple<String, String>("city",
									grandChild.getValue()));
						}
						if (grandChild.getName().equalsIgnoreCase("country")) {
							// Add the venue country tuple
							tuples.add(new DataTuple<String, String>("country",
									grandChild.getValue()));
						}
					}
				}
			}

			// Add each event id with its list of tuples
			if ((null != eventId) && (tuples.size() != 0)) {
				dataTable.put(eventId, tuples);
			}
		}

		return dataTable;
	}

	/**
	 * Source an XML from web API, and get a list of parent nodes for an xpath
	 * 
	 * @param resourcePath
	 * @param resourceAttrs
	 * @param xpath
	 * @return
	 */
	private List<Element> getElementsFromXML(String resourcePath,
			String resourceAttrs, String xpath) {
		List<Element> elements = new ArrayList<Element>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder domBuilder = factory.newDocumentBuilder();

			String completeUri = createCompleteUri(resourcePath, resourceAttrs);
			// Create the w3c Document from the complete API URI
			org.w3c.dom.Document w3cDocument = domBuilder.parse(completeUri);

			DOMBuilder jdomBuilder = new DOMBuilder();
			// Create the JDOM document from w3c document
			Document jdomDocument = jdomBuilder.build(w3cDocument);

			XPathFactory xFactory = XPathFactory.instance();
			// Create an xpath expression to get data of interest
			XPathExpression<Element> expr = xFactory.compile(xpath,
					Filters.element());

			// Get the elements based on the xpath
			elements = expr.evaluate(jdomDocument);
		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException -- " + e.getMessage());
		} catch (SAXException e) {
			logger.error("SAXException -- " + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException -- " + e.getMessage());
		}

		return elements;
	}

	/**
	 * Create a complete API URL from resource path and attributes
	 * 
	 * @param resourcePath
	 * @param resourceAttrs
	 * @return
	 */
	private String createCompleteUri(String resourcePath, String resourceAttrs) {
		return this.uri + resourcePath + "?" + this.apiKey + "="
				+ this.apiToken + "&" + resourceAttrs;
	}

}
