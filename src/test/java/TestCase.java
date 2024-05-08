import java.util.Set;

public class TestCase {
    private String query;
    private String startYear;
    private String endYear;
    private String selectedJournal;
    private String[] selectedTopics;
    private Set<String> expectedResults;

    // Constructor with all necessary fields
    public TestCase(String query, String startYear, String endYear, String selectedJournal, String[] selectedTopics, Set<String> expectedResults) {
        this.query = query;
        this.startYear = startYear;
        this.endYear = endYear;
        this.selectedJournal = selectedJournal;
        this.selectedTopics = selectedTopics;
        this.expectedResults = expectedResults;
    }

    // Getters for all fields
    public String getQuery() {
        return query;
    }

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public String getSelectedJournal() {
        return selectedJournal;
    }

    public String[] getSelectedTopics() {
        return selectedTopics;
    }

    public Set<String> getExpectedResults() {
        return expectedResults;
    }
}
