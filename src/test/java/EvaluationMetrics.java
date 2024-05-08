import org.example.search.LuceaneSearch;
import org.example.data.ModelObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationMetrics {

    public static double calculatePrecision(Set<String> retrieved, Set<String> relevant) {
        Set<String> intersection = new HashSet<>(retrieved);
        intersection.retainAll(relevant);
        return relevant.isEmpty() ? 0 : intersection.size() / (double) retrieved.size();
    }

    public static double calculateRecall(Set<String> retrieved, Set<String> relevant) {
        Set<String> intersection = new HashSet<>(retrieved);
        intersection.retainAll(relevant);
        return relevant.isEmpty() ? 0 : intersection.size() / (double) relevant.size();
    }

    public static double calculateF1Score(double precision, double recall) {
        return (precision + recall) == 0 ? 0 : 2 * (precision * recall) / (precision + recall);
    }

    public static double calculateAveragePrecision(Set<String> retrieved, Set<String> relevant) {
        int i = 0;
        double sumPrecision = 0;
        int relevantRetrieved = 0;
        for (String doc : retrieved) {
            i++;
            if (relevant.contains(doc)) {
                relevantRetrieved++;
                sumPrecision += relevantRetrieved / (double) i;
            }
        }
        return relevant.isEmpty() ? 0 : sumPrecision / relevant.size();
    }
    public static double calculateMAP(List<TestCase> testCases, LuceaneSearch searcher) throws IOException {
        double sumAP = 0.0;
        for (TestCase tc : testCases) {
            List<ModelObject> searchResults = searcher.query(tc.getQuery(), tc.getStartYear(), tc.getEndYear(), tc.getSelectedJournal(), tc.getSelectedTopics());
            Set<String> retrievedResults = new HashSet<>();
            for (ModelObject mo : searchResults) {
                retrievedResults.add(mo.getId());
            }
            double ap = calculateAveragePrecision(retrievedResults, tc.getExpectedResults());
            sumAP += ap;
        }
        return sumAP / testCases.size();
    }
}
