/* 
 * Created and modified by Evan Saboo
 */
package client.view;

/**
 *
 * @author Evan
 */
public class synchronizedPrintLine {

    synchronized void print(String output) {
        System.out.print(output);
    }

    synchronized void println(String output) {
        System.out.println(output);
    }
}
