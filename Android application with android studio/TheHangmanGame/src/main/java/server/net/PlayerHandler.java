/* 
 * Created and modified by Evan Saboo
 */
package server.net;

import java.io.*;
import java.net.Socket;
import common.*;
import java.util.ArrayList;
import java.util.StringJoiner;
import server.controller.Controller;
/**
 *
 * @author Evan
 */
public class PlayerHandler  implements Runnable{
    private final Controller contr;
    private final Socket clientSocket;
    private BufferedReader fromPlayer;
    private PrintWriter toPlayer;
    private boolean connected;
    private String word;
    private String hiddenWord;
    private int gameScore;
    private int lives;
    ArrayList<String> prevWords = new ArrayList();
    
    
    PlayerHandler(Socket clientSocket, Controller controller){
        this.clientSocket = clientSocket;
        connected = true;
        gameScore = 0;
        contr = controller;
    }
    
    @Override
    public void run(){
        try{
            boolean autoFlush = true;
            fromPlayer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            toPlayer = new PrintWriter(clientSocket.getOutputStream(), autoFlush);

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        
        while (connected) {
            try {
                String msgFromPlayer = fromPlayer.readLine();
                ReceivedMsg newMsg = new ReceivedMsg(msgFromPlayer);            
                switch(newMsg.cmdType){
                    case START:
                        startNewGame();
                        break;
                    case QUIT:
                        closeConnection();
                        break;
                    case GUESS:
                        guess(newMsg.stringBody);
                        break;
                    default:
                        System.err.println("The command '"+ newMsg.receivedString+ "' is corrupt.");
                        
                }
            } catch (IOException | NullPointerException ioe) {
                System.out.println("Disconnecting client...");
                closeConnection();
            }
     
        }
    }
    
    /**
     * Send a string to player with Socket.println
     * @param parts Message to player
     */
    private void sendToPlayer(String... parts) {
        StringJoiner joiner = new StringJoiner("##");
        for(String part : parts) {
            joiner.add(part);
        }
        toPlayer.println(joiner.toString());
    }
    
    /**
     * Starts a new game by doing the following tasks:
     * - Get a new word from word dictonary
     * - Marks the word as used by adding it to the prevw´Words arraylist
     * - Masks the word
     * - Assigns amount of live to the lives variable by counting all the characters i the word
     * - Sends the masked word and the variable lives to the play via Socket.println
     */
    private void startNewGame(){
        word = contr.getWord(prevWords);
        prevWords.add(word);
        hiddenWord = contr.hideWord(word);
        lives = word.length();
        sendToPlayer(gameScore+"", lives+"", "False", "False", "Guess a letter or the word", hiddenWord);
    }
    

    /**
     * Checks if the letter or word is correct.
     * If the letter is correct the game continues, 
     * otherwise the player has to guess again until his/her amount of chances "lives" reaches zero.
     * @param msg The guessed word or letter sent from the player.
     */
    private void guess(String msg){

        if(msg.length() > 1){
            if(contr.guessWord(word, msg)){
                correctWord();
            } else{
                wrongAnswer(msg);
            }

        }
        else{
            char letter = msg.charAt(0);
            String hword = contr.guessLetter(word, hiddenWord, letter);

            if(contr.removeSpaces(hword).equals(word)){
                correctWord();

            }
            else if(hword.length() > 0){
                hiddenWord = hword;
                sendToPlayer(gameScore+"", lives+"", "True", "False","Your guess '"+msg+"' was correct", hiddenWord);
                
            }
            else{
                wrongAnswer(msg);
            }
        }
                        
    }
    
    /**
     * Decrease amount of chances left and send a message to the player.
     */
    private void wrongAnswer(String msg){
        --lives;
        if(lives > 0)
            sendToPlayer(gameScore+"", lives+"", "False", "False", "Your guess '"+msg+"' was incorrect", hiddenWord);
        else{
            sendToPlayer(--gameScore+"", lives+"", "False", "True", "The correct word was '"+word+"'", hiddenWord);
        }
    }
    
    private void correctWord(){
        sendToPlayer(++gameScore+"", lives+"", "True", "True", "You found the word '"+word+"'" , hiddenWord);
    }
    /**
     * Close current socket.
     */
    private void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Something went wrong while closing a socket.");
        }
        connected = false;
    }
    
    /**
     * Parses the received message
     */
    private static class ReceivedMsg {
        private Commands cmdType;
        private String stringBody;
        private final String receivedString;
        
        private ReceivedMsg(String receivedString) throws IOException{
            splitMsg(receivedString);
            this.receivedString = receivedString;
        }
        private void splitMsg(String msgParam){
                String[] msg = msgParam.split(Constants.STRING_DELIMITER);
                cmdType = Commands.valueOf(msg[Constants.STRING_TYPE_INDEX].toUpperCase());

            if (msg.length > 1) {
                stringBody = msg[Constants.STRING_BODY_INDEX].toLowerCase();
            }
        }
    }
}
    
