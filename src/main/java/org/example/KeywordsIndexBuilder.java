package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class KeywordsIndexBuilder {
    private static final String INDEX_DIR = "data";

    public static void buildIndex() throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(indexDir, config);

        // Path to the directory containing the text file
        Path textFilePath = Paths.get("keywords.txt");

        try {
            // Read all lines from the text file
            List<String> lines = Files.readAllLines(textFilePath);

            // Iterate through each line
            for (String line : lines) {
                // Split the line by comma to get individual keywords
                String[] keywords = line.split(",");

                // Create a Lucene document for each keyword
                for (String keyword : keywords) {
                    Document doc = new Document();
                    doc.add(new TextField("keyword", keyword.trim(), Field.Store.YES));
                    // Add document to the Lucene index
                    indexWriter.addDocument(doc);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        indexWriter.commit();
        indexWriter.close();
    }
}
