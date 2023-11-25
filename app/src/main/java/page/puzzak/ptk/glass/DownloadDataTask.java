// DownloadDataTask.java
package page.puzzak.ptk.glass;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class DownloadDataTask extends AsyncTask<String, Void, String> {
    private static final int UPDATE_INTERVAL = 1000; // 1000 milliseconds = 1 second

    private RemoteViews remoteViews;
    private Handler handler;
    private LiveCard mLiveCard;
    public static String formatBytes(long bytes) {
        double kilobytes = bytes / 1024.0;
        double megabytes = kilobytes / 1024.0;
        double gigabytes = megabytes / 1024.0;

        DecimalFormat format = new DecimalFormat("0.00");

        if (gigabytes >= 1.0) {
            return format.format(gigabytes) + "GB";
        } else if (megabytes >= 1.0) {
            return format.format(megabytes) + "MB";
        } else if (kilobytes >= 1.0) {
            return format.format(kilobytes) + "KB";
        } else {
            return bytes + "B";
        }
    }

    DownloadDataTask(RemoteViews remoteViews, LiveCard liveCard) {
        this.remoteViews = remoteViews;
        this.mLiveCard = liveCard;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected String doInBackground(String... urls) {
        String urlString = urls[0];
        try {
            return downloadData(urlString);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (result != null) {
                // Parse the JSON and update the UI with the fetched data
                parseAndSetData(result);
            } else {
                remoteViews.setTextViewText(R.id.data, "Error downloading data");
            }

            // Schedule the task to run every second (outside onPostExecute)
//            scheduleTask();
        } catch (Exception e) {
            remoteViews.setTextViewText(R.id.data, "Error parsing data");
            // Cancel the task if an error occurs
            cancelTask();
        }
    }

    private void scheduleTask() {
        // Use Handler for periodic updates
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Execute the task again
                new DownloadDataTask(remoteViews, mLiveCard).execute("http://192.168.88.61/AIO.php");
            }
        }, UPDATE_INTERVAL);
    }

    private void parseAndSetData(String jsonData) {
        DecimalFormat format = new DecimalFormat("0.00");
        try {
            JSONObject json = new JSONObject(jsonData);

            // Parse data from JSON
            JSONObject netspd = json.getJSONObject("netspd");
            long inSpeed = netspd.getInt("in");
            int outSpeed = netspd.getInt("out");
            remoteViews.setTextViewText(R.id.netspeed,"Speed ↓: " + formatBytes(inSpeed) + "/s" +", ↑: " + formatBytes(outSpeed) + "/s" );
            double time = json.getDouble("time");
            double ping =  (System.currentTimeMillis() - time*1000.00);
            remoteViews.setTextViewText(R.id.ping,"Ping: " + (int)ping + "ms");
            double temp = json.getDouble("temp");
            remoteViews.setTextViewText(R.id.temperature,"Temp: " + temp + "°C");
            double  util = json.getDouble("util");
            remoteViews.setTextViewText(R.id.cpuUtilPercent,"CPU: " + format.format(util) + "%");
            remoteViews.setProgressBar(R.id.cpuBar,0, (int)util, false);
            JSONObject memo = json.getJSONObject("memo");
            long totalMemo = memo.getLong("total");
            long availMemo = memo.getLong("avail");
            String ramPercent = format.format((availMemo / totalMemo) * 100);
            remoteViews.setTextViewText(R.id.memUtilDatal,"RAM: " + formatBytes(availMemo*1000) + "/" + formatBytes(totalMemo*1000) + " (" + ramPercent + "%)");
            long uptime = json.getLong("uptime");
            remoteViews.setViewVisibility(R.id.heading, View.GONE);
            remoteViews.setViewVisibility(R.id.data, View.GONE);
            remoteViews.setViewVisibility(R.id.progressBar, View.GONE);
            // Update UI with parsed data
            remoteViews.setTextViewText(R.id.data, "In Speed: " + inSpeed + "\n"
                    + "Out Speed: " + outSpeed + "\n"
                    + "Time: " + time + "\n"
                    + "Temperature: " + temp + "\n"
                    + "Utilization: " + util + "\n"
                    + "Total Memo: " + totalMemo + "\n"
                    + "Available Memo: " + availMemo + "\n"
                    + "Uptime: " + uptime);

            // Update the views using mLiveCard
            mLiveCard.setViews(remoteViews);

            // Continue scheduling the next update
            scheduleTask();
        } catch (JSONException e) {
            remoteViews.setTextViewText(R.id.data, "Error parsing data");
            // Cancel the task if an error occurs
            cancelTask();
        }
    }

    private String downloadData(String urlString) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the connection
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);

            // Connect to the server
            connection.connect();

            // Get the input stream
            inputStream = connection.getInputStream();

            // Read the data
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void cancelTask() {
        // Remove any existing callbacks to prevent multiple executions
        handler.removeCallbacksAndMessages(null);
        // Cancel the AsyncTask
        cancel(true);
    }
}
