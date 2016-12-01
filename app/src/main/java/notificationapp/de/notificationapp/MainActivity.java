package notificationapp.de.notificationapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyFirebaseMsgService";

    private ListView mainList;
    private LinkedHashMap<String, String> itemMap = new LinkedHashMap<>();
    private HashMapAdapter adapter;

    private TextView messageTitle;
    private TextView messageBody;

    private boolean isAppRunning = false;

    private static final int BLITZER_NOTIFICATION_ID = 1;

    public static final String RECEIVE_DATA = "notificationapp.de.notificationapp.RECEIVE_DATA";
    public static final String ADD_WARNING = "notificationapp.de.notificationapp.ADD_WARNING";
    public static final String REMOVE_WARNING = "notificationapp.de.notificationapp.REMOVE_WARNING";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "New intent " + intent.getAction());
            if (intent.getAction().equals(RECEIVE_DATA)) {
                refreshList(intent.getStringExtra("title"), intent.getStringExtra("body"));
            } else if (intent.getAction().equals(ADD_WARNING)) {
                adapter.put(intent.getStringExtra("key"), intent.getStringExtra("value"));
                showNotification();
            } else if (intent.getAction().equals(REMOVE_WARNING)) {
                adapter.remove(intent.getStringExtra("key"));
            }
        }
    };
    private LocalBroadcastManager bManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseMessaging.getInstance().subscribeToTopic("blitzer");

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_DATA);
        intentFilter.addAction(ADD_WARNING);
        intentFilter.addAction(REMOVE_WARNING);
        bManager.registerReceiver(bReceiver, intentFilter);

        setContentView(R.layout.activity_main);
        mainList = (ListView) findViewById(R.id.mainList);
        messageTitle = (TextView) findViewById(R.id.messageTitle);
        messageBody = (TextView) findViewById(R.id.messageBody);

        adapter = new HashMapAdapter(this, R.layout.list_item, itemMap);

        mainList.setAdapter(adapter);

        MyFirebaseDatabaseService.startActionInit(this);

        if (getIntent().hasExtra("click_action")) {
            Log.d(TAG, "OnCreate " + getIntent().getExtras().getString("click_action"));

            String title = "";
            String body = "";

            if (getIntent().hasExtra("title")) title = getIntent().getExtras().getString("title");
            if (getIntent().hasExtra("body")) body = getIntent().getExtras().getString("body");

            refreshList(title, body);
        }

        Intent intent = new Intent(this, MyFirebaseMessagingService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    private void refreshList(final String title, final String body) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Title: " + title);
                messageTitle.setText(title);

                Log.d(TAG, "Body: " + body);
                messageBody.setText(body);
            }
        });
    }

    private void clearWarningList() {
        itemMap.clear();
    }

    private void showNotification() {
        if (isAppRunning) return;

        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_blitzer_icon)
                        .setContentTitle("Blitzer warning")
                        .setContentText((adapter.getCount()==0 ? "No" : adapter.getCount()) + " warnings");
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("Warnings:");
// Moves events into the expanded layout

        for (int i = 0; i < adapter.getCount(); i++) {
            inboxStyle.addLine(adapter.getItem(i).toString());
        }

        mBuilder.setStyle(inboxStyle);
        mBuilder.setAutoCancel(true);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(BLITZER_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isAppRunning = true;
    }
}
