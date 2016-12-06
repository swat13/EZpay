package magia.af.ezpay;

import android.app.KeyguardManager;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import magia.af.ezpay.Parser.DOMParser;
import magia.af.ezpay.Parser.RSSFeed;
import magia.af.ezpay.helper.ContactDatabase;
import magia.af.ezpay.helper.GetContact;

public class Splash extends BaseActivity {

    ContactDatabase database;
    JSONArray jsonArray;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if (!keyguardManager.isKeyguardSecure()) {

            Toast.makeText(this,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {

            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Register at least one fingerprint in Settings",
                    Toast.LENGTH_LONG).show();
        }


        if (!getSharedPreferences("EZpay", 0).getString("token", "").isEmpty()) {
            new ComparingContactWithDatabase().execute();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }
            }, 2500);
        }

    }


    public String newContact(JSONArray jsonArray) {
        return jsonArray.toString();
    }

    public class ComparingContactWithDatabase extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... params) {
            database = new ContactDatabase(Splash.this);
            GetContact getContact = new GetContact();

            RSSFeed databaseContact = database.getAllData();
            RSSFeed phoneContact = getContact.getNewContact(Splash.this);
            for (int i = 0; i < phoneContact.getItemCount(); i++) {
//        Log.e("(((", "doInBackground i: " + i);
                for (int j = 0; j < databaseContact.getItemCount(); j++) {
//          Log.e("(((", "doInBackground j: " + j);
                    if (phoneContact.getItem(i).getTelNo().equals(databaseContact.getItem(j).getTelNo())
                            && phoneContact.getItem(i).getContactName().equals(databaseContact.getItem(j).getContactName())) {
                        phoneContact.removeItem(i);
                        break;
                    }
                }
            }
            jsonArray = new JSONArray();
            for (int i = 0; i < phoneContact.getItemCount()
                    ; i++) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("m", phoneContact.getItem(i).getTelNo());
                    jsonObject.put("t", phoneContact.getItem(i).getContactName());
                    jsonArray.put(i, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                database.createData(phoneContact.getItem(i).getTelNo(), phoneContact.getItem(i).getContactName());
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            if (jsonArray != null) {
                new fillContact().execute(newContact(jsonArray));
            }
            super.onPostExecute(jsonArray);
        }
    }

    private class fillContact extends AsyncTask<String, Void, RSSFeed> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected RSSFeed doInBackground(String... params) {
            DOMParser domParser = new DOMParser(getSharedPreferences("EZpay", 0).getString("token", ""));
            return domParser.checkContactListWithGroup(params[0]);
        }

        @Override
        protected void onPostExecute(RSSFeed result) {
            if (result != null) {
                startActivity(new Intent(Splash.this, MainActivity.class).putExtra("contact", result));
                finish();
            } else {
                Toast.makeText(Splash.this, "problem in connection!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

    }

}
