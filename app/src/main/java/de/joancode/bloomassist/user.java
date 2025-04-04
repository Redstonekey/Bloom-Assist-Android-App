package de.joancode.bloomassist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        ImageButton btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finish());
        Button btn_signout = findViewById(R.id.btn_sign_out);
        btn_signout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        });

    }
}
