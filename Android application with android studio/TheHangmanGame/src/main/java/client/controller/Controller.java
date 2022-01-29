package client.controller;

import java.io.IOException;

import client.net.ServerCommunication;

/**
 * Created by Evan on 2017-12-15.
 */

public class Controller {

    public static void connect(String host, int portNo) throws IOException{
        ServerCommunication.connect(host, portNo);
    }

    public static void sendToServer(String... msg) throws IOException{
        ServerCommunication.sendToServer(msg);
    }

    public static String recvFromServer() throws IOException{
        return ServerCommunication.recvFromServer();
    }

    public static void disconnect() {
        new ServerCommunication.doDisconnect();
    }
}
