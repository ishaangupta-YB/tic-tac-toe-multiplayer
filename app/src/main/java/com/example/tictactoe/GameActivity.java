package com.example.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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


public class GameActivity extends AppCompatActivity {

    String[][] board = new String[3][3];
    String turn, firstTurn, code, player;

    int score_x, score_y;

    FirebaseDatabase database;
    DatabaseReference myGameRef;

    TextView player_text, turn_text, score_x_text, score_y_text;
    TextView one, two, three, four, five, six, seven, eight, nine;
    private Boolean gameEnd;
    private Button restartBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        code = intent.getStringExtra("roomcode");
        player = intent.getStringExtra("myself");

        database = FirebaseDatabase.getInstance();
        myGameRef = database.getReference("games").child(code);

        player_text = (TextView) findViewById(R.id.game_activity_heading_tv);
        player_text.setText("You are: " + player);

        turn_text = (TextView) findViewById(R.id.turn_text);

        score_x_text = (TextView) findViewById(R.id.x_score_text);
        score_y_text = (TextView) findViewById(R.id.y_score_text);

        one =  findViewById(R.id.oneTV);
        two =   findViewById(R.id.twoTV);
        three = findViewById(R.id.threeTV);
        four =   findViewById(R.id.fourTV);
        five =   findViewById(R.id.fiveTV);
        six = findViewById(R.id.sixTV);
        seven = findViewById(R.id.sevenTV);
        eight =  findViewById(R.id.eightTV);
        nine = findViewById(R.id.nineTV);

        restartBtn = (Button) findViewById(R.id.restart_game);

        startLocal();
        startFB();
        updateUI();


        myGameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateLocal(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });

        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myGameRef.child("restart").child(player).setValue(true);
            }
        });
    }

    public void playerTaps(View view) {
        CardView card = (CardView) view;
        int i = Integer.parseInt(card.getTag().toString());

        if(!gameEnd) {
            if(turn.equals(player)) {
                if(is_valid_move(i)) {
                    board[(i-1)/3][(i-1)%3] = player;
                    turn = player.equals("X") ? "O" : "X";
                    updateUI();
                    updateFB(i);
                    String wincheck = checkWinning();
                    if(wincheck.equals("-")) {
                        if(check_draw()) {
                            turn_text.setText("DRAW!");
                            gameEnd = true;
                        }
                    } else {
                        turn_text.setText(wincheck + " WON!");
                        gameEnd = true;
                        if(wincheck.equals("X")) {
                            score_x += 1;
                            score_x_text.setText("X - " + String.valueOf(score_x));
                            myGameRef.child("scores").child(wincheck).setValue(score_x);
                        } else {
                            score_y += 1;
                            score_y_text.setText("O - " + String.valueOf(score_y));
                            myGameRef.child("scores").child(wincheck).setValue(score_y);
                        }
                    }
                }
            }
        }
    }

    private void updateFB(int i) {
        myGameRef.child("board").child(String.valueOf(i)).setValue(board[(i-1)/3][(i-1)%3]);
        myGameRef.child("turn").setValue(turn);
    }

    private void startFB() {
        for(int i = 1; i <= 9; i++)
            myGameRef.child("board").child(String.valueOf(i)).setValue("-");

        myGameRef.child("turn").setValue("X");
        myGameRef.child("scores").child("X").setValue(0);
        myGameRef.child("scores").child("O").setValue(0);
        myGameRef.child("restart").child("X").setValue(false);
        myGameRef.child("restart").child("O").setValue(false);
        myGameRef.child("first_turn").setValue("X");
    }

    private void startLocal() {
        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 3; j++)
                board[i][j] = "-";

        turn = firstTurn = "X";
        score_x = score_y = 0;
        gameEnd = false;
    }

    private void updateLocal(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child("restart").child("X").getValue(Boolean.class) &&
                dataSnapshot.child("restart").child("O").getValue(Boolean.class)) {

            for(int i = 0; i < 3; i++)
                for(int j = 0; j < 3; j++) board[i][j] = "-";

            for(int i = 1; i <= 9; i++) myGameRef.child("board").child(String.valueOf(i)).setValue("-");

            gameEnd = false;
            firstTurn = firstTurn.equals("X") ? "O" : "X";
            myGameRef.child("first_turn").setValue(firstTurn);
            turn = firstTurn;
            myGameRef.child("turn").setValue(turn);
            myGameRef.child("restart").child("X").setValue(false);
            myGameRef.child("restart").child("O").setValue(false);
            score_x = dataSnapshot.child("scores").child("X").getValue(Integer.class);
            score_y = dataSnapshot.child("scores").child("O").getValue(Integer.class);
            updateUI();
        }

        if(!turn.equals(player)) {
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    if (!board[i][j].equals(dataSnapshot.child("board").child(String.valueOf((i * 3) + j + 1))
                            .getValue(String.class))) {
                        turn = player;
                        board[i][j] = dataSnapshot.child("board").child(String.valueOf((i * 3) + j + 1))
                                .getValue(String.class);
                    }
                }
            }
        }
        updateUI();
        String wincheck = checkWinning();
        if(wincheck.equals("-")) {
            if(check_draw()) {
                turn_text.setText("DRAW!");
                gameEnd = true;
            }
        } else {
            score_x = dataSnapshot.child("scores").child("X").getValue(Integer.class);
            score_y = dataSnapshot.child("scores").child("O").getValue(Integer.class);
            score_x_text.setText("X - " + String.valueOf(score_x));
            score_y_text.setText("O - " + String.valueOf(score_y));
            turn_text.setText(wincheck + " WON!");
            gameEnd = true;
        }
    }

    private void updateUI() {
        score_x_text.setText("X - " + String.valueOf(score_x));
        score_y_text.setText("O - " + String.valueOf(score_y));
        turn_text.setText(turn);

        switch (board[0][0]) {
            case "X":
                one.setText("X");
                break;
            case "O":
                one.setText("O");
                break;
            default:
                one.setText("");
                break;
        }

        switch (board[0][1]) {
            case "X":
                two.setText("X");
                break;
            case "O":
                two.setText("O");
                break;
            default:
                two.setText("");
                break;
        }

        switch (board[0][2]) {
            case "X":
                three.setText("X");
                break;
            case "O":
                three.setText("O");
                break;
            default:
                three.setText("");
                break;
        }

        switch (board[1][0]) {
            case "X":
                four.setText("X");
                break;
            case "O":
                four.setText("O");
                break;
            default:
                four.setText("");
                break;
        }

        switch (board[1][1]) {
            case "X":
                five.setText("X");
                break;
            case "O":
                five.setText("O");
                break;
            default:
                five.setText("");
                break;
        }

        switch (board[1][2]) {
            case "X":
                six.setText("X");
                break;
            case "O":
                six.setText("O");
                break;
            default:
                six.setText("");
                break;
        }

        switch (board[2][0]) {
            case "X":
                seven.setText("X");
                break;
            case "O":
                seven.setText("O");
                break;
            default:
                seven.setText("");
                break;
        }

        switch (board[2][1]) {
            case "X":
                eight.setText("X");
                break;
            case "O":
                eight.setText("O");
                break;
            default:
                eight.setText("");
                break;
        }

        switch (board[2][2]) {
            case "X":
                nine.setText("X");
                break;
            case "O":
                nine.setText("O");
                break;
            default:
                nine.setText("");
                break;
        }
    }

    private boolean is_valid_move(int i) {
        if(i < 1 || i > 9) return false;

        return board[(i - 1) / 3][(i - 1) % 3].equals("-");
    }

    @SuppressLint("ResourceAsColor")
    private String checkWinning() {
        for(int i = 0; i < 3; i++) {
            if(board[i][0].equals(board[i][1]) && board[i][0].equals(board[i][2])) {
                return board[i][0];
            }
            if(board[0][i].equals(board[1][i]) && board[0][i].equals(board[2][i])) {
                return board[0][i];
            }
        }
        if(board[0][0].equals(board[1][1]) && board[0][0].equals(board[2][2])) {
            return board[0][0];
        }
        if(board[0][2].equals(board[1][1]) && board[0][2].equals(board[2][0])) {
            return board[0][2];
        }
        return "-";
    }

    private boolean check_draw() {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(board[i][j].equals("-")) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed();
    }
}