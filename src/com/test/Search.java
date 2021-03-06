package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

/**
 * Print a list of videos matching a search term.
 *
 * @author Jeremy Walker
 */
public class Search {

	/**
	 * Define a global variable that identifies the name of a file that contains
	 * the developer's API key.
	 */

	private static final long NUMBER_OF_VIDEOS_RETURNED = 10;

	/**
	 * Define a global instance of the HTTP transport.
	 */
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	public static final String apiKey = "AIzaSyB0ybbklH3__l_wJ9_ik1WkKa2ffCVCcg4";

	/**
	 * Define a global instance of a Youtube object, which will be used to make
	 * YouTube Data API requests.
	 */
	private static YouTube youtube;

	/**
	 * Initialize a YouTube object to search for videos on YouTube. Then display
	 * the name and thumbnail image of each video in the result set.
	 *
	 * @param args
	 *            command line args.
	 */
	public static void main(String[] args) {
		// Read the developer key from the properties file.

		try {
			// This object is used to make YouTube Data API requests. The last
			// argument is required, but since we don't need anything
			// initialized when the HttpRequest is initialized, we override
			// the interface and provide a no-op function.
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
				public void initialize(HttpRequest request) throws IOException {
				}
			}).setApplicationName("youtube-cmdline-search-sample").build();

			// Prompt the user to enter a query term.
			String queryTerm = getInputQuery();

			// Define the API request for retrieving search results.
			YouTube.Search.List search = youtube.search().list("id,snippet");

			// Set your developer key from the {{ Google Cloud Console }} for
			// non-authenticated requests. See:
			// {{ https://cloud.google.com/console }}

			search.setKey(apiKey);
			search.setQ(queryTerm);

			// Restrict the search results to only include videos. See:
			// https://developers.google.com/youtube/v3/docs/search/list#type
			// search.setType("video");

			// To increase efficiency, only retrieve the fields that the
			// application uses.
			search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
			search.setMaxResults(new Long(10));
			Calendar today = Calendar.getInstance();
			today.setTime(new Date());
			today.add(Calendar.DAY_OF_YEAR, -1);
			search.setPublishedAfter(new DateTime(today.getTime()));
			search.setOrder("viewCount");
			search.setType("video");

			// Call the API and print results.
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			if (searchResultList != null) {
				prettyPrint(searchResultList.iterator(), queryTerm);
			}
		} catch (GoogleJsonResponseException e) {
			System.err.println(
					"There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
		} catch (IOException e) {
			System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	 * Prompt the user to enter a query term and return the user-specified term.
	 */
	private static String getInputQuery() throws IOException {

		String inputQuery = "";

		System.out.print("Please enter a search term: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		inputQuery = bReader.readLine();

		if (inputQuery.length() < 1) {
			// Use the string "YouTube Developers Live" as a default.
			inputQuery = "YouTube Developers Live";
		}
		return inputQuery;
	}

	/*
	 * Prints out all results in the Iterator. For each result, print the title,
	 * video ID, and thumbnail.
	 *
	 * @param iteratorSearchResults Iterator of SearchResults to print
	 *
	 * @param query Search query (String)
	 */
	private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) throws IOException {

		System.out.println("\n=====================================================================================");
		System.out.println("   Top " + NUMBER_OF_VIDEOS_RETURNED
				+ " most watched videos in past 24 hours for search on \"" + query + "\".");
		System.out.println("=====================================================================================\n");

		if (!iteratorSearchResults.hasNext()){
			System.out.println(" There aren't any results for your query.");
		}
		
		List<ResultObj> results = new ArrayList<ResultObj>();
		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();
			if (rId.getKind().equals("youtube#video")) {
				// Define the API request for retrieving search results.
				YouTube.Videos.List search = youtube.videos().list("id,statistics").setId(rId.getVideoId())
						.setMaxResults(new Long(1)).setKey(apiKey);
				List<Video> videoList = search.execute().getItems();
				BigInteger viewCount = new BigInteger("0");

				if (videoList != null) {
					Video res = videoList.get(0);
					viewCount = res.getStatistics().getViewCount();
					ResultObj resObj = new ResultObj();
					resObj.setViewCount(viewCount);
					resObj.setVideoId(rId.getVideoId());
					resObj.setTitle(singleVideo.getSnippet().getTitle());
					results.add(resObj);
				}
			}
		}

		if (!results.isEmpty() && results.size() != 0) {
			results.sort((ResultObj o1, ResultObj o2) -> o2.getViewCount().compareTo(o1.getViewCount()));
		}

		for (ResultObj res : results) {
			// Confirm that the result represents a video. Otherwise, the
			// item will not contain a video ID.

			System.out.print(" Video Id: " + res.getVideoId());
			System.out.print("\t||\t Title: " + res.getTitle());
			System.out.println("\t||\t View Count: " + res.getViewCount());
		}
	}
}
