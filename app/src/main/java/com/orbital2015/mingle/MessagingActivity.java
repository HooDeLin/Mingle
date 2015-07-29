package com.orbital2015.mingle;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;


public class MessagingActivity extends ActionBarActivity {

    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
        populateMessageHistory();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    //get previous messages from parse & display
    private void populateMessageHistory() {
        String[] userIds = {currentUserId, recipientId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipientId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message = new WritableMessage(messageList.get(i).get("recipientId").toString(), messageList.get(i).get("messageText").toString());
                        if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                        }
                    }
                }
            }
        });
    }

    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        messageService.sendMessage(recipientId, messageBody);
        messageBodyField.setText("");

        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("ProfileCredentials");
        userQuery.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString());

        ParseQuery<ParseObject> recipientQuery = ParseQuery.getQuery("ProfileCredentials");
        recipientQuery.whereEqualTo("userId", recipientId);

        try {
            ParseObject userParseObject = userQuery.getFirst();
            ParseObject recipientParseObject = recipientQuery.getFirst();

            List<String> userChatHistory = userParseObject.getList("ChatHistory");
            List<Integer> userNewMessage = userParseObject.getList("newMessage");
            List<String> recipientChatHistory = recipientParseObject.getList("ChatHistory");
            List<Integer> recipientNewMessage = recipientParseObject.getList("newMessage");

            if(userChatHistory.size() == 0){
                userChatHistory.add(recipientId);
                userParseObject.put("ChatHistory", userChatHistory);
                userNewMessage.add(0);
                userParseObject.put("newMessage", userNewMessage);
                userParseObject.save();
            } else {
                userChatHistory.remove(recipientId);
                userChatHistory.add(0, recipientId);
                userParseObject.put("ChatHistory", userChatHistory);
                userNewMessage.add(0);
                userParseObject.put("newMessage", userNewMessage);
                userParseObject.save();
            }

            if(recipientChatHistory.size() == 0){
                recipientChatHistory.add(currentUserId);
                recipientParseObject.put("ChatHistory", recipientChatHistory);
                recipientNewMessage.add(0);
                recipientParseObject.put("newMessage", userNewMessage);
                recipientParseObject.save();
            } else {

                recipientChatHistory.remove(currentUserId);
                recipientChatHistory.add(0, currentUserId);
                recipientParseObject.put("ChatHistory", recipientChatHistory);
                recipientNewMessage.add(0);
                recipientParseObject.put("newMessage", userNewMessage);
                recipientParseObject.save();
            }
        } catch(Exception e) {

        }



    }

    @Override
    public void onDestroy() {
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            if (message.getSenderId().equals(recipientId)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {

            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //only add message to parse database if it doesn't already exist there
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId);
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                        }
                    }
                }
            });


        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
            ParseQuery userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("objectId", writableMessage.getRecipientIds().get(0));

            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereMatchesQuery("user", userQuery);

            ParsePush push = new ParsePush();
            push.setQuery(pushQuery);
            push.setMessage(ParseUser.getCurrentUser().getUsername().toString() + " sent you a message");
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.e("sendPush", "successful");
                    } else {
                        Log.e("sendPush", e.toString());
                    }
                }
            });
        }
    }
}