package org.example.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.FSDirectory;
import org.example.data.ModelObject;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuceaneSearch {
    private static final String INDEX_DIR = "data";
    private static final String SUGGEST_DIR = "data/suggest";
    private static final int MAX_NUM_FRAGMENTS = 3;
    private AnalyzingInfixSuggester suggester;

    private StandardAnalyzer analyzer = new StandardAnalyzer();
    public LuceaneSearch() throws IOException {

        Directory suggestDir = FSDirectory.open(Paths.get(SUGGEST_DIR));
        suggester = new AnalyzingInfixSuggester(suggestDir, analyzer);
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

    public List<ModelObject> query(String term, String startYear, String endYear) throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        try (IndexReader reader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            String[] fields = {"title", "abstract", "url", "doi", "authors", "description"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);

            List<ModelObject> results = new ArrayList<>();

            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            if (term != null && !term.isEmpty()) {
                Query textQuery = queryParser.parse(term);
                builder.add(textQuery, BooleanClause.Occur.MUST);
            }

            if (startYear != null && !startYear.isEmpty() && endYear != null && !endYear.isEmpty()) {
                Query rangeQuery = IntPoint.newRangeQuery("published_year", Integer.parseInt(startYear), Integer.parseInt(endYear));
                builder.add(rangeQuery, BooleanClause.Occur.MUST);
            }

            Query baseQuery = builder.build();

            // Boosting by citation count
            DoubleValuesSource citationCountSource = DoubleValuesSource.fromLongField("citation_count");
            FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(baseQuery, citationCountSource);

            TopDocs topDocs = indexSearcher.search(functionScoreQuery, 50);
            UnifiedHighlighter highlighter = new UnifiedHighlighter(indexSearcher, analyzer);
            String[] snippets = highlighter.highlight("description", baseQuery, topDocs, MAX_NUM_FRAGMENTS);

            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document doc = indexSearcher.doc(docId);
                String snippet = snippets.length > i ? snippets[i] : null;
                results.add(new ModelObject(
                        doc.get("id"),
                        doc.get("title"),
                        doc.get("abstract"),
                        doc.get("description"),
                        doc.get("url"),
                        doc.get("doi"),
                        Arrays.asList(doc.get("authors").split(", ")),
                        doc.get("published_year"),
                        doc.getField("citation_count") != null ? Integer.parseInt(doc.get("citation_count")) : 0,
                        snippet));
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
}
