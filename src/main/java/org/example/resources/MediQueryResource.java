package org.example.resources;

import org.example.IndexBuilder;
import org.example.data.ModelObject;
import org.example.data.SearchResponse;
import org.example.search.LuceaneSearch;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MediQueryResource {
    @GET
    public String sayHello() {
        return "Hello, World!";
    }
    @GET
    @Path("/suggest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuggestions(@QueryParam("query") String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Query parameter 'query' is required").build();
            }
            LuceaneSearch luceaneSearch = new LuceaneSearch();
            List<String> suggestions = luceaneSearch.getSuggestions(query);
            return Response.ok(suggestions).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request").build();
        }
    }
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSearchResult(String jsonData) throws IOException {
        JSONObject jsonObject = new JSONObject(jsonData);
        String searchTerm = jsonObject.getString("searchTerm");
        String lookupType = jsonObject.getString("lookupType");
        String startYear = jsonObject.optString("startYear"); // Use optString to handle missing values
        String endYear = jsonObject.optString("endYear");   // Use optString to handle missing values

        // Additional facet parameters
        JSONArray selectedJournalsJsonArray = jsonObject.optJSONArray("selectedJournals");
        String[] selectedJournals = selectedJournalsJsonArray != null ? selectedJournalsJsonArray.toList().toArray(new String[0]) : new String[0];

        JSONArray topicsJsonArray = jsonObject.optJSONArray("selectedTopics");
        String[] selectedTopics = topicsJsonArray != null ? topicsJsonArray.toList().toArray(new String[0]) : new String[0];

        LuceaneSearch ls = new LuceaneSearch();
        // Update the query method call to include journal and topics facets
        List<ModelObject> res = ls.query(searchTerm, startYear, endYear, selectedJournals, selectedTopics);
        SearchResponse searchResultResponse = new SearchResponse(searchTerm, lookupType, res);
        return Response.ok(searchResultResponse).build();
    }


    @POST
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createUser(String userData) {
        // Logic to create a new user
        return "User created successfully";
    }
    @PUT
    @Path("/update-index")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateIndex( ) throws IOException {
        // Logic to create a new user
        IndexBuilder.buildIndex();
        return "Index created successfully";
    }
    @PUT
    @Path("/update-index1")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateIndex1( ) throws IOException {
        // Logic to create a new user
        IndexBuilder.buildSuggesterIndex();
        return "Index created successfully";
    }
    @GET
    @Path("/most-cited")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMostCitedArticles() throws IOException {
        LuceaneSearch ls = new LuceaneSearch();
        List<ModelObject> mostCited = ls.getMostCitedArticles();
        return Response.ok(mostCited).build();
    }
}