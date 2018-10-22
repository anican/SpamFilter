// Do not submit with package statements if you are using eclipse.
// Only use what is provided in the standard libraries.

import java.io.*;

import java.util.*;

public class NaiveBayes {

    /**
     * Mappings of each word that appear in the entire training set to the probabilities
     * that they appear in ham or spam emails respectively.
     */
    private Map<String, Double> hamProbs, spamProbs;
    
    /**
     * Probabilities that any given email is spam or ham respectively.
     */
    private double prSpam, prHam;

    /**
     * Trains the NaiveBayes classifier based on the given training set
     * of ham and spam emails.
     *
     * @param hams collection of email files labeled as 'ham'
     * @param spams collection of email files labeled as 'spam'
     * @throws IOException if &exist; <tt>File</tt> that is not well-formed
     */
    public void train(File[] hams, File[] spams) throws IOException {

        spamProbs = new HashMap<>();
        hamProbs = new HashMap<>();

        // maps words in entire training set to number of times they appear in ham
        // and spam respectively
        calculateCounts(hams, hamProbs);
        calculateCounts(spams, spamProbs);

        // adds words that appear in ham emails but not spam to the spam mappings
        for (String check : hamProbs.keySet()) {
            if (!spamProbs.containsKey(check)) {
                spamProbs.put(check, 0.0);
            }
        }

        // adds words that appear in spam emails but not ham to the ham mappings
        for (String check : spamProbs.keySet()) {
            if (!hamProbs.containsKey(check)) {
                hamProbs.put(check, 0.0);
            }
        }

        // turn mappings of words to counts into mapping of words to probabilities
        calculateProbs(hams, hamProbs);
        calculateProbs(spams, spamProbs);

        // calculates P(H) and P(S)
        prSpam = (double) (spams.length) / (double) (spams.length + hams.length);
        prHam = (double) (hams.length) / (double) (spams.length + hams.length);

    }

    /**
     * Private helper method. Calculates and stores the probabilities of each word
     * in the given map.
     *
     * @param files collection of email files labeled as either ham or spam
     * @param probs mapping of words in the entire training set to the number of times
     * they appear post laplace smoothing.
     * @throws IOException if &exist; <tt>File</tt> that is not well-formed
     */
    private void calculateProbs(File[] files, Map<String, Double> probs) {
        for (String word : probs.keySet()) {
            double wordGivenType = (double)(probs.get(word) + 1) / (double)(files.length + 2);
            probs.put(word, wordGivenType);
        }
    }

    /**
     * Private helper method. Calculates and stores the counts of each word
     * that appears in the entire training set.
     *
     * @param files collection of email files labeled as either ham or spam
     * @param counts empty map
     * @throws IOException if &exist; <tt>File</tt> that is not well-formed
     */
    private void calculateCounts(File[] files, Map<String, Double> counts) throws IOException {
        for (int i = 0; i < files.length; i++) {
            Set<String> tokens = tokenSet(files[i]);
            for (String token : tokens) {
                if (!counts.containsKey(token)) {
                    counts.put(token, 1.0);
                } else {
                    counts.put(token, counts.get(token) + 1.0);
                }
            }
        }
    }

    /**
     * Trains the NaiveBayes classifier based on the given training set
     * of ham and spam emails.
     *
     * @param files collection of unlabeled email files to be classifed
     * @throws IOException if &exist; <tt>File</tt> that is not well-formed
     */
    public void classify(File[] emails) throws IOException {
        for (int i = 0; i < emails.length; i++) {
            Set<String> tokens = tokenSet(emails[i]);
            String result = classifyLog(tokens);
            System.out.println(emails[i].getName() + " " + result);
        }
    }

    /**
     * private helper method. Avoids floating point underflow by taking logarithms
     * of spam and ham probabilities to classify set of emails
     *
     * @param tokens set of tokens appearing in an email
     * @return "spam" if the likelihood of an email being spam is greater than 50%,
     * otherwise "ham"
     */
    private String classifyLog(Set<String> tokens) {
        double logSpam = Math.log(prSpam);
        double logHam = Math.log(prHam);

        for (String token : tokens) {
            // updates log sum if token exists in spam
            if (spamProbs.containsKey(token) && hamProbs.containsKey(token)) {
                logSpam += Math.log(spamProbs.get(token));
                logHam += Math.log(hamProbs.get(token));
            }
        }
        return (logSpam > logHam) ? "spam" : "ham";
    }


    /*
     *  Helper Function:
     *  This function reads in a file and returns a set of all the tokens.
     *  It ignores "Subject:" in the subject line.
     *
     *  If the email had the following content:
     *
     *  Subject: Get rid of your student loans
     *  Hi there ,
     *  If you work for us , we will give you money
     *  to repay your student loans . You will be
     *  debt free !
     *  FakePerson_22393
     *
     *  This function would return to you
     *  ['be', 'student', 'for', 'your', 'rid', 'we', 'of', 'free', 'you',
     *   'us', 'Hi', 'give', '!', 'repay', 'will', 'loans', 'work',
     *   'FakePerson_22393', ',', '.', 'money', 'Get', 'there', 'to', 'If',
     *   'debt', 'You']
     */
    public static HashSet<String> tokenSet(File filename) throws IOException {
        HashSet<String> tokens = new HashSet<String>();
        Scanner filescan = new Scanner(filename);
        filescan.next(); // Ignoring "Subject"
        while(filescan.hasNextLine() && filescan.hasNext()) {
            tokens.add(filescan.next());
        }
        filescan.close();
        return tokens;
    }
}
