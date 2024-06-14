package pj.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import pj.telegram.telegramservice.BotService;
import pj.telegram.telegramservice.AirtableService;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        BotService bot = new BotService();
        botsApi.registerBot(bot);
        bot.sendText(6300840933L, "Mirei is online");
        AirtableService.getUserList().forEach(System.out::println);
    }
}
