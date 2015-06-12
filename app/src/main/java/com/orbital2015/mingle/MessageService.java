package com.orbital2015.mingle;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener{

    private static final String APP_KEY = "key";
    private static final String APP_SECRET = "secret";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        if(currentUserId != null && !isSinchClientStarted()){
            startSinchClient(currentUserId);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String username){
        sinchClient = Sinch.getSinchClientBuilder().context(this).userId(username).applicationKey(APP_KEY).applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();
    }

    private boolean isSinchClientStarted(){
        return sinchClient != null && sinchClient.isStarted();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    @Override
    public void onClientStarted(SinchClient sinchClient) {
        sinchClient.startListeningOnActiveConnection();
        messageClient = sinchClient.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient sinchClient) {
        sinchClient = null;
    }

    @Override
    public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
        sinchClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

    }

    @Override
    public void onLogMessage(int i, String s, String s1) {

    }

    public void sendMessage(String recipientUserId, String textBody){
        if(messageClient != null){
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener){
        if(messageClient != null){
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener){
        if(messageClient != null){
            messageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy(){
        sinchClient.startListeningOnActiveConnection();
        sinchClient.terminate();
    }

    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody){
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener){
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener){
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted(){
            return MessageService.this.isSinchClientStarted();
        }
    }
}