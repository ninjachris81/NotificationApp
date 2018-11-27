package notificationapp.de.notificationapp.notificationapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        DatabaseReference myRef = database.getReference("blitzerservice_data");

        myRef.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:" + dataSnapshot.toString());
                Log.d(TAG, "onDataChange:" + dataSnapshot.getChildrenCount());

                final Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();

                final ArrayList<String> warningsList = new ArrayList<String>();

                while (it.hasNext()) {
                    DataSnapshot ds = it.next();
                    warningsList.add(ds.getValue().toString());
                }

                Intent intent = new Intent(MainActivity.DATA_CHANGED);
                intent.putStringArrayListExtra("values", warningsList);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("blitzerservice_status");

        myRef2.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:" + dataSnapshot.toString());
                Log.d(TAG, "onDataChange:" + dataSnapshot.getChildrenCount());

                final Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();

                Intent intent = new Intent(MainActivity.STATUS_CHANGED);

                while (it.hasNext()) {
                    DataSnapshot ds = it.next();
                    intent.putExtra(ds.getKey(), Long.parseLong(ds.getValue(String.class)));
                }

                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        });

    }
}
