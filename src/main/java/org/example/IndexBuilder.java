package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

public class IndexBuilder {
    private static final String INDEX_DIR = "data";
    public static void buildIndex() throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(indexDir, config);

        // Path to the directory containing JSON files
        Path jsonDir = Paths.get(INDEX_DIR);

        // Iterate through each JSON file in the directory
        try (Stream<Path> paths = Files.walk(jsonDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(jsonFile -> {
                        try {
                            // Read JSON file
                            byte[] jsonData = Files.readAllBytes(jsonFile);
                            // Convert byte array to string
                            String jsonString = new String(jsonData);
                            JSONObject jsonObj = new JSONObject(jsonString);

                            String nctId = jsonObj.getJSONObject("protocolSection")
                                    .getJSONObject("identificationModule")
                                    .getString("nctId");;
                            String briefTitle = jsonObj.getJSONObject("protocolSection")
                                    .getJSONObject("identificationModule")
                                    .getString("briefTitle");
                            String briefSummary = jsonObj.getJSONObject("protocolSection")
                                    .getJSONObject("descriptionModule")
                                    .getString("briefSummary");
                            String detailedDescription = jsonObj.getJSONObject("protocolSection")
                                    .getJSONObject("descriptionModule").has("detailedDescription") ?
                                    jsonObj.getJSONObject("protocolSection")
                                            .getJSONObject("descriptionModule").getString("detailedDescription") : "";
                            // Create Lucene document and add title and summary fields
                            Document doc = new Document();
                            doc.add(new TextField("nctId", nctId, Field.Store.YES));
                            doc.add(new TextField("briefTitle", briefTitle, Field.Store.YES));
                            doc.add(new TextField("briefSummary", briefSummary, Field.Store.YES));
                            doc.add(new TextField("detailedDescription", detailedDescription, Field.Store.YES));
                            // Add document to the Lucene index
                            indexWriter.addDocument(doc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        indexWriter.commit();
        indexWriter.close();
    }
}
