package com.sapient.meetupclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple data collector from the Meetup groups and events API. Please get
 * your Meetup API key before running this program. If you don't have a API key,
 * please login at https://www.meetup.com and get your key at
 * https://secure.meetup.com/meetup_api/key/.
 * 
 * @author abhinavg6
 *
 */
public class MeetupDataCollector {

	private static final Logger logger = LogManager
			.getLogger(MeetupDataCollector.class);

	private static final String API_URL = "https://api.meetup.com/";
	private static final String GROUP_API_PATH = "2/groups.xml";
	private static final String EVENT_API_PATH = "2/events.xml";
	private static final String ENDLINE_CHAR = "\n";

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		logger.info("Data collection starts");

		MeetupJDOMClient meetupClient = new MeetupJDOMClient();
		// Initialize the JDOM client with API key and API URL - Please provide
		// your API key as the first argument while running this class
		meetupClient.initClient("key", args[0], API_URL);

		// Create the topic resource path using the topic argument (second
		// argument passed while running this class)
		String topicAttr = "topic=" + args[1];
		logger.info("Get the group data for topic " + args[1]);
		Map<Integer, String> seedData = meetupClient.getSeedData(
				GROUP_API_PATH, topicAttr);
		logger.info("Got " + seedData.size() + " groups for topic " + args[1]);

		if (MapUtils.isNotEmpty(seedData)) {
			// Create a writer handle to store the collected data
			PrintWriter fileWriter = new PrintWriter("meetupoutput.csv");

			// Write the CSV file header to the output
			String fileHeader = "GROUP_NAME,EVENT_NAME,EVENT_STATUS,EVENT_CITY,EVENT_COUNTRY";
			IOUtils.write(fileHeader, fileWriter);
			IOUtils.write(ENDLINE_CHAR, fileWriter);

			List<String> eventList = new ArrayList<String>();
			for (Integer groupId : seedData.keySet()) {
				// Get the group name for the groupId
				String groupName = seedData.get(groupId);

				// Create the group id resource for API call
				String groupIdResAttr = "group_id=" + Integer.toString(groupId);
				// Add status attribute
				String allResourceAttrs = groupIdResAttr
						+ "&status=past,upcoming";

				// Get the past and upcoming events for this group
				Map<String, List<DataTuple<String, String>>> dataTuples = meetupClient
						.getDataTable(EVENT_API_PATH, allResourceAttrs);
				logger.info("Got " + dataTuples.size() + " events for group "
						+ groupName);

				// Process all events for the group
				for (String eventId : dataTuples.keySet()) {
					String eventStatus = null;
					String eventName = null;
					String eventCity = null;
					String eventCountry = null;
					List<DataTuple<String, String>> eventTuples = dataTuples
							.get(eventId);

					for (DataTuple<String, String> eventTuple : eventTuples) {
						if (eventTuple.getKey().equalsIgnoreCase("status")) {
							eventStatus = eventTuple.getValue();
						} else if (eventTuple.getKey().equalsIgnoreCase("name")) {
							eventName = eventTuple.getValue();
						} else if (eventTuple.getKey().equalsIgnoreCase("city")) {
							eventCity = eventTuple.getValue();
						} else if (eventTuple.getKey().equalsIgnoreCase(
								"country")) {
							eventCountry = eventTuple.getValue();
						}
					}

					// Create the record to be added to output file
					String fileRecord = "\""
							+ StringUtils.trim(StringUtils.replace(groupName,
									"\"", ""))
							+ "\",\""
							+ StringUtils.trim(StringUtils.replace(eventName,
									"\"", ""))
							+ "\",\""
							+ StringUtils.trim(StringUtils.replace(eventStatus,
									"\"", ""))
							+ "\",\""
							+ StringUtils.trim(StringUtils.replace(eventCity,
									"\"", ""))
							+ "\",\""
							+ StringUtils.trim(StringUtils.replace(
									eventCountry, "\"", "")) + "\"";
					eventList.add(fileRecord);
				}
			}

			// Write the event records to output file
			int numEvents = eventList.size();
			int count = 0;
			for (String eventRecord : eventList) {
				IOUtils.write(eventRecord, fileWriter);
				count++;
				if (numEvents != count) {
					IOUtils.write(ENDLINE_CHAR, fileWriter);
				}
			}
		}

		logger.info("Data collection complete");

	}
}
