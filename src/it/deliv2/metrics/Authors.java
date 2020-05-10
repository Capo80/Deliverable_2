package it.deliv2.metrics;

import java.util.ArrayList;
import java.util.List;

public class Authors {
	
	private int total;
	private List<String> authors;
	
	public Authors() {
		total = 0;
		authors = new ArrayList<String>();
	}
	
	public void increaseTotal() {
		total++;
	}
	public int getTotal() {
		return total;
	}
	public  void addAuthor(String newAuthor) {
		authors.add(newAuthor);
		total++;
	}
	
	public void printAuth() {
		for (int i = 0; i < authors.size(); i++)
			System.out.println(authors.get(i));
		
	}
	public boolean isInList(String author) {
		return authors.contains(author);
	}
	

}
