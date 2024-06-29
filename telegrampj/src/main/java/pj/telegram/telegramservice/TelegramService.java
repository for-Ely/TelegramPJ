package pj.telegram.telegramservice;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.AuthorizationState;
import it.tdlight.jni.TdApi.BasicGroupFullInfo;
import it.tdlight.jni.TdApi.CreatePrivateChat;
import it.tdlight.jni.TdApi.MessageContent;
import it.tdlight.jni.TdApi.MessageSenderUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class TelegramService {

    // Main để chạy TelegramService
    public static void main(String[] args) {
        try {
            new TelegramService().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start the Telegram service");
        }
    }
    // Hàm start để khởi tạo TelegramApp
    private void start() throws Exception {
        long adminId = 6300840933L;
        Init.init();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(
                Integer.parseInt(System.getenv("TELEGRAM_API_ID")),
                System.getenv("TELEGRAM_API_HASH")
            );
            TDLibSettings settings = configureTDLibSettings(apiToken);
            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(System.getenv("PHONE_NUMBER"));

            TelegramApp app = new TelegramApp(clientBuilder, authenticationData, adminId);
            
            app.getClient().getMeAsync().get(1, TimeUnit.MINUTES);
            
            Thread.currentThread().join();
        }
    }

    private TDLibSettings configureTDLibSettings(APIToken apiToken) {
        TDLibSettings settings = TDLibSettings.create(apiToken);
        Path sessionPath = Paths.get("tdlight-session");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));
        return settings;
    }

    public static class TelegramApp {

        private final SimpleTelegramClient client;
        private final long adminId;

        public TelegramApp(SimpleTelegramClientBuilder clientBuilder, SimpleAuthenticationSupplier<?> authenticationData, long adminId) {
            this.adminId = adminId;
            clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
            clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);
            clientBuilder.addCommandHandler("stop", this::onStopCommand);
            clientBuilder.addCommandHandler("addmember", this::onAddMemberToGroupCommand);
            clientBuilder.addCommandHandler("getinfo", this::getSupergroupMembers);
            clientBuilder.addCommandHandler("search", this::searchPublicChat);

            this.client = clientBuilder.build(authenticationData);
        }

        public SimpleTelegramClient getClient() {
            return client;
        }

        private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
            AuthorizationState authorizationState = update.authorizationState;
            if (authorizationState instanceof TdApi.AuthorizationStateReady) {
                System.out.println("Logged in");
            } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
                System.out.println("Closing...");
            } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
                System.out.println("Closed");
            } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
                System.out.println("Logging out...");
            }
        }

        private void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
            MessageContent messageContent = update.message.content;
            String text;
            if (messageContent instanceof TdApi.MessageText messageText) {
                text = messageText.text.text;
            } else {
                text = String.format("(%s)", messageContent.getClass().getSimpleName());
            }

            long chatId = update.message.chatId;
            TdApi.MessageSender senderId = update.message.senderId;
            client.send(new TdApi.GetChat(chatId))
                .whenCompleteAsync((chatIdResult, error) -> {
                    if (error != null) {
                        System.err.printf("Can't get chat title of chat %s%n", chatId);
                        error.printStackTrace(System.err);
                    } else {
                        String title = chatIdResult.title;
                        System.out.println("--------------------------------------------------------");
                        System.out.printf("Received new message from chat %s (%s %s): %s%n", title, chatId, ((TdApi.MessageSenderUser) senderId).userId, text);
                    }
                });
        }

        private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (isAdmin(commandSender)) {
                System.out.println("Received stop command. closing...");
                client.sendClose();
            }
        }
        public boolean isAdmin(TdApi.MessageSender sender) {
            if (sender instanceof MessageSenderUser messageSenderUser) {
                return messageSenderUser.userId == adminId;
            } else {
                return false;
            }
        }
        private void onAddMemberToGroupCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (isAdmin(commandSender)) {
                String[] args = arguments.split(" ");
                long chatId = chat.id;
                long userId = args.length > 0 ? Long.parseLong(args[0]) : 0;
                // client.send(new TdApi.AddContact(new TdApi.Contact(null, "unknow", "unknow", "unknow", userId),false));
                System.out.println("_______________________");
                client.send(new TdApi.AddChatMember(chatId, userId, 0));
                System.out.printf("Added user %s to chat %s%n", userId, chatId);

            }
        }
        private void getSupergroupFullInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            long superchatId = Long.parseLong(arguments);
            System.out.println(superchatId);
            client.send(new TdApi.GetSupergroupFullInfo(superchatId))
                .whenCompleteAsync((supergroupFullInfo, error) -> {
                    System.out.println("_______________________");
                    System.out.printf("Supergroup full info: %s%n", supergroupFullInfo);
                });
        }
        private void getSupergroupMembers(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            System.out.println("On going");
            long superchatId = Long.parseLong(arguments);
            client.send(new TdApi.GetSupergroupMembers(superchatId, null, 0, 5))
                .whenCompleteAsync((supergroupMembers, error) -> {
                    for (TdApi.ChatMember chatMember : supergroupMembers.members) {
                        System.out.println("_______________________");
                        System.out.printf("Supergroup member: %s%n", chatMember);
                    }
                });
        }
        private void getBasicGroupFullInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            long basicGroupId = Long.parseLong(arguments);
            client.send(new TdApi.GetBasicGroupFullInfo(basicGroupId))
                .whenCompleteAsync((basicGroupFullInfo, error) -> {
                    System.out.println("_______________________");
                    System.out.println(error);
                    System.out.printf("Basic group full info: %s%n", basicGroupFullInfo);
                });
        }
        private void getUserInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            long userId = Long.parseLong(arguments);
            client.send(new TdApi.GetUserFullInfo(userId))
                .whenCompleteAsync((user, error) -> {
                    System.out.println("_______________________");
                    System.out.printf("User info: %s%n", user);
                });
        }
        private void searchPublicChat(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            client.send(new TdApi.SearchPublicChat(arguments))
                .whenCompleteAsync((chatResult, error) -> {
                    System.out.println("_______________________");
                    System.out.println(error);
                    System.out.printf("Chat result: %s%n", chatResult);
                });
        }
    }
}
