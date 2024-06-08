package pj.telegram.TelegramService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class ChatService {
    // Create a chat invite link
    public static String createChatInviteLink(String apiKey, String chatId) {
        // Make an HTTP request to the Telegram API with apiKey and chatId
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.telegram.org/bot%s/createChatInviteLink?chat_id=%s", apiKey, chatId);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                String inviteLink = jsonObject.getJSONObject("result").getString("invite_link");
                return inviteLink;
            } else {
                return "Failed to create chat invite link. Status code: " + response.statusCode();
            }
        } catch (Exception e) {
        }
        
        return "Failed";
    }
}