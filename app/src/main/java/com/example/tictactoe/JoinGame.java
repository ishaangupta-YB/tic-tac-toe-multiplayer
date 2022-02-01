package com.example.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class JoinGame extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    DataSnapshot dataSnapshot;
    Calendar c;
    ProgressDialog pd;
    private Button joinbtn;
    private EditText codeET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        codeET = findViewById(R.id.et_join_game);
        joinbtn = findViewById(R.id.joinbtn_join_game);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("rooms");

        pd = new ProgressDialog(JoinGame.this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Please wait");
        pd.show();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pd.dismiss();
                dataSnapshot = snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeET.getText().toString();
                if(code.length() == 4){
                    if(dataSnapshot.child(code).exists()){
                        if(dataSnapshot.child(code).child("player2").exists()){
                            Toast.makeText(getApplicationContext(),"Game has already started",Toast.LENGTH_SHORT).show();
                        }else {
                            c = Calendar.getInstance();
                            if (dataSnapshot.child(code).child("StartTime")
                                    .getValue(Long.class) - c.getTimeInMillis() >= 86400000) {
                                Toast.makeText(getBaseContext(),"The code has expired. Please generate a new code.",Toast.LENGTH_LONG).show();
                            } else {
                                @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                                if (android_id.equals(dataSnapshot.child(code).child("player1").getValue(String.class))) {
                                    Toast.makeText(getBaseContext(), "You can't join a game started by you.", Toast.LENGTH_LONG).show();
                                } else {
                                    myRef.child(code).child("player2").setValue(android_id);
                                    Toast.makeText(getBaseContext(), "Game Started!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getBaseContext(), GameActivity.class).putExtra("roomcode", code).putExtra("myself", "O"));
                                    finish();
                                }
                            }
                        }
                    }else{
                        Toast.makeText(getBaseContext(), "Invalid Code", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }
}