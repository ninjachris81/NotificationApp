package notificationapp.de.notificationapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyFirebaseDatabaseService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this

    private static final String TAG = "MyFirebaseDBService";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_INIT = "notificationapp.de.notificationapp.action.INIT";

    public MyFirebaseDatabaseService() {
        super("MyFirebaseDatabaseService");
    }

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private static Context mContext;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionInit(Context context) {
        Intent intent = new Intent(context, MyFirebaseDatabaseService.class);
        intent.setAction(ACTION_INIT);
        context.startService(intent);
        mContext = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT.equals(action)) {
                handleActionInit();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInit() {

        Log.d(TAG, "Init database");

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("blitzerservice");

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Intent intent = new Intent(MainActivity.ADD_WARNING);
                intent.putExtra("key", dataSnapshot.getKey());
                intent.putExtra("value", dataSnapshot.getValue(String.class));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                Intent intent = new Intent(MainActivity.ADD_WARNING);
                intent.putExtra("key", dataSnapshot.getKey());
                intent.putExtra("value", dataSnapshot.getValue(String.class));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                Intent intent = new Intent(MainActivity.REMOVE_WARNING);
                intent.putExtra("key", dataSnapshot.getKey());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        });


    }
}
