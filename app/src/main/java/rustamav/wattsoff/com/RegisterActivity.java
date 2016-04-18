package rustamav.wattsoff.com;

/**
 * Created by Rustam on 3/6/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends Activity {
    EditText email, password, firstname, lastname, city, street;
    Button login, register;
    String emailtxt, passwordtxt, firstnametxt, lastnametxt, citytxt, streettxt;
    List<NameValuePair> jsonparams;
    String serverName;

    GoogleCloudMessaging gcm;
    String regid;
    String PROJECT_NUMBER = "695309083645";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        serverName = getString(R.string.serverName);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        firstname = (EditText) findViewById(R.id.firstname);
        lastname = (EditText) findViewById(R.id.lastname);
        city = (EditText) findViewById(R.id.city);
        street = (EditText) findViewById(R.id.street);
        register = (Button) findViewById(R.id.registerbtn);
        login = (Button) findViewById(R.id.login);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regactivity = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(regactivity);
                finish();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                emailtxt = email.getText().toString();
                passwordtxt = password.getText().toString();
                firstnametxt = firstname.getText().toString();
                lastnametxt = lastname.getText().toString();
                citytxt = city.getText().toString();
                streettxt = street.getText().toString();
                jsonparams = new ArrayList<NameValuePair>();
                getRegId();


            }
        });
    }

    public void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);

                    msg = "Device registered, registration ID=" + regid;

                    Log.i("GCM", msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //etRegId.setText(msg + "\n");
                jsonparams.add(new BasicNameValuePair("email", emailtxt));
                jsonparams.add(new BasicNameValuePair("password", passwordtxt));
                jsonparams.add(new BasicNameValuePair("firstname", firstnametxt));
                jsonparams.add(new BasicNameValuePair("lastname", lastnametxt));

                jsonparams.add(new BasicNameValuePair("street", streettxt));
                jsonparams.add(new BasicNameValuePair("regid", regid)); // not sure if its a good prace
                jsonparams.add(new BasicNameValuePair("city", citytxt));
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSON(serverName + "register", jsonparams);
                //JSONObject json = sr.getJSON("http://192.168.56.1:8080/register",params);

                if (json != null) {
                    try {
                        String jsonstr = json.getString("response");

                        Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();

                        Log.d("Hello", jsonstr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(null, null, null);
    }

}