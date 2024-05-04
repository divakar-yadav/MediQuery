package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.util.BytesRef;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexBuilder {
    private static final String INDEX_DIR = "data";
    private static final String SUGGEST_DIR = "data/suggest";

    public static void buildIndex() throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        Directory suggestDir = FSDirectory.open(Paths.get(SUGGEST_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(suggestDir, new StandardAnalyzer());

        try (IndexWriter indexWriter = new IndexWriter(indexDir, config)) {
            Path jsonDir = Paths.get(INDEX_DIR);

            Files.walk(jsonDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(jsonFile -> {
                        try {
                            indexFile(jsonFile, indexWriter, suggester);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            indexWriter.commit();
            indexWriter.close();
            suggester.commit();
            suggester.close();
        }
    }

    private static void indexFile(Path jsonFile, IndexWriter indexWriter, AnalyzingInfixSuggester suggester) throws IOException {
        String jsonString = new String(Files.readAllBytes(jsonFile));
        JSONObject jsonObj = new JSONObject(jsonString);
        String docId = jsonFile.getFileName().toString().replace(".json", "");
        String content = jsonObj.optString("title", "") + " " + jsonObj.optString("abstract", "") + " " + jsonObj.optString("description", "");
        Document doc = new Document();
        doc.add(new StringField("id", docId, Field.Store.YES));
        String title = jsonObj.optString("title", "");
        String abstractText = jsonObj.optString("abstract", "");
        String description = jsonObj.optString("description", "");

        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("abstract", abstractText, Field.Store.YES));
        doc.add(new TextField("description", description, Field.Store.YES));
        doc.add(new StringField("url", jsonObj.optString("url", ""), Field.Store.YES));
        doc.add(new StringField("doi", jsonObj.optString("doi", ""), Field.Store.YES));

        // Index authors
        JSONArray authors = jsonObj.optJSONArray("authors");
        if (authors != null) {
            String authorList = authors.toList().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            doc.add(new TextField("authors", authorList, Field.Store.YES));
        }

        if (jsonObj.has("citation_count")) {
            int citationCount = jsonObj.getInt("citation_count");
            doc.add(new NumericDocValuesField("citation_count", citationCount));
            doc.add(new StoredField("citation_count", citationCount));
        }

        String yearString = jsonObj.optString("published_year", "").trim();
        if (yearString.isEmpty()) {
            yearString = "Not Available";  // Use a placeholder for empty or missing years
        }
        try {
            int publishedYear = Integer.parseInt(yearString);
            doc.add(new IntPoint("published_year", publishedYear));
            doc.add(new NumericDocValuesField("published_year", publishedYear));
            doc.add(new StoredField("published_year", publishedYear));
        } catch (NumberFormatException e) {
            System.err.println("Published year is not a valid integer for document ID " + docId + ": " + yearString);
        }
        doc.add(new StringField("published_year_text", yearString, Field.Store.YES));  // Always store year as text for consistency

        indexWriter.addDocument(doc);

        // Extract keywords and add to suggester
        addKeywordsToSuggester(content, suggester);
    }

    private static void addKeywordsToSuggester(String text, AnalyzingInfixSuggester suggester) throws IOException {
        Set<String> keywords = extractKeywords(text);
        for (String keyword : keywords) {
            suggester.add(new BytesRef(keyword), null, 1, new BytesRef(keyword.getBytes()));
        }
    }
    private static Set<String> extractKeywords(String text) {
        String[] words = text.split("\\s+");
        return new HashSet<>(List.of(words));
    }
    static class CustomAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            StandardTokenizer src = new StandardTokenizer();
            src.setMaxTokenLength(255);
            TokenStream result = new LowerCaseFilter(src);
            result = new NGramTokenFilter(result, 3); // Generates n-grams from 1 to 20 characters
            return new TokenStreamComponents(src, result);
        }
    }
}
