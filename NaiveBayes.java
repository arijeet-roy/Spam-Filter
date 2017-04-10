import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class NaiveBayes {

	public Set<String> distinctVocab;
	public Map<String, Integer> trainSpamMap;
	public Map<String, Integer> trainHamMap;
	public Map<String, Double> spamLikehood;
	public Map<String, Double> hamLikehood;

	public int spamTotal = 0;
	public int hamTotal = 0;

	public NaiveBayes(TreeMap<String, Integer> trainHamMap, TreeMap<String, Integer> trainSpamMap,
			Set<String> distinctVocab) {
		// TODO Auto-generated constructor stub
		this.trainHamMap = trainHamMap;
		this.trainSpamMap = trainSpamMap;
		this.distinctVocab = distinctVocab;
		spamLikehood = new HashMap<>();
		hamLikehood = new HashMap<>();
	}

	public void train() {
		// TODO Auto-generated method stub
		int spamTotalWords = 0;
		for (Entry<String, Integer> entry : trainSpamMap.entrySet()) {
			spamTotalWords += entry.getValue();
		}
		int hamTotalWords = 0;
		for (Entry<String, Integer> entry : trainHamMap.entrySet()) {
			hamTotalWords += entry.getValue();
		}

		for (String word : distinctVocab) {
			if (trainSpamMap.containsKey(word)) {
				// add laplacian smooting of 1.0
				double spamLogLikehood = Math.log(trainSpamMap.get(word) + 1.0)
						/ (spamTotalWords + distinctVocab.size() + 1.0);
				spamLikehood.put(word, spamLogLikehood);
			}
		}
		for (String word : distinctVocab) {
			if (trainHamMap.containsKey(word)) {
				// add laplacian smooting of 1.0
				double hamLogLikehood = Math.log(trainHamMap.get(word) + 1.0)
						/ (hamTotalWords + distinctVocab.size() + 1.0);
				hamLikehood.put(word, hamLogLikehood);
			}
		}

		spamTotal = spamTotalWords;
		hamTotal = hamTotalWords;

	}

	public boolean test(File file, double logPriorHamProb, double logPriorSpamProb, Set<String> stopwordList,
			String toFilter) {

		double spamProbablity = 0.0;
		double hamProbablity = 0.0;
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (toFilter.equals("yes")) {
					for (String word : line.trim().toLowerCase().split(" ")) {
						if (!stopwordList.contains(word)) {
							if (spamLikehood.containsKey(word)) {
								spamProbablity += spamLikehood.get(word);
							} else {
								spamProbablity += Math.log(1.0 / (spamTotal + distinctVocab.size() + 1.0));

							}
							if (hamLikehood.containsKey(word)) {
								hamProbablity += hamLikehood.get(word);
							} else {
								hamProbablity += Math.log(1.0 / (hamTotal + distinctVocab.size() + 1.0));
							}
						}
					}
				} else {
					for (String word : line.trim().toLowerCase().split(" ")) {
						if (spamLikehood.containsKey(word)) {
							spamProbablity += spamLikehood.get(word);
						} else {
							spamProbablity += Math.log(1.0 / (spamTotal + distinctVocab.size() + 1.0));

						}
						if (hamLikehood.containsKey(word)) {
							hamProbablity += hamLikehood.get(word);
						} else {
							hamProbablity += Math.log(1.0 / (hamTotal + distinctVocab.size() + 1.0));
						}
					}

				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		spamProbablity += logPriorSpamProb;
		hamProbablity += logPriorHamProb;

		if (spamProbablity > hamProbablity) {
			return false; // spam
		}

		else {
			return true;
		}

	}
}
