package client.view;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import client.controller.Controller;
import common.Constants;
import id1212.client.R;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startApp();
    }

    private void startApp(){
        final Button startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v){
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(v.getContext().CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    setTextView("", false);
                    setProgressBar(true);
                    startButton.setEnabled(false);
                    new ConnectToServer().execute();
                } else{
                    setTextView("Check your connection and try again.", true);
                }

            }
        });
    }

    private class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Controller.connect("10.0.2.2", Constants.NETWORK_PORT);
                return true;
            } catch (IOException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success ){
            setProgressBar(false);

            if(success) {
                Intent intent = new Intent(MainActivity.this, GameInterface.class);
                finish();
                startActivity(intent);
            }
            else{
                setTextView("Failed to connect to Server.", true);
            }
            final Button startButton = (Button) findViewById(R.id.startButton);
            startButton.setEnabled(true);
        }

    }

    private void setTextView(String msg, boolean visible){
        TextView textView = (TextView) findViewById(R.id.errorMsg);
        if(visible) {
            textView.setText("Failed to connect to server.");
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void setProgressBar(boolean visible){
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        if(visible)
            pb.setVisibility(View.VISIBLE);
        else
            pb.setVisibility(View.INVISIBLE);
    }

}
