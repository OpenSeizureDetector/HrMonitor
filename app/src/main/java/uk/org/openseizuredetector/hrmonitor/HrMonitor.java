package uk.org.openseizuredetector.hrmonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

//MPAndroidChart
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


public class HrMonitor extends Activity {

    private static String TAG = "HrMonitor";
    private HrmUtil mUtil = new HrmUtil(this);
    private String mHrmAddrStr = "not defined";
    private Timer mUiTimer;
    private HrmDb mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_monitor);
        mDb = new HrmDb(this);
        Button b;
        // Button click handler for scan button
        b = (Button) findViewById(R.id.scan_ble_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "scan_ble_button");
                try {
                    Intent prefsIntent = new Intent(
                            HrMonitor.this,
                            HrMonitorScanner.class);
                    startActivity(prefsIntent);
                } catch (Exception ex) {
                    Log.v(TAG, "exception starting HR Monitor Scanner activity " + ex.toString());
                }
            }
        });

        // button click handler for start server button
        b = (Button) findViewById(R.id.start_server_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "start_server_button");
                mUtil.startServer();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG,"onStart()");
        mUtil.startServer();
        SharedPreferences SP = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        mHrmAddrStr = SP.getString("HrmAddr", "10");
        Log.v(TAG, "onStart() - mHrmAddrStr = " + mHrmAddrStr);
        mUiTimer = new Timer();
        mUiTimer.schedule(new UpdateUITask(), 0, 1000);
        updateUi();
    }

    private void updateUi() {
        TextView tv = (TextView)findViewById(R.id.hrm_addr_tv);
        tv.setText(mHrmAddrStr);
    }

    class UpdateUITask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv;
                    tv = (TextView) findViewById(R.id.hrm_addr_tv);
                    tv.setText(mHrmAddrStr);

                    int hr = -1;
                    long dateInt = -1;
                    if (mDb != null) {
                        Cursor rows = mDb.getLatestReadings(60);  // get 60 readings (about 1 minute)
                        rows.moveToFirst();
                        hr = rows.getInt(2);
                        tv = (TextView) findViewById(R.id.heart_rate_tv);
                        tv.setText(hr + " bpm");

                        dateInt = rows.getLong(1);
                        tv = (TextView) findViewById(R.id.heart_time_tv);
                        tv.setText(getDate(dateInt,"dd/MM/yyyy HH:mm:ss"));

                        plotChart(rows);
                    } else {
                        Log.v(TAG, "mDb is null");
                    }
                }
            });
        }
    }

    /**
     * plotChart - plots a scatter chart of heart rate data from the database cursor object rows.
     * @param rows
     */
    private void plotChart(Cursor rows) {
        Log.v(TAG,"plotChart() - nRows = "+rows.getCount());
        rows.moveToFirst();
        long lastMillis = rows.getLong(1);  // Get number of ms of last record.
        ArrayList<Entry> entries = new ArrayList();
        rows.moveToLast();
        while (!rows.isBeforeFirst()) {
            long timeSec = (rows.getLong(1)-lastMillis)/1000;
            int hr = rows.getInt(2);
            entries.add(new Entry(timeSec,hr));
            //rows.moveToNext();
            rows.moveToPrevious();
        }
        ScatterChart scatterChart = (ScatterChart)findViewById(R.id.chart1);
        ScatterDataSet dataset = new ScatterDataSet(entries, "HR (bpm)");
        ScatterData data = new ScatterData(dataset);
        scatterChart.setData(data);
        scatterChart.setTouchEnabled(false);
        YAxis yAxis = scatterChart.getAxisLeft();
        yAxis.setAxisMinimum(0F);
        yAxis.setAxisMaximum(220F);
        data.setDrawValues(false);



        scatterChart.invalidate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG,"onStop()");
        mUiTimer.cancel();
        //mUtil.stopServer();
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}