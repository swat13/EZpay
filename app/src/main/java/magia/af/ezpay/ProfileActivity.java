package magia.af.ezpay;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import magia.af.ezpay.Parser.RSSFeed;

public class ProfileActivity extends BaseActivity {

  RSSFeed rssFeed;
  private String phone;
  private int position;
  private String contactName;
  private String imageUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_user);
    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    final ImageView userAvatar = (ImageView)findViewById(R.id.user_avatar);

    TextView txtUsername = (TextView)findViewById(R.id.txt_user_name);
    TextView txtPhoneNumber = (TextView)findViewById(R.id.txt_phone_number);
    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      phone = bundle.getString("phone");
      position = bundle.getInt("pos");
      Log.i("PhoneNumber", phone);
      contactName = bundle.getString("contactName");
      Log.e("ContactName", "contactName" + contactName);
      imageUrl = bundle.getString("image");
      Log.e("ImageUrl", "ImageUrl:" + imageUrl);
    }

    Glide.with(this)
      .load(imageUrl)
      .asBitmap()
      .centerCrop()
      .placeholder(R.drawable.pic_profile)
      .into(new BitmapImageViewTarget(userAvatar) {
        @Override
        protected void setResource(Bitmap resource) {
          RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
          circularBitmapDrawable.setCornerRadius(700);
          userAvatar.setImageDrawable(circularBitmapDrawable);
        }
      });
    txtUsername.setText(contactName);
    txtPhoneNumber.setText(phone);
    AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
    appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
      @Override
      public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.e("Test1", "onOffsetChange: " + verticalOffset);
        toolbar.setBackgroundColor(Color.parseColor("#b07d79"));
      }
    });
  }
}