package org.example.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.example.data.ModelObject;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LuceaneSearch {
    private static final String INDEX_DIR = "data";

    public   String generateDynamicSummary(String briefSummary, String term){
        StringBuilder filteredSummary = new StringBuilder();

        // Split the briefSummary into lines
        String[] lines = briefSummary.split("\\n");

        // Iterate over each line
        for (String line : lines) {
            // Check if the line contains the search term (case insensitive)
            if (line.toLowerCase().contains(term.toLowerCase())) {
                // Append the line to the filtered summary
                filteredSummary.append(line).append("\n");
            }
        }

        // Return the filtered summary
        return filteredSummary.toString().trim(); // Trim any trailing newline characters
    }

    public  String trimFirstNWords(String paragraph, int n) {
        String[] words = paragraph.split("\\s+");
        StringBuilder trimmed = new StringBuilder();

        // Append first n words
        for (int i = 0; i < Math.min(n, words.length); i++) {
            trimmed.append(words[i]).append(" ");
        }

        return trimmed.toString().trim();
    }
    public  List<ModelObject> query(String term) throws IOException {

        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexDir));
        String[] fields = {"nctId", "briefTitle", "briefSummary", "detailedDescription"};
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        List<ModelObject> res = new ArrayList<>();
            if (term!=null) {
                try {
                    Query query = multiFieldQueryParser.parse(term);
                    TopDocs topDocs = indexSearcher.search(query, 10);
                    System.out.println("Total hits: " + topDocs.totalHits);

                    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        Document doc = indexSearcher.doc(scoreDoc.doc);
                        ModelObject obj = new ModelObject(doc.get("nctId"), doc.get("briefTitle"), doc.get("briefSummary"), doc.get("detailedDescription"));
                        res.add(obj);
                    }
                } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                    System.out.println("Invalid query format. Please try again.");
                    System.out.println("msg:" + e.getMessage());
                }
            }
        return res;
    }
}