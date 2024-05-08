package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.util.BytesRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;


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
    private static final FacetsConfig facetsConfig = new FacetsConfig();

    public IndexBuilder() {
        initializeFacetsConfig();
    }
    private void initializeFacetsConfig() {
        // Define facet configuration for the "journal" field
        facetsConfig.setHierarchical("journal", false); // Assuming "journal" is not hierarchical
        facetsConfig.setMultiValued("journal", true); // Assuming multiple journals can be associated with a document
        facetsConfig.setRequireDimCount("journal", true); // Whether to require the number of dimensions for the field

// Define facet configuration for the "topics" field
        facetsConfig.setHierarchical("topics", true); // Assuming "topics" is hierarchical
        facetsConfig.setMultiValued("topics", true); // Assuming multiple topics can be associated with a document
        facetsConfig.setRequireDimCount("topics", true); // Whether to require the number of dimensions for the field
    }

    public static void buildIndex() throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        Directory taxoDir = FSDirectory.open(Paths.get(INDEX_DIR, "taxonomy"));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        try (IndexWriter indexWriter = new IndexWriter(indexDir, config);
             TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir)) {
            Path jsonDir = Paths.get(INDEX_DIR);

            Files.walk(jsonDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(jsonFile -> {
                        try {
                            indexFile(jsonFile, indexWriter,taxoWriter);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            indexWriter.commit();
            indexWriter.close();
            taxoWriter.commit();
        }
    }

    private static void indexFile(Path jsonFile, IndexWriter indexWriter, TaxonomyWriter taxoWriter) throws IOException {
        String jsonString = new String(Files.readAllBytes(jsonFile));
        JSONObject jsonObj = new JSONObject(jsonString);
        String docId = jsonFile.getFileName().toString().replace(".json", "");
        String content = jsonObj.optString("title", "") + " " + jsonObj.optString("abstract", "") + " " + jsonObj.optString("description", "");
        Document doc = new Document();
        doc.add(new StringField("id", docId, Field.Store.YES));
        String title = jsonObj.optString("title", "");
        String abstractText = jsonObj.optString("abstract", "");
        String description = jsonObj.optString("description", "");


        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        type.setStored(true);
        type.setTokenized(true);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorPositions(true);

        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("abstract", abstractText, Field.Store.YES));
        doc.add(new TextField("description", description, Field.Store.YES));
        doc.add(new StringField("url", jsonObj.optString("url", ""), Field.Store.YES));
        doc.add(new StringField("doi", jsonObj.optString("doi", ""), Field.Store.YES));

        // Faceting fields
        String journal = jsonObj.optString("journal", "");
        if (!journal.isEmpty()) {
            doc.add(new FacetField("journal", journal));
        }

        JSONArray topics = jsonObj.optJSONArray("topics");
        if (topics != null) {
            for (int i = 0; i < topics.length(); i++) {
                String[] topicTerms = topics.getString(i).split(",\\s*"); // Split the topic string by comma
                for (String topic : topicTerms) {
                    doc.add(new FacetField("topics", topic)); // Add each term as a separate facet
                }
            }
        }


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

        indexWriter.addDocument(facetsConfig.build(taxoWriter, doc));

    }

    public static void buildSuggesterIndex() throws IOException {
        Directory suggestDir = FSDirectory.open(Paths.get(SUGGEST_DIR));
        AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(suggestDir, new StandardAnalyzer());

        try (Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
             IndexReader reader = DirectoryReader.open(indexDir)) {

            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                String content = doc.get("title") + " " + doc.get("abstract") + " " + doc.get("description");
                addKeywordsToSuggester(content, suggester);
            }
        }

        suggester.commit();
        suggester.close();
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
    static {
        // Facets configuration
        facetsConfig.setMultiValued("topics", true);
    }
}
