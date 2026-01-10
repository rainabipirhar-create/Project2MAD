package com.example.myproject2;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when a new token for the default Firebase project is generated.
     * This is called when the app is first installed and whenever a new token is generated.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // You could send this token to your server here if needed
    }

    /**
     * Called when a message is received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // You can handle data messages here if you send them from your server
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            // This is where you would typically create and show a custom notification
            // to the user, especially when the app is in the foreground.
        }
    }
}
