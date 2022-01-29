/* 
 * Created and modified by Evan Saboo
 */
package client.net;
import java.io.*;
import java.net.*;
import java.util.StringJoiner;
import common.*;

/**
 *
 * @author Evan
 */
public class ServerCommunication {
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private volatile boolean connected;
    
    /**
     * Creates a new socket which connects to the server via port and ip address.
     * Also defines variables which sends messages to server and receives messages from server
     * Creates a listener thread which handles all received message from the server.
     * @param host IP address
     * @param port port number
     * @param msgToPlayer Server responses 
     * @throws IOException 
     */
    public void connect(String host, int port, ServerResponses msgToPlayer) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        boolean autoFlush = true;
        toServer = new PrintWriter(socket.getOutputStream(), autoFlush);
        fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(new ServerListener(msgToPlayer)).start();
    }
    
    /**
     * Send a "disconnect" command to server and close socket connection
     * @throws IOException Socket failure
     */
    public void disconnect() throws IOException {
        sendToServer(Commands.QUIT.toString());
        socket.close();
        socket = null;
        connected = false;
    }
    
    /**
     * Send message to server
     * @param parts takes one or several string parameters and joins them 
     */
    public void sendToServer(String... parts){
        StringJoiner joiner = new StringJoiner(Constants.STRING_DELIMETER);
        for(String part : parts) {
            joiner.add(part);
        }
        toServer.println(joiner.toString());
    }
    
    /**
     * A thread which only handles messages from the server
     */
    private class ServerListener implements Runnable {
        private final ServerResponses serverResponses;
        private ServerListener(ServerResponses serverResponses){
            this.serverResponses = serverResponses;
        }
        
        @Override
        public void run(){
            try{
                for(;;){
                    serverResponses.msgHandler(fromServer.readLine());
                }
            } catch(IOException e) {
                if(connected)
                    serverResponses.msgHandler("Connection lost");
            }
        }
        
    }
}
