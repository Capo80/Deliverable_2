package it.deliv2.metrics;

public class ChgSet {
	
	private int totalChg;
	private int averageChg;
	private int maxChg;
	private int updates;
	
	public ChgSet() {
		totalChg = 0;
		averageChg = 0;
		maxChg = 0;
		updates = 0;

	}

	public void addTotal(int toAdd) {
		totalChg += toAdd;
		updates++;
	}
	
	public int getUpdates() {
		return updates;
	}
	public int getTotalChg() {
		return totalChg;
	}

	public void setTotalChg(int totalChg) {
		this.totalChg = totalChg;
	}

	public int getAverageChg() {
		return averageChg;
	}

	public void setAverageChg(int averageChg) {
		this.averageChg = averageChg;
	}

	public int getMaxChg() {
		return maxChg;
	}

	public void setMaxChg(int maxChg) {
		this.maxChg = maxChg;
	}

}
