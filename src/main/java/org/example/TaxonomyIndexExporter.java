package org.example;

import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;

public class TaxonomyIndexExporter {
    private static final String TAXONOMY_INDEX_DIR = "data/taxonomy";  // Adjust path as necessary

    public static void exportTaxonomyIndex() throws IOException {
        Path taxonomyDirPath = Paths.get(TAXONOMY_INDEX_DIR);
        Path outputPath = taxonomyDirPath.resolve("taxonomy_output.txt");  // Define where you want the output file

        try (DirectoryTaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(taxonomyDirPath));
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            int size = taxonomyReader.getSize();
            for (int ord = 0; ord < size; ord++) {
                FacetLabel facetLabel = taxonomyReader.getPath(ord);
                if (facetLabel != null) {
                    writer.write(ord + ": " + facetLabel.toString());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to export taxonomy index: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            exportTaxonomyIndex();
        } catch (IOException e) {
            System.err.println("Error writing taxonomy index: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

