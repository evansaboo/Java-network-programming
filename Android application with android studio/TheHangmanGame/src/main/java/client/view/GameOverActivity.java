package client.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import client.controller.Controller;
import id1212.client.R;

/**
 * Created by Evan on 2017-12-12.
 */

public class GameOverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);
        initUI();
        initButtons();
    }
    private void initUI(){
        Bundle extras = getIntent().getExtras();
        setTextView(extras.getString("GAME_OVER_TITLE"), R.id.gameOverTitel);
        setTextView(extras.getString("GAME_OVER_MSG"), R.id.gameOverMsg);
        setTextView(extras.getString("CURRENT_SCORE"), R.id.gameOverScore);
    }

    private void initButtons(){
        final Button newRound = (Button) findViewById(R.id.playAgainButton);
        newRound.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent intent =  new Intent(GameOverActivity.this, GameInterface.class);
                startActivity(intent);
            }
        });
        final Button buttonQuit = (Button) findViewById(R.id.goHomeButton);
        buttonQuit.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Controller.disconnect();
                Intent startScreen = new Intent(GameOverActivity.this, MainActivity.class);
                finish();
                startActivity(startScreen);
            }
        });
    }
    private void setTextView(String msg, int id){
        TextView textView = (TextView) findViewById(id);
        textView.setText(msg);
    }
}
