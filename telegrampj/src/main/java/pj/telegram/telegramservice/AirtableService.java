package pj.telegram.telegramservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.json.JSONObject;

public class AirtableService {
    private static final String AIRTABLE_ACCESS_TOKEN = System.getenv("AIRTABLE_ACCESS_TOKEN");
    private static final String AIRTABLE_BASE_ID = System.getenv("AIRTABLE_BASE_ID");
    private static final String AIRTABLE_TABLE_NAME_CHATINFO = System.getenv("AIRTABLE_TABLE_NAME_CHATINFO");

    public static void sendChatInfoData(JSONObject data) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME_CHATINFO);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean ifExistChatInfo(long chatId) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME_CHATINFO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            for (int i = 0; i < jsonObject.getJSONArray("records").length(); i++) {
                long chatIdFromAirtable = jsonObject.getJSONArray("records").getJSONObject(i).getJSONObject("fields")
                        .getLong("chatId");
                if (chatIdFromAirtable == chatId) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public static ChatInfo getChatInfo(long chatId) {
        // https://api.airtable.com/v0/apphQwFKwdK1suNIC/tblZlaKn2jdn5A4Tz?filterByFormula=chatId=6300840933
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s?filterByFormula=chatId=%d", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME_CHATINFO, chatId);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
            .GET()
            .build();
        try {         
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            JSONObject fields = jsonObject.getJSONArray("records").getJSONObject(0).getJSONObject("fields");

            if (fields == null) {
                return null;
            }

            long chatId1 = fields.optLong("chatId", -1);
            String chatType = fields.optString("chatType", "");
            long chatTypeId = fields.optLong("chatTypeId", -1);
            boolean isChannel = fields.optBoolean("isChannel", false);
            String chatTitle = fields.optString("chatTitle", "");
            long memberCount = fields.optLong("memberCount", -1);
            String inviteLink = fields.optString("inviteLink", "");
            ChatInfo chatInfo = new ChatInfo(chatId1, chatType, chatTypeId, isChannel, chatTitle, memberCount, inviteLink);
            System.out.println("Sy");

            return chatInfo;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    public static ArrayList<ChatInfo> getChatsInfo() {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.airtable.com/v0/%s/%s", AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME_CHATINFO);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AIRTABLE_ACCESS_TOKEN)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            ArrayList<ChatInfo> chatsInfo = new ArrayList<>();
            for (int i = 0; i < jsonObject.getJSONArray("records").length(); i++) {
                JSONObject fields = jsonObject.getJSONArray("records").getJSONObject(i).getJSONObject("fields");
                chatsInfo.add(new ChatInfo(
                        fields.optLong("chatId", -1),
                        fields.optString("chatType", ""),
                        fields.optLong("chatTypeId", -1),
                        fields.optBoolean("isChannel", false),
                        fields.optString("chatTitle", ""),
                        fields.optLong("memberCount", -1),
                        fields.optString("inviteLink", "")
                ));
            }
            return chatsInfo;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
