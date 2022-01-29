/* 
 * Created and modified by Evan Saboo
 */
package server.controller;

import java.util.ArrayList;
import server.model.GameCalculations;

/**
 *
 * @author Evan
 */
public class Controller {

    private final GameCalculations model = new GameCalculations();

    /**
     * @see GameCalculations#getWord(java.util.ArrayList)
     * @param prevWords contains already used words
     * @return new word
     */
    public String getWord(ArrayList<String> prevWords) {
        return model.getWord(prevWords);
    }

    /**
     * @see GameCalculations#guessLetter(java.lang.String, java.lang.String,
     * char)
     * @return New hidden word or empty string
     */
    public String guessLetter(String word, String hiddenWord, char Letter) {
        return model.guessLetter(word, hiddenWord, Letter);
    }

    /**
     * @see GameCalculations#guessWord(java.lang.String, java.lang.String)
     * @return if the two string match (boolean value)
     */
    public boolean guessWord(String word, String guessedWord) {
        return model.guessWord(word, guessedWord);
    }

    /**
     * @see GameCalculations#hideWord(java.lang.String)
     * @return String containing only underscores
     */
    public String hideWord(String word) {
        return model.hideWord(word);
    }

    /**
     * @see GameCalculations#removeSpaces(java.lang.String)
     * @param s string containing spaces
     * @return string without spaces
     */
    public String removeSpaces(String s) {
        return model.removeSpaces(s);
    }

    public String welcomeText() {
        return model.welcomeText();
    }
}
