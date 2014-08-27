package net.dean.jraw.http;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditObjectParser;
import net.dean.jraw.models.RedditObject;
import net.dean.jraw.models.core.Comment;
import net.dean.jraw.models.core.Listing;
import net.dean.jraw.models.core.Submission;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * This class is used to show the result of a request to a RESTful web service, such as Reddit's JSON API.
 */
public class RestResponse {
    /** The ObjectMapper used to read a JSON tree into a JsonNode */
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RedditObjectParser redditObjectParser;

    /** A list of all thea headers received from the server */
    private List<Header> headers;

    /**
     * The root node of the JSON
     */
    private JsonNode rootNode;

    /** The raw data of the response's content */
    private String raw;

    private ApiException[] apiExceptions;

    /**
     * Instantiates a new RestResponse. This constructor also reads the contents of the input stream and parses it into
     * the root JsonNode, and then consumes the response's entity.
     *
     * @param response The HttpResponse used to get the information
     */
    public RestResponse(HttpResponse response) {
        this.headers = new ArrayList<>(Arrays.asList(response.getAllHeaders()));
        this.redditObjectParser = new RedditObjectParser();

        try {
            // http://stackoverflow.com/a/5445161
            Scanner s = new Scanner(response.getEntity().getContent()).useDelimiter("\\A");
            this.raw = s.hasNext() ? s.next() : "";

            try {
                this.rootNode = OBJECT_MAPPER.readTree(raw);
            } catch (JsonParseException e) {
                System.err.println("Unable to parse JSON: " + raw.replace("\n", "").replace("\r", ""));
                if (raw.substring(0, 20).toLowerCase().contains("<!doctype")) {
                    System.err.println("Received HTML from Reddit API instead of JSON. Have you been making more than 30 requests per minute?");
                }
                throw e;
            }

            JsonNode errorsNode = rootNode.get("json");
            if (errorsNode != null) {
                errorsNode = errorsNode.get("errors");
            }

            if (errorsNode != null) {
                apiExceptions = new net.dean.jraw.ApiException[errorsNode.size()];
                if (errorsNode.size() > 0) {

                    for (int i = 0; i < errorsNode.size(); i++) {
                        JsonNode error = errorsNode.get(i);
                        apiExceptions[i] = new ApiException(error.get(0).asText(), error.get(1).asText());
                    }
                }
            } else {
                // We still have to initialize it
                apiExceptions = new net.dean.jraw.ApiException[0];
            }
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RedditObject> T as(Class<T> thingClass) {
        if (thingClass.equals(Submission.class)) {
            // Special handling for submissions, not just submission data being returned, also its comments.
            // For example: http://www.reddit.com/92dd8.json

            // rootNode is an array where the first is a listing which contains one element in its "children": the submission.
            // The second element in the array is a listing of comments

            // Get the list of comments first
            JsonNode commentListingDataNode = rootNode.get(1).get("data");
            Listing<Comment> comments = new Listing<>(commentListingDataNode, Comment.class);
            return (T) new Submission(rootNode.get(0).get("data").get("children").get(0).get("data"), comments);
        }

        // Normal Thing
        return redditObjectParser.parse(rootNode, thingClass);
    }

    public <T extends RedditObject> Listing<T> asListing(Class<T> thingClass) {
        return new Listing<>(rootNode.get("data"), thingClass);
    }

    public boolean hasErrors() {
        return apiExceptions.length != 0;
    }

    public ApiException[] getApiExceptions() {
        return apiExceptions;
    }

    /**
     * Gets a Header object by name from the list of headers
     *
     * @param name The name of the header, such as {@code Content-Length}
     * @return A Header object with a given name
     */
    public Header getHeader(String name) {
        for (Header h : headers) {
            if (h.getName().equalsIgnoreCase(name)) {
                return h;
            }
        }

        return null;
    }

    /**
     * Gets all the headers sent by the server
     *
     * @return A List of Header objects
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * Gets the root JsonNode
     *
     * @return The root JsonNode
     */
    public JsonNode getJson() {
        return rootNode;
    }

    /**
     * Gets the raw data returned from the request
     *
     * @return The raw data of the request
     */
    public String getRaw() {
        return raw;
    }
}