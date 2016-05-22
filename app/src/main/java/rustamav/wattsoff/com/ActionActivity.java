package rustamav.wattsoff.com;


/**
 * Created by Rustam on 3/6/2016.
 */

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class ActionActivity extends AppCompatActivity {


    SharedPreferences pref;
    String token, grav, oldpasstxt, newpasstxt, userLogged, userEmail, totalContribution, totalRequiredAmount, applicationState;
    WebView web;
    TextView tvMain, tvProgressPercent, tvRequiredAmaunt, tvTimer, tvTimeLeftToAct;
    boolean timerOn = false, timerStarted = false;
    long startTime, endTime;
    Button chgpass, chgpassfr, cancel, logout, btnSubmit, action50W, btnNotHome;
    LinearLayout llLightBulb, llOven, llBoiler, llOther, llNotHome;
    CheckBox chkBoxLightBulb, chkBoxOven, chkBoxBoiler, chkBoxOther, chkBoxNotHome;
    int storedChoicePower;
    Dialog dlg;
    EditText oldpass, newpass;
    List<NameValuePair> params;
    String serverName;
    int i = 0;
    int animationDuration = 2500; // 2500ms = 2,5s

    Timer timer;

    CircularProgressBar circularProgressBar;

    private void progressThread() {

        new Thread() {
            public void run() {
                while (!interrupted()) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String state = pref.getString("state", "");
                                updateUI();
                                if (state.equalsIgnoreCase("actionStarted") || state.equalsIgnoreCase("acted")) {
                                    controlTimer(true);
//                                    if (state.equalsIgnoreCase("acted")) {
//                                        tvTimer.setText(" ");
//                                        tvTimeLeftToAct.setText(" ");
//                                    }
                                    totalContribution = pref.getString("totalAmountContributed", "");
                                    if (totalContribution != null && !totalContribution.isEmpty()) {
                                        if (totalRequiredAmount == null || (totalRequiredAmount != null && totalRequiredAmount.isEmpty()))
                                            totalRequiredAmount = "1";
                                        float result = Float.valueOf(totalContribution) / Float.valueOf(totalRequiredAmount) * 100;
                                        tvProgressPercent.setText(String.valueOf(new DecimalFormat("#.##").format(result)) + "%");
                                        circularProgressBar.setProgressWithAnimation(result, animationDuration); // Default duration = 1500ms
//                                        donutProgress.setProgress((int) result);

                                        //Toast.makeText(getApplication(), String.valueOf(new DecimalFormat("#.##").format(result)) + "%", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (state.equalsIgnoreCase("actionEnded")) {
                                    tvProgressPercent.setText(" ");
                                    tvTimer.setText(" ");
                                    circularProgressBar.setProgressWithAnimation(0, animationDuration);
                                    controlTimer(false);
                                }

                            }
                        });
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        serverName = getString(R.string.serverName);
        super.onCreate(savedInstanceState);



        pref = getSharedPreferences("AppPref", MODE_PRIVATE);


        token = pref.getString("token", "");
        grav = pref.getString("grav", "");
        userLogged = pref.getString("userLogged", "");
        userEmail = pref.getString("userEmail", "");
        if (pref.contains("state")) {
            applicationState = pref.getString("state", "");
        } else {
            applicationState = "actionEnded";
            pref.edit().putString("state", "actionEnded");
        }


        if (!userLogged.equalsIgnoreCase("user1")) {
            Intent loginactivity = new Intent(ActionActivity.this, LoginActivity.class);

            startActivity(loginactivity);
            finish();

        }


        setContentView(R.layout.activity_action);
        //web = (WebView) findViewById(R.id.webView);
        chgpass = (Button) findViewById(R.id.chgbtn);
        logout = (Button) findViewById(R.id.logout);
        tvMain = (TextView) findViewById(R.id.textViewMain);

        tvTimer = (TextView) findViewById(R.id.tvTimer);
        tvTimeLeftToAct = (TextView) findViewById(R.id.tvTimeLeftToAct);

        tvProgressPercent = (TextView) findViewById(R.id.tvProgressPercent);
        //tvRequiredAmaunt = (TextView) findViewById(R.id.tvRequiredAmount);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnNotHome = (Button) findViewById(R.id.btnNotHome);
        //action50W = (Button) findViewById(R.id.button50W);

        llOven = (LinearLayout) findViewById(R.id.llOven);
        chkBoxOven = (CheckBox) findViewById(R.id.chkBoxOven);

        llBoiler = (LinearLayout) findViewById(R.id.llBoiler);
        chkBoxBoiler = (CheckBox) findViewById(R.id.chkBoxBoiler);

//        llOther = (LinearLayout) findViewById(R.id.llOther);
//        chkBoxOther = (CheckBox) findViewById(R.id.chkBoxOther);

        llLightBulb = (LinearLayout) findViewById(R.id.llLightBulb);
        chkBoxLightBulb = (CheckBox) findViewById(R.id.chkBoxLightBulb);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);


        if (!pref.contains("state")) {
            if (btnNotHome.getVisibility() == View.VISIBLE)
                btnNotHome.setVisibility(View.INVISIBLE);
            if (btnSubmit.getVisibility() == View.VISIBLE) btnSubmit.setVisibility(View.INVISIBLE);
            pref.edit().putString("state", "actionEnded");
        }


        tvProgressPercent.setText(" ");
        tvTimeLeftToAct.setText(" ");
        tvTimer.setText(" ");

        updateUI();
        timer = new Timer();


        circularProgressBar = (CircularProgressBar) findViewById(R.id.donut_progress);



        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = pref.edit();
                //Storing Data using SharedPreferences
                edit.putString("token", "");
                edit.remove("userLogged");
                edit.commit();
                Intent loginactivity = new Intent(ActionActivity.this, LoginActivity.class);

                startActivity(loginactivity);
                finish();
            }
        });

        llLightBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chkBoxLightBulb.isChecked()) {
                    chkBoxLightBulb.setChecked(true);
                } else {
                    chkBoxLightBulb.setChecked(false);
                }
            }
        });

        llOven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chkBoxOven.isChecked()) {
                    chkBoxOven.setChecked(true);
                } else {
                    chkBoxOven.setChecked(false);
                }
            }
        });
        llBoiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chkBoxBoiler.isChecked()) {
                    chkBoxBoiler.setChecked(true);
                } else {
                    chkBoxBoiler.setChecked(false);
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int totalPower = 0;

                if (chkBoxOven.isChecked()) totalPower += 20;
                if (chkBoxLightBulb.isChecked()) totalPower += 35;
                if (chkBoxBoiler.isChecked()) totalPower += 130;

                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("action", String.valueOf(totalPower)));
                params.add(new BasicNameValuePair("userEmail", userEmail));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON(serverName + "api/action", params);

                if (json != null) {
                    try {
                        String jsonstr = json.getString("response");
                        if (json.getBoolean("res")) {
                            Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();
                            totalContribution = jsonstr;

                        } else {
                            Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                if (btnSubmit.isEnabled()) btnSubmit.setEnabled(false);
                if (btnSubmit.getVisibility() == View.VISIBLE) btnSubmit.setVisibility(View.GONE);
                if (btnNotHome.isEnabled()) btnNotHome.setEnabled(false);
                if (btnNotHome.getVisibility() == View.VISIBLE) btnNotHome.setVisibility(View.GONE);
//                if (action50W.isEnabled()) action50W.setEnabled(false);
//                if (action50W.getVisibility() == View.VISIBLE) action50W.setVisibility(View.GONE);

                tvMain.setText("Thank you. Penguins feel safer now!");
                pref.edit().putString("state", "acted").commit();


            }
        });

        btnNotHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("action", "0"));
                params.add(new BasicNameValuePair("userEmail", userEmail));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON(serverName + "api/action", params);

                if (json != null) {
                    try {
                        String jsonstr = json.getString("response");
                        if (json.getBoolean("res")) {
                            Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();
                            totalContribution = jsonstr;
                        } else {
                            Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                if (btnSubmit.isEnabled()) btnSubmit.setEnabled(false);
                if (btnSubmit.getVisibility() == View.VISIBLE) btnSubmit.setVisibility(View.GONE);
                if (btnNotHome.isEnabled()) btnNotHome.setEnabled(false);
                if (btnNotHome.getVisibility() == View.VISIBLE) btnNotHome.setVisibility(View.GONE);
//                if (action50W.isEnabled()) action50W.setEnabled(false);
//                if (action50W.getVisibility() == View.VISIBLE) action50W.setVisibility(View.GONE);

                tvMain.setText("Thank you. Penguins feel safer now!");
                pref.edit().putString("state", "acted").commit();

            }
        });

        // web.getSettings().setUseWideViewPort(true);
        //web.getSettings().setLoadWithOverviewMode(true);
        //web.loadUrl(grav);

//        chgpass.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dlg = new Dialog(ActionActivity.this);
//                dlg.setContentView(R.layout.chgpassword_frag);
//                dlg.setTitle("Change Password");
//                chgpassfr = (Button) dlg.findViewById(R.id.chgbtn);
//
//                chgpassfr.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        oldpass = (EditText) dlg.findViewById(R.id.oldpass);
//                        newpass = (EditText) dlg.findViewById(R.id.newpass);
//                        oldpasstxt = oldpass.getText().toString();
//                        newpasstxt = newpass.getText().toString();
//                        params = new ArrayList<NameValuePair>();
//                        params.add(new BasicNameValuePair("oldpass", oldpasstxt));
//                        params.add(new BasicNameValuePair("newpass", newpasstxt));
//                        params.add(new BasicNameValuePair("id", token));
//                        ServerRequest sr = new ServerRequest();
//                        //    JSONObject json = sr.getJSON("http://192.168.56.1:8080/api/chgpass",params);
//                        JSONObject json = sr.getJSON(serverName + "api/chgpass", params);
//                        if (json != null) {
//                            try {
//                                String jsonstr = json.getString("response");
//                                if (json.getBoolean("res")) {
//
//                                    dlg.dismiss();
//                                    Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_SHORT).show();
//
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//                });
//                cancel = (Button) dlg.findViewById(R.id.cancelbtn);
//                cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dlg.dismiss();
//                    }
//                });
//                dlg.show();
//            }
//        });
        setSupportActionBar(myToolbar);
        progressThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pref.contains("state")) {
            if (btnNotHome.getVisibility() == View.VISIBLE)
                btnNotHome.setVisibility(View.INVISIBLE);
            if (btnSubmit.getVisibility() == View.VISIBLE) btnSubmit.setVisibility(View.INVISIBLE);
            pref.edit().putString("state", "actionEnded");
        }
        updateUI();
    }

    void updateUI() {
        String gcmMessage = pref.getString("gcmmessage", "");
        tvMain.setText("Penguins are safe!");

        String state = pref.getString("state", "");

//        if (bundle != null) {
//            gcmMessage = bundle.getString("gcmmessage");
//        }
//        if (gcmMessage != null && !gcmMessage.isEmpty()) {

        if (state.equalsIgnoreCase("actionStarted")) {
            //totalRequiredAmount = bundle.getString("gcmrequiredamount");
            totalRequiredAmount = pref.getString("requiredAmount", "");
            tvMain.setText(gcmMessage);
            //tvRequiredAmaunt.setText(totalRequiredAmount);
            if (!btnSubmit.isEnabled()) btnSubmit.setEnabled(true);
            if (btnSubmit.getVisibility() != View.VISIBLE)
                btnSubmit.setVisibility(View.VISIBLE);
            if (!btnNotHome.isEnabled()) btnNotHome.setEnabled(true);
            if (btnNotHome.getVisibility() != View.VISIBLE)
                btnNotHome.setVisibility(View.VISIBLE);
//            if (!action50W.isEnabled()) action50W.setEnabled(true);
//            if (action50W.getVisibility() != View.VISIBLE)
//                action50W.setVisibility(View.VISIBLE);
            //bundle.remove("gcmmessage");
            // }
        } else if (state.equalsIgnoreCase("actionEnded")) {
            tvMain.setText("Penguins are safe!");
            if (btnSubmit.isEnabled()) btnSubmit.setEnabled(false);
            if (btnSubmit.getVisibility() == View.VISIBLE) btnSubmit.setVisibility(View.GONE);
            if (btnNotHome.isEnabled()) btnNotHome.setEnabled(false);
            if (btnNotHome.getVisibility() == View.VISIBLE) btnNotHome.setVisibility(View.GONE);
            //tvRequiredAmaunt.setText("");
//            if (action50W.isEnabled()) action50W.setEnabled(false);
////            if (action50W.getVisibility() == View.VISIBLE) action50W.setVisibility(View.GONE);
        } else if (state.equalsIgnoreCase("acted")) {
            tvMain.setText("Penguins appreciate your help!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_logout:
                // User chose the "Settings" item, show the app settings UI...
                SharedPreferences.Editor edit = pref.edit();
                //Storing Data using SharedPreferences
                edit.putString("token", "");
                edit.remove("userLogged");
                edit.commit();
                Intent loginactivity = new Intent(ActionActivity.this, LoginActivity.class);

                startActivity(loginactivity);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    public void controlTimer(boolean timerOn) {
        if (timerOn) {
            if (!timerStarted) {
                startTime = System.currentTimeMillis();
                long timeLength = Long.valueOf(pref.getString("timeToAct", ""));
                endTime = startTime + timeLength * 60000; // Minutes * 60 milliseconds
                timerStarted = true;
                tvTimeLeftToAct.setText(R.string.TimeLeftToAct);
            } else {

            }
            long millis = System.currentTimeMillis() - startTime;
            long millisLeft = endTime - System.currentTimeMillis();
            int seconds = (int) (millis / 1000);
            int secondsLeft = (int) (millisLeft / 1000);
            int minutesLeft = secondsLeft / 60;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            secondsLeft = secondsLeft % 60;

            tvTimer.setText(String.format("%02d:%02d", minutesLeft, secondsLeft));
        } else {
            timerStarted = false;
            tvTimer.setText(" ");
            tvTimeLeftToAct.setText(" ");
        }
    }

}