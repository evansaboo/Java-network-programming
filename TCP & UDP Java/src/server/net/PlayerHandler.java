/* 
 * Created and modified by Evan Saboo
 */
package server.net;

import java.io.*;
import java.net.Socket;
import common.*;
import java.util.ArrayList;
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
    private boolean gameStarted;
    
    
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
            gameStarted = false;
            sendToPlayer("\nWelcome to the hangerman game!\n"
                        + "1. To start a new game type 'start' \n"
                        + "2. To guess a letter or word type the command 'guess' followed by a letter/word e.g. 'guess a' or 'guess hangerman'\n"
                        + "3. To quit the game type 'quit'\n"
                        + "Good Luck!");

        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        
        while (connected) {
            try {
                ReceivedMsg newMsg = new ReceivedMsg(fromPlayer.readLine());            
                switch(newMsg.cmdType){
                    case START:
                        if(gameStarted){
                            sendToPlayer("You can't preform this action!");
                            continue;
                        }
                        startNewGame();
                        break;
                    case QUIT:
                        closeConnection();
                        break;
                    case GUESS:
                        if(!gameStarted){
                            sendToPlayer("Incorrect operation. You haven't started a game yet.");
                            continue;
                        }
                        guess(newMsg.stringBody);
                        break;
                    default:
                        System.err.println("The command '"+ newMsg.receivedString+ "' is corrupt.");
                        
                }
            } catch (IOException ioe) {
                System.out.println("Something went wrong, disconnecting client...");
                closeConnection();
            }
     
        }
    }
    
    /**
     * Send a string to player with Socket.println
     * @param msg Message to player 
     */
    private void sendToPlayer(String msg) {
        toPlayer.println(msg);
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
        sendToPlayer("Your word is ' " + hiddenWord +"' and you have " + lives + " lives");
        gameStarted = true;
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
                wrongAnswer();
            }

        }
        else{
            char letter = msg.charAt(0);
            String hword = contr.guessLetter(word, hiddenWord, letter);

            if(contr.removeSpaces(hword).equals(word)){
                correctWord();

            }
            else if(hword.equals(hiddenWord)){
                sendToPlayer("You have already guessed the letter '"+ letter +"', try again.");
            }
            else if(hword.length() > 0){

                sendToPlayer("'You guessed correct: '" + hword +"'\nYou have " + lives + " lives left");
                hiddenWord = hword;
            }
            else{
                wrongAnswer();
            }
        }
                        
    }
    
    /**
     * Decrease amount of chances left and send a message to the player.
     */
    private void wrongAnswer(){
        --lives;
        if(lives > 0)
            sendToPlayer("Your guess was incorrect. \nYou have " + lives +" lives left.");
        else{
            sendToPlayer("Game Over \nYour current score is "+ --gameScore + 
                    "\nThe correct word is '"+ word +"'\n"
                    + "Start a new game by typing 'start'");
                    gameStarted = false;
        }
    }
    
    private void correctWord(){
        sendToPlayer("Congratulations! \n'You found the word '"+ word +
                "'.\n Your current score is "+ ++gameScore
                    + "\nStart a new game by typing 'start'");
        gameStarted = false;
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
                String[] msg = msgParam.split(Constants.STRING_DELIMETER);
                cmdType = Commands.valueOf(msg[Constants.STRING_TYPE_INDEX].toUpperCase());

            if (msg.length > 1) {
                stringBody = msg[Constants.STRING_BODY_INDEX].toLowerCase();
            }
        }
    }
}
    
