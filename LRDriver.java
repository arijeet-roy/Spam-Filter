import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class LRDriver {

	// contains a set of distinct words present in the entire vocabulary
	public Set<String> distinctVocab;
	public Set<String> stopWords;

	// hashmap of vocabulary count of words for each class
	public Map<String, Integer> trainSpamMap;
	public Map<String, Integer> trainHamMap;
	// list of files for each class
	public Set<String> hamFiles;
	public Set<String> spamFiles;
	public Set<String> allFiles;

	public Map<String, HashMap<String, Integer>> spamFilemap;
	public Map<String, HashMap<String, Integer>> hamFilemap;

	public LRDriver() {
		// TODO Auto-generated constructor stub
		distinctVocab = new HashSet<>();
		stopWords = new HashSet<>();
		trainSpamMap = new HashMap<>();
		trainHamMap = new HashMap<>();
		hamFiles = new HashSet<>();
		spamFiles = new HashSet<>();
		allFiles = new HashSet<>();
		spamFilemap = new HashMap<>();
		hamFilemap = new HashMap<>();

	}

	public static void main(String[] args) {
		String toFilter = "no";
		double learningRate = 0.01;
		double lambda = 0.01;
		
		try {
			toFilter = args[0];		
			learningRate = Double.parseDouble(args[1]);
			lambda = Double.parseDouble(args[2]);
		} catch(Exception e) {
			
		}

		File trainSpamDir = new File("train/spam");
		File trainHamDir = new File("train/ham");
		File testSpamDir = new File("test/spam");
		File testHamDir = new File("test/ham");

		File stopwordsDir = new File("stopwords.txt");

		LRDriver driver = new LRDriver();
		// add all words from training directory to the vocabulary
		driver.addToDistinctVocab(trainSpamDir);
		driver.addToDistinctVocab(trainHamDir);

		Scanner scanner = null;
		try {
			scanner = new Scanner(stopwordsDir);
			while (scanner.hasNext()) {
				String stopWord = scanner.next();
				driver.stopWords.add(stopWord);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}

		if (toFilter.toLowerCase().equals("yes")) {
			System.out.println("Removing stop words...");
			for (String str : driver.stopWords) {
				if (driver.distinctVocab.contains(str)) {
					driver.distinctVocab.remove(str);
				}
			}
		}

		driver.addVocabToClass(trainSpamDir, (HashMap<String, HashMap<String, Integer>>) driver.spamFilemap,
				(HashMap<String, Integer>) driver.trainSpamMap, (HashSet<String>) driver.spamFiles);
		driver.addVocabToClass(trainHamDir, (HashMap<String, HashMap<String, Integer>>) driver.hamFilemap,
				(HashMap<String, Integer>) driver.trainHamMap, (HashSet<String>) driver.hamFiles);

		LogisticRegression logisticRegression = new LogisticRegression(driver.distinctVocab,
				(HashMap<String, HashMap<String, Integer>>) driver.hamFilemap,
				(HashMap<String, HashMap<String, Integer>>) driver.spamFilemap, driver.trainHamMap, driver.trainSpamMap,
				learningRate, lambda, driver.allFiles, driver.spamFiles, driver.hamFiles);

		// train samples
		System.out.println("----Logistic Regression----");
		System.out.println("Training samples(May take time upto 15 minutes)....");
		logisticRegression.train();

		// Test samples from spam folder
		System.out.println("Testing Spam folder....");
		int totalSpamFileCount = testSpamDir.listFiles().length, numSpamFiles = 0;
		for (File testFile : testSpamDir.listFiles()) {
			HashMap<String, Integer> testMap = new HashMap<String, Integer>();
			Scanner scanner2 = null;
			try {
				scanner2 = new Scanner(testFile);
				while (scanner2.hasNext()) {
					String line = scanner2.nextLine();
					for (String word : line.trim().toLowerCase().split(" ")) {
						if (testMap.containsKey(word)) {
							testMap.put(word, testMap.get(word) + 1);
						} else {
							testMap.put(word, 1);
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				scanner2.close();
			}

			if (toFilter.equals("yes")) {
				for (String stopword : driver.stopWords) {
					if (testMap.containsKey(stopword)) {
						testMap.remove(stopword);
					}
				}
			}

			boolean isHam = logisticRegression.test(testMap);
			if (!isHam) {
				numSpamFiles++;
			}

		}

		double spamAccuracy = ((double) numSpamFiles / (double) totalSpamFileCount) * 100;
		System.out.println("Spam Accuracy : " + (spamAccuracy));

		// Test samples from ham folder
		System.out.println("Testing ham folder....");
		int totalHamFileCount = testHamDir.listFiles().length, numHamFiles = 0;
		for (File testFile : testHamDir.listFiles()) {
			HashMap<String, Integer> testHamMap = new HashMap<String, Integer>();
			Scanner scanner2 = null;
			try {
				scanner2 = new Scanner(testFile);
				while (scanner2.hasNext()) {
					String line = scanner2.nextLine();
					for (String word : line.trim().toLowerCase().split(" ")) {
						if (testHamMap.containsKey(word)) {
							testHamMap.put(word, testHamMap.get(word) + 1);
						} else {
							testHamMap.put(word, 1);
						}
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				scanner2.close();
			}

			if (toFilter.equals("yes")) {
				for (String stopword : driver.stopWords) {
					if (testHamMap.containsKey(stopword)) {
						testHamMap.remove(stopword);
					}
				}
				
			}

			boolean isHam = logisticRegression.test(testHamMap);
			if (isHam) {
				numHamFiles++;
			}

		}

		double hamAccuracy = ((double) numHamFiles / (double) totalHamFileCount) * 100;
		System.out.println("Ham Accuracy : " + (hamAccuracy));
		System.out.println("Overall Accuracy  : "
				+ ((double)(numHamFiles + numSpamFiles) / (double)(totalHamFileCount + totalSpamFileCount)) * 100);

		System.out.println("Logistic Regression Complete.");
	}

	private void addVocabToClass(File trainFile, HashMap<String, HashMap<String, Integer>> fileMap,
			HashMap<String, Integer> wordMap, HashSet<String> fileSet) {

		for (File file : trainFile.listFiles()) {
			Map<String, Integer> fileVocab = new HashMap<String, Integer>();

			fileSet.add(file.getName());
			allFiles.add(file.getName());
			Scanner sc = null;
			try {
				sc = new Scanner(file);
				while (sc.hasNext()) {
					String line = sc.nextLine();
					for (String word : line.trim().toLowerCase().split(" ")) {
						// only add to the vocabulary if exists in the distinct
						// vocabulary set
						if (!word.isEmpty()) {
							if (distinctVocab.contains(word)) {

								if (wordMap.containsKey(word)) {
									wordMap.put(word, wordMap.get(word) + 1);
								} else {
									wordMap.put(word, 1);
								}

								if (fileVocab.containsKey(word)) {
									fileVocab.put(word, fileVocab.get(word) + 1);
								} else {
									fileVocab.put(word, 1);
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
			fileMap.put(file.getName(), (HashMap<String, Integer>) fileVocab);
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
