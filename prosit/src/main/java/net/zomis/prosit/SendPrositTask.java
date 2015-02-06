package net.zomis.prosit;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SendPrositTask extends AsyncTask<PrositInfo, Void, String> {

    private final Context context;

    public SendPrositTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(PrositInfo... params) {
        String url = "http://www2.zomis.net/prosit/prosit.php";
        String charset = "UTF-8";
        PrositInfo data = params[0];
        String result = "";
        HttpURLConnection httpConnection = null;
        try {
            String query = String.format("from=%s&fromName=%s&to=%s&prosit=42",
                    URLEncoder.encode(data.getIMEI(), charset),
                    URLEncoder.encode(data.getFrom(), charset),
                    URLEncoder.encode(data.getTo(), charset));
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Accept-Charset", charset);
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            httpConnection.setRequestMethod("POST");

            OutputStream output = httpConnection.getOutputStream();
            output.write(query.getBytes(charset));
            output.close();

            String contentType = httpConnection.getHeaderField("Content-Type");

            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    charset = param.split("=", 2)[1];
                    break;
                }
            }

            if (charset != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), charset));
                StringBuilder str = new StringBuilder();
                for (String line; (line = reader.readLine()) != null;) {
                    str.append(line);
                }
                reader.close();
                return str.toString();
            }
        } catch (IOException e) {
            return e + ": " + e.getMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        new AlertDialog.Builder(this.context)
                .setMessage(result)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
