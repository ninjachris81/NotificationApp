package notificationapp.de.notificationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyFirebaseMsgService";

    private ListView mainList;
    private List<String> itemList = new ArrayList<>();
    private ArrayAdapter adapter;

    private TextView messageTitle;
    private TextView messageBody;

    public static final String RECEIVE_DATA = "notificationapp.de.notificationapp.RECEIVE_DATA";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_DATA)) {
                refreshList(intent.getStringExtra("title"), intent.getStringExtra("body"), intent.getStringExtra("data"));
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
        bManager.registerReceiver(bReceiver, intentFilter);

        setContentView(R.layout.activity_main);
        mainList = (ListView) findViewById(R.id.mainList);
        messageTitle = (TextView) findViewById(R.id.messageTitle);
        messageBody = (TextView) findViewById(R.id.messageBody);

        adapter = new ArrayAdapter<>(this, R.layout.list_item, itemList);
        mainList.setAdapter(adapter);

        if (getIntent().hasExtra("click_action")) {
            Log.d(TAG, "OnCreate " + getIntent().getExtras().getString("click_action"));

            String title = "";
            String body = "";
            String data = "";

            if (getIntent().hasExtra("title")) title = getIntent().getExtras().getString("title");
            if (getIntent().hasExtra("body")) body = getIntent().getExtras().getString("body");
            if (getIntent().hasExtra("data")) data = getIntent().getExtras().getString("data");

            refreshList(title, body, data);
        }

        Intent intent = new Intent(this, MyFirebaseMessagingService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bManager.unregisterReceiver(bReceiver);
    }

    private void refreshList(final String title, final String body, final String data) {

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Title: " + title);
                messageTitle.setText(title);

                Log.d(TAG, "Body: " + body);
                messageBody.setText(body);
            }
        });

        Log.d(TAG, "Data: " + data);
        String[] items = data.split(Pattern.quote("|"));

        adapter.clear();

        for (String item : items) {
            adapter.add(item);
        }

        Log.d(TAG, "Items: " + adapter.getCount());
    }

}
