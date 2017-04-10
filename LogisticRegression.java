import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LogisticRegression {

	public Set<String> distinctVocab;
	public Map<String,HashMap<String, Integer>>spamFilemap;	
	public Map<String,HashMap<String, Integer>>hamFilemap;
	public Map<String, Integer> trainSpamMap;
	public Map<String, Integer> trainHamMap;
	public double learningRate;
	public double lambda;
	public Set<String> hamFiles;
	public Set<String> spamFiles;
	public Set<String> allFiles;
	
	public double w0 = 0.1;
	public static int NUM_ITERATIONS = 20;
	public HashMap<String, Double> weightsMap = new HashMap<String, Double>();
	

	public LogisticRegression(Set<String> distinctVocab, HashMap<String, HashMap<String, Integer>> hamFilemap,
			HashMap<String, HashMap<String, Integer>> spamFilemap, Map<String, Integer> trainHamMap,
			Map<String, Integer> trainSpamMap, double learningRate, double lambda, Set<String> allFiles,
			Set<String> spamFiles, Set<String> hamFiles) {
		// TODO Auto-generated constructor stub
		this.distinctVocab = distinctVocab;
		this.spamFilemap = spamFilemap;
		this.hamFilemap = hamFilemap;
		this.trainSpamMap = trainSpamMap;
		this.trainHamMap = trainHamMap;
		this.learningRate = learningRate;
		this.lambda = lambda;
		this.hamFiles = hamFiles;
		this.spamFiles = spamFiles;
		this.allFiles = allFiles;
		
	}

	public void train() {
		
		//assign weights to the weight map of 0.5 initially
		for (String word : distinctVocab) {
			weightsMap.put(word, 0.5);
		}
		
		for (int i = 0; i < NUM_ITERATIONS; i++) {
			System.out.println("Iteration number : "+(i+1));
			for(String word : distinctVocab) {
				double weightedError = 0;
				for(String fileName : allFiles) {
					int fileClass;
					int countOfWord = getCountOfWord(fileName, word);
					if(spamFiles.contains(fileName)) {
						//file is of spam class
						fileClass = 0;
					}
					else {
						//file is of ham class
						fileClass = 1;
					}
					
					double calculatedClass = classify(fileName);
					weightedError += countOfWord * (fileClass - calculatedClass);
				}
				//calculate new weight of the word
				double newWeightOfWord = weightsMap.get(word) + learningRate * weightedError - (learningRate * lambda * weightsMap.get(word));
				//update weight in the weight map
				weightsMap.put(word, newWeightOfWord);
			}
			
		}
	}

	//classify the file based on prediction and taking the sigmoid of the predicted value
	private double classify(String fileName) {
		// TODO Auto-generated method stub
		double predicted = w0;
		if(hamFiles.contains(fileName)) {
			for(Entry<String, Integer> wordMap : hamFilemap.get(fileName).entrySet()) {
				predicted += weightsMap.get(wordMap.getKey()) * wordMap.getValue();
			}
			return sigmoid(predicted);
		}
		else {
			for(Entry<String, Integer> wordMap : spamFilemap.get(fileName).entrySet()) {
				predicted += weightsMap.get(wordMap.getKey()) * wordMap.getValue();
			}
			return sigmoid(predicted);
		}
	}

	private double sigmoid(double predicted) {
		// TODO Auto-generated method stub
		//upper and lower limits to prevent double overflow
		if(predicted>307){
			return 1.0;
		}
		else if(predicted<-307){

			return 0.0;
		}
		else{
			return (1.0 /(1.0+ Math.exp(-predicted)));
		}
	}

	private int getCountOfWord(String fileName, String word) {
		// TODO Auto-generated method stub
		if(hamFiles.contains(fileName)) {
			for(Entry<String, Integer> wordSet : hamFilemap.get(fileName).entrySet()) {
				if(wordSet.getKey().equals(word)) {
					return wordSet.getValue();
				}
			}
		}
		else if(spamFiles.contains(fileName)) {
			for(Entry<String, Integer> wordSet : spamFilemap.get(fileName).entrySet()) {
				if(wordSet.getKey().equals(word)) {
					return wordSet.getValue();
				}
			}
		}
		return 0;
	}

	public boolean test(HashMap<String, Integer> testMap) {
		// TODO Auto-generated method stub
		double predicted = w0;
		for(Entry<String, Integer> val :testMap.entrySet()){
			if(weightsMap.containsKey(val.getKey())){
				predicted += (weightsMap.get(val.getKey())* val.getValue());
			}
		}
		//if the predicted value is 0 then spam else ham
		if(predicted>=0){
			return true;
		}
		else{
			return false;
		}
	}
	
	
}
