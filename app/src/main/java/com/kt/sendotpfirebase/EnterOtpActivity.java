package com.kt.sendotpfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

//nhập mã nhận được xem thỏa mãn ko
public class EnterOtpActivity extends AppCompatActivity {

    private EditText edtOtp;
    private Button btnSendOtp;
    private TextView tv_send_otp_again;

    private String mPhoneNumber;
    private String mVerificationId;

    private String TAG = "EnterOtpActivity";

    //public abstract class FirebaseAuth extends Object
    private FirebaseAuth mAuth;

    private PhoneAuthProvider.ForceResendingToken mForceResendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_otp);

        //FirebaseAuth.getInstance(); : gọi để lấy 1 instance của FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        initUi();
        setTitleToolbar();

        getDataIntent();//
        //
        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strOtp = edtOtp.getText().toString().trim();
                onClickSendOtpCode(strOtp);
            }
        });
        
        tv_send_otp_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSendOtpAgain();
            }
        });
    
    }
    //Gửi lại mã otp
    private void onClickSendOtpAgain() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(mPhoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setForceResendingToken(mForceResendingToken)       //
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            //thành công
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                //
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }
                            //thất bại
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                //
                                Toast.makeText(EnterOtpActivity.this,"VerificationFailed",Toast.LENGTH_LONG).show();
                            }
                            //otp gửi vào đây và nhập tay
                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);  //verificationId: mã

                                mVerificationId = verificationId;
                                mForceResendingToken = forceResendingToken;

                            }
                        })// OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    //gửi mã otp lên firebase xác minh
    private void onClickSendOtpCode(String strOtp) {
        //một đối tượng xác minh với mã otp mà id tương ứng với số điện thoại
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, strOtp);
        //xác minh
        signInWithPhoneAuthCredential(credential);
    }

    //xem đang ở activity nào - set title
    private void setTitleToolbar(){
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Enter Otp Code");
        }

    }

    private void initUi(){
        edtOtp = findViewById(R.id.edt_otp);
        btnSendOtp = findViewById(R.id.btn_otp);
        tv_send_otp_again = findViewById(R.id.tv_send_otp_again);
    }

    private void getDataIntent(){
        mPhoneNumber = getIntent().getStringExtra("phone_number");
        mVerificationId = getIntent().getStringExtra("verificationId");
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI

                            //Nếu successful
                            goToMainActivity(user.getPhoneNumber());
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                                //false
                                Toast.makeText(EnterOtpActivity.this,"The verification code entered was invalid",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void goToMainActivity(String phoneNumber) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("phone_number",phoneNumber);
        startActivity(intent);
    }
}

//Lưu ý: Tài khoản firebase free bị giới hạn otp trong ngày chỉ vài lượt