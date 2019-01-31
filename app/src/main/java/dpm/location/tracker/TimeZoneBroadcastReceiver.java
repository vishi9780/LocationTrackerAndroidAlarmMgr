package dpm.location.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class TimeZoneBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG=TimeZoneBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: 12-=>>" );
        Toast.makeText(context, "received", Toast.LENGTH_SHORT).show();
    }
}
