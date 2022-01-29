/* 
 * Created and modified by Evan Saboo
 */
package client.net;

import java.io.*;
import java.net.*;
import java.util.*;
import common.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Evan
 */
public class ServerCommunication implements Runnable {

    private final String CONNECTION_FAILURE_MSG = "Connection lost";
    private final String DISCONNECT_FAILURE_MSG = "Couldn't disconnect from server.";
    private final MsgExtractor msgExtracter = new MsgExtractor();
    private final ByteBuffer fromServer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private final ByteBuffer msgToSend = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private ServerResponses listeners;
    private InetSocketAddress serverAddress;
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean connected;
    private volatile boolean timeToSend = false;
    private volatile boolean canSendMsg = true;

    /**
     * A selector is being used to determine if server key wants to read from,
     * write to or accept new connection from server.
     */
    @Override
    public void run() {
        try {
            initConnAndSelector();

            while (connected) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        finishConnection(key);
                    } else if (key.isReadable()) {
                        readFromServer();
                    } else if (key.isWritable()) {
                        writeToServer(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(CONNECTION_FAILURE_MSG);
        }
        try {
            disconnectFromServer();
        } catch (IOException ex) {
            System.err.println(DISCONNECT_FAILURE_MSG);

        }
    }

    /**
     * Start new connection with server by providing ipadress and port number,
     * also Start a new thread which will handle the communication with server
     *
     * @param ipAddress network address to connect to
     * @param portNo port number
     */
    public void connectToServer(String ipAddress, int portNo) {
        serverAddress = new InetSocketAddress(ipAddress, portNo);
        new Thread(this).start();
    }
    
    /**
     * Send message to server that the client will disconnect.
     * @throws IOException if message failed to send
     */
    public void sendDisconenctSignal() throws IOException {
        connected = false;
        prepareToSendMsg(Commands.QUIT.toString(), null);
    }
    
    /**
     * Close socket channel and cancel sever key.
     * @throws IOException If socket channel failed to close or ket failed to cancel
     */
    private void disconnectFromServer() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }
    
    /**
     * Intilize socket channel by opening it and configure as non blocking socket.
     * Connect to the provided serverAddress and initilize the selector.
     * @throws IOException if initilizing the socket channel or selector failed
     */
    private void initConnAndSelector() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;

        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
    
    /**
     * Finish socket channel coonection and set key to read.
     * @param key Server Selection Kwey
     * @throws IOException if failed to complete connection or set key to read
     */
    private void finishConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }
    /**
     * Give the task (printing a message to player) to forkjoinpool 
     * @param msg Given message from server
     */
    private void printMsgFromServer(String msg) {
        CompletableFuture.runAsync(() -> listeners.msgHandler(msg));
    }
    

    public void assignListener(ServerResponses listener) {
        listeners = listener;
    }
    
    /**
     * Reads from socket channel and prints it to player
     * @param key Server selection key
     * @throws IOException if method failed
     */
    private void readFromServer() throws IOException {
        fromServer.clear();
        int numOfReadBytes = socketChannel.read(fromServer);
        if (numOfReadBytes == -1) {
            throw new IOException(CONNECTION_FAILURE_MSG);
        }
        String recvString = msgFromBuffer();

        if (msgExtracter.cmdType(recvString) != Commands.GUESS) {
            System.err.println("The received message is corrupt");
        }
        printMsgFromServer(msgExtracter.bodyOfMsg(recvString));
        canSendMsg = true;

    }
    
    /**
     * Convert bytebuffer to string
     * @return string
     */
    private String msgFromBuffer() {
        fromServer.flip();
        byte[] bytes = new byte[fromServer.remaining()];
        fromServer.get(bytes);
        return new String(bytes);
    }
    /**
     * Joins one to several string parameters and adds them to a bytebuffer to be sent to Server
     * @param parts 
     */
    public void prepareToSendMsg(String... parts) {
        StringJoiner joiner = new StringJoiner(Constants.STRING_DELIMETER);
        for (String part : parts) {
            joiner.add(part);
        }
        synchronized (msgToSend) {
            msgToSend.put(ByteBuffer.wrap(joiner.toString().getBytes()));
        }
        timeToSend = true;
        selector.wakeup();
    }
    
    /**
     * Write to socket channel. The message gets sent to server
     * @param key Server selection key
     * @throws IOException  if socket channel write failed or selection key change failed
     */
    private void writeToServer(SelectionKey key) throws IOException {

        synchronized (msgToSend) {
            msgToSend.flip();
            canSendMsg = false;
            socketChannel.write(msgToSend);
            if (msgToSend.hasRemaining()) {
                return;
            }
            msgToSend.clear();
        }
        key.interestOps(SelectionKey.OP_READ);
    }
    
    /**
     * Check if the player can send a message to server. 
     * @return true or false
     */
    public boolean canSendMsg() {
        return canSendMsg;
    }

}
