package it.deliv2.metrics;

public class ClassifierInfo {
	
	int trainingRelease;
	String name;
	String samplingName;
	double trainingPer;
	double trainingPositives;
	double testingPositives;
	double trueP;
	double falseP;
	double trueN;
	double falseN;
	double precision;
	double recall;
	double AUC;
	double kappa;
	
	public ClassifierInfo(int trainingRelease, String name, String samplingName, double trainingPer, double trainingPositives, double testingPositives, double trueP, double falseP, double trueN, double falseN, double precision, double recall, double AUC, double kappa) {
		this.trainingRelease = trainingRelease;
		this.name = name;
		this.samplingName = samplingName;
		this.trainingPer = trainingPer;
		this.trainingPositives = trainingPositives;
		this.testingPositives = testingPositives;
		this.trueP = trueP;
		this.falseP = falseP;
		this.trueN = trueN;
		this.falseN = falseN;
		this.precision = precision;
		this.recall = recall;
		this.AUC = AUC;
		this.kappa = kappa;
	}
	
	
	public String getSamplingName() {
		return samplingName;
	}


	public void setSamplingName(String samplingName) {
		this.samplingName = samplingName;
	}


	public double getTrueP() {
		return trueP;
	}


	public void setTrueP(double trueP) {
		this.trueP = trueP;
	}


	public double getFalseP() {
		return falseP;
	}


	public void setFalseP(double falseP) {
		this.falseP = falseP;
	}


	public double getTrueN() {
		return trueN;
	}


	public void setTrueN(double trueN) {
		this.trueN = trueN;
	}


	public double getFalseN() {
		return falseN;
	}


	public void setFalseN(double falseN) {
		this.falseN = falseN;
	}


	public double getTrainingPositives() {
		return trainingPositives;
	}


	public void setTrainingPositives(double trainingPositives) {
		this.trainingPositives = trainingPositives;
	}


	public double getTestingPositives() {
		return testingPositives;
	}


	public void setTestingPositives(double testingPositives) {
		this.testingPositives = testingPositives;
	}


	public double getTrainingPer() {
		return trainingPer;
	}

	public void setTrainingPer(double trainingPer) {
		this.trainingPer = trainingPer;
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
