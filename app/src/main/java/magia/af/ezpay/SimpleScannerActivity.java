package magia.af.ezpay;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import magia.af.ezpay.Parser.DOMParser;
import magia.af.ezpay.Parser.RSSFeed;
import magia.af.ezpay.Parser.RSSItem;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    RSSFeed rssFeed;
    private String id;
    private boolean commit = false;
    int pos;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Log.e("Created", "baaaar ");
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);
        // Set the scanner view as the content view

        rssFeed = (RSSFeed) getIntent().getSerializableExtra("contact");

        Log.e("Feeed", String.valueOf(rssFeed));


    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
//        rssFeed= (RSSFeed) getIntent().getSerializableExtra("contact");
    }


    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e("dddd", rawResult.getText()); // Prints scan results
        Log.e("eeeeeeeee", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        if (rawResult.getBarcodeFormat().toString().contains("QR_CODE")) {

            if (rawResult.getText().contains("/")) {

                id = rawResult.getText().substring(rawResult.getText().lastIndexOf("/") + 1);
                Log.e("id", "iiiiiiiii: " + id);
                new getAccountId().execute(id);

                // Split it.
            } else {
                throw new IllegalArgumentException("String " + rawResult.getText() + " does not contain /");
            }


        }

        // If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this);
    }


    public class getAccountId extends AsyncTask<String, Void, RSSItem> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            ((FriendListActivity) getActivity()).waitingDialog.setVisibility(View.VISIBLE);
//            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(((FriendListActivity) getActivity()).imageView);
//            Glide.with(getActivity()).load(R.drawable.gif_loading).into(imageViewTarget);

        }


        @Override
        protected RSSItem doInBackground(String... params) {

            DOMParser domParser = new DOMParser(getSharedPreferences("EZpay", 0).getString("token", ""));
            return domParser.getAccountId(params[0]);

        }

        @Override
        protected void onPostExecute(RSSItem jsons) {
            Log.e("jsons", String.valueOf(jsons));

            if (jsons != null) {
                commit = true;

                Log.e("LastFeed", String.valueOf(rssFeed));

                rssFeed.addItem(jsons);
                pos = rssFeed.getItemCount() - 1;
                finish();
            } else {
                Log.e(TAG, "onPostExecute: 1111111111");
                Toast.makeText(getApplicationContext(), "Json Is Null!", Toast.LENGTH_SHORT).show();
            }


        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commit) {
            Intent goToChatPageActivity = new Intent(this, ChatPageActivity.class);
            goToChatPageActivity.putExtra("phone", rssFeed.getItem(pos).getTelNo());
            goToChatPageActivity.putExtra("contactName", rssFeed.getItem(pos).getContactName());
            goToChatPageActivity.putExtra("image", rssFeed.getItem(pos).getContactImg());
            goToChatPageActivity.putExtra("pos", pos);
            startActivityForResult(goToChatPageActivity, 10);

        }else
            finish();

    }
}