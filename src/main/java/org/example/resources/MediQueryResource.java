package org.example.resources;

import org.example.IndexBuilder;
import org.example.data.ModelObject;
import org.example.data.SearchResponse;
import org.example.search.LuceaneSearch;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MediQueryResource {
    @GET
    public String sayHello() {
        return "Hello, World!";
    }


    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSearchResult(String jsonData) throws IOException {
        JSONObject jsonObject = new JSONObject(jsonData);
        String searchTerm = jsonObject.getString("searchTerm");
        String lookupType = jsonObject.getString("lookupType");
        LuceaneSearch ls = new LuceaneSearch();
        List<ModelObject> res = ls.query(searchTerm);
        // Logic to fetch search result based on searchTerm and lookupType
        String result = "Searching for searchTerm: " + searchTerm + ", with lookupType: " + lookupType;

        // Create a SearchResultResponse object
        SearchResponse searchResultResponse = new SearchResponse(searchTerm, lookupType, res);

        // Return the SearchResultResponse object in JSON format
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
}
