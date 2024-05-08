package org.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchResponse {
    @JsonProperty("searchTerm")
    private String searchTerm;

    @JsonProperty("lookupType")
    private String lookupType;

    @JsonProperty("result")
    private List<ModelObject> result;

    // Constructor
    public SearchResponse(String searchTerm, String lookupType, List<ModelObject> result) {
        this.searchTerm = searchTerm;
        this.lookupType = lookupType;
        this.result = result;
    }

    // Getters
    public String getSearchTerm() {
        return searchTerm;
    }

    public String getLookupType() {
        return lookupType;
    }

    public List<ModelObject> getResult() {
        return result;
    }
}
