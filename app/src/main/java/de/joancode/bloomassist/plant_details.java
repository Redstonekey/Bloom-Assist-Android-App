package de.joancode.bloomassist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class plant_details extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_details);
        LinearLayout btn_back = findViewById(R.id.backButton);
        btn_back.setOnClickListener(v -> finish());
    }
}