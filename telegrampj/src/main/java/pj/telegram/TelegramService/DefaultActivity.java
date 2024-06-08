package pj.telegram.TelegramService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class DefaultActivity {
    private static final String ACCESS_TOKEN = System.getenv("AIRTABLE_ACCESS_TOKEN");
    private static final String BASE_ID = System.getenv("AIRTABLE_BASE_ID");
    private static final String TABLE_NAME = "tblfcbLGJmfL4G0gY";
    
    public static void sendUserData(JSONObject data) {
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
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
