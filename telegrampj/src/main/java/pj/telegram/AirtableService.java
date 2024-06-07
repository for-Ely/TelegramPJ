package pj.telegram;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AirtableService {
    private static final String ACCESS_TOKEN = "patsdb8zpB9snCAXE.6f83dd062c8d5370f9b5644172cc4f53490d601d3dff3eb5c8283caa7910fe0c";
    private static final String BASE_ID = "apphQwFKwdK1suNIC";
    private static final String TABLE_NAME = "tblMGGLRC0nNzgwSq";

    public void sendUserData(JSONObject data) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", BASE_ID, TABLE_NAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Data sent successfully:");
                System.out.println(response.body());
            } else {
                System.out.println("Failed to send data. Status code: " + response.statusCode());
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public JSONObject convertUserJsonObjectToFormattedJsonObject(JSONObject userJsonObject) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Convert the input JSONObject to a JsonNode
        JsonNode userNode = mapper.readTree(userJsonObject.toString());

        // Create an ObjectNode for fields
        ObjectNode fieldsNode = mapper.createObjectNode();
        fieldsNode.put("id", userNode.path("id").asText());
        fieldsNode.put("firstName", userNode.path("firstName").asText());
        fieldsNode.put("isBot", userNode.path("isBot").asText());
        fieldsNode.put("lastName", userNode.path("lastName").asText());
        fieldsNode.put("userName", userNode.path("userName").asText());
        fieldsNode.put("languageCode", userNode.path("languageCode").asText());
        fieldsNode.put("canJoinGroups", userNode.path("canJoinGroups").asText("null"));
        fieldsNode.put("canReadAllGroupMessages", userNode.path("canReadAllGroupMessages").asText("null"));
        fieldsNode.put("supportInlineQueries", userNode.path("supportInlineQueries").asText("null"));

        // Create an ObjectNode for record
        ObjectNode recordNode = mapper.createObjectNode();
        recordNode.set("fields", fieldsNode);

        // Create an ArrayNode for records
        ArrayNode recordsArray = mapper.createArrayNode();
        recordsArray.add(recordNode);

        // Create the final ObjectNode
        ObjectNode recordsNode = mapper.createObjectNode();
        recordsNode.set("records", recordsArray);

        // Convert the ObjectNode to a JSONObject
        JSONObject resultJsonObject = new JSONObject(recordsNode.toString());
        return resultJsonObject;
    }
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("fields", new JSONObject()
                .put("Name", "Quan Sy")
                .put("Age", 20)
                .put("ID", "3623463"));
        AirtableService airtableService = new AirtableService();
        airtableService.sendUserData(data);
    }
}
