package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.deliv2.helpers.Filenames;
import it.deliv2.metrics.ClassifierInfo;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class GetClassifierInfo {
	
	private static List<ClassifierInfo> info;
	private static Logger logger = Logger.getLogger("classifiers");
	
	//Function that creates a file if it does not exists
	private static File createFile(String filename) throws IOException {
		File newFile = new File(filename);
		if (!newFile.exists() && !newFile.createNewFile()) {
				//Log error
				logger.log(Level.SEVERE, "Cannot create file for BuggynessInfo");
			}
		return newFile;
	}
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
			
		//Create file if it does not exist
		File newCSV = createFile(fileName);
	
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Training Release, Classifier, Precision, Recall, AUC, kappa\n");
			
			//Iterate trought info
			for (int i = 0;  i < info.size(); i++) {
				
				ClassifierInfo curr = info.get(i);
				System.out.println(curr.getTrainingRelease() + ", " + curr.getName() + ", "+  curr.getPrecision() + ", " + curr.getRecall() + ", " 
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				fw.write(curr.getTrainingRelease() + ", " + curr.getName() + ", "+  curr.getPrecision() + ", " + curr.getRecall() + ", " 
						+ curr.getAUC() + ", " + curr.getKappa() + "\n");
				
				
			}
		}
		
		
	}
	
	
	private static List<Integer> getVersionIdexes(String filename) throws FileNotFoundException {
		
		List<Integer> to_return = new ArrayList<>();
		
		to_return.add(0);
		
		File metricFile = new File(filename);
		
		try (Scanner fr = new Scanner(metricFile)) {
			
			//Throw away first line
			fr.nextLine();
			
			int counter = 0;
			int current = 1;
			while (fr.hasNextLine()) {
				String line = fr.nextLine();
				String[] splitted = line.split(",");
				if ( current != Integer.parseInt(splitted[0])) {
					current++;
					to_return.add(counter);
				}
				counter++;
			}

			to_return.add(counter);
		}
		
		return to_return;
	}
	private static void testAndEval(Classifier classifier,Instances training, Instances testing, int trainingRelease, String name) throws Exception {

		//build
		classifier.buildClassifier(training);

		//eval
		Evaluation eval = new Evaluation(testing);	

		eval.evaluateModel(classifier, testing);
		
		//Recover metrics
		double AUC = eval.areaUnderPRC(1);
		double kappa = eval.kappa();
		double precision = eval.precision(1);
		double recall = eval.recall(1);
		
		//Add to structure
		ClassifierInfo to_add = new ClassifierInfo(trainingRelease, name, precision, recall, AUC, kappa);
		
		info.add(to_add);
	}
	
	private static void simpleAnalyze(Instances training, Instances testing, int trainingRelease) throws Exception {
		
		//Set prediction attribute
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);
		
		// Bayes
		NaiveBayes classifier = new NaiveBayes();
		testAndEval(classifier, training, testing, trainingRelease, "NaiveBayes");
		
		//Random forest
		RandomForest classifier2 = new RandomForest();
		testAndEval(classifier2, training, testing, trainingRelease, "RandomForest");
		
		// IBK
		IBk classifier3 = new IBk();
		testAndEval(classifier3, training, testing, trainingRelease, "IBk");
		
	}
	public static void main(String[] args) throws Exception {

		//Recover start value of each vesrion
		System.out.println(Filenames.FINAL_FILE);
		List<Integer> versionIndex = getVersionIdexes(Filenames.FINAL_FILE);
		
		//Load full file
		DataSource source1 = new DataSource(Filenames.FINAL_FILE);
		Instances allInstances = source1.getDataSet();
		
		//Initialize structure to contain all the information
		info = new ArrayList<>();
		
		//Walk forward
		for (int i = 1; i < versionIndex.size()-2; i++) {
			
			//System.out.println(versionIndex.get(i));
			
			//Get correct instances for each step
			Instances training = new Instances(allInstances, 0 , versionIndex.get(i));
			Instances testing = new Instances(allInstances, versionIndex.get(i) , versionIndex.get(i+1)-versionIndex.get(i));
			
			//Analyze instances
			simpleAnalyze(training, testing, i+1);
			
		}
		
		saveToCSV(Filenames.CLASSIFIER_FILE);
	}

}
