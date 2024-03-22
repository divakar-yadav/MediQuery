package org.example.data;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelObject {
    @JsonProperty("nctId")
    private String nctId;

    @JsonProperty("briefTitle")
    private String briefTitle;

    @JsonProperty("briefSummary")
    private String briefSummary;

    @JsonProperty("detailedDescription")
    private String detailedDescription;

    // Constructor
    public ModelObject(String nctId, String briefTitle, String briefSummary, String detailedDescription) {
        this.nctId = nctId;
        this.briefTitle = briefTitle;
        this.briefSummary = briefSummary;
        this.detailedDescription = detailedDescription;
    }

    public ModelObject() {

    }

    // Getters
    public String getNctId() {
        return nctId;
    }

    public String getBriefTitle() {
        return briefTitle;
    }

    public String getBriefSummary() {
        return briefSummary;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }
}

