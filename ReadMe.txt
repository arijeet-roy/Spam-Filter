Implementation of Spam filter using Naive Bayes and Logistic Regression with and without filtering stopwords. Accuracy varies with learning rate and the number of iterations it takes to converge.

Steps to run the program:

“test” and “train” folders contain the test and train data respectively and should be in the same folder as the java files.
“stopwords.txt” file contain the stopwords and should be in the same folder as the java files.

For executing Naive Bayes:
“javac NBDriver.java”
“java NBDriver no”  -  Takes 1 parameter "yes" or "no" to remove/not remove the stopwords(default : no)
	

For executing Logistic Regression:
“javac LRDriver.java”
“java LRDriver no 0.01 0.01”
	Takes 3 parameters:
	1."yes" or "no" to remove/not remove the stopwords (default : no) 
	2. learning rate(usually around 0.5 - 0.005) (default : 0.01)
	3. lambda regularization parameter(usually around 0.5 - 0.005) (default : 0.01)
 