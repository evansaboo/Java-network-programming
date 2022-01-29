package client.net;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringJoiner;

import common.Commands;
import common.Constants;

/**
 * Created by Evan on 2017-12-11.
 */

public class ServerCommunication {
    private static Socket clientSocket;
    private static PrintWriter toServer;
    private static BufferedReader fromServer;
    private final static int CONNECT_TIMEOUT = 30000;
    private final static int SOCKET_TIMEOUT = 1800000;


    public static void connect(String host, int portNo) throws IOException{
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(host, portNo), CONNECT_TIMEOUT);
        clientSocket.setSoTimeout(SOCKET_TIMEOUT);

        toServer = new PrintWriter(clientSocket.getOutputStream(), true);
        fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }


    public static void sendToServer(String... parts) throws IOException{
        StringJoiner joiner = new StringJoiner(Constants.STRING_DELIMITER);
        for(String part : parts) {
            joiner.add(part);
        }
        toServer.println(joiner.toString());
    }

    public static String recvFromServer() throws  IOException{
        return fromServer.readLine();
    }

    private synchronized static void disconnect() throws IOException {

        toServer.println(Commands.QUIT.toString());
        clientSocket.close();
        clientSocket = null;
    }
    public static class doDisconnect extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                disconnect();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}
