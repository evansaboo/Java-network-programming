/* 
 * Created and modified by Evan Saboo
 */
package server.net;

import java.io.*;
import common.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import server.controller.Controller;

/**
 *
 * @author Evan
 */
public class PlayerHandler implements Runnable {

    private final Controller contr;
    private final GameServer server;
    private final SocketChannel playerChannel;
    private final ByteBuffer bufferFromPlayer = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    public final ByteBuffer msgsToSend = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);

    private String msgFromPlayer = "";
    private final MsgExtractor msgExtracter = new MsgExtractor();
    private String word;
    private String hiddenWord;
    private int gameScore;
    private int lives;
    ArrayList<String> prevWords = new ArrayList();
    private boolean gameStarted = false;

    PlayerHandler(GameServer server, SocketChannel playerChannel, Controller contr) {
        msgsToSend.put(server.msgtoByteBuffer(contr.welcomeText()));
        this.server = server;
        this.playerChannel = playerChannel;
        this.contr = contr;
        gameScore = 0;
    }

    @Override
    public void run() {

        String msg = msgFromPlayer;
        Commands cmdType = msgExtracter.cmdType(msg);
        String msgBody = "";
        if (gameStarted && cmdType != Commands.START) {
            msgBody = msgExtracter.bodyOfMsg(msg);
        }
        switch (cmdType) {
            case START:
                if (gameStarted) {
                    server.prepareMsgToSend("You have already started a game.", playerChannel);
                } else {
                    startNewGame();
                }
                break;
            case QUIT:
                server.prepareMsgToSend("Quit", playerChannel);
                break;
            case GUESS:
                if (!gameStarted) {
                    server.prepareMsgToSend("Incorrect operation. You haven't started a game yet.", playerChannel);
                } else {
                    guess(msgBody);
                }
                break;
            default:
                System.err.println("The command '" + msg + "' is corrupt.");

        }

    }

    /**
     * Starts a new game by doing the following tasks: - Get a new word from
     * word dictonary - Marks the word as used by adding it to the prevw´Words
     * arraylist - Masks the word - Assigns amount of live to the lives variable
     * by counting all the characters i the word - Sends the masked word and the
     * variable lives to the play via Socket.println
     */
    private void startNewGame() {
        word = contr.getWord(prevWords);
        prevWords.add(word);
        hiddenWord = contr.hideWord(word);
        lives = word.length();
        server.prepareMsgToSend("Your word is ' " + hiddenWord + "' and you have " + lives + " lives", playerChannel);
        gameStarted = true;
    }

    /**
     * Checks if the letter or word is correct. If the letter is correct the
     * game continues, otherwise the player has to guess again until his/her
     * amount of chances "lives" reaches zero.
     *
     * @param msg The guessed word or letter sent from the player.
     */
    private void guess(String msg) {

        if (msg.length() > 1) {
            if (contr.guessWord(word, msg)) {
                correctWord();
            } else {
                wrongAnswer();
            }

        } else {
            char letter = msg.charAt(0);
            String hword = contr.guessLetter(word, hiddenWord, letter);

            if (contr.removeSpaces(hword).equals(word)) {
                correctWord();

            } else if (hword.equals(hiddenWord)) {
                server.prepareMsgToSend("You have already guessed the letter '" + letter + "', try again.", playerChannel);
            } else if (hword.length() > 0) {

                server.prepareMsgToSend("You guessed correct: '" + hword + "', Lives: " + lives, playerChannel);
                hiddenWord = hword;
            } else {
                wrongAnswer();
            }
        }

    }

    /**
     * Writes the specified bytebuffer message to the socket channel.
     *
     * @param msg Bytebuffer message
     * @throws IOException If the message failed to send
     */
    public void sendMsg(ByteBuffer msg) throws IOException {
        playerChannel.write(msg);
        if (msg.hasRemaining()) {
            System.err.println("Could not send message");
        }
    }

    /**
     * Receives a message from a client by reading bytebuffer message from a
     * socket channel
     *
     * @throws IOException If failed to read from socket channel
     */
    public void recvMsg() throws IOException {
        bufferFromPlayer.clear();
        int numOfReadBytes = playerChannel.read(bufferFromPlayer);
        if (numOfReadBytes == -1) {
            throw new IOException("Player has closed connection");
        }

        msgFromPlayer = bufferToString();
        ForkJoinPool.commonPool().execute(this);
    }

    /**
     * Converts bytebuffer to String
     *
     * @return String
     */
    private String bufferToString() {
        bufferFromPlayer.flip();
        byte[] bytes = new byte[bufferFromPlayer.remaining()];
        bufferFromPlayer.get(bytes);
        return new String(bytes);
    }

    /**
     * Disconnects a player by closing the socket channel
     *
     * @throws IOException
     */
    public void disconnectPlayer() throws IOException {
        playerChannel.close();
    }

    /**
     * Decrease amount of chances left and send a message to the player.
     */
    private void wrongAnswer() {
        --lives;
        if (lives > 0) {
            server.prepareMsgToSend("Your guess was incorrect. Lives: " + lives, playerChannel);
        } else {
            server.prepareMsgToSend("Game Over \nYour current score is: " + --gameScore
                    + "\nThe correct word is '" + word + "'\n"
                    + "Start a new game by typing 'start'", playerChannel);
            gameStarted = false;
        }
    }

    /**
     * Send a 'victory' message to the player and signals the playhandler that a
     * new game have not started yet.
     */
    private void correctWord() {
        server.prepareMsgToSend("Congratulations! \n'You found the word '" + word
                + "'.\nYour current score is " + ++gameScore
                + "\nStart a new game by typing 'start'", playerChannel);
        gameStarted = false;
    }
    
    /**
     * Put buffer message in msgToSend bytebuffer
     * @param msg bytebuffer containing the message to send
     */
    public void addMsgToBuffer(ByteBuffer msg) {
        synchronized (msgsToSend) {
            msgsToSend.put(msg.duplicate());
        }

    }

    /**
     *Use sendMsg() to send a bytebuffer messageto client
     * @throws IOException
     */
    public void sendAllMsgs() throws IOException {
        synchronized (msgsToSend) {
            msgsToSend.flip();
            sendMsg(msgsToSend);
            msgsToSend.clear();
        }
    }
}
