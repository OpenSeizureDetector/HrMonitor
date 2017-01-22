package uk.org.openseizuredetector.hrmonitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by graham on 18/01/17.
 */

public class HrmUtil {
    private Context mContext;
    private final String TAG = "HrmUtil";

    public HrmUtil(Context context) {
        mContext = context;
    }

    /**
     * Start the HrMonitorService service
     */
    public void startServer() {
        // Start the server
        Log.v(TAG,"startServer()");
        Intent hrMonitorServiceIntent;
        hrMonitorServiceIntent = new Intent(mContext, HrMonitorService.class);
        hrMonitorServiceIntent.setData(Uri.parse("Start"));
        mContext.startService(hrMonitorServiceIntent);
    }

    /**
     * Stop the HrMonitorService service
     */
    public void stopServer() {
        Log.v(TAG, "stopping Server...");

        // then send an Intent to stop the service.
        Intent hrmServerIntent;
        hrmServerIntent = new Intent(mContext, HrMonitorService.class);
        hrmServerIntent.setData(Uri.parse("Stop"));
        mContext.stopService(hrmServerIntent);
    }

    /**
     * isHrMonitorServiceRunning()
     * @return the number of instances of HrMonitorService that are running
     */
    public int isHrMonitorServiceRunning() {
        int nInstances = 0;
        Class serviceClass = HrMonitorService.class;
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                nInstances++;
            }
        }
        return nInstances;
    }

}
