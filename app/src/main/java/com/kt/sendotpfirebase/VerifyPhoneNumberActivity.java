package com.kt.sendotpfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

//activity yêu cầu gửi mã otp về sdt
public class VerifyPhoneNumberActivity extends AppCompatActivity {

    private EditText edtPhoneNumber;
    private Button btnVerifyPhoneNumber;

    //Firebase Authentication: Xác thực Firebase
    //Xác thực Firebase nhằm mục đích làm cho việc xây dựng hệ thống xác thực an toàn trở nên dễ dàng,
    // đồng thời cải thiện trải nghiệm đăng nhập và gia nhập cho người dùng cuối.
    private FirebaseAuth mAuth;

    private String TAG = "VerifyPhoneNumberActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        initUi();//
        setTitleToolbar();//

        mAuth = FirebaseAuth.getInstance();///
        //
        btnVerifyPhoneNumber.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //
            String strPhoneNumber = edtPhoneNumber.getText().toString().trim();
            onClickVerifyPhoneNumber(strPhoneNumber);
        }
    });
}

    private void initUi(){
        edtPhoneNumber = findViewById(R.id.edt_phone_number);
        btnVerifyPhoneNumber = findViewById(R.id.btn_verify_phone_number);
    }

    private void setTitleToolbar(){
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Verify phone number");
        }

    }

    //xử lý firebase với sdt
    private void onClickVerifyPhoneNumber(String strPhoneNumber) {
        //
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(strPhoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            //kích hoạt tin nhắn tự động truy xuất hoặc số điện thoại đã được xác minh ngay lập tức
                            //nếu sử dụng app ứng với số điện thoại => tự động nhập mã otp
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                //xác minh
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }
                            //thất bại
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                //
                                Toast.makeText(VerifyPhoneNumberActivity.this,"VerificationFailed",Toast.LENGTH_LONG).show();
                            }
                            //otp gửi vào đây và nhập tay
                            //có id xác minh và thông báo gửi lại
                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);  //verificationId: mã

                                //Chạy màn hình nhập mã pin otp
                                //strPhoneNumber: truyền phone number qua vì khi mà chưa nhận được mã otp thì ta thực hiện logic gửi lại mã otp
                                goToEnterOtpActivity(strPhoneNumber, verificationId);
                            }
                        })// OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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
                                Toast.makeText(VerifyPhoneNumberActivity.this,"The verification code entered was invalid",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
    //
    private void goToMainActivity(String phoneNumber) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("phone_number",phoneNumber);
        startActivity(intent);
    }
    private void goToEnterOtpActivity(String strPhoneNumber, String verificationId) {
        Intent intent = new Intent(this, EnterOtpActivity.class);
        intent.putExtra("phone_number",strPhoneNumber);
        intent.putExtra("verificationId",verificationId);
        startActivity(intent);
    }
}