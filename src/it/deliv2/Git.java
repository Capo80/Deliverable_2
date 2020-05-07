package it.deliv2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Git {
	
	//Pattern for date recognition
    private static final Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
	private static final String DATE_ISO_STRICT = "--date=iso-strict";
	private Path repository;
	
	public Git (String repoURL, String directory) {
		this.repository = Paths.get(directory);
		importRepository(repoURL, directory);
	}
	
   private void importRepository(String repoURL, String directory) {
	   
	   String repoName;
	   //Url must be in format https://../.../RepoName.git
	   String[] splitted = repoURL.split("/");
	   String repoGitName = splitted[splitted.length-1];
	   repoName = repoGitName.substring(0, repoGitName.length()-4);

	   
	   //The command will simply fail if the repository is already there
	   try {
			runCommand("git",  "clone", repoURL);
	   } catch (IOException e1) {
			e1.printStackTrace();
	   } 
	   
	   this.repository = Paths.get(directory + "/" + repoName);
   }
	   
	
	private List<String> runCommand(String... command) throws IOException {
		ProcessBuilder pb = new ProcessBuilder()
				.command(command)
				.directory(repository.toFile());
		Process p = pb.start();                       
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s;
		ArrayList<String> toReturn = new ArrayList<>();
		while ((s = stdInput.readLine()) != null) {
				toReturn.add(s);
		}
		return toReturn;
	}
	
	public String getLastCommitDate(String key) throws IOException {
		
		//Recover last commit
		List<String> lastCommit = runCommand("git",  "log","--grep="+key, "HEAD", DATE_ISO_STRICT, "-n", "1");

		
		//Recover date of last commit
		String lastCommitDate = "";
		for (int s = 0; s < lastCommit.size(); s++ ) {
    		Matcher m = date.matcher(lastCommit.get(s));
			if (m.find()) {
				lastCommitDate = m.group(0);
				break;
			}
		}
		
		return lastCommitDate;
		
	}
	
	//Date must be ISO strict
	public List<String> getFilesAddedBeforeDate(String date) throws IOException {
		
		return runCommand("git", "log", "--diff-filter=A", "--no-commit-id", "--name-only", "--until", date);
		
	}
	
	public List<String> getFilesModifiedBeforeDate(String date) throws IOException {
		
		return runCommand("git", "log", "--format='%an'", "--name-only", "--until="+date);
		
	}
	
	public List<String> getFilesByKey(String key) throws IOException {

		return runCommand("git", "log", "-p", "--name-only", "--format=" , "--grep="+key);
	}
	
	// TODO currently broken -- Incorporate in get files modified
	public HashMap<String, String> getFileModififications(String dateIn) throws IOException {
		
		//Recover all modifications
		List<String> output = runCommand("git", "log", "--format=\"%aI\"", "--numstat", "--until="+dateIn);
		
		HashMap<String, String> info = new HashMap<String, String>();
		
		//Interpret output and return it
		for (int s = 0; s < output.size(); s++ ) {
    		Matcher m = date.matcher(output.get(s));
			if (m.find()) {
				String[] splitted = output.get(s+2).split("\t");
				info.put(m.group(0), splitted[0] + " " + splitted[1]);
			}
		}
		
		return info;
		
	}
}
