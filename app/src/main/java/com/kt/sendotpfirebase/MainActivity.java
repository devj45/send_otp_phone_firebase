package com.kt.sendotpfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitleToolbar();
        //Lấy số điện thoại hiển thị lên màn hình
        getDataIntent();

    }
    private void setTitleToolbar(){
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Main Activity");
        }

    }

    private void getDataIntent(){
        String strPhoneNumber = getIntent().getStringExtra("phone_number");
        TextView textView = findViewById(R.id.tv_user_infor);
        textView.setText(strPhoneNumber);
    }
}