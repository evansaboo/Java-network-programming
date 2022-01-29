package client.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import client.controller.Controller;
import common.Commands;
import id1212.client.R;

/**
 * Created by Evan on 2017-12-11.
 */

public class GameInterface extends Activity {
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        runGame();
    }
    private void runGame(){

        new UpdateInterface(context).execute(Commands.START.toString());
        Button sendWordButton = (Button) findViewById(R.id.guessWordButton);
        final EditText wordInput = (EditText) findViewById(R.id.wordInput);
        sendWordButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v){
                new UpdateInterface(context).execute(Commands.GUESS.toString(), wordInput.getText().toString());
            }
        });
        final Button buttonQuit = (Button) findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Controller.disconnect();
                Intent startScreen = new Intent(GameInterface.this, MainActivity.class);
                finish();
                startActivity(startScreen);
            }
        });
    }

    public void onGuessLetterClick(View v){
        Button b = (Button)v;
        new UpdateInterface(context).execute(Commands.GUESS.toString(), b.getText().toString());
        b.setEnabled(false);
    }
}
