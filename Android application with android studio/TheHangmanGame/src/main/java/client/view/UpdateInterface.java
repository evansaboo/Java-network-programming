package client.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

import client.controller.Controller;
import common.Constants;
import id1212.client.R;

/**
 * Created by Evan on 2017-12-13.
 */

public class UpdateInterface extends AsyncTask<String, Void, Void> {
    private boolean isCorrectLetter;
    private boolean gameOver;
    private String hiddenWord;
    private int lives;
    private int score;
    private String msgToUser;
    private Context context;

    public UpdateInterface(Context context){
        this.context = context;
    }
    @Override
    protected Void doInBackground(String... string) {
        String msgFromServer = "";
        try {
            Controller.sendToServer(string);
            while((msgFromServer = Controller.recvFromServer()) == null);
        } catch (IOException e){
            Controller.disconnect();
        }
        splitMsg(msgFromServer);
        return null;
    }


    @Override
    protected void onPostExecute(Void v){

        if(gameOver){
            Intent intent = new Intent(context, GameOverActivity.class);
            Bundle extras = new Bundle();
            extras.putString("CURRENT_SCORE", "Score: "+ score);
            if(isCorrectLetter){
                extras.putString("GAME_OVER_TITLE", "Congratulations!");
                extras.putString("GAME_OVER_MSG", msgToUser);

            } else {
                extras.putString("GAME_OVER_TITLE", "Game Over!");
                extras.putString("GAME_OVER_MSG", msgToUser);
            }
            intent.putExtras(extras);
            ((GameInterface)context).finish();

            context.startActivity(intent);

        } else {
            setText(R.id.hiddenWord, hiddenWord);
            setText(R.id.lives, "Lives: " + lives);
            setText(R.id.score, "Score: " + score);
            setText(R.id.msgToUser, msgToUser);
        }
    }

    public void setText(int textId, String str){
        TextView textView = (TextView) ((GameInterface)context).findViewById(textId);
        textView.setText(str);
    }

    private void splitMsg(String s){
        String[] parts = s.split(Constants.STRING_DELIMITER);
        score = Integer.parseInt(parts[0]);
        lives = Integer.parseInt(parts[1]);
        isCorrectLetter = Boolean.parseBoolean(parts[2]);
        gameOver = Boolean.parseBoolean(parts[3]);
        msgToUser = parts[4];
        hiddenWord = parts[5];
    }
}