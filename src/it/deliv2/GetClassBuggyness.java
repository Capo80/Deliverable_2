package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetClassBuggyness {

	private static HashMap<String, Issue> issuesMap;
	private static List<String> versionDates;
	private static List<Integer> versionIDs;
	
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
	
	private static void initVersionDates(String fileName) throws FileNotFoundException {
		
		File versionCSV = new File(fileName);
		
		try (Scanner fr = new Scanner(versionCSV)) {
			
			versionDates = new ArrayList<>();
			versionIDs = new ArrayList<>();
			//Throw away first line
			fr.nextLine();
			
			while (fr.hasNextLine()) {
	          String line = fr.nextLine();
	          String[] splitted = line.split(",");
	          versionIDs.add(Integer.parseInt(splitted[0]));
	          versionDates.add(splitted[3]);
	        }
		}
		
		
		
	}
	public static void main(String[] args) throws IOException, JSONException {
		
		//Read info from version file
		HashMap<String, Integer> versionIDMap = recoverVersionIDs("BOOKKEEPERVersionInfo.csv");
		initVersionDates("BOOKKEEPERVersionInfo.csv");
		
		String projName = "BOOKKEEPER";
		Integer j = 0;
		Integer i = 0; 
		Integer total = 1;
		float[] averages = new float[versionIDs.size()];
		int[] totals = new int[versionIDs.size()];
		Arrays.fill(averages, 0);
		Arrays.fill(totals, 0);
		
		do {
			j = i + 1000;
			//Send query to jira for issues (1000 at the time)
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20=%22"+projName+"%22AND%22issueType%22=%22Bug%22&fields=versions,fixVersions,created&startAt="+ i.toString() + "&maxResults=" + j.toString();
			JSONObject json = JsonManager.readJsonFromUrl(url);
			
			total = json.getInt("total");
			JSONArray issues = json.getJSONArray("issues");			
			//Iterate trought the issues
			issuesMap = new HashMap<String, Issue>();
	        for (; i < total && i < j; i++) {
	        	String key = issues.getJSONObject(i%1000).get("key").toString();
	        	
	        	JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
	        
	        	//Recover affected versions field
	        	JSONArray versions = fields.getJSONArray("versions");
	        	
	        	//Find earliest affected version
	        	int introVers = Issue.INTRO_DEF;
	        	for(int h = 0; h < versions.length(); h++) {
	        		int currVers = versionIDMap.get(versions.getJSONObject(h).get("id").toString());
	        		if (currVers < introVers)
	        			introVers = currVers;
	        	}
	        	
	        	//Recover opning date of the issue
	        	String date = fields.getString("created").split("T")[0];
	        	
	        	//Find opening version
	        	int m = 0;
	        	while (date.compareTo(versionDates.get(m)) > 0) {
	        		if (m >= versionDates.size())
	        			break;
	        		m++;
	        	}
	        	int openVers = versionIDs.get(m);
	        	
	        	//Recover fixed version
	        	JSONArray fixVersions = fields.getJSONArray("fixVersions");
	        	
	        	//Find lastest fixed version
	        	int fixVers = Issue.FIX_DEF;
	        	for(int h = 0; h < fixVersions.length(); h++) {
	        		int currVers = versionIDMap.get(fixVersions.getJSONObject(h).get("id").toString());
	        		if (currVers > fixVers)
	        			fixVers = currVers;
	        	}
	        	
	        	if (fixVers != Issue.FIX_DEF)
	        		issuesMap.put(key, new Issue(key, introVers, openVers ,  fixVers));
	        	
	        	//Calculate sums for average
	        	if (introVers != Issue.INTRO_DEF && fixVers != Issue.FIX_DEF) {
	        		for(int h = fixVers+1; h < versionIDs.size(); h++) {
	        			totals[h]++;
	        			if (fixVers != openVers) {
	        				averages[h] += (fixVers - introVers)/(fixVers - openVers)*1.0;
	        				//System.out.println((fixVers - introVers)/(fixVers - openVers)*1.0);
	        			} else
	        				averages[h] += (fixVers - introVers);
	        		}
	        	}
	        	//System.out.println(key + " " + introVers + " " + openVers + " " + fixVers);
	        }
		} while (i < total);
		
		//Calculate P averages for each version
		for (int h = 0; h < versionIDs.size(); h++) {
			//System.out.println(averages[h] + " " + totals[h]);
			if (totals[h] != 0)	
				averages[h] = averages[h]/totals[h];
			else
				averages[h] = 0;
			//System.out.println(averages[h] + " " + totals[h]);
		}

		//Calculate missing IV with proportion
		for (Entry<String, Issue> entry : issuesMap.entrySet()) {
		    String key = entry.getKey();
		    Issue value = entry.getValue();
		    if (value.getIntroVersion() == Issue.INTRO_DEF && value.getFixVersion() != Issue.FIX_DEF) {
		    	value.setIntroVersion(Math.round(value.getFixVersion() - (value.getFixVersion() - value.getOpenVersion())*averages[value.getFixVersion()-1]));
		    	issuesMap.put(key, value);
		    }
		}
		
//		for (Entry<String, Issue> entry : issuesMap.entrySet()) {
//		    String key = entry.getKey();
//		    Issue value = entry.getValue();
//		    System.out.println(key + " " + value.getIntroVersion() + " " + value.getOpenVersion() + " " + value.getFixVersion());
//		}
	
	}

}
