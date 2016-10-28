package magia.af.ezpay.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import magia.af.ezpay.Parser.DOMParser;
import magia.af.ezpay.Parser.JSONParser;
import magia.af.ezpay.R;

/**
 * Created by Saeid Yazdany on 10/26/2016.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {
  private ImageButton btn_done;
  private String phone;
  private EditText edtInputPhoneNumber;

  public static LoginFragment getInstance() {
    return new LoginFragment();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_login, container, false);
    btn_done = (ImageButton) rootView.findViewById(R.id.btn_done);
    btn_done.setOnClickListener(this);
    edtInputPhoneNumber = (EditText) rootView.findViewById(R.id.edt_input_phone_number);
    edtInputPhoneNumber = (EditText)rootView.findViewById(R.id.edt_input_phone_number);

    edtInputPhoneNumber.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        if (edtInputPhoneNumber.getText().length() == 11) {
          hideKey(edtInputPhoneNumber);
        }
              if (edtInputPhoneNumber.getText().length()==11) {
                  btn_done.setVisibility(View.VISIBLE);
                  hideKey(edtInputPhoneNumber);
              }
              else
                  btn_done.setVisibility(View.GONE);

      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });

    return rootView;
  }

  public void hideKey(View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }


  @Override
  public void onClick(View v) {
    //Handle All View Click Here
    switch (v.getId()) {
      case R.id.btn_done:
        phone = edtInputPhoneNumber.getText().toString();
        new registration().execute(edtInputPhoneNumber.getText().toString());
        Toast.makeText(getActivity(), edtInputPhoneNumber.getText().toString(), Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private class registration extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
      JSONParser parser = new JSONParser();
      List<NameValuePair> pairs = new ArrayList<>();
      pairs.add(new BasicNameValuePair("mobile","09036004960"));
      JSONObject object = parser.makeHttpRequest("http://new.opaybot.ir/api/Account/RegisterByMobile","POST",pairs);
      try {
        JSONObject obj = object.put("mobile","09036004960");
        Log.i("TEST PARS" , obj.getString("mobile"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      DOMParser domParser = new DOMParser();
      return domParser.register(params[0]);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
//      getActivity().findViewById(R.id.wait_layout).setVisibility(View.GONE);
      Log.e("^^^^^^^^^", "onPostExecute: " + result);
      if (result) {
        Toast.makeText(getActivity(), "Test", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString("number", phone);
        Log.i("Input phone" , phone);
        ActivationCodeFragment activationCodeFragment = ActivationCodeFragment.getInstance();
        activationCodeFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.container, activationCodeFragment)
          .commit();


      } else {
        Toast.makeText(getActivity(), "مشکل در برقراری ارتباط!", Toast.LENGTH_SHORT).show();
      }
    }
  }


}
