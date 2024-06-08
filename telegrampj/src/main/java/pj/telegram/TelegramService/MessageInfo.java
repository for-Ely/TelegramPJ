package pj.telegram.TelegramService;

import java.util.ArrayList;

import org.json.JSONObject;

public class MessageInfo {
    // userID, firstName, lastName, text, chatID,  messageID, date, isBot, userName, chatType, title, caption
    private Long userID;
    private String firstName;
    private String lastName;
    private String text;
    private Long chatID;
    private Long messageID;
    private Long date;
    private Boolean isBot;
    private String userName;
    private String chatType;
    private String title;
    private String caption;

    public Long getUserID() {
        return this.userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getChatID() {
        return this.chatID;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
    }

    public Long getMessageID() {
        return this.messageID;
    }

    public void setMessageID(Long messageID) {
        this.messageID = messageID;
    }

    public Long getDate() {
        return this.date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Boolean getIsBot() {
        return this.isBot;
    }

    public void setIsBot(Boolean isBot) {
        this.isBot = isBot;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChatType() {
        return this.chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
// Message(messageId=33, from=User(id=6300840933, firstName=Sy, isBot=false, lastName=Quân, userName=null, languageCode=en, canJoinGroups=null, canReadAllGroupMessages=null, supportInlineQueries=null), date=1717816947, chat=Chat(id=-1002187560280, type=supergroup, title=Dothycon, firstName=null, lastName=null, userName=null, photo=null, description=null, inviteLink=null, pinnedMessage=null, stickerSetName=null, canSetStickerSet=null, permissions=null, slowModeDelay=null, bio=null, linkedChatId=null, location=null, messageAutoDeleteTime=null, hasPrivateForwards=null, HasProtectedContent=null), forwardFrom=null, forwardFromChat=null, forwardDate=null, text=null, entities=null, captionEntities=null, audio=null, document=null, photo=[PhotoSize(fileId=AgACAgUAAyEFAASCY4VYAAMhZmPOc390DMjm8MOI24Rus4MuhU8AApa9MRskhSBXF_C2zVhzZmgBAAMCAANzAAM1BA, fileUniqueId=AQADlr0xGySFIFd4, width=90, height=51, fileSize=1270, filePath=null), PhotoSize(fileId=AgACAgUAAyEFAASCY4VYAAMhZmPOc390DMjm8MOI24Rus4MuhU8AApa9MRskhSBXF_C2zVhzZmgBAAMCAANtAAM1BA, fileUniqueId=AQADlr0xGySFIFdy, width=320, height=180, fileSize=17901, filePath=null), PhotoSize(fileId=AgACAgUAAyEFAASCY4VYAAMhZmPOc390DMjm8MOI24Rus4MuhU8AApa9MRskhSBXF_C2zVhzZmgBAAMCAAN4AAM1BA, fileUniqueId=AQADlr0xGySFIFd9, width=800, height=450, fileSize=77621, filePath=null), PhotoSize(fileId=AgACAgUAAyEFAASCY4VYAAMhZmPOc390DMjm8MOI24Rus4MuhU8AApa9MRskhSBXF_C2zVhzZmgBAAMCAAN5AAM1BA, fileUniqueId=AQADlr0xGySFIFd-, width=1280, height=720, fileSize=147668, filePath=null)], sticker=null, video=null, contact=null, location=null, venue=null, animation=null, pinnedMessage=null, newChatMembers=[], leftChatMember=null, newChatTitle=null, newChatPhoto=null, deleteChatPhoto=null, groupchatCreated=null, replyToMessage=null, voice=null, caption=syne, superGroupCreated=null, channelChatCreated=null, migrateToChatId=null, migrateFromChatId=null, editDate=null, game=null, forwardFromMessageId=null, invoice=null, successfulPayment=null, videoNote=null, authorSignature=null, forwardSignature=null, mediaGroupId=null, connectedWebsite=null, passportData=null, forwardSenderName=null, poll=null, replyMarkup=null, dice=null, viaBot=null, senderChat=null, proximityAlertTriggered=null, messageAutoDeleteTimerChanged=null, isAutomaticForward=null, hasProtectedContent=null, webAppData=null, videoChatStarted=null, videoChatEnded=null, videoChatParticipantsInvited=null, videoChatScheduled=null)
    public void getMessageInfo(JSONObject msg) throws Exception {
            // userID, firstName, lastName, text, chatID,  messageID, date, isBot, userName, chatType, title, caption
        this.userID = msg.getJSONObject("from").getLong("id");
        this.firstName = msg.getJSONObject("from").getString("firstName");
        this.lastName = msg.getJSONObject("from").getString("lastName");
        this.text = msg.optString("text", null);
        this.chatID = msg.getJSONObject("chat").getLong("id");
        this.messageID = msg.getLong("messageId");
        this.date = msg.getLong("date");
        this.isBot = msg.getJSONObject("from").getBoolean("isBot");
        this.userName = msg.getJSONObject("from").optString("userName", null);
        this.chatType = msg.getJSONObject("chat").getString("type");
        this.title = msg.getJSONObject("chat").optString("title", null);
        this.caption = msg.optString("caption", null);
    }

    public JSONObject messageInfoToAirTableFormat() {
        JSONObject fields = new JSONObject();
        fields.put("userID", this.userID);
        fields.put("firstName", this.firstName);
        fields.put("lastName", this.lastName);
        fields.put("text", this.text);
        fields.put("chatID", this.chatID);
        fields.put("messageID", this.messageID);
        fields.put("date", this.date);
        fields.put("isBot", this.isBot);
        fields.put("userName", this.userName);
        fields.put("chatType", this.chatType);
        fields.put("title", this.title);
        fields.put("caption", this.caption);
        
        ArrayList<JSONObject> recordsArray = new ArrayList<>();
        recordsArray.add(new JSONObject().put("fields", fields));

        JSONObject records = new JSONObject();
        records.put("records", recordsArray);
        
        return records;
    }
}
