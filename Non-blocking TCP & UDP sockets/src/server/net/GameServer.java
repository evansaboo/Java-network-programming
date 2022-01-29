/* 
 * Created and modified by Evan Saboo
 */
package server.net;

import common.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.StringJoiner;
import server.controller.Controller;

/**
 *
 * @author Evan
 */
public class GameServer {

    private static final int LINGER_TIME = 5000;
    private final Controller contr;
    private Selector selector;
    private ServerSocketChannel listeningServerChannel;

    public GameServer() {
        contr = new Controller();
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer();
    }

    /**
     * Set client key to write and add given message bytebuffer
     *
     * @param msg message to send
     * @param socket Socket channel used for one client
     */
    public void prepareMsgToSend(String msg, SocketChannel socket) {
        ByteBuffer completeMsg = msgtoByteBuffer(msg);

        SelectionKey key = socket.keyFor(selector);
        PlayerHandler player;
        if (key.isValid()) {
            player = (PlayerHandler) key.attachment();
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            try {
                removePlayer(key);
            } catch (IOException e) {

            }
            return;
        }
        synchronized (player.msgsToSend) {
            player.addMsgToBuffer(completeMsg);
        }
        selector.wakeup();
    }

    /**
     * A selector is being used to determine if valid keys wants to read from,
     * write to or accept new connection from client.
     */
    private void startServer() {
        try {
            initSocketAndSelector();
            while (true) {

                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        acceptNewPlayer(key);
                    } else if (key.isReadable()) {
                        getPlayerMsg(key);
                    } else if (key.isWritable()) {
                        sendToPlayer(key);
                    }
                }

            }
        } catch (IOException e) {
            System.err.println("Connection failed.");
        }
    }

    /**
     * Accepts a new connection by creating a new socket channel with a new
     * playerhandler for the connected client
     * Also configure socket channel to non-blocking
     * @param key Clients selection key
     * @throws IOException if failed to create new socket channel
     */
    private void acceptNewPlayer(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel playerChannel = serverSocketChannel.accept();
        playerChannel.configureBlocking(false);
        PlayerHandler handler = new PlayerHandler(this, playerChannel, contr);
        playerChannel.register(selector, SelectionKey.OP_WRITE, handler);
        playerChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }

    /**
     * Initilize the listening server socket channel and make it non-blocking.
     * Also initilize the selector and register it to the server channel.
     *
     * @throws IOException If failed to initilize server socket channel or
     * selector.
     */
    private void initSocketAndSelector() throws IOException {
        listeningServerChannel = ServerSocketChannel.open();
        listeningServerChannel.configureBlocking(false);
        listeningServerChannel.bind(new InetSocketAddress(Constants.NETWORK_PORT));

        selector = Selector.open();
        listeningServerChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Remove a Clients key if the client has disconnected. Also close client
     * socket channel.
     *
     * @param key Clients selection key
     * @throws IOException if failed to cancel selecton key
     */
    public void removePlayer(SelectionKey key) throws IOException {
        PlayerHandler player = (PlayerHandler) key.attachment();
        player.disconnectPlayer();
        key.cancel();
    }

    /**
     * Uses recvMsg() in player handler class to get message from client
     *
     * @param key Client selection key
     * @throws IOException if client disconnected
     */
    private void getPlayerMsg(SelectionKey key) throws IOException {
        PlayerHandler player = (PlayerHandler) key.attachment();
        try {
            player.recvMsg();
        } catch (IOException playerDiconnected) {
            removePlayer(key);
        }
    }

    /**
     * Uses sendAllMsgs() in playerHandler class to send message to client
     *
     * @param key Client selection key
     * @throws IOException if client disconnected
     */
    private void sendToPlayer(SelectionKey key) throws IOException {
        PlayerHandler player = (PlayerHandler) key.attachment();
        try {
            player.sendAllMsgs();
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException playerDisconnected) {
            removePlayer(key);
        }
    }

    /**
     * Adds a string delimeter between enum and string converts it all to
     * bytebuffer
     *
     * @param msg String to convert to bytebuffer
     * @return Bytebuffer containing enum, string delimeter and string
     * containing a message
     */
    public ByteBuffer msgtoByteBuffer(String msg) {
        StringJoiner joiner = new StringJoiner(Constants.STRING_DELIMETER);
        joiner.add(Commands.GUESS.toString());
        joiner.add(msg);
        return ByteBuffer.wrap(joiner.toString().getBytes());
    }
}
