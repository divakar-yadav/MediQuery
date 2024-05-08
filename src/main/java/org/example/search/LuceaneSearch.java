package org.example.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.DrillDownQuery;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.example.CustomSearcher;  // Ensure this import statement is correct based on your package structure
import org.example.data.ModelObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuceaneSearch {
    private static final String INDEX_DIR = "data";
    private StandardAnalyzer analyzer = new StandardAnalyzer();
    private static final String SUGGEST_DIR = "data/suggest";
    private static final int MAX_NUM_FRAGMENTS = 3;
    private AnalyzingInfixSuggester suggester;
    public LuceaneSearch() throws IOException {
        synchronized (LuceaneSearch.class) {
            if (suggester == null) {
                initializeSuggester();
            }
        }
    }
    private void initializeSuggester() throws IOException {
        Directory suggestDir = FSDirectory.open(Paths.get(SUGGEST_DIR));
        if (suggester != null) {
            suggester.close(); // Properly close the previous suggester if it exists
        }
        suggester = new AnalyzingInfixSuggester(suggestDir, analyzer);
        // Assuming you are calling buildSuggester() to populate it somewhere after initialization
    }
    private void buildSuggester() throws IOException {
        // Example of preparing suggester with data
        List<String> sampleData = List.of("Artificial Intelligence", "Machine Learning", "Deep Learning", "Neural Networks", "Natural Language Processing");
        for (String data : sampleData) {
            suggester.add(new BytesRef(data.getBytes(StandardCharsets.UTF_8)), null, 1, new BytesRef(data.getBytes(StandardCharsets.UTF_8)));
        }
        suggester.refresh();
    }
    public List<String> getSuggestions(String queryText) throws IOException {
        List<String> suggestions = new ArrayList<>();
        if (queryText != null && !queryText.isEmpty()) {
            List<Lookup.LookupResult> results = suggester.lookup(queryText, false, 5);
            for (Lookup.LookupResult result : results) {
                suggestions.add(result.key.toString());
            }
        }
        return suggestions;
    }
    public void close() throws IOException {
        if (suggester != null) {
            suggester.close();
            suggester = null;
        }
    }
    public List<ModelObject> query(String term, String startYear, String endYear, String[] selectedJournals, String[] selectedTopics) throws IOException {
        System.out.println("Querying for term: " + term + " between years " + startYear + " and " + endYear);

        Map<String, Float> boosts = new HashMap<>();
        boosts.put("title", 5.0f);   // Higher boost for title
        boosts.put("abstract", 2.0f);  // Slightly lower boost for abstract
        // boosts.put("description", 1.0f); // Lowest boost for general description
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        Directory taxoDir = FSDirectory.open(Paths.get(INDEX_DIR, "taxonomy"));
        try (IndexReader reader = DirectoryReader.open(indexDir);
             TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir)) {
            CustomSearcher indexSearcher = new CustomSearcher(reader);  // Use CustomSearcher
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
                    new String[]{"title", "abstract", "description"},
                    analyzer,
                    boosts
            );
            Query termQuery = queryParser.parse(QueryParser.escape( term ));

            FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("description", term), 2); // Fuzzy query with max edit distance 2
            PhraseQuery phraseQuery = new PhraseQuery("description", term.split("\\s+")); // Exact phrase query
            SpanNearQuery spanNearQuery = new SpanNearQuery(new SpanQuery[]{
                    new SpanTermQuery(new Term("description", term.split("\\s+")[0])),
                    new SpanTermQuery(new Term("description", term.split("\\s+").length > 1 ? term.split("\\s+")[1] : term.split("\\s+")[0]))
            }, 10, true); // Proximity query with a maximum distance of 10

            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(termQuery, BooleanClause.Occur.MUST);
            builder.add(fuzzyQuery, BooleanClause.Occur.SHOULD); // Fuzzy, Phrase, and Proximity queries are optional enhancements
            builder.add(phraseQuery, BooleanClause.Occur.SHOULD);
            builder.add(spanNearQuery, BooleanClause.Occur.SHOULD);
            if (startYear != null && !startYear.isEmpty() && endYear != null && !endYear.isEmpty()) {
                Query rangeQuery = IntPoint.newRangeQuery("published_year", Integer.parseInt(startYear), Integer.parseInt(endYear));
                builder.add(rangeQuery, BooleanClause.Occur.MUST);
            }
            // Create a DrillDownQuery for faceting
            DrillDownQuery drillDownQuery = new DrillDownQuery(new FacetsConfig(), builder.build());
            if (selectedJournals != null) {
                for (String journal : selectedJournals) {
                    drillDownQuery.add("journal", journal);  // Adding each selected journal filter
                }
            }
            if (selectedTopics != null) {
                for (String topic : selectedTopics) {
                    drillDownQuery.add("topics", topic);  // Similarly, 'topics' must match the indexing field name
                }
            }

            // Collect facets during the search
            FacetsCollector facetsCollector = new FacetsCollector();
            TopDocs topDocs = FacetsCollector.search(indexSearcher, drillDownQuery, 50, facetsCollector);

        /*    if (topDocs.totalHits.value == 0) {
                System.out.println("No results found. Consider removing or altering filters to broaden your search.");
                return new ArrayList<>();
            }*/

            Facets journalFacets = new FastTaxonomyFacetCounts("journal", taxoReader, new FacetsConfig(), facetsCollector);
            // System.out.println("Journal facet counts: " + (journalFacets == null ? "null" : journalFacets.getTopChildren(10, "journal")));

            Facets topicsFacets = new FastTaxonomyFacetCounts("topics", taxoReader, new FacetsConfig(), facetsCollector);
            //System.out.println("Topic facet counts: " + (topicsFacets == null ? "null" : topicsFacets.getTopChildren(10, "topics")));


            UnifiedHighlighter highlighter = new UnifiedHighlighter(indexSearcher, analyzer);
            String[] snippets = highlighter.highlight("description", drillDownQuery, topDocs, MAX_NUM_FRAGMENTS);
            // System.out.println("Number of documents found: " + topDocs.totalHits);

            List<ModelObject> results = new ArrayList<>();
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document doc = indexSearcher.doc(docId);
                Explanation explanation = indexSearcher.explain(drillDownQuery, docId);
                processExplanation(explanation);
                String authorsFieldValue = doc.get("authors");
                List<String> authors = new ArrayList<>();
                if (authorsFieldValue != null) {
                    authors = Arrays.asList(authorsFieldValue.split(", "));
                }
                //float tf = getScoreComponent(explanation, "tf(freq=");
                // float idf = getScoreComponent(explanation, "idf,");
                //float fieldNorm = getScoreComponent(explanation, "fieldNorm=");
                // float customScore = getScoreComponent(explanation, "custom score:"); // Adjust based on actual output
                // float totalScore = (float) explanation.getValue();
                //float tfValue = getScoreComponent(explanation, "tf");
                // float idfValue = getScoreComponent(explanation, "idf");
                // float docFreqValue = getScoreComponent(explanation, "docFreq");
                float totalScore = (float) explanation.getValue();

                String snippet = snippets.length > i ? snippets[i] : null;
                results.add(new ModelObject(
                        doc.get("id"),
                        doc.get("title"),
                        doc.get("abstract"),
                        doc.get("description"),
                        doc.get("url"),
                        doc.get("doi"),
                        authors,
                        doc.get("published_year"),
                        doc.getField("citation_count") != null ? Integer.parseInt(doc.get("citation_count")) : 0,
                        snippet  // Include snippet here
                ));
                //System.out.println("TF Value: " + tfValue);
                // System.out.println("IDF Value: " + idf);
                // System.out.println("DocFreq Value: " + docFreqValue);
                //  System.out.println("Custom Score: " + totalScore);

            }
            return results;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public List<ModelObject> getMostCitedArticles() throws IOException {
        try (Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
             IndexReader reader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            Query allDocs = new MatchAllDocsQuery();
            Sort sort = new Sort(new SortField("citation_count", SortField.Type.LONG, true));
            TopDocs topDocs = indexSearcher.search(allDocs, 5, sort);  // Limit to top 5 most cited

            List<ModelObject> mostCited = new ArrayList<>();

            StandardAnalyzer analyzer = new StandardAnalyzer();
            UnifiedHighlighter highlighter = new UnifiedHighlighter(indexSearcher, analyzer);
            String[] descriptions = highlighter.highlight("description", allDocs, topDocs, MAX_NUM_FRAGMENTS);


            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document doc = indexSearcher.doc(docId);
                String snippet = descriptions.length > i ? descriptions[i] : null;
                mostCited.add(new ModelObject(
                        doc.get("id"),
                        doc.get("title"),
                        doc.get("abstract"),
                        doc.get("description"),
                        doc.get("url"),
                        doc.get("doi"),
                        Arrays.asList(doc.get("authors").split(", ")),
                        doc.get("published_year"),
                        doc.getField("citation_count") != null ? Integer.parseInt(doc.get("citation_count")) : 0,
                        snippet
                ));

            }
            return mostCited;
        }
    }
    private float getScoreComponent(Explanation explanation, String component) {
        // System.out.println("Checking explanation: " + explanation);
        String description = explanation.getDescription();

        // Recursively check child explanations
        for (Explanation detail : explanation.getDetails()) {
            float value = getScoreComponent(detail, component);
            if (value != 0) return value;
        }
        // Extract tf value
        if (component.equals("tf") && description.contains("tf(freq=")) {
            return extractValue(description, "tf\\(freq=([\\d\\.]+)\\)");
        }
        // Extract idf value
        if (component.equals("idf")) {
            return extractValue(description, "idf, computed as log\\(\\(docCount\\+1\\)/\\(docFreq\\+1\\)\\)\\+1from:\\s+" +
                    "                        \"(\\d+) = docFreq, number of documents containing term\\s+" +
                    "                        \"(\\d+) = docCount, total number of documents with field");
        }
        // Extract docFreq value
        if (component.equals("docFreq") && description.contains("docFreq=")) {
            return extractValue(description, "docFreq=([\\d]+)");
        }
        return 0;
    }
    private float extractValue(String description, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group(1));
        }
        return 0;
    }
    public void printScoreComponentsFromExplanation(Explanation explanation) {
        //System.out.println("Explanation: " + explanation);
        // Recursively process all explanations
        if (explanation.getDetails() != null) {
            for (Explanation detail : explanation.getDetails()) {
                printScoreComponentsFromExplanation(detail);
            }
        }

        String description = explanation.getDescription();
        //System.out.println("Current explanation text: " + description); // Directly print the raw explanation text

        // Matching and printing TF
        Pattern patternTf = Pattern.compile("tf\\(freq=([\\d\\.]+)\\)");
        Matcher matcherTf = patternTf.matcher(description);
        if (matcherTf.find()) {
            //  System.out.println("TF: " + matcherTf.group(1));
        } else {
            // System.out.println("No TF values found.");
        }

        // Matching and printing IDF and DocFreq
        Pattern patternIdfDocFreq = Pattern.compile(
                "idf, computed as log\\(\\(docCount\\+1\\)\\/\\(docFreq\\+1\\)\\) \\+ 1 from:\\s+" +
                        "(\\d+) = docFreq, number of documents containing term\\s+" +
                        "(\\d+) = docCount", Pattern.DOTALL);
        Matcher matcherIdfDocFreq = patternIdfDocFreq.matcher(description);
        if (matcherIdfDocFreq.find()) {
            // System.out.println("IDF: " + matcherIdfDocFreq.group(1));
            //  System.out.println("DocFreq: " + matcherIdfDocFreq.group(2));
            // System.out.println("DocCount: " + matcherIdfDocFreq.group(3));
        } else {
            //System.out.println("No IDF or DocFreq values found.");
        }
    }

    public void processExplanation(Explanation explanation) {
        // System.out.println("Starting explanation processing...");
        printScoreComponentsFromExplanation(explanation);
    }

}
