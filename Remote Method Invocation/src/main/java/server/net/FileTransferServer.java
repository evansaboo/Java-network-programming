/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.net;

import common.netFunctions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Evan
 */
public class FileTransferServer implements Runnable {

    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private final Map<Integer, Socket> clientSockets = new HashMap<>();
    ServerSocket listeningSocket;
    netFunctions func = new netFunctions();

    /**
     * An independent thread which listens on the provided network port and
     * accepts new connections and pass them on to other new threads.
     */
    @Override
    public void run() {
        try {
            listeningSocket = new ServerSocket(netFunctions.NETWORK_PORT);

            while (true) {
                Socket clientSocket = listeningSocket.accept();
                clientSocket.setSoLinger(true, LINGER_TIME);
                clientSocket.setSoTimeout(TIMEOUT_HALF_HOUR);

                InputStream fromClient = clientSocket.getInputStream();

                byte[] contents = new byte[4];
                fromClient.read(contents);
                fromClient = null;
                int sessionId = ByteBuffer.wrap(contents).getInt();

                synchronized (clientSockets) {
                    clientSockets.put(sessionId, clientSocket);
                }
            }

        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    /**
     * Sends a file to the client with the correct session id via client socket.
     * @param file provided file
     * @param sessionId session id which identifies a client socket 
     * @return true if file was sent, else false.
     * @throws FileNotFoundException If the file was not found in the provided filepath
     * @throws IOException socket failure
     */
    public boolean sendToClient(File file, int sessionId) throws FileNotFoundException, IOException {
        Socket clientSocket = getClientSocket(sessionId);
        if(clientSocket == null){
            return false;
        }

        return func.sendToNode(clientSocket, file);
    }
    
    /**
     * Receive a file from the client with the correct session id via client socket.
     * @param filepath provided file
     * @param sessionId session id which identifies a client socket 
     * @return true if file was recieved, else false.
     * @throws IOException socket failure
     */
    public boolean recvFromClient(String filepath, int sessionId) throws IOException {
        Socket clientSocket = getClientSocket(sessionId);
        if(clientSocket == null){
            return false;
        }

        return func.recvFromNode(clientSocket, filepath);
    }
    
    /**
     * Gets a socket from the hashmap by searching for the correct session id.
     * @param sessionId random int
     * @return client socket
     */
    private Socket getClientSocket(int sessionId) {
        long tStart = System.currentTimeMillis();
        Socket clientSocket = null;
        while (clientSocket == null) {
            long tEnd = System.currentTimeMillis();
            synchronized (clientSockets) {
                clientSocket = clientSockets.get(sessionId);
            }
            if (((tEnd - tStart) / 1000) > 30 && clientSocket == null) {
                return null;
            }
        }
        synchronized (clientSockets) {
            clientSockets.remove(sessionId);

        }
        return clientSocket;
    }

}
