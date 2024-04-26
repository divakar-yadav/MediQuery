package org.example.resources;

import org.example.IndexBuilder;
import org.example.data.ModelObject;
import org.example.data.SearchResponse;
import org.example.search.LuceaneSearch;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML}) // Added TEXT_HTML as well
public class MediQueryResource {



    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSearchResult(String jsonData) throws IOException {
        JSONObject jsonObject = new JSONObject(jsonData);
        String searchTerm = jsonObject.getString("searchTerm");
        String lookupType = jsonObject.getString("lookupType");
        LuceaneSearch ls = new LuceaneSearch();
        List<ModelObject> res = ls.query(searchTerm);
        SearchResponse searchResultResponse = new SearchResponse(searchTerm, lookupType, res);
        return Response.ok(searchResultResponse).build();
    }


    @PUT
    @Path("/update-index")
    @Consumes(MediaType.APPLICATION_JSON)
    public String updateIndex() throws IOException {
        // Logic to create a new user
        IndexBuilder.buildIndex();
        return "Index created successfully";
    }
}
