package com.example.islamic_app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TasbihActivity extends AppCompatActivity {

    private CardView tabSubhanAllah, tabAlhamdulillah, tabAllahuAkbar, tabLaIlaha;
    private TextView tvCurrentDhikr, tvCounter, laps, tvTotalToday, btnReset;
    private EditText tvTargetnumber;
    private LinearLayout tasbihbutton;

    private DatabaseReference mDatabase;

    private String currentDhikrKey = "subhanAllah";
    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasbih);

        // Get current Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User is not logged in
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // Get today's date
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Firebase path:
        // tasbih_data/users/USER_ID/daily/YYYY-MM-DD
        mDatabase = FirebaseDatabase.getInstance()
                .getReference("tasbih_data")
                .child("users")
                .child(userId)
                .child("daily")
                .child(todayDate);

        // Initialize views
        tabSubhanAllah = findViewById(R.id.tabSubhanAllah);
        tabAlhamdulillah = findViewById(R.id.tabAlhamdulillah);
        tabAllahuAkbar = findViewById(R.id.tabAllahuAkbar);
        tabLaIlaha = findViewById(R.id.tabLaIlaha);

        tvCurrentDhikr = findViewById(R.id.tvCurrentDhikr);
        tvCounter = findViewById(R.id.tvCounter);
        tvTargetnumber = findViewById(R.id.tvTargetnumber);

        tasbihbutton = findViewById(R.id.tasbihbutton);

        laps = findViewById(R.id.laps);
        tvTotalToday = findViewById(R.id.tvTotalToday);
        btnReset = findViewById(R.id.btnReset);

        // Default values
        tvCurrentDhikr.setText("سبحان الله");
        tvCounter.setText("0");

        // Load today's total
        loadTodayData();

        // Dhikr tabs
        tabSubhanAllah.setOnClickListener(v -> {
            currentDhikrKey = "subhanAllah";
            tvCurrentDhikr.setText("سبحان الله");
            tvCounter.setText("0");
        });

        tabAlhamdulillah.setOnClickListener(v -> {
            currentDhikrKey = "alhamdulillah";
            tvCurrentDhikr.setText("الحمد لله");
            tvCounter.setText("0");
        });

        tabAllahuAkbar.setOnClickListener(v -> {
            currentDhikrKey = "allahuAkbar";
            tvCurrentDhikr.setText("الله أكبر");
            tvCounter.setText("0");
        });

        tabLaIlaha.setOnClickListener(v -> {
            currentDhikrKey = "laIlahaIllaAllah";
            tvCurrentDhikr.setText("لا إله إلا الله");
            tvCounter.setText("0");
        });

        // Count button
        if (tasbihbutton != null) {
            tasbihbutton.setOnClickListener(v -> incrementTasbih());
        }

        // Reset button
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetTodayData());
        }
    }

    private void loadTodayData() {
        mDatabase.child("totalToday").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long total = snapshot.getValue(Long.class);
                    tvTotalToday.setText(String.valueOf(total != null ? total : 0));
                } else {
                    tvTotalToday.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvTotalToday.setText("0");
            }
        });

        mDatabase.child("laps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long lapsValue = snapshot.getValue(Long.class);
                    laps.setText(String.valueOf(lapsValue != null ? lapsValue : 0));
                } else {
                    laps.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                laps.setText("0");
            }
        });
    }

    private void incrementTasbih() {
        try {
            int count = parseTextViewInt(tvCounter);
            int total = parseTextViewInt(tvTotalToday);
            int lapsCount = parseTextViewInt(laps);
            int target = parseEditTextInt(tvTargetnumber);

            count++;
            total++;

            if (target > 0 && count > target) {
                count = 1;
                lapsCount++;
            }

            tvCounter.setText(String.valueOf(count));
            tvTotalToday.setText(String.valueOf(total));
            laps.setText(String.valueOf(lapsCount));

            // Save daily total
            mDatabase.child("totalToday").setValue(total);

            // Save laps
            mDatabase.child("laps").setValue(lapsCount);

            // Increment selected dhikr count
            incrementCurrentDhikrInFirebase();

        } catch (NumberFormatException e) {
            // In case one of the fields contains invalid text
            tvCounter.setText("0");
            tvTotalToday.setText("0");
            laps.setText("0");
        }
    }

    private void incrementCurrentDhikrInFirebase() {
        DatabaseReference dhikrRef = mDatabase
                .child("dhikr")
                .child(currentDhikrKey);

        dhikrRef.get().addOnSuccessListener(snapshot -> {
            long oldValue = 0;

            if (snapshot.exists()) {
                Long firebaseValue = snapshot.getValue(Long.class);
                if (firebaseValue != null) {
                    oldValue = firebaseValue;
                }
            }

            dhikrRef.setValue(oldValue + 1);
        });
    }

    private void resetTodayData() {
        tvCounter.setText("0");
        tvTargetnumber.setText("0");
        laps.setText("0");
        tvTotalToday.setText("0");

        mDatabase.child("totalToday").setValue(0);
        mDatabase.child("laps").setValue(0);

        mDatabase.child("dhikr").child("subhanAllah").setValue(0);
        mDatabase.child("dhikr").child("alhamdulillah").setValue(0);
        mDatabase.child("dhikr").child("allahuAkbar").setValue(0);
        mDatabase.child("dhikr").child("laIlahaIllaAllah").setValue(0);
    }

    private int parseTextViewInt(TextView textView) {
        String value = textView.getText().toString().trim();

        if (value.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(value);
    }

    private int parseEditTextInt(EditText editText) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(value);
    }
}