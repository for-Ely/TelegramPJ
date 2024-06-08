package pj.telegram.telegramservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotService extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "Mirei";
    }

    @Override
    public String getBotToken() {
        return System.getenv("api_telegram_mirei");
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();

        JSONObject msgJson = new JSONObject(msg);
        MessageInfo messageInfo = new MessageInfo();
        try {
            messageInfo.getMessageInfo(msgJson);
            AirtableService.sendUserData(messageInfo.messageInfoToAirTableFormat());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Command

        if (msg.isCommand()) {
            switch (msg.getText()) {
                case "/createchatinvitelink@mireithelosebot":   
                    String respond = createChatInviteLink(getBotToken(), msg.getChatId().toString());
                    sendText(msg.getChat().getId(), respond);
                    break;
                case "/tell1":
                    tell1(id);
                    break;
                default:
                    sendText(id, "I don't understand that command.");
                    break;
            }
        }

        if (msg.getText().equals("Mirei")) {
            sendText(id, "This is Mirei, desu.");
        }
    }


    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder().chatId(who.toString()).text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void tell1(Long id) {
        sendText(id, "I would say 1");
    }
    public String createChatInviteLink(String apiKey, String chatId) {
        // Make an HTTP request to the Telegram API with apiKey and chatId
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.telegram.org/bot%s/createChatInviteLink?chat_id=%s", apiKey, chatId);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonObject = new JSONObject(response.body());
                return jsonObject.getJSONObject("result").getString("invite_link");
            } else {
                return "Failed, status code: " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "Failed";
    }
}