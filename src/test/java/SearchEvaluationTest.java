import org.example.data.ModelObject;
import org.example.search.LuceaneSearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchEvaluationTest {
    public static void main(String[] args) throws IOException {
        LuceaneSearch searcher = new LuceaneSearch();
        List<TestCase> testCases = Arrays.asList(
                new TestCase("brain cancer", "2015", "2020", "", new String[] {}, new HashSet<>(Arrays.asList("7589016", "6356431", "5897444", "3895389", "4313844", "4482228"))),
                new TestCase("breast cancer", "2021", "2023", "", new String[] {}, new HashSet<>(Arrays.asList("4335262", "5767023", "6284786", "7297796", "8080072", "9026731","10444861"))),
                new TestCase("lung cancer", "2015", "2023", "", new String[] {}, new HashSet<>(Arrays.asList("4957793", "5972630", "6449330", "7082730", "8005792", "9052526","10131892")))
        );


        for (TestCase tc : testCases) {
            // Using all required parameters for the query method
            List<ModelObject> results = searcher.query(tc.getQuery(), tc.getStartYear(), tc.getEndYear(), tc.getSelectedJournal(), tc.getSelectedTopics());
            Set<String> retrieved = new HashSet<>();
            for (ModelObject mo : results) {
                retrieved.add(mo.getId()); // Assuming ModelObject has a method getId() that returns a String
            }
            Set<String> relevant = tc.getExpectedResults();

            double precision = EvaluationMetrics.calculatePrecision(retrieved, relevant);
            double recall = EvaluationMetrics.calculateRecall(retrieved, relevant);
            double f1Score = EvaluationMetrics.calculateF1Score(precision, recall);

            System.out.printf("Query: %s\nPrecision: %.2f\nRecall: %.2f\nF1 Score: %.2f\n\n", tc.getQuery(), precision, recall, f1Score);
        }

        double map = EvaluationMetrics.calculateMAP(testCases, searcher);
        System.out.println("Mean Average Precision (MAP): " + map);
    }
}
