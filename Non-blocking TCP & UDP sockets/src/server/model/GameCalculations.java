/* 
 * Created and modified by Evan Saboo
 */
package server.model;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author Evan
 */
public class GameCalculations {

    private ArrayList<String> words = new ArrayList();

    public GameCalculations() {
        try {
            words = toArrayList("resources/words.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses readText to get all words from a file and inserts them to an
     * arraylist.
     *
     * @param filepath location of file
     * @return arraylist containing all words from a file
     */
    private ArrayList<String> toArrayList(String filepath) throws IOException {
        FileHandler fileHandler = new FileHandler();
        String file = fileHandler.readText(filepath);
        return new ArrayList<>(Arrays.asList(file.split(" ")));
    }

    /**
     * Gets an unused random word from ArrayList<String> words.
     *
     * @param prevWords contains previous used words.
     * @return the random word
     */
    public String getWord(ArrayList<String> prevWords) {
        Random rn = new Random();
        String word;
        while (true) {
            int n = rn.nextInt(words.size()) + 1;
            word = words.get(n);
            if (!prevWords.contains(word)) {
                return word.toLowerCase();
            }
        }
    }

    /**
     * Remove all blank spaces from a string
     *
     * @param word a String containing blank spaces
     * @return string without blank spaces
     */
    public String removeSpaces(String word) {
        return word.replaceAll("\\s+", "");
    }

    /**
     * Checks if the given letter is included in the given word. If it's true
     * add the letter to the "hidden word", in other ords replace character "_"
     * in the hidden word with the letter.
     *
     * @param word An english word
     * @param hiddenWord Contains letters and underscores
     * @param letter The given letter
     * @return hiddenWord containing the letter, else return empty string
     */
    public String guessLetter(String word, String hiddenWord, char letter) {

        String newWord = "";
        String testHWord = hiddenWord;

        hiddenWord = removeSpaces(hiddenWord);
        if (testHWord.contains(letter + "")) {
            return testHWord;
        } else if (word.indexOf(letter) > -1) {
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == letter) {
                    newWord += letter;
                } else {
                    newWord += hiddenWord.charAt(i);
                }
                if (i < (word.length() - 1)) {
                    newWord += " ";
                }
            }
            return newWord;
        } else {
            return "";
        }
    }

    /**
     * Check if two given strings match
     *
     * @param word First string
     * @param guessedWord Second string
     * @return boolean value
     */
    public boolean guessWord(String word, String guessedWord) {
        return word.equals(guessedWord);
    }

    /**
     * Replace all letter in the given string with underscore
     *
     * @param word string contaning english letters
     * @return String contaning which only contains underscores
     */
    public String hideWord(String word) {
        return word.replaceAll(".", "_ ");
    }

    public String welcomeText() {
        return "Welcome to the hangerman game!\n1. To start a new game type 'start' \n2. To guess a letter or word type the command 'guess' followed by a letter/word e.g. 'guess a' or 'guess hangerman'\n3. To quit the game type 'quit'\nGood Luck!";
    }
}
