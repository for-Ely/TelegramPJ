package pj.telegram.telegramservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class AirtableService {
    private static final String AIRTABLE_ACCESS_TOKEN = System.getenv("AIRTABLE_ACCESS_TOKEN");
    private static final String AIRTABLE_BASE_ID = System.getenv("AIRTABLE_BASE_ID");
    private static final String AIRTABLE_TABLE_NAME = System.getenv("AIRTABLE_TABLE_NAME");
    
    private AirtableService() {
    }

    public static void sendUserData(JSONObject data) {
        System.out.println(data);
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json") 
                .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public static List<Long> getUserList() {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s?fields[]=userID", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            List<Long> userIds = new ArrayList<>();
            jsonObject.getJSONArray("records").forEach(record -> {
                long userId = ((JSONObject) record).getJSONObject("fields").getLong("userID");
                if (!userIds.contains(userId)) {
                    userIds.add(userId);
                }
            });
            return userIds;
            
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
