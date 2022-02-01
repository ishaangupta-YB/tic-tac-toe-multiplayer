package com.example.tictactoe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OnlineGame extends AppCompatActivity {

    private Button createbtn,joinbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        createbtn = findViewById(R.id.createbtn);
        joinbtn = findViewById(R.id.joinbtn);

        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OnlineGame.this,CreateGame.class));
            }
        });

        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OnlineGame.this,JoinGame.class));
            }
        });

    }
}