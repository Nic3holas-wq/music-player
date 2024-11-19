package com.example.musicplayer;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Equaliser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equaliser);
        // Back navigation function
        ImageView goBack = findViewById(R.id.back_icon);
        goBack.setOnClickListener(v -> {
            Intent intent = new Intent(Equaliser.this, MainActivity.class);
            startActivity(intent);
            finish();  // Close the Equaliser activity to prevent stacking
        });

        //set linear background
        RelativeLayout layout = findViewById(R.id.main); // Replace with your root layout ID

        // Create a gradient drawable programmatically
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,  // Diagonal gradient
                new int[]{0xFFFF5733, 0xFF4A90E2}    // Start and end colors
        );

        layout.setBackground(gradientDrawable);
        
    }
}
