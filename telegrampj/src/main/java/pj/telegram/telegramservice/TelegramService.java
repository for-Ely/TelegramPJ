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
import it.tdlight.jni.TdApi.MessageContent;
import it.tdlight.jni.TdApi.MessageSenderUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
        long adminId = Long.parseLong(System.getenv("ADMIN_ID"));
        Init.init();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(
                    Integer.parseInt(System.getenv("TELEGRAM_API_ID")),
                    System.getenv("TELEGRAM_API_HASH"));
            TDLibSettings settings = configureTDLibSettings(apiToken);
            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier
                    .user(System.getenv("PHONE_NUMBER"));

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

        public TelegramApp(SimpleTelegramClientBuilder clientBuilder,
                SimpleAuthenticationSupplier<?> authenticationData, long adminId) {
            this.adminId = adminId;
            clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
            clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);
            clientBuilder.addCommandHandler("stop", this::onStopCommand);
            clientBuilder.addCommandHandler("searchPublicChat", this::onSearchPublicChat);
            clientBuilder.addCommandHandler("createGroupChat", this::onCreateNewSupergroupChat);
            clientBuilder.addCommandHandler("collectChatInfo", this::onCollectChatInfo);
            clientBuilder.addCommandHandler("addChatMembers", this::onAddChatMembers);
            clientBuilder.addCommandHandler("getChatList", this::onGetChatList);

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

        private void selfNotice(String text) {
            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = adminId;
            TdApi.InputMessageContent inputMessageContent = new TdApi.InputMessageText();
            ((TdApi.InputMessageText) inputMessageContent).text = new TdApi.FormattedText(text, null);
            sendMessage.inputMessageContent = inputMessageContent;
            System.out.println("Sending message: " + text);
            client.send(sendMessage);
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
                            System.out.printf("Received new message from chat %s (%s %s): %s%n", title, chatId,
                                    ((TdApi.MessageSenderUser) senderId).userId, text);
                        }
                    });
        }

        private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (isAdmin(((MessageSenderUser) commandSender).userId)) {
                System.out.println("Received stop command. closing...");
                client.sendClose();
            }
        }

        private boolean isAdmin(long senderId) {
            return senderId == adminId;
        }

        private void getSupergroupMembers(long chatId, long superchatId, int offset, int limit) {
            client.send(new TdApi.GetSupergroupMembers(superchatId, null, offset, limit))
                    .whenCompleteAsync((result, error) -> {
                        if (error == null) {
                            for (TdApi.ChatMember chatMember : result.members) {
                                addChatMember(chatId, (((MessageSenderUser) chatMember.memberId).userId));
                            }
                        }
                    });
        }

        private void addChatMember(long chatId, long userId) {
                client.send(new TdApi.AddChatMember(chatId, userId, 0));
        }
        private void onAddChatMembers(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            long chatId = chat.id;
            long superchatId = Long.parseLong(arguments);
            getSupergroupMembers(chatId, superchatId, 0, 200);
        }

        private void onGetChatList(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            ArrayList<ChatInfo> chatInfoList = AirtableService.getChatsInfo();
            for (ChatInfo chatInfo : chatInfoList) {
                // chat title and chat id
                String result = String.format("Chat title: %s, Chat id: %s, Member count: %s , Chat Type id: %s",
                        chatInfo.getChatTitle(), chatInfo.getChatId(), chatInfo.getMemberCount(), chatInfo.getChatTypeId());
                selfNotice(result);
            }
        }

        private void onSearchPublicChat(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            client.send(new TdApi.SearchPublicChat(arguments))
                    .whenCompleteAsync((chatResult, error) -> {
                        if (error == null) {
                            selfNotice(chatResult.toString());
                        }
                    });
        }

        private void onCreateNewSupergroupChat(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            String title = arguments;
            client.send(new TdApi.CreateNewSupergroupChat(title, false, false, "This is a telegram group", null,
                    365 * 86400, false))
                    .whenCompleteAsync((chatResult, error) -> {
                        if (error == null) {
                            selfNotice("Create new supergroup chat: " + title + " successfully");
                        }
                    });
        }

        private void onCollectChatInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            client.send(new TdApi.DeleteMessages(chat.id, new long[] { chat.lastMessage.id }, true));

            client.send(new TdApi.GetChat(chat.id))
                    .whenCompleteAsync((chatResult, error) -> {
                        if (error == null) {
                            if (chatResult.type instanceof TdApi.ChatTypeSupergroup chatTypeSupergroup) {
                                client.send(new TdApi.GetSupergroupFullInfo(chatTypeSupergroup.supergroupId))
                                        .whenCompleteAsync((supergroupFullInfo, error1) -> {
                                            if (error1 != null) {
                                                System.out.println("_______________________");
                                                System.out.println(error1);
                                            } else {
                                                long chatId = chat.id;
                                                String chatType = chatResult.type.getClass().getSimpleName();
                                                long chatTypeId = chatTypeSupergroup.supergroupId;
                                                boolean isChannel = chatTypeSupergroup.isChannel;
                                                String chatTitle = chatResult.title;
                                                long memberCount = supergroupFullInfo.memberCount;
                                                String inviteLink = supergroupFullInfo.inviteLink.inviteLink;

                                                if (!AirtableService.ifExistChatInfo(chatId)) {
                                                    ChatInfo chatInfo = new ChatInfo(chatId, chatType, chatTypeId,
                                                            isChannel, chatTitle, memberCount, inviteLink);
                                                    AirtableService
                                                            .sendChatInfoData(chatInfo.chatInfotoAirTableFormat());
                                                    selfNotice("Add chat info of: " + chatTitle + " " + chat.id
                                                            + " successfully");
                                                } else {
                                                    selfNotice("Chat info of: " + chatTitle + " " + chat.id
                                                            + " is already in the database");
                                                }
                                            }
                                        });
                            }
                            if (chatResult.type instanceof TdApi.ChatTypePrivate chatTypePrivate) {
                                client.send(new TdApi.GetChat(chatTypePrivate.userId))
                                        .whenCompleteAsync((userInfo, error1) -> {
                                            if (error1 != null) {
                                                System.out.println("_______________________");
                                                System.out.println(error1);
                                            } else {
                                                long chatId = chat.id;
                                                String chatType = chatResult.type.getClass().getSimpleName();
                                                long chatTypeId = chatTypePrivate.userId;
                                                boolean isChannel = false;
                                                String chatTitle = userInfo.title;
                                                long memberCount = 1;
                                                String inviteLink = null;
                                                if (!AirtableService.ifExistChatInfo(chatId)) {
                                                    ChatInfo chatInfo = new ChatInfo(chatId, chatType, chatTypeId,
                                                            isChannel, chatTitle, memberCount, inviteLink);
                                                    AirtableService
                                                            .sendChatInfoData(chatInfo.chatInfotoAirTableFormat());
                                                    selfNotice("Add chat info of: " + chatTitle + " " + chat.id
                                                            + " successfully");
                                                } else {
                                                    selfNotice("Chat info of: " + chatTitle + " " + chat.id
                                                            + " is already in the database");
                                                }
                                            }
                                        });
                            }

                        }
                    });
        }
    }
}
