/* 
 * Created and modified by Evan Saboo
 */
package server.model;

import java.io.*;
/**
 *
 * @author Evan
 */
public class FileHandler {
    private static final String BLANK_SPACE = " ";
    
    /**
     * Takes a filepath to file in the filesystem and converts the content of the file to string 
     * @param filePath path to a file
     * @return file content
     * @throws IOException file not found or error reading file
     */
    public String readText(String filePath) throws IOException{
       try(BufferedReader fileContent = new BufferedReader(new FileReader(filePath))) {
           StringBuilder textBasedContent = new StringBuilder();
           fileContent.lines().forEachOrdered(line -> appendElement(textBasedContent, line));
           return stringBuilderToString(textBasedContent);
       } 
    }
    
    /**
     * Adds a line to stringbuilder a blank space after the line 
     * @param sb Stringbuilder
     * @param line string
     */
    private void appendElement(StringBuilder sb, String line) {
        sb.append(line);
        sb.append(BLANK_SPACE);
    } 
    
    /**
     * Converts stringbuilder to string
     * @param sb Stringbuilder 
     * @return String
     */
    private String stringBuilderToString(StringBuilder sb) {
        return sb.toString().trim();
    }
}
