import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class NBDriver {

	//set containing all distinct words present in vocabulary
	public Set<String> distinctVocab;
	//dictionary of count of each word in spam folder
	public Map<String, Integer> trainSpamMap;
	//dictionary of count of each word in ham folder
	public Map<String, Integer> trainHamMap;
	
	public Set<String> stopwordList;

	public NBDriver() {
		// TODO Auto-generated constructor stub
		distinctVocab = new HashSet<>();
		trainHamMap = new TreeMap<>();
		trainSpamMap = new TreeMap<>();
		stopwordList = new HashSet<>();
	}

	public static void main(String[] args) {

		String toFilter = "no";
		try {
			toFilter = args[0];			
		} catch(Exception e) {
			
		}
		//location of training and testing set
		File trainSpamDir = new File("train/spam");
		File trainHamDir = new File("train/ham");
		File testSpamDir = new File("test/spam");
		File testHamDir = new File("test/ham");

		File stopwordsDir = new File("stopwords.txt");
		NBDriver driver = new NBDriver();
		// add all words from training directory to the vocabulary
		driver.addToDistinctVocab(trainSpamDir);
		driver.addToDistinctVocab(trainHamDir);

		Scanner scanner = null;
		try {
			scanner = new Scanner(stopwordsDir);
			while (scanner.hasNext()) {
				String stopWord = scanner.next();
				driver.stopwordList.add(stopWord);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		//remove stop words in case required
		if (toFilter.toLowerCase().equals("yes")) {
			System.out.println("Removing stop words...");
			for (String str : driver.stopwordList) {
				str = str.trim().toLowerCase();
				if (driver.distinctVocab.contains(str)) {
					driver.distinctVocab.remove(str);
				}
			}

		}

		driver.addVocabToClass(trainSpamDir, (TreeMap<String, Integer>) driver.trainSpamMap);
		driver.addVocabToClass(trainHamDir, (TreeMap<String, Integer>) driver.trainHamMap);

		NaiveBayes naiveBayes = new NaiveBayes((TreeMap<String, Integer>) driver.trainHamMap,
				(TreeMap<String, Integer>) driver.trainSpamMap, driver.distinctVocab);
		System.out.println("----Using Naive Bayes----");
		naiveBayes.train();

		// Calculate priors
		double priorSpamProbablity = trainSpamDir.listFiles().length
				/ (double) (trainSpamDir.listFiles().length + trainHamDir.listFiles().length);
		double priorHamProbablity = 1 - priorSpamProbablity;
		//Calculate log priors
		double logPriorSpamProb = Math.log(priorSpamProbablity);
		double logPriorHamProb = Math.log(priorHamProbablity);

		double numCorrectSpam = 0;
		int totalSpamFiles = testSpamDir.listFiles().length;
		for (File file : testSpamDir.listFiles()) {
			if (naiveBayes.test(file, logPriorHamProb, logPriorSpamProb, driver.stopwordList, toFilter) == false) {
				numCorrectSpam++;
			}
		}

		double spam_accuracy = numCorrectSpam / (double) totalSpamFiles;
		System.out.println("Spam Accuracy " + spam_accuracy * 100);

		double numCorrectHam = 0;
		int totalHamFiles = testHamDir.listFiles().length;
		for (File file : testHamDir.listFiles()) {
			if (naiveBayes.test(file, logPriorHamProb, logPriorSpamProb, driver.stopwordList, toFilter) == true) {
				numCorrectHam++;
			}
		}
		double ham_accuracy = numCorrectHam / (double) totalHamFiles;
		System.out.println("Ham Accuracy : " + ham_accuracy * 100);
		System.out.println(
				"Overall Accuracy  : " + ((numCorrectHam + numCorrectSpam) / (totalHamFiles + totalSpamFiles)) * 100);
		System.out.println("Naive Bayes Complete.");

	}

	//add word and its count to the dictionary if present in the distinct vocabulary
	private void addVocabToClass(File trainDir, TreeMap<String, Integer> trainMap) {
		// TODO Auto-generated method stub
		for (File file : trainDir.listFiles()) {
			Scanner sc = null;
			try {
				sc = new Scanner(file);
				while (sc.hasNext()) {
					String line = sc.nextLine();
					for (String word : line.toLowerCase().trim().split(" ")) {
						if (!word.isEmpty()) {
							if (distinctVocab.contains(word)) {
								if (trainMap.containsKey(word)) {
									trainMap.put(word, trainMap.get(word) + 1);
								} else {
									trainMap.put(word, 1);
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				sc.close();
			}
		}

	}

	private void addToDistinctVocab(File trainSpamDir) {
		// TODO Auto-generated method stub
		for (File file : trainSpamDir.listFiles()) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					for (String word : line.trim().toLowerCase().split(" ")) {
						// add the word to the vocabulary if not empty
						if (!word.isEmpty()) {
							distinctVocab.add(word);
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				scanner.close();
			}

		}

	}

}
