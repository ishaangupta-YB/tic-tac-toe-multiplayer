package com.example.tictactoe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class OfflineGame extends AppCompatActivity {

    private Button restart;
    private TextView player1,player2,status;
    private LinearLayout gridsingleplayer;

    int activePlayer;
    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                            {0, 4, 8}, {2, 4, 6}};
    public static int counter;
    private boolean gameActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_game);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        restart = findViewById(R.id.restartsinglebtn);
        player1 = findViewById(R.id.player1winsingle);
        player2 = findViewById(R.id.player2winssingle);
        status = findViewById(R.id.offlinestatus);
        gridsingleplayer = findViewById(R.id.gridsingleplayer);
        gameActive = true;
        counter=0;
        activePlayer = 0;

        gameReset();

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameReset();
            }
        });
    }

    public void gameReset() {
        gameActive = true;
        activePlayer = 0;
        counter=0;
        Arrays.fill(gameState, 2);

        for(int i=0;i<3;i++){
            LinearLayout ll = (LinearLayout) gridsingleplayer.getChildAt(i);
            for(int j=0;j<3;j++){
                CardView cv = (CardView) ll.getChildAt(j);
                TextView tv = (TextView) cv.getChildAt(0);
                tv.setText("");
            }
        }
        status.setText("X's Turn - Tap to play");
    }

    public void playerTap(View view){
        CardView card = (CardView) view;
        int tapped = Integer.parseInt(card.getTag().toString());

        if (!gameActive) gameReset();

        if (gameState[tapped] == 2) {
            counter++;

            if (counter == 9) gameActive = false;
            gameState[tapped] = activePlayer;

            if (activePlayer == 0) {
                TextView tv = (TextView) card.getChildAt(0);
                tv.setText("X");
                activePlayer = 1;
                status.setText("O's Turn - Tap to play");
            } else {
                TextView tv = (TextView) card.getChildAt(0);
                tv.setText("O");
                activePlayer = 0;
                status.setText("X's Turn - Tap to play");
            }
        }
        int flag = 0;
        for (int[] winPosition : winPositions) {
            if (gameState[winPosition[0]] == gameState[winPosition[1]] &&
                    gameState[winPosition[1]] == gameState[winPosition[2]] &&
                    gameState[winPosition[0]] != 2) {
                flag = 1;

                String winnerStr;
                gameActive = false;
                if (gameState[winPosition[0]] == 0) {
                    int w = Integer.parseInt(player1.getText().toString());
                    player1.setText(String.valueOf(w+1));
                    winnerStr = "X has won";
                }
                else  {
                    int w = Integer.parseInt(player2.getText().toString());
                    player2.setText(String.valueOf(w+1));
                    winnerStr = "O has won";
                }
                status.setText(winnerStr);
            }
        }
        if (counter == 9 && flag == 0) status.setText("Match Draw");
    }
}