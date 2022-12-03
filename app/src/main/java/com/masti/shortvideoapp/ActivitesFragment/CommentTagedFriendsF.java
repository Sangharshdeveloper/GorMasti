package com.masti.shortvideoapp.ActivitesFragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.masti.shortvideoapp.Adapters.UsersAdapter;
import com.masti.shortvideoapp.ApiClasses.ApiLinks;
import com.masti.shortvideoapp.Interfaces.AdapterClickListener;
import com.masti.shortvideoapp.Interfaces.FragmentCallBack;
import com.masti.shortvideoapp.Models.UserModel;
import com.masti.shortvideoapp.Models.UsersModel;
import com.masti.shortvideoapp.R;
import com.masti.shortvideoapp.SimpleClasses.DataParsing;
import com.masti.shortvideoapp.SimpleClasses.Functions;
import com.masti.shortvideoapp.SimpleClasses.Variables;
import com.masti.shortvideoapp.databinding.FragmentCommentTagedFriendsBinding;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class CommentTagedFriendsF extends BottomSheetDialogFragment {

    UsersAdapter adapter;
    ArrayList<UsersModel> datalist=new ArrayList<>();

    private Timer timer = new Timer();
    private final long DELAY = 1000; // Milliseconds

    int pageCount = 0;
    boolean ispostFinsh;
    LinearLayoutManager linearLayoutManager;
    FragmentCommentTagedFriendsBinding binding;

    FragmentCallBack callBack;

    public CommentTagedFriendsF(FragmentCallBack callBack) {
        this.callBack=callBack;
    }

    public CommentTagedFriendsF() {
    }

    private BottomSheetBehavior mBehavior;
    BottomSheetDialog dialog;

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = View.inflate(getContext(), R.layout.fragment_comment_taged_friends, null);
        dialog.setContentView(view);

        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setHideable(false);
        mBehavior.setDraggable(false);
        mBehavior.setPeekHeight((int) view.getContext().getResources().getDimension(R.dimen._500sdp),true);
        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState!=BottomSheetBehavior.STATE_EXPANDED)
                {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        return  dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_comment_taged_friends, container, false);
        initControl();
        actionControl();
        return binding.getRoot();
    }

    private void initControl() {
        setupUserAdapter();
        callApiForGetAllfollowing(true);
    }

    private void setupUserAdapter() {
        linearLayoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recylerview.setLayoutManager(linearLayoutManager);
        binding.recylerview.setHasFixedSize(true);
        adapter = new UsersAdapter(binding.getRoot().getContext(), datalist, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                UsersModel item1 = datalist.get(pos);
                switch (view.getId()) {
                    case R.id.mainlayout:
                        passDataBack(item1);
                        break;
                }
            }
        });
        binding.recylerview.setAdapter(adapter);
        binding.recylerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Functions.printLog("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (binding.loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        if (binding.searchEdit.getText().toString().length()>0)
                        {
                            callApiForOtherUsers();
                        }
                        else
                        {
                            callApiForGetAllfollowing(false);
                        }
                    }
                }


            }
        });
    }

    private void actionControl() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

       binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount=0;
                if (binding.searchEdit.getText().toString().length()>0)
                {
                    callApiForOtherUsers();
                }
                else
                {
                    callApiForGetAllfollowing(false);
                }
            }
        });


        binding.searchEdit.addTextChangedListener(
                new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String search_txt = binding.searchEdit.getText().toString();
                                                pageCount=0;
                                                if (search_txt.length() > 0) {
                                                    callApiForOtherUsers();
                                                }
                                                else
                                                {
                                                    callApiForGetAllfollowing(true);
                                                }
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
                    }
                });
    }


    //call api for get the all follwers of specific profile
    private void callApiForOtherUsers() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("type", "user");
            parameters.put("keyword", binding.searchEdit.getText().toString());
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, parameters,Functions.getHeaders(binding.getRoot().getContext()), new Callback() {
            @Override
            public void onResponce(String resp) {
                binding.refreshLayout.setRefreshing(false);
                Functions.checkStatus(getActivity(),resp);
                parseFollowingData(resp);
            }
        });


    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void callApiForGetAllfollowing(boolean isProgressShow) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(binding.getRoot().getContext()).getString(Variables.U_ID, ""));
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isProgressShow)
        {
            binding.pbar.setVisibility(View.VISIBLE);
        }
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFollowing, parameters,Functions.getHeaders(binding.getRoot().getContext()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                binding.refreshLayout.setRefreshing(false);
                if (isProgressShow)
                {
                    binding.pbar.setVisibility(View.GONE);
                }
                parseFollowingData(resp);
            }
        });


    }

    public void parseFollowingData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<UsersModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);

                    JSONObject userObj = data.optJSONObject("User");
                    if (userObj == null)
                        userObj = data.optJSONObject("FollowingList");

                    UserModel userDetailModel= DataParsing.getUserDataModel(userObj);

                    UsersModel user = new UsersModel();
                    user.fb_id = userDetailModel.getId();
                    user.username = userDetailModel.getUsername();
                    user.first_name = userDetailModel.getFirstName();
                    user.last_name = userDetailModel.getLastName();
                    user.gender = userDetailModel.getGender();

                    user.profile_pic = userDetailModel.getProfilePic();

                    user.followers_count = userDetailModel.getFollowersCount();
                    user.videos = userDetailModel.getVideoCount();


                    temp_list.add(user);


                }

                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                } else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();
            }

            if (datalist.isEmpty()) {
                binding.noDataLayout.setVisibility(View.VISIBLE);
            } else {
                binding.noDataLayout.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            binding.loadMoreProgress.setVisibility(View.GONE);
        }
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void passDataBack(final UsersModel item) {
        Bundle bundle=new Bundle();
        bundle.putBoolean("isShow", true);
        bundle.putSerializable("data", item);
        callBack.onResponce(bundle);
        dismiss();
    }

}