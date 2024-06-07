package pj.telegram;


import ca.denisab85.tdlib.*;

public class SendMessageExample {

    public static void main(String[] args) {

        // Initialize TDLib parameters (replace placeholders)
        String apiId = "YOUR_API_ID";
        String apiHash = "YOUR_API_HASH";
        String phoneNumber = "YOUR_PHONE_NUMBER";

        // Initialize TDLib client (adjust based on the wrapper's API)
        Client client = new Client(apiId, apiHash, new UpdateHandler() {
            @Override
            public void onUpdate(TdApi.Object object) {
                if (object instanceof TdApi.UpdateAuthorizationState) {
                    TdApi.UpdateAuthorizationState update = (TdApi.UpdateAuthorizationState) object;
                    if (update.authorizationState instanceof TdApi.AuthorizationStateWaitPhoneNumber) {
                        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null));
                    } else if (update.authorizationState instanceof TdApi.AuthorizationStateWaitCode) {
                        // Handle code input (you'll need to prompt the user)
                    }
                    // ... (Handle other authorization states)
                }
            }
        });

        client.start(); // Start the client

        // After authorization is successful...

        long chatId = 123456789L; // Replace with actual chat ID
        String messageText = "Hello from TDLib Java!";

        TdApi.InputMessageContent content = new TdApi.InputMessageText(
            new TdApi.FormattedText(messageText, null), null, true); // Formatted text

        client.send(new TdApi.SendMessage(chatId, 0, null, null, content), 
            (Object object) -> {
                // Handle the response, e.g., check if the message was sent successfully
            });

        // ... (Keep the client running to receive updates)
    }
}
