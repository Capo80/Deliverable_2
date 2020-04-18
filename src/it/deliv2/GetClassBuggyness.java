package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetClassBuggyness {

	private static HashMap<String, Issue> issuesMap;
	
	private static HashMap<String, Integer> recoverVersionIDs(String fileName) throws FileNotFoundException {
		
		HashMap<String, Integer> versionIDs = new HashMap<String, Integer>();
		
		File versionCSV = new File(fileName);
		
		try (Scanner fr = new Scanner(versionCSV)) {
			
			//Throw away first line
			fr.nextLine();
			
	      while (fr.hasNextLine()) {
	          String line = fr.nextLine();
	          String[] splitted = line.split(",");
	          versionIDs.put(splitted[1], Integer.parseInt(splitted[0]));
	        }
		}
		
		return versionIDs;
		
	}
	public static void main(String[] args) throws IOException, JSONException {
		
		HashMap<String, Integer> versionIDs = recoverVersionIDs("BOOKKEEPERVersionInfo.csv");
		String projName = "BOOKKEEPER";
		Integer j = 0;
		Integer i = 0; 
		Integer total = 1;
	
		do {
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20=%22"+projName+"%22AND%22issueType%22=%22Bug%22&fields=versions,fixVersions&startAt="+ i.toString() + "&maxResults=" + j.toString();
			JSONObject json = JsonManager.readJsonFromUrl(url);
			
			System.out.println("request done");
			total = json.getInt("total");
			JSONArray issues = json.getJSONArray("issues");
			
			System.out.println(issues);
			issuesMap = new HashMap<String, Issue>();
	        for (; i < total && i < j; i++) {
	        	String key = issues.getJSONObject(i%1000).get("key").toString();
	        	
	        	JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
	        
	        	JSONArray versions = fields.getJSONArray("versions");
	        	int introVers = 100000000;
	        	for(int h = 0; h < versions.length(); h++) {
	        		int currVers = versionIDs.get(versions.getJSONObject(h).get("id").toString());
	        		if (currVers < introVers)
	        			introVers = currVers;
	        	}
	        	
	        	JSONArray fixVersions = fields.getJSONArray("fixVersions");
	        	int fixVers = -1;
	        	for(int h = 0; h < fixVersions.length(); h++) {
	        		int currVers = versionIDs.get(fixVersions.getJSONObject(h).get("id").toString());
	        		if (currVers > fixVers)
	        			fixVers = currVers;
	        	}
	        	
	        	issuesMap.put(key, new Issue(key, introVers, fixVers));
	        	System.out.println(key + " " + introVers + " " + fixVers);
	        }
		} while (i < total);
	
	}

}
