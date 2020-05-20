package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.deliv2.helpers.Filenames;
import it.deliv2.metrics.ClassifierInfo;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

public class GetClassifierInfo {
	
	private static List<ClassifierInfo> simpleInfo;
	private static List<ClassifierInfo> featureAnalyze;
	private static List<ClassifierInfo> sampledInfo;
	private static List<ClassifierInfo> sampledFeatureInfo;
	private static Logger logger = Logger.getLogger("classifiers");
	private static List<Integer> positives;
	private static List<Integer> versionIndex;
	

	//Function that creates a file if it does not exists
	private static File createFile(String filename) throws IOException {
		File newFile = new File(filename);
		if (!newFile.exists() && !newFile.createNewFile()) {
				//Log error
				logger.log(Level.SEVERE, "Cannot create file for BuggynessInfo");
			}
		return newFile;
	}
	
	private static void buildFinalCSV(String buggyFilename, String metricFilename, String finalFilename) throws IOException {
		File newFile = createFile(finalFilename);
		File metricFile = new File(metricFilename);
		File buggyFile = new File(buggyFilename);
		
		try (FileWriter fw = new FileWriter(newFile);
			 Scanner buggy = new Scanner(buggyFile);
			 Scanner metric = new Scanner(metricFile)) {
			
			//First line
			fw.write("Version, File, size, LOC_touched, LOC_added, MAX_LOC_Added, AVG_LOC_Added, Churn, MAX_Churn, AVG_Churn, ChgSetSize, MAX_ChgSet, AVG_CHGSET, NR, NAuth, Buggy\n");
			metric.nextLine();
			buggy.nextLine();
			
			while (metric.hasNextLine()) {
				String metricString = metric.nextLine();
				
				String buggyString = buggy.nextLine();
				String[] splitted = buggyString.split(",");
				
				fw.write(metricString + "," + splitted[2] + "\n");
			}
		}
	}
	
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
			
		//Create file if it does not exist
		File newCSV = createFile(fileName);
	
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Dataset, Balancing, Feature Selection, Training Release, Classifier, Training% of total, Positive% in training, Positive% in testing, TP, FP, TN, FN, Precision, Recall, AUC, kappa\n");
			
			//Iterate trought simpleinfo
			for (int i = 0;  i < simpleInfo.size(); i++) {
				
				ClassifierInfo curr = simpleInfo.get(i);
				System.out.println(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", None, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa());
				fw.write(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", None, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				 
				
			}
			
			//Iterate trought featureinfo
			for (int i = 0;  i < featureAnalyze.size(); i++) {
				
				ClassifierInfo curr = featureAnalyze.get(i);
				System.out.println(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", BestFirst, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa());
				fw.write(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", BestFirst, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				 
				
			}
			
			
			//Iterate trought samplinginfo
			for (int i = 0;  i < sampledFeatureInfo.size(); i++) {
				
				ClassifierInfo curr = sampledFeatureInfo.get(i);
				System.out.println(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", None, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa());
				fw.write(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", None, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				 
				
			}
			
			
			//Iterate trought samplingFeatureinfo
			for (int i = 0;  i < sampledFeatureInfo.size(); i++) {
				
				ClassifierInfo curr = sampledFeatureInfo.get(i);
				System.out.println(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", BestFirst, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa());
				fw.write(Filenames.PROJ_NAME + ", " + curr.getSamplingName()   + ", BestFirst, " + curr.getTrainingRelease() + ", " + curr.getName() + ", " + curr.getTrainingPer() + ", " + curr.getTrainingPositives() + ", " + curr.getTestingPositives() + ", " +
						curr.getTrueP() + ", " + curr.getFalseP() + ", " + curr.getTrueN() + ", " + curr.getFalseN() + ", " + curr.getPrecision() + ", " + curr.getRecall() + ", "  
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				 
				
			}
		}
		
		
	}
	
	
	private static List<Integer> getVersionIdexes(String filename) throws FileNotFoundException {
		
		List<Integer> to_return = new ArrayList<>();
		positives = new ArrayList<>();
		
		to_return.add(0);
		
		File metricFile = new File(filename);
		
		try (Scanner fr = new Scanner(metricFile)) {
			
			//Throw away first line
			fr.nextLine();
			
			int counter = 0;
			int current = 1;
			int positive_c = 0;
			while (fr.hasNextLine()) {
				String line = fr.nextLine();
				String[] splitted = line.split(",");
				
				if ( current != Integer.parseInt(splitted[0])) {
					current++;
					positives.add(positive_c);
					to_return.add(counter);
				}
				
				if (splitted[15].compareTo(" Yes") == 0) 
					positive_c++;
				
				counter++;
			}

			to_return.add(counter);
		}
		
		return to_return;
	}
	private static void testAndEval(Classifier classifier, Instances training, Instances testing, int trainingRelease, String name, String samplingName, List<ClassifierInfo> to_use) throws Exception {

		//build
		classifier.buildClassifier(training);

		//eval
		Evaluation eval = new Evaluation(testing);	

		eval.evaluateModel(classifier, testing);
		
		//Recover metrics+
		double AUC = eval.areaUnderPRC(1);
		double kappa = eval.kappa();
		double precision = eval.precision(1);
		double recall = eval.recall(1);
		double trueP = eval.numTruePositives(1);
		double falseP = eval.numFalsePositives(1);
		double trueN = eval.numTrueNegatives(1);
		double falseN = eval.numFalseNegatives(1);
		double trainingPer = training.numInstances()*1.0 / (training.numInstances()+testing.numInstances()) *100;
		double trainingPositives = positives.get(trainingRelease-2)*1.0 / training.numInstances() * 100;
		double testingPositives = (positives.get(trainingRelease-1) - positives.get(trainingRelease-2))*1.0 / testing.numInstances() *100;
		
		//Add to structure
		ClassifierInfo to_add = new ClassifierInfo(trainingRelease, name, samplingName, trainingPer, trainingPositives, testingPositives, trueP, falseP, trueN, falseN, precision, recall, AUC, kappa);
		
		to_use.add(to_add);
	}

	
	private static void featureSelectionAnalyze(Instances training, Instances testing, int trainingRelease, boolean sampling) throws Exception {
		
		//create AttributeSelection object
		AttributeSelection filter = new AttributeSelection();
		
		//create evaluator and search algorithm objects
		CfsSubsetEval eval = new CfsSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		
		//set the filter to use the evaluator and search algorithm
		filter.setEvaluator(eval);
		filter.setSearch(search);
		
		//specify the dataset
		filter.setInputFormat(training);
		//apply
		Instances filteredTraining = Filter.useFilter(training, filter);
		
		Instances testingFiltered = Filter.useFilter(testing, filter);
		
		if (sampling)
			sampledAnalyze(filteredTraining, testingFiltered, trainingRelease, sampledFeatureInfo);
		else
			simpleAnalyze(filteredTraining, testingFiltered, trainingRelease, featureAnalyze);	
		
		
	}
	
	private static void sampledAnalyze(Instances training, Instances testing, int trainingRelease, List<ClassifierInfo> to_use) throws Exception {


		//Set prediction attribute
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);
		
    	Resample resample = new Resample();
		resample.setInputFormat(training);
		FilteredClassifier fc = new FilteredClassifier();
		
		// Under sampling
		
		SpreadSubsample  spreadSubsample = new SpreadSubsample();
		String[] opts = new String[]{ "-M", "1.0"};
		spreadSubsample.setOptions(opts);
		fc.setFilter(spreadSubsample);
		
		NaiveBayes naiveB = new NaiveBayes();
		fc.setClassifier(naiveB);
		
		testAndEval(fc, training, testing, trainingRelease, "NaiveBayes", "UnderSampling", to_use);
		

		RandomForest randomF = new RandomForest();
		fc.setClassifier(randomF);
		
		testAndEval(fc, training, testing, trainingRelease, "RandomForest", "UnderSampling", to_use);
		

		IBk ibk = new IBk();
		fc.setClassifier(ibk);
		testAndEval(fc, training, testing, trainingRelease, "IBk", "UnderSampling", to_use);
		
		// OverSampling
		double trainingPositives = positives.get(trainingRelease-2)*1.0 / training.numInstances() * 100;
		if (trainingPositives > 50)
			trainingPositives = 2*trainingPositives;
		else
			trainingPositives = 2*(100-trainingPositives);
		
		DecimalFormat df = new DecimalFormat("#.##");
		opts = new String[]{ "-B", "1", "-Z", df.format(trainingPositives)};
		resample.setOptions(opts);
		fc.setFilter(resample);
		
		naiveB = new NaiveBayes();
		fc.setClassifier(naiveB);
		
		testAndEval(fc, training, testing, trainingRelease, "NaiveBayes", "OverSampling", to_use);
		

		randomF = new RandomForest();
		fc.setClassifier(randomF);
		
		testAndEval(fc, training, testing, trainingRelease, "RandomForest", "OverSampling", to_use);
		

		ibk = new IBk();
		fc.setClassifier(ibk);
		testAndEval(fc, training, testing, trainingRelease, "IBk", "OverSampling", to_use);
		
		//SMOTE
		SMOTE smote = new SMOTE();
		smote.setInputFormat(training);
		fc.setFilter(smote);
		
		naiveB = new NaiveBayes();
		fc.setClassifier(naiveB);
		
		testAndEval(fc, training, testing, trainingRelease, "NaiveBayes", "SMOTE", to_use);
		

		randomF = new RandomForest();
		fc.setClassifier(randomF);
		
		testAndEval(fc, training, testing, trainingRelease, "RandomForest", "SMOTE", to_use);
		

		ibk = new IBk();
		fc.setClassifier(ibk);
		testAndEval(fc, training, testing, trainingRelease, "IBk", "SMOTE", to_use);
	}
	
	private static void simpleAnalyze(Instances training, Instances testing, int trainingRelease, List<ClassifierInfo> to_use) throws Exception {
		
		//Set prediction attribute
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);
		
		// Bayes
		NaiveBayes classifier = new NaiveBayes();
		testAndEval(classifier, training, testing, trainingRelease, "NaiveBayes", "None", to_use);
		
		//Random forest
		RandomForest classifier2 = new RandomForest();
		testAndEval(classifier2, training, testing, trainingRelease, "RandomForest", "None", to_use);
		
		// IBK
		IBk classifier3 = new IBk();
		testAndEval(classifier3, training, testing, trainingRelease, "IBk", "None", to_use);
		
	}
	public static void main(String[] args) throws Exception {

		//Build final file
		buildFinalCSV(Filenames.BUG_FILE, Filenames.METRICS_FILE, Filenames.FINAL_FILE);
		
		//Recover start value of each vesrion
		versionIndex = getVersionIdexes(Filenames.FINAL_FILE);
		
		for (int i = 0; i < positives.size(); i++)
			System.out.println(i+1 + " " + positives.get(i) + " " + versionIndex.get(i));
		
		
		//Load full file
		DataSource source1 = new DataSource(Filenames.FINAL_FILE);
		Instances allInstances = source1.getDataSet();
		
		//Initialize structure to contain all the information
		simpleInfo = new ArrayList<>();
		featureAnalyze = new ArrayList<>();
		sampledInfo = new ArrayList<>();
		sampledFeatureInfo = new ArrayList<>();
		
		//Walk forward
		System.out.println(versionIndex.size() + " " + (versionIndex.size()/2-2));
		for (int i = 1; i < versionIndex.size()/2; i++) {
			
			//System.out.println(versionIndex.get(i));
			
			//Get correct instances for each step
			Instances training = new Instances(allInstances, 0 , versionIndex.get(i));
			Instances testing = new Instances(allInstances, versionIndex.get(i) , versionIndex.get(i+1)-versionIndex.get(i));
			
			//Analyze instances - no feature - no sampling
			simpleAnalyze(training, testing, i+1, simpleInfo);
			
			logger.log(Level.INFO, "Completed with no feature selection with training version {0}", i+1);
			
			// no sampling - feature
			featureSelectionAnalyze(training, testing, i+1, false);
			
			logger.log(Level.INFO, "Completed with best first feature selection with training version {0}", i+1);
			
			//	no feature - sampling
			sampledAnalyze(training, testing, i+1, sampledInfo);
			
			logger.log(Level.INFO, "Completed with sampling with training version {0}", i+1);
			
			// feature -sampling
			featureSelectionAnalyze(training, testing, i+1, true);
			
			logger.log(Level.INFO, "Completed with best first feature selection and sampling with training version {0}", i+1);
			
		}
		
		saveToCSV(Filenames.CLASSIFIER_FILE);
	}

}
