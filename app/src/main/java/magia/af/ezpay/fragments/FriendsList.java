package magia.af.ezpay.Fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import magia.af.ezpay.ChatPageActivity;
import magia.af.ezpay.ChooseMemberActivity;
import magia.af.ezpay.Firebase.MessagingService;
import magia.af.ezpay.GroupChatPageActivity;
import magia.af.ezpay.MainActivity;
import magia.af.ezpay.Parser.ChatListFeed;
import magia.af.ezpay.Parser.ChatListItem;
import magia.af.ezpay.Parser.GroupItem;
import magia.af.ezpay.Parser.PayLogItem;
import magia.af.ezpay.Parser.MembersFeed;
import magia.af.ezpay.Parser.Parser;
import magia.af.ezpay.R;
import magia.af.ezpay.helper.ContactDatabase;
import magia.af.ezpay.helper.GetContact;
import magia.af.ezpay.interfaces.MessageHandler;
import magia.af.ezpay.interfaces.OnClickHandler;

/**
 * Created by erfan on 11/3/2016.
 */

public class FriendsList extends Fragment implements OnClickHandler, MessageHandler {

    static ChatListFeed _ChatList_feed;
    static GroupItem groups;
    static MembersFeed membersFeed;
    ArrayList<ChatListItem> contacts;
    RecyclerView recBills;
    FriendsList.ListAdapter adapter;
    public String comment;
    public int amount;
    public int position;
    ContactDatabase database;
    JSONArray jsonArray;
    static Handler handler = new Handler();
    public static MessageHandler mHandler;

    public static FriendsList getInstance(ChatListFeed chatListFeed) {
        _ChatList_feed = chatListFeed;
        return new FriendsList();
    }

    public static MessageHandler informNotif() {
        return mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MessagingService.mode = 3;
        mHandler = this;
        View v = inflater.inflate(R.layout.activity_friend_list, container, false);
        ((MainActivity) getActivity()).fragment_status = 2;
        recBills = (RecyclerView) v.findViewById(R.id.contact_recycler);
//        _ChatList_feed = (ChatListFeed) getActivity().getIntent().getSerializableExtra("contact");
        for (int i = 0; i < _ChatList_feed.getItemCount(); i++) {
            if (_ChatList_feed.getItem(i).getGroupItem() != null) {
                groups = _ChatList_feed.getItem(i).getGroupItem();
            } else {
                contacts = _ChatList_feed.getItem(i).getContactMembers();
            }


        }

        new getAccount().execute();

        CardView inviteFriends = (CardView) v.findViewById(R.id.invite_friends);
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseMemberActivity.class);
                intent.putExtra("contact", contacts);
                intent.putExtra("contact2", _ChatList_feed);
                getActivity().startActivity(intent);
            }
        });
        Bundle bundle = getArguments();
        if (bundle != null) {
            comment = getArguments().getString("description");
            amount = getArguments().getInt("amount");
            position = getArguments().getInt("pos");
            if (comment.length() > 0 && amount > 0) {
                _ChatList_feed.getItem(position).setComment(comment);
                _ChatList_feed.getItem(position).setLastChatAmount(amount);
            }
        }
        if (_ChatList_feed != null && _ChatList_feed.getItemCount() != 0) {
            adapter = new FriendsList.ListAdapter(this);
            recBills.setAdapter(adapter);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recBills.setNestedScrollingEnabled(true);
            recBills.setLayoutManager(llm);
            adapter.notifyDataSetChanged();
        }

        return v;
    }

    @Override
    public void onClick(ChatListItem chatListItem) {

        if (chatListItem.getGroupItem() != null) {
            Intent goToChatPageActivity = new Intent(getActivity(), GroupChatPageActivity.class);
            GroupItem groupItem = chatListItem.getGroupItem();
            MembersFeed membersFeed = groupItem.getMembersFeed();
            goToChatPageActivity.putExtra("title", groupItem.getGroupTitle());
            goToChatPageActivity.putExtra("photo", "http://new.opaybot.ir" + groupItem.getGroupPhoto());
            goToChatPageActivity.putExtra("id", groupItem.getGroupId());
            goToChatPageActivity.putExtra("members", membersFeed);
            goToChatPageActivity.putExtra("contact", _ChatList_feed);
            startActivityForResult(goToChatPageActivity, 10);
        } else {
            Intent goToChatPageActivity = new Intent(getActivity(), ChatPageActivity.class);
            goToChatPageActivity.putExtra("phone", chatListItem.getTelNo());
            goToChatPageActivity.putExtra("contactName", chatListItem.getTitle());
            goToChatPageActivity.putExtra("image", chatListItem.getContactImg());
            goToChatPageActivity.putExtra("pos", chatListItem.getPosition());
            goToChatPageActivity.putExtra("date", chatListItem.getLastChatDate());
            goToChatPageActivity.putExtra("contact", _ChatList_feed);
            startActivityForResult(goToChatPageActivity, 10);
        }
    }

    @Override
    public void handleMessage(final PayLogItem payLogItem, final boolean deleteState, final String chatMemberMobile) {

        Log.e("handleMessage", "In Pv");
        new fillContact().execute("[]");

    }

    @Override
    public void handleMessageGp(final String body, final String chatMemberMobile) {


        Log.e("handleMessage", "In Gp");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new fillContact().execute("[]");



            }
        });


    }

    public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView contactName;
        TextView description;
        TextView pay;
        ImageView contactImage;
        ImageView contactStat;
        OnClickHandler onClickHandler;

        public FeedViewHolder(View v, OnClickHandler onClickHandler) {
            super(v);
            contactName = (TextView) v.findViewById(R.id.txt_contact_item_name);
            description = (TextView) v.findViewById(R.id.txt_contact_item_description);
            pay = (TextView) v.findViewById(R.id.txt_contact_item_pay);
            contactImage = (ImageView) v.findViewById(R.id.contact_img);
            contactStat = (ImageView) v.findViewById(R.id.status_circle);
            v.setOnClickListener(this);
            this.onClickHandler = onClickHandler;
        }

        @Override
        public void onClick(View v) {
            ChatListItem chatListItem = _ChatList_feed.getItem(getAdapterPosition());
            onClickHandler.onClick(chatListItem);
        }
    }

    public class ListAdapter extends RecyclerView.Adapter<FriendsList.FeedViewHolder> {
        OnClickHandler onClickHandler;

        public ListAdapter(OnClickHandler onClickHandler) {
            this.onClickHandler = onClickHandler;
        }

        @Override
        public int getItemCount() {
            return _ChatList_feed.getItemCount();
        }

        @Override
        public void onBindViewHolder(final FriendsList.FeedViewHolder FeedViewHolder, final int position) {
            String contactName = "";
            ChatListItem fe = _ChatList_feed.getItem(position);
//      ContactDatabase database = new ContactDatabase(getActivity());
//      fe.setContactName(database.getNameFromNumber(fe.getTelNo()));
            if (fe.getGroupItem() != null) {

                if (!fe.getGroupItem().isGroupLastChatOrderPay()) {
                    FeedViewHolder.pay.setTextColor(Color.parseColor("#6db314"));
                    FeedViewHolder.pay.setText("+" + getDividedToman((long) fe.getGroupItem().getGroupLastChatAmount()) + "");
                } else {
                    FeedViewHolder.pay.setTextColor(Color.RED);
                    FeedViewHolder.pay.setText("-" + getDividedToman((long) fe.getGroupItem().getGroupLastChatAmount()) + "");
                }
                FeedViewHolder.description.setText(fe.getGroupItem().getGroupLastChatComment());
                if (fe.getGroupItem().getGroupTitle().length() > 15) {
                    contactName = fe.getGroupItem().getGroupTitle();
                    FeedViewHolder.contactName.setText(contactName.substring(0, 15) + "...");
                } else {
                    FeedViewHolder.contactName.setText(fe.getGroupItem().getGroupTitle());
                }
//          FeedViewHolder.description.setText(fe.getGroupLastChatComment());
                Glide.with(getActivity())
                        .load("http://new.opaybot.ir" + fe.getGroupItem().getGroupPhoto())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.pic_profile)
                        .into(new BitmapImageViewTarget(FeedViewHolder.contactImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCornerRadius(700);
                                FeedViewHolder.contactImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });

                fe.setPosition(position);


            } else {

                if (fe.isLastChatOrderByFromOrTo()) {
                    FeedViewHolder.pay.setTextColor(Color.RED);
                    FeedViewHolder.pay.setText("-" + getDividedToman((long) fe.getLastChatAmount()) + "");
                } else if (!fe.isLastChatOrderByFromOrTo()) {
                    FeedViewHolder.pay.setTextColor(Color.GRAY);
                    FeedViewHolder.pay.setText(getDividedToman((long) fe.getLastChatAmount()) + "");
                } else {
                    FeedViewHolder.pay.setTextColor(Color.parseColor("#6db314"));
                    FeedViewHolder.pay.setText("+" + getDividedToman((long) fe.getLastChatAmount()) + "");
                }
                if (fe.isContactStatus()) {
                    FeedViewHolder.pay.setTextColor(Color.GRAY);
                    FeedViewHolder.pay.setText(getDividedToman((long) fe.getLastChatAmount()) + "");
                }
                FeedViewHolder.description.setText(fe.getComment());
                if (fe.getTitle().length() > 15) {
                    contactName = fe.getTitle();
                    FeedViewHolder.contactName.setText(contactName.substring(0, 15) + "...");
                } else {
                    FeedViewHolder.contactName.setText(fe.getTitle());
                }

                Glide.with(getActivity())
                        .load("http://new.opaybot.ir" + fe.getContactImg())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.pic_profile)
                        .into(new BitmapImageViewTarget(FeedViewHolder.contactImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCornerRadius(700);
                                FeedViewHolder.contactImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });

            }
        }

        @Override
        public FriendsList.FeedViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_item, viewGroup, false);
            return new FeedViewHolder(itemView, onClickHandler);
        }

    }

    private String getDividedToman(Long price) {
        if (price == 0) {
            return price + "";
        } else if (price < 1000) {
            return price + "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < price.toString().length(); i += 3) {
                try {
                    if (i == 0)
                        stringBuilder.insert(0, price.toString().substring(price.toString().length() - 3 - i, price.toString().length() - i));
                    else
                        stringBuilder.insert(0, price.toString().substring(price.toString().length() - 3 - i, price.toString().length() - i) + ",");
                } catch (Exception e) {
                    try {
                        stringBuilder.insert(0, price.toString().substring(price.toString().length() - 2 - i, price.toString().length() - i) + ",");
                    } catch (Exception e1) {
                        stringBuilder.insert(0, price.toString().substring(price.toString().length() - 1 - i, price.toString().length() - i) + ",");
                    }
                }

            }
            return stringBuilder.toString();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new fillContact().execute("[]");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public String newContact(JSONArray jsonArray) {
        return jsonArray.toString();
    }

    public class ComparingContactWithDatabase extends AsyncTask<Void, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Void... params) {
            database = new ContactDatabase(getActivity());
            GetContact getContact = new GetContact();

            ChatListFeed databaseContact = database.getAllData();
            ChatListFeed phoneContact = getContact.getNewContact(getActivity());
            for (int i = 0; i < phoneContact.getItemCount(); i++) {
                for (int j = 0; j < databaseContact.getItemCount(); j++) {
                    if (phoneContact.getItem(i).getTelNo().equals(databaseContact.getItem(j).getTelNo())
                            && phoneContact.getItem(i).getContactName().equals(databaseContact.getItem(j).getContactName())) {
                        phoneContact.removeItem(i);
                    }
                }
            }
            jsonArray = new JSONArray();
            for (int i = 0; i < phoneContact.getItemCount(); i++) {
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

    public class fillContact extends AsyncTask<String, Void, ChatListFeed> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ChatListFeed doInBackground(String... params) {
            Parser parser = new Parser(getActivity().getSharedPreferences("EZpay", 0).getString("token", ""));
            return parser.checkContactListWithGroup(params[0]);
        }

        @Override
        protected void onPostExecute(ChatListFeed result) {
            if (result != null) {
                _ChatList_feed = result;
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(getActivity(), "problem in connection!", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

    }

    public class getAccount extends AsyncTask<Void, Void, ChatListItem> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ChatListItem doInBackground(Void... params) {
            Parser parser = new Parser(getActivity().getSharedPreferences("EZpay", 0).getString("token", ""));
            return parser.getAccount();
        }

        @Override
        protected void onPostExecute(ChatListItem result) {
            Log.e("jsons", String.valueOf(result));

            if (result != null) {
                getActivity().getSharedPreferences("EZpay", 0).edit().putString("phoneNumber", result.getTelNo()).apply();
            } else {
                Toast.makeText(getActivity(), "Json Is Null!", Toast.LENGTH_SHORT).show();
            }

        }
    }


    public class GetGpWithMsg extends AsyncTask<String, Void, GroupItem> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected GroupItem doInBackground(String... params) {
            Parser parser = new Parser(getActivity().getSharedPreferences("EZpay", 0).getString("token", ""));

            return parser.groupWithChat(params[0], 10);


        }

        @Override
        protected void onPostExecute(GroupItem gp) {


            if (gp!=null) {

                ChatListItem chatListItem =new ChatListItem();
                chatListItem.setGroupItem(gp);
                _ChatList_feed.addItem(chatListItem);
                adapter.notifyDataSetChanged();

            }






        }
    }

}

