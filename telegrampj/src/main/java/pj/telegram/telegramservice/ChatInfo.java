package pj.telegram.telegramservice;

import java.util.ArrayList;

import org.json.JSONObject;

public class ChatInfo {
    private long chatId;
    private String chatType;
    private long chatTypeId;
    private boolean isChannel;
    private String chatTitle;
    private long memberCount;
    private String inviteLink;

    public long getChatId() {
        return chatId;
    }

    public String getChatType() {
        return chatType;
    }

    public long getChatTypeId() {
        return chatTypeId;
    }

    public boolean isChannel() {
        return isChannel;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public ChatInfo(long chatId, String chatType, long chatTypeId, boolean isChannel, String chatTitle, long memberCount, String inviteLink) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.chatTypeId = chatTypeId;
        this.isChannel = isChannel;
        this.chatTitle = chatTitle;
        this.memberCount = memberCount;
        this.inviteLink = inviteLink;
    }


    public JSONObject chatInfotoAirTableFormat() {
        System.out.println("ChatInfo: " + this.chatId);
        JSONObject fields = new JSONObject();
        fields.put("chatId", this.chatId);
        fields.put("chatType", this.chatType);
        fields.put("chatTypeId", this.chatTypeId);
        fields.put("isChannel", this.isChannel);
        fields.put("chatTitle", this.chatTitle);
        fields.put("memberCount", this.memberCount);
        fields.put("inviteLink", this.inviteLink);

        ArrayList<JSONObject> recordsArray = new ArrayList<>();
        recordsArray.add(new JSONObject().put("fields", fields));

        JSONObject records = new JSONObject();
        records.put("records", recordsArray);
        
        return records;
    }

}
