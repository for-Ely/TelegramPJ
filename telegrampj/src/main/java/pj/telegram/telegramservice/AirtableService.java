package pj.telegram.telegramservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

class AirtableService {
    private static final String AIRTABLE_ACCESS_TOKEN = System.getenv("AIRTABLE_ACCESS_TOKEN");
    private static final String AIRTABLE_BASE_ID = System.getenv("AIRTABLE_BASE_ID");
    private static final String AIRTABLE_TABLE_NAME = "tblfcbLGJmfL4G0gY";
    
    private AirtableService() {
    }

    public static void sendUserData(JSONObject data) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
                .header("Content-Type", "application/json") 
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

}
