/* 
 * Created and modified by Evan Saboo
 */
package client.controller;

import client.net.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import client.net.ServerResponses;
/**
 *
 * @author Evan
 */
public class Controller {
    private final ServerCommunication gameServer = new ServerCommunication();
    
    /**
     * @see ServerCommunication#connect(java.lang.String, int, client.net.ServerResponses)
     * Uses completable future to assign the connect() task to the ForkJoinPool class.
     * The ForkJoinPool assigns the task to a thread in the pool and the thread returns to the pool
     * when it's done.
     */
    public void connect(String host, int port, ServerResponses msgToPlayer){
        CompletableFuture.runAsync(() -> {
            try{
                gameServer.connect(host, port, msgToPlayer);
            } catch(IOException e){
                throw new UncheckedIOException(e);
            }
        }).thenRun(() -> msgToPlayer.msgHandler("Connected to " + host + ":" + port));
    }
    /**
     *@see ServerCommunication#disconnect() 
     * @throws IOException 
     */
    public void disconnect() throws IOException {
        gameServer.disconnect();
    }
    
    /**
     *@see ServerCommunication#sendToServer(java.lang.String...)
     */
    public void sendToServer(String... playerCmd) {
        CompletableFuture.runAsync(() -> gameServer.sendToServer(playerCmd));
    }
}
