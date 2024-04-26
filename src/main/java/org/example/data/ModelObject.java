package org.example.data;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelObject {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("abstract_")
    private String abstract_;

    @JsonProperty("url")
    private String url;

    @JsonProperty("authors")
    private String authors;

    @JsonProperty("doi")
    private String doi;
    // Constructor
    public ModelObject(String title, String abstract_,String description, String url, String authors,String doi ) {
        this.title = title;
        this.abstract_ = abstract_;
        this.description = description;
        this.url = url;
        this.authors = authors;
        this.doi = doi;
    }

    public ModelObject() {

    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAbstract() {
        return abstract_;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthors() {
        return authors;
    }

    public String getDoi() {
        return doi;
    }



}

