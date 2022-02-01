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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Random;

public class CreateGame extends AppCompatActivity {

    private Button sharecode;
    private TextView codetv;
    private ProgressDialog pd;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean codeGenerated;
    private Calendar c;
    int roomcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("rooms");

        codetv = findViewById(R.id.codetv_create_game);
        sharecode = findViewById(R.id.sharecode);
        codeGenerated = false;
        roomcode = 0;

        showPD("Generating Code");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                while(!codeGenerated){
                    Random random = new Random();
                    int code = random.nextInt(9000) + 1000;

                    if(snapshot.child(String.valueOf(code)).exists()) {
                        if(snapshot.child(String.valueOf(code)).child("player1").exists()) {
                            c = Calendar.getInstance();
                            if(snapshot.child(String.valueOf(code)).child("StartTime")
                                    .getValue(Long.class) - c.getTimeInMillis() >= 86400000) {
                                createRoom(code);
                            }
                        } else {
                            createRoom(code);
                        }
                    } else {
                        createRoom(code);
                    }
                }
                if(codeGenerated) {
                    if(snapshot.child(String.valueOf(roomcode)).child("player2").exists()) {
                        Toast.makeText(getBaseContext(), "Game Started!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), GameActivity.class)
                                .putExtra("roomcode", String.valueOf(roomcode))
                                .putExtra("myself", "X"));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Failed to generate code",Toast.LENGTH_SHORT).show();
            }
        });

        sharecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codeGenerated) {
                    Intent shareIntent =  new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Tic-Tac-Toe Code");
                    String shareMessage = "Want To Play Tic-Tac-Toe With Me? Join my game with this code: " + roomcode;
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,shareMessage);
                    startActivity(Intent.createChooser(shareIntent,"Share via"));
                }
            }
        });
    }

    private void createRoom(int code) {
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        codeGenerated = true;
        roomcode = code;
        myRef.child(String.valueOf(code)).child("player1").setValue(android_id);
        c = Calendar.getInstance();
        myRef.child(String.valueOf(code)).child("StartTime").setValue(c.getTimeInMillis());
        codetv.setText(String.valueOf(code));
        pd.dismiss();
    }

    void showPD(String message){
        pd = new ProgressDialog(CreateGame.this);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(message);
        pd.show();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }
}