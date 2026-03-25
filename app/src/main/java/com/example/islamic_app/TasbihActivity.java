package com.example.islamic_app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TasbihActivity extends AppCompatActivity {

    private CardView tabSubhanAllah, tabAlhamdulillah, tabAllahuAkbar, tabLaIlaha, btnCount;
    private TextView tvCurrentDhikr, tvCounter, laps, tvTotalToday, btnReset;
    private EditText tvTargetnumber;
    private LinearLayout tasbihbutton;
    
    // Database Reference
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasbih);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("tasbih_data");

        // Step 1: Initialize all views
        tabSubhanAllah = findViewById(R.id.tabSubhanAllah);
        tabAlhamdulillah = findViewById(R.id.tabAlhamdulillah);
        tabAllahuAkbar = findViewById(R.id.tabAllahuAkbar);
        tabLaIlaha = findViewById(R.id.tabLaIlaha);
        
        tvCurrentDhikr = findViewById(R.id.tvCurrentDhikr);
        tvCounter = findViewById(R.id.tvCounter);
        tvTargetnumber = findViewById(R.id.tvTargetnumber);
        
        btnCount = findViewById(R.id.btnCount);
        tasbihbutton = findViewById(R.id.tasbihbutton);
        
        laps = findViewById(R.id.laps);
        tvTotalToday = findViewById(R.id.tvTotalToday);
        btnReset = findViewById(R.id.btnReset);

        // Load the saved value from Firebase when opening the screen
        mDatabase.child("totalToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalToday.setText(snapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Step 2: Dhikr actions - Click tabs to change title
        tabSubhanAllah.setOnClickListener(v -> { tvCurrentDhikr.setText("سبحان الله"); tvCounter.setText("0"); });
        tabAlhamdulillah.setOnClickListener(v -> { tvCurrentDhikr.setText("الحمد لله"); tvCounter.setText("0"); });
        tabAllahuAkbar.setOnClickListener(v -> { tvCurrentDhikr.setText("الله أكبر"); tvCounter.setText("0"); });
        tabLaIlaha.setOnClickListener(v -> { tvCurrentDhikr.setText("لا إله إلا الله"); tvCounter.setText("0"); });

        // Step 3: Increment and Save to Firebase
        if (tasbihbutton != null) {
            tasbihbutton.setOnClickListener(v -> {
                try {
                    int count = Integer.parseInt(tvCounter.getText().toString());
                    int total = Integer.parseInt(tvTotalToday.getText().toString());
                    int target = Integer.parseInt(tvTargetnumber.getText().toString());
                    int lapsCount = Integer.parseInt(laps.getText().toString());

                    count++;
                    total++;

                    if (count > target) {
                        count = 1;
                        lapsCount++;
                    }

                    tvCounter.setText(String.valueOf(count));
                    tvTotalToday.setText(String.valueOf(total));
                    laps.setText(String.valueOf(lapsCount));

                    // Save the new total value to Firebase
                    mDatabase.child("totalToday").setValue(total);
                    
                } catch (NumberFormatException e) {
                    // Ignore if target is not a valid number
                }
            });
        }

        // Step 4: Reset everything (including Firebase)
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                tvCounter.setText("0");
                tvTargetnumber.setText("0");
                laps.setText("0");
                tvTotalToday.setText("0");
                mDatabase.child("totalToday").setValue(0);
            });
        }
    }
}
