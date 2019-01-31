package dpm.location.tracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;


public class JobServiceDemoActivity extends AppCompatActivity {
    private static final String TAG = JobServiceDemoActivity.class.getSimpleName();


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";

    private TextView locationMsg;

    // as google doc says
    // Handler for incoming messages from the service.
    private IncomingMessageHandler mHandler;

    /***
     * @deprecated Detect has location??
     * @param context
     * @return
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    private boolean isFromSetting = false;
    TimePicker myTimePicker;
    Button buttonstartSetDialog;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;

    final static int RQS_1 = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_job_service_demo);
        locationMsg = findViewById(R.id.location);
        mHandler = new IncomingMessageHandler();
        requestPermissions();
        progressDialog = new ProgressDialog(JobServiceDemoActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");
        scheduleTask();
    }

    private void scheduleTask() {
        int timeInSec = 5;
        Intent intent = new Intent(this, TimeZoneBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (timeInSec * 1000), pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+ (timeInSec*1000),3*60*1000,pendingIntent);
//        Toast.makeText(this, "Alarm set to after " +"" + " seconds",Toast.LENGTH_LONG).show();
    }

    ProgressDialog progressDialog;

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onRequestPermissionsResult: 90-=>");
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
                if (isLocationEnabled(JobServiceDemoActivity.this)) {
                    Intent startServiceIntent = new Intent(this, LocationUpdatesService.class);
                    Messenger messengerIncoming = new Messenger(mHandler);
                    startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
                    startService(startServiceIntent);
                }else if (!isLocationEnabled(JobServiceDemoActivity.this)){
                    showAlertDialogButtonClicked();
                }
            } else {
                // Permission denied.
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

    class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            switch (msg.what) {
                case LocationUpdatesService.LOCATION_MESSAGE:
                    Location obj = (Location) msg.obj;
                    Log.e(TAG, "handleMessage: -=>(lat)->" + ((Location) msg.obj).getLatitude()
                            + "\n(lng)->" + ((Location) msg.obj).getLongitude());
                    locationMsg.setText("LAT :  " + obj.getLatitude() + "\nLNG : " + obj.getLongitude());
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    break;
            }
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            // Request permission
            ActivityCompat.requestPermissions(JobServiceDemoActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(JobServiceDemoActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFromSetting == true) {
            mHandler = null;
            finish();
            startActivity(getIntent());
            isFromSetting = false;
        }
    }

    public void showAlertDialogButtonClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an animal");
        String[] animals = {"Hanuman", "Sunny Deol"};
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // horse
                        dialog.dismiss();
                        isFromSetting = true;
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        break;

                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
