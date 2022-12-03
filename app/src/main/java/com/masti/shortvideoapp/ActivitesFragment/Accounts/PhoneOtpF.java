package com.masti.shortvideoapp.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.masti.shortvideoapp.ActivitesFragment.SplashA;
import com.masti.shortvideoapp.Models.UserModel;
import com.masti.shortvideoapp.Models.UserRegisterModel;
import com.masti.shortvideoapp.MainMenu.MainMenuActivity;
import com.masti.shortvideoapp.R;
import com.masti.shortvideoapp.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.masti.shortvideoapp.SimpleClasses.DataParsing;
import com.masti.shortvideoapp.SimpleClasses.Functions;
import com.masti.shortvideoapp.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class PhoneOtpF extends Fragment implements View.OnClickListener {
    View view;

    TextView tv1, resendCode, editNum;
    ImageView back;
    RelativeLayout rl1;
    SharedPreferences sharedPreferences;
    String phoneNum;
    UserRegisterModel userRegisterModel;
    Button sendOtpBtn;
    PinView etCode;
    private FirebaseAuth mAuth;
    private String verificationId;

    public PhoneOtpF() {
        // Required empty public constructor
    }
    String phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp, container, false);

        Bundle bundle = getArguments();
        phoneNum = bundle.getString("phone_number");
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_data");
        sharedPreferences = Functions.getSharedPreference(getContext());

        initViews();
        addClicklistner();
        oneMinuteTimer();
        mAuth = FirebaseAuth.getInstance();
        phone = phoneNum;
        sendVerificationCode(phone);
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // this will check th opt code validation
                String txtName = etCode.getText().toString();
                if (txtName.length() == 6) {
                    sendOtpBtn.setEnabled(true);
                    sendOtpBtn.setClickable(true);
                } else {
                    sendOtpBtn.setEnabled(false);
                    sendOtpBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void initViews() {
        tv1 = (TextView) view.findViewById(R.id.tv1_id);
         resendCode = (TextView) view.findViewById(R.id.resend_code);
        editNum = (TextView) view.findViewById(R.id.edit_num_id);
        editNum.setText(phoneNum);

        back = view.findViewById(R.id.goBack);
        rl1 = view.findViewById(R.id.rl1_id);
        sendOtpBtn = view.findViewById(R.id.send_otp_btn);
        etCode = view.findViewById(R.id.et_code);

    }

    // initlize all the click lister
    private void addClicklistner() {
        back.setOnClickListener(this);
        resendCode.setOnClickListener(this);
        editNum.setOnClickListener(this);
        sendOtpBtn.setOnClickListener(this);
    }

    // run the one minute countdown timer
    private void oneMinuteTimer() {
        rl1.setVisibility(View.VISIBLE);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                tv1.setText(view.getContext().getString(R.string.resend_code)+" 00:" + l / 1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }

        }.start();

    }

    // this method will call the api for code varification
    private void callApiCodeVerification() {







        JSONObject parameters = new JSONObject();


        try {

            parameters.put("phone", phoneNum);
            parameters.put("verify", "1");
            parameters.put("code", etCode.getText().toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.verifyPhoneNo, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseOptData(resp);

            }
        });
    }

    // this method will parse the api responce
    public void parseOptData(String loginData) {
        Log.e("---->",loginData.toString());
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                callApiPhoneRegisteration();
            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // call api for phone register code
    private void callApiPhoneRegisteration() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phoneNum);
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.registerUser, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseLoginData(resp);

            }
        });
    }


    private void parseLoginData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject jsonObj = jsonObject.getJSONObject("msg");
                UserModel userDetailModel = DataParsing.getUserDataModel(jsonObj.optJSONObject("User"));
                Functions.storeUserLoginDataIntoDb(view.getContext(),userDetailModel);

                Functions.setUpMultipleAccount(view.getContext());
                Variables.reloadMyVideos = true;
                Variables.reloadMyVideosInner = true;
                Variables.reloadMyLikesInner = true;
                Variables.reloadMyNotification = true;

                if (Paper.book(Variables.MultiAccountKey).getAllKeys().size()>1)
                {
                    Intent intent=new Intent(view.getContext(), SplashA.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                }
                else
                {
                    Intent intent=new Intent(view.getContext(), MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                }

            } else if (code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")) {

                if (userRegisterModel.dateOfBirth == null) {
                    Functions.showAlert(getActivity(), "", view.getContext().getString(R.string.incorrect_login_details), view.getContext().getString(R.string.signup),view.getContext().getString(R.string.cancel_) , new Callback() {
                        @Override
                        public void onResponce(java.lang.String resp) {
                            if (resp.equalsIgnoreCase("yes")) {
                                openDobFragment();
                            }
                        }
                    });
                } else {
                    openUsernameF();
                }

            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openUsernameF() {
        CreateUsernameF signUp_fragment = new CreateUsernameF("fromPhone");
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        userRegisterModel.phoneNo = phoneNum;
        bundle.putSerializable("user_model", userRegisterModel);
        signUp_fragment.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.dob_fragment, signUp_fragment).commit();

    }

    private void openDobFragment() {
        DateOfBirthF DOBF = new DateOfBirthF();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        userRegisterModel.phoneNo = phoneNum;
        bundle.putSerializable("user_model", userRegisterModel);
        bundle.putString("fromWhere", "fromPhone");
        DOBF.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.sign_up_fragment, DOBF).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goBack:
                getActivity().onBackPressed();
                break;

            case R.id.resend_code:
                resendCode.setVisibility(View.GONE);
                etCode.setText("");
                oneMinuteTimer();
                sendVerificationCode(phone);
                break;


            case R.id.edit_num_id:
                getActivity().onBackPressed();
                break;

            case R.id.send_otp_btn:
                callApiCodeVerification();
                break;

        }
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(view.getContext(), "Success", Toast.LENGTH_SHORT).show();

                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
//                            Intent i = new Intent(this, HomeActivity.class);
//                            startActivity(i);
//                            finish();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(view.getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this.getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                etCode.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }

}