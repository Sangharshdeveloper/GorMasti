package com.masti.shortvideoapp.ActivitesFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.masti.shortvideoapp.Constants;
import com.masti.shortvideoapp.SimpleClasses.AppCompatLocaleActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.masti.shortvideoapp.MainMenu.MainMenuActivity;
import com.masti.shortvideoapp.Models.HomeModel;
import com.masti.shortvideoapp.R;
import com.masti.shortvideoapp.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.masti.shortvideoapp.SimpleClasses.Functions;
import com.masti.shortvideoapp.SimpleClasses.Variables;

import org.json.JSONObject;

import io.paperdb.Paper;

public class SplashA extends AppCompatLocaleActivity {

    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Functions.setLocale(Functions.getSharedPreference(SplashA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SplashA.class,false);
        setContentView(R.layout.activity_splash);


        apiCallHit();
    }

    private void apiCallHit() {
        callApiForGetad();
        if (Functions.getSharedPreference(this).getString(Variables.DEVICE_ID, "0").equals("0")) {
            callApiRegisterDevice();
        }
        else
            setTimer();
    }


    private void callApiForGetad() {

        JSONObject params=new JSONObject();
        try {
            if (Functions.getSharedPreference(SplashA.this).getBoolean(Variables.IS_LOGIN,false))
            {
                params.put("user_id",Functions.getSharedPreference(SplashA.this).getString(Variables.U_ID,""));
            }
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }
        VolleyRequest.JsonPostRequest(SplashA.this, ApiLinks.showVideoDetailAd, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SplashA.this,resp);
                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");

                    if(code!=null && code.equals("200")){
                        JSONObject msg=jsonObject.optJSONObject("msg");
                        JSONObject video=msg.optJSONObject("Video");
                        JSONObject user=msg.optJSONObject("User");
                        JSONObject sound = msg.optJSONObject("Sound");
                        JSONObject pushNotification=user.optJSONObject("PushNotification");
                        JSONObject privacySetting=user.optJSONObject("PrivacySetting");
                        HomeModel item = Functions.parseVideoData(user, sound, video, privacySetting, pushNotification);
                        item.promote="1";
                        Paper.book(Variables.PromoAds).write(Variables.PromoAdsModel,item);
                    }
                    else
                    {
                        Paper.book(Variables.PromoAds).destroy();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        });


    }


    // show the splash for 3 sec
    public void setTimer() {
        countDownTimer = new CountDownTimer(2500, 500) {

            public void onTick(long millisUntilFinished) {
                // this will call on every 500 ms
            }

            public void onFinish() {

                Intent intent = new Intent(SplashA.this, MainMenuActivity.class);

                if (getIntent().getExtras() != null) {

                    try {
                        // its for multiple account notification handling
                        String userId=getIntent().getStringExtra("receiver_id");
                        Functions.setUpSwitchOtherAccount(SplashA.this,userId);
                    }catch (Exception e){}

                    intent.putExtras(getIntent().getExtras());
                    setIntent(null);
                }

                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();

            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(countDownTimer!=null)
        countDownTimer.cancel();
    }

    // register the device on server on application open
    public void callApiRegisterDevice() {

        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        JSONObject param = new JSONObject();
        try {
            param.put("key", androidId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.registerDevice, param,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SplashA.this,resp);

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        setTimer();
                        JSONObject msg = jsonObject.optJSONObject("msg");
                        JSONObject Device = msg.optJSONObject("Device");
                        SharedPreferences.Editor editor2 = Functions.getSharedPreference(SplashA.this).edit();
                        editor2.putString(Variables.DEVICE_ID, Device.optString("id")).commit();
                    }
                    else
                    {
                        setTimer();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }

}
