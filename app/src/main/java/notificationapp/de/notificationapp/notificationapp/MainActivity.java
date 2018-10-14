package notificationapp.de.notificationapp.notificationapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedHashMap;

import notificationapp.de.notificationapp.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyFirebaseMsgService";

    public static final String CHANNEL_ID = "NotificationApp_BlitzerService";
    public static final String DATA_CHANGED = "notificationapp.de.notificationapp.DATA_CHANGED";

    private static final int BLITZER_NOTIFICATION_ID = 1;

    private LocalBroadcastManager bManager;

    private HashMapAdapter adapter;
    private LinkedHashMap<String, String> itemMap = new LinkedHashMap<>();

    private ListView mainList;
    //private TextView messageTitle;
    //private TextView messageBody;

    private boolean isAppRunning = false;

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "New intent " + intent.getAction());
            if (intent.getAction().equals(DATA_CHANGED)) {
                adapter.clear();

                if (intent.hasExtra("values")) {
                    int i = 0;
                    for (String o : intent.getStringArrayListExtra("values")) {
                        adapter.put("" + i, o);
                        i++;
                    }
                    showNotification();
                } else {
                    Log.e(TAG, "Intent has no extra !");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DATA_CHANGED);
        bManager.registerReceiver(bReceiver, intentFilter);

        setContentView(R.layout.activity_main);
        mainList = (ListView) findViewById(R.id.mainList);
        //messageTitle = (TextView) findViewById(R.id.messageTitle);
        //messageBody = (TextView) findViewById(R.id.messageBody);

        adapter = new HashMapAdapter(this, R.layout.list_item, itemMap);

        mainList.setAdapter(adapter);

        MyFirebaseDatabaseService.startActionInit(this);

        if (getIntent().hasExtra("click_action")) {
            Log.d(TAG, "OnCreate " + getIntent().getExtras().getString("click_action"));

            String title = "";
            String body = "";

            if (getIntent().hasExtra("title")) title = getIntent().getExtras().getString("title");
            if (getIntent().hasExtra("body")) body = getIntent().getExtras().getString("body");
        }

        createNotificationChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    private void showNotification() {
        if (isAppRunning) {
            Log.i(TAG, "App is running - not showing notification");
            return;           // dont show notification when app visible
        }

        Log.i(TAG, "Showing notification");

        NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_blitzer_icon)
                        .setContentTitle("Blitzer warning")
                        .setContentText((adapter.getCount()==0 ? "No" : adapter.getCount()) + " warnings");
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        if (adapter.getCount()>0) {
            inboxStyle.setBigContentTitle("New Warnings:");

            for (int i = 0; i < adapter.getCount(); i++) {
                inboxStyle.addLine(adapter.getItem(i).toString());
            }
        } else {
            inboxStyle.setBigContentTitle("No Warnings");
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
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        // mId allows you to update the notification later on.
        Log.i(TAG, "Submit notification");
        mNotificationManager.notify(TAG, BLITZER_NOTIFICATION_ID, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "NotificationApp", importance);
            channel.setDescription("Channel for Notification App");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
