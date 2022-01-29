/* 
 * Created and modified by Evan Saboo
 */
package server.net;

import java.io.IOException;
import java.net.*;

import common.Constants;
import server.controller.Controller;

/**
 * @author Evan
 */
public class GameServer {

    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private final Controller contr;

    public GameServer() {
        contr = new Controller();
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer();
    }


    /**
     * In independent thread which listens on the provided network port and
     * accepts new connections and pass them on to other new threads.
     */
    private void startServer() {
        try {
            ServerSocket newPlayers = new ServerSocket(Constants.NETWORK_PORT);
            while (true) {
                Socket playerSocket = newPlayers.accept();
                playerHandler(playerSocket);
            }
        } catch (IOException e) {
            System.err.println("Connection failed.");
        }
    }

    /**
     * Sets linger time and timout for the newly established socket and
     * it gets past to a new thread which handles all the game logic
     *
     * @param playerSocket newly established socket to handle one player
     * @throws SocketException Socket failure
     */
    private void playerHandler(Socket playerSocket) throws SocketException {
        playerSocket.setSoLinger(true, LINGER_TIME);
        playerSocket.setSoTimeout(TIMEOUT_HALF_HOUR);
        PlayerHandler handler = new PlayerHandler(playerSocket, contr);

        Thread playerThread = new Thread(handler);
        playerThread.setPriority(Thread.MAX_PRIORITY);
        playerThread.start();

    }

}
