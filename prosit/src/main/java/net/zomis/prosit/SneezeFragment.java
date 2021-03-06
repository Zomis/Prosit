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

    private static final String KEY_COMPETITORS = "competitors";

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

        List<String> competitors = new ArrayList<String>(Arrays.asList(prefs.getString(KEY_COMPETITORS, "").split(",")));
        if (!competitors.isEmpty() && competitors.get(0).isEmpty()) {
            competitors.remove(0);
        }
        final String[] items = new String[competitors.size() + 1];
        for (int i = 0; i < competitors.size(); i++) {
            items[i] = competitors.get(i);
        }
        items[items.length - 1] = "(Other)";

        new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.prosit_dialog_title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != items.length - 1) {
                            sendProsit(items[which]);
                            return;
                        }

                        LayoutInflater factory = LayoutInflater.from(getActivity());
                        final View textEntryView = factory.inflate(R.layout.text_entry, null);
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Who should get the point?")
                                .setView(textEntryView)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        EditText name = (EditText) textEntryView.findViewById(R.id.edit_name);
                                        addReceipient(name.getText().toString());
                                        sendProsit(name.getText().toString());
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null).show();
                    }
                })
                .show();
    }

    private void addReceipient(String name) {
        String competitors = prefs.getString(KEY_COMPETITORS, "");
        if (competitors.isEmpty()) {
            prefs.edit().putString(KEY_COMPETITORS, name).commit();
        }
        else {
            prefs.edit().putString(KEY_COMPETITORS, competitors + "," + name).commit();
        }
    }

    private boolean checkForName() {
        String name = prefs.getString("name", "");
        if (name.isEmpty()) {

            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View textEntryView = factory.inflate(R.layout.text_entry, null);
            new AlertDialog.Builder(getActivity())
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
        SendPrositTask task = new SendPrositTask(this.getActivity());
        PrositInfo info = new PrositInfo(getMyName(), getIMEI(), to);
        task.execute(info);
    }

    private String getMyName() {
        return prefs.getString("name", "");
    }

    private String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

}
