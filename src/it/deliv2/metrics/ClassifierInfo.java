package it.deliv2.metrics;

public class ClassifierInfo {
	
	int trainingRelease;
	String name;
	double precision;
	double recall;
	double AUC;
	double kappa;
	
	public ClassifierInfo(int trainingRelease, String name, double precision, double recall, double AUC, double kappa) {
		this.trainingRelease = trainingRelease;
		this.name = name;
		this.precision = precision;
		this.recall = recall;
		this.AUC = AUC;
		this.kappa = kappa;
	}
	
	public int getTrainingRelease() {
		return trainingRelease;
	}
	public void setTrainingRelease(int trainingRelease) {
		this.trainingRelease = trainingRelease;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	public double getAUC() {
		return AUC;
	}
	public void setAUC(double aUC) {
		AUC = aUC;
	}
	public double getKappa() {
		return kappa;
	}
	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
	
	

}
