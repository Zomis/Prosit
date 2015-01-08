package net.zomis.prosit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Fragment containing a simple button.
 */
public class SneezeFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences prefs;

    public SneezeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.findViewById(R.id.sneeze_button).setOnClickListener(this);
        prefs = getActivity().getSharedPreferences("prosit", Context.MODE_PRIVATE);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (!checkForName()) {
            return;
        }

        List<String> competitors = new ArrayList<String>(Arrays.asList(prefs.getString("competitors", "").split(",")));
        if (!competitors.isEmpty() && competitors.get(0).isEmpty()) {
            competitors.remove(0);
        }
        competitors.add("test");
        competitors.add("test2");
        final String[] items = new String[competitors.size() + 1];
        for (int i = 0; i < competitors.size(); i++) {
            items[i] = competitors.get(i);
        }
        items[items.length - 1] = "(Other)";

        AlertDialog builder = new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.prosit_dialog_title)
//                .setMessage(R.string.prosit_dialog_message)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendProsit(items[which]);
                    }
                })
                .show();
    }

    private boolean checkForName() {
        String name = prefs.getString("name", "");
        if (name.isEmpty()) {

            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View textEntryView = factory.inflate(R.layout.text_entry, null);
            new AlertDialog.Builder(getActivity())
//                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle("Enter your name")
                    .setView(textEntryView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            EditText name = (EditText) textEntryView.findViewById(R.id.edit_name);
                            prefs.edit().putString("name", name.getText().toString()).commit();
                            SneezeFragment.this.onClick(null);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    }).show();
            return false;
        }
        return true;
    }

    private void sendProsit(String to) {
        String url = "http://www2.zomis.net/prosit/prosit.php";
        String charset = "UTF-8";

        HttpURLConnection httpConnection = null;
        try {
            String query = String.format("from=%s&fromName=%s&to=%s&prosit=42",
                    URLEncoder.encode(getIMEI(), charset),
                    URLEncoder.encode(getMyName(), charset),
                    URLEncoder.encode(to, charset));
            httpConnection = (HttpURLConnection) new URL(url).openConnection();
            httpConnection.setDoOutput(true); // Triggers POST.
            httpConnection.setRequestProperty("Accept-Charset", charset);
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            httpConnection.setRequestMethod("POST");
//            httpConnection.connect();

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
                for (String line; (line = reader.readLine()) != null;) {
                    new AlertDialog.Builder(this.getActivity()).setMessage(line).setPositiveButton(android.R.string.ok, null).show();
/*                    new AlertDialog.Builder(getActivity())
                            .setMessage(getString(R.string.point_given, items[which]))
                            .show();*/
                }
                reader.close();
            }

        } catch (IOException e) {
            showFailure(e);
        }
    }

    private String getMyName() {
        return prefs.getString("name", "");
    }

    private String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private void showFailure(Exception e) {
        new AlertDialog.Builder(this.getActivity()).setMessage(e + ": " + e.getMessage()).setPositiveButton(android.R.string.ok, null).show();

    }
}
