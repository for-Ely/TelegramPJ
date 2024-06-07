package pj.telegram;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "Mirei";
    }

    @Override
    public String getBotToken() {
        String apiKey = System.getenv("api_telegram_mirei");
        return apiKey;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        System.out.println(user.getFirstName() + ": " + msg.getText());
        JSONObject userJson = new JSONObject(user);
        AirtableService airtableService = new AirtableService();
        System.out.println(msg);
        try {
            airtableService.sendUserData(airtableService.convertUserJsonObjectToFormattedJsonObject(userJson));
            System.out.println(airtableService.convertUserJsonObjectToFormattedJsonObject(userJson));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // copyMessage(id, msg.getMessageId());
        if (msg.isCommand()) {
            if (msg.getText().equals("/tell1")) {
                tell1(id);
            }
        }

        if (msg.getText().equals("Mirei")) {
            System.out.println(id);
            sendText(id, "This is Mirei, desu.");
        }
    }
        
    public void copyMessage(Long who, Integer msgId){
    CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
            .chatId(who.toString())      //And send it back to him
            .messageId(msgId)            //Specifying what message
            .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder().chatId(who.toString()).text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void tell1(Long id) {
        sendText(id, "I would say 1");
    }


    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        bot.sendText(6300840933L, "Mirei is online");
    }

}