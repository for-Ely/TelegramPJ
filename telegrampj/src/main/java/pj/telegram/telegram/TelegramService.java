package pj.telegram.telegram;

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
import it.tdlight.jni.TdApi.MessageContent;
import it.tdlight.jni.TdApi.MessageSenderUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TelegramService {

    // Main để chạy TelegramService
    public static void main(String[] args) {
        try {
            new TelegramService().start();
        } catch (Exception e) {
        }
    }

    private TelegramService() {
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
            clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);
            clientBuilder.addCommandHandler("stop", this::onStopCommand);
            clientBuilder.addCommandHandler("searchpublicchat", this::onSearchPublicChat);
            clientBuilder.addCommandHandler("creategroupchat", this::onCreateNewSupergroupChat);
            clientBuilder.addCommandHandler("collectchatinfo", this::onCollectChatInfo);
            clientBuilder.addCommandHandler("addchatmembers", this::onAddChatMembers);
            clientBuilder.addCommandHandler("getchatlist", this::onGetChatList);
            clientBuilder.addCommandHandler("updatechatsinfo", this::onUpdateChatsInfo);

            this.client = clientBuilder.build(authenticationData);
        }

        public SimpleTelegramClient getClient() {
            return client;
        }

        private void selfNotice(String text) {
            TdApi.SendMessage sendMessage = new TdApi.SendMessage();
            sendMessage.chatId = 7181176105L;
            TdApi.InputMessageContent inputMessageContent = new TdApi.InputMessageText();
            ((TdApi.InputMessageText) inputMessageContent).text = new TdApi.FormattedText(text, null);
            sendMessage.inputMessageContent = inputMessageContent;
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
                        if (error == null) {
                            String title = chatIdResult.title;
                            System.out.printf("Received new message from chat %s (%s %s): %s%n", title, chatId,
                                    ((TdApi.MessageSenderUser) senderId).userId, text);
                        }
                    });
        }

        private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (isAdmin(((MessageSenderUser) commandSender).userId)) {
                client.sendClose();
            }
        }

        private boolean isAdmin(long senderId) {
            return senderId == adminId;
        }

        private void addChatMember(long chatId, long superchatId, int offset, int limit) {

            client.send(new TdApi.GetSupergroupMembers(superchatId, null, offset, limit))
                    .whenCompleteAsync((result, error) -> {
                        if (error == null) {
                            for (TdApi.ChatMember chatMember : result.members) {
                                client.send(new TdApi.AddChatMember(chatId,
                                        (((MessageSenderUser) chatMember.memberId).userId), 0));
                            }
                            for (TdApi.ChatMember chatMember : result.members) {
                                kickChatMemeber(chatId, ((MessageSenderUser) chatMember.memberId).userId);
                            }
                        }
                    });
        }

        private void kickChatMemeber(long chatId, long userId) {
            client.send(new TdApi.SetChatMemberStatus(chatId, new TdApi.MessageSenderUser(userId),
                    new TdApi.ChatMemberStatusLeft()));
        }

        private void onAddChatMembers(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            long chatId = chat.id;
            long targetchatId = Long.parseLong(arguments);
            ChatInfo dataInfo = AirtableService.getChatInfo(targetchatId);
            long superchatId = dataInfo.getChatTypeId();
            int quantity = (int) dataInfo.getMemberCount();
            for (int i = quantity; i > 0; i -= 200) {
                addChatMember(chatId, superchatId, quantity - i, Math.min(i, 200));
            }
            // addChatMember(chatId, superchatId, 0, 200);
        }

        private void onGetChatList(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            List<ChatInfo> chatInfoList = AirtableService.getChatsInfo();
            for (ChatInfo chatInfo : chatInfoList) {
                // chat title and chat id
                String result = String.format(
                        "Chat title: %s\nChat id: %s \nMember count: %s\nChat Type: %s \nChat Type id: %s",
                        chatInfo.getChatTitle(), chatInfo.getChatId(), chatInfo.getMemberCount(),
                        chatInfo.getChatType(), chatInfo.getChatTypeId());
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

        private void collectChatInfo(long chatId) {
            client.send(new TdApi.GetChat(chatId))
                    .whenCompleteAsync((chatResult, error) -> {
                        if (error == null && chatResult.type instanceof TdApi.ChatTypeSupergroup chatTypeSupergroup) {
                            client.send(new TdApi.GetSupergroupFullInfo(chatTypeSupergroup.supergroupId))
                                    .whenCompleteAsync((supergroupFullInfo, error1) -> {
                                        String chatType = chatResult.type.getClass().getSimpleName();
                                        long chatTypeId = chatTypeSupergroup.supergroupId;
                                        boolean isChannel = chatTypeSupergroup.isChannel;
                                        String chatTitle = chatResult.title;
                                        long memberCount = supergroupFullInfo.memberCount;
                                        String inviteLink = new String();
                                        if (supergroupFullInfo.inviteLink == null) {
                                            inviteLink = "";
                                        } else {
                                            inviteLink = supergroupFullInfo.inviteLink.inviteLink;
                                        }

                                        ChatInfo chatInfo = new ChatInfo(chatId, chatType, chatTypeId,
                                                isChannel, chatTitle, memberCount, inviteLink);
                                        if (!AirtableService.ifExistChatInfo(chatId)) {
                                            AirtableService
                                                    .sendChatInfoData(chatInfo.chatInfotoAirTableFormat());
                                            selfNotice("Add chat info of: " + chatTitle + " " + chatId
                                                    + " successfully");
                                        } else {
                                            AirtableService
                                                    .updateChatInfo(chatId,
                                                            chatInfo.chatInfotoAirTableFormat());
                                            selfNotice("Update chat info of: " + chatTitle + " " + chatId
                                                    + " successfully");
                                        }
                                    });
                        }
                        if (error == null && chatResult.type instanceof TdApi.ChatTypePrivate chatTypePrivate) {
                            client.send(new TdApi.GetUser(chatTypePrivate.userId))
                                    .whenCompleteAsync((user, error1) -> {
                                        String chatType = chatResult.type.getClass().getSimpleName();
                                        long chatTypeId = chatTypePrivate.userId;
                                        boolean isChannel = false;
                                        String chatTitle = user.firstName + " " + user.lastName;
                                        long memberCount = 1;
                                        String inviteLink = "";

                                        ChatInfo chatInfo = new ChatInfo(chatId, chatType, chatTypeId,
                                                isChannel, chatTitle, memberCount, inviteLink);
                                        if (!AirtableService.ifExistChatInfo(chatId)) {
                                            AirtableService
                                                    .sendChatInfoData(chatInfo.chatInfotoAirTableFormat());
                                            selfNotice("Add chat info of: " + chatTitle + " " + chatId
                                                    + " successfully");
                                        } else {
                                            AirtableService
                                                    .updateChatInfo(chatId,
                                                            chatInfo.chatInfotoAirTableFormat());
                                            selfNotice("Update chat info of: " + chatTitle + " " + chatId
                                                    + " successfully");
                                        }
                                    });
                        }

                    });
        }

        private void onCollectChatInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            client.send(new TdApi.DeleteMessages(chat.id, new long[] { chat.lastMessage.id }, true));
            long chatId = chat.id;
            collectChatInfo(chatId);
        }

        private void updateChatsInfo() {
            List<ChatInfo> chatInfoList = AirtableService.getChatsInfo();
            for (ChatInfo chatInfo : chatInfoList) {
                long chatId = chatInfo.getChatId();
                collectChatInfo(chatId);
            }
        }

        private void onUpdateChatsInfo(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (!isAdmin(((MessageSenderUser) commandSender).userId)) {
                return;
            }
            updateChatsInfo();
        }
    }
}
