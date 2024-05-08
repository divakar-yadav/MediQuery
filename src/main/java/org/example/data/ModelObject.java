package org.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ModelObject {
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("abstract")
    private String briefAbstract;

    @JsonProperty("description")
    private String description;

    @JsonProperty("url")
    private String url;

    @JsonProperty("doi")
    private String doi;

    @JsonProperty("authors")
    private List<String> authors;

    @JsonProperty("published_year")
    private String publishedYear;

    @JsonProperty("citation_count")
    private int citationCount;

    @JsonProperty("snippet")  // Adding snippet field
    private String snippet;

    public ModelObject(String id, String title, String briefAbstract, String description, String url, String doi, List<String> authors, String publishedYear, int citationCount, String snippet) {
        this.id = id;
        this.title = title;
        this.briefAbstract = briefAbstract;
        this.description = description;
        this.url = url;
        this.doi = doi;
        this.authors = authors;
        this.publishedYear = publishedYear;
        this.citationCount = citationCount;
        this.snippet = snippet;
    }

    public ModelObject(String id, String title, String abstractText, String description, String url, String doi, List<String> authors, String publishedYear, int citationCount) {
        // No-arg constructor for deserialization
    }

    // Getters and setters for all fields including snippet
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBriefAbstract() { return briefAbstract; }
    public void setBriefAbstract(String briefAbstract) { this.briefAbstract = briefAbstract; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }

    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }

    public String getPublishedYear() { return publishedYear; }
    public void setPublishedYear(String publishedYear) { this.publishedYear = publishedYear; }

    public int getCitationCount() { return citationCount; }
    public void setCitationCount(int citationCount) { this.citationCount = citationCount; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
}
