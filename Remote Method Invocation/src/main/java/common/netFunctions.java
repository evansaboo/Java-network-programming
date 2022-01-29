/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Socket functions used by client and server
 * @author Evan
 */
public class netFunctions {
    public static final int NETWORK_PORT = 8000; 
    
    public boolean sendToNode(Socket clientSocket, File file) throws FileNotFoundException, IOException {
        OutputStream toClient = clientSocket.getOutputStream();

        FileInputStream fInputStream = new FileInputStream(file);
        try (BufferedInputStream bInputStream = new BufferedInputStream(fInputStream)) {
            byte[] contents;
            long fileLength = file.length();
            long current = 0;

            while (current != fileLength) {
                int size = 10000;
                if (fileLength - current >= size) {
                    current += size;
                } else {
                    size = (int) (fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bInputStream.read(contents, 0, size);
                toClient.write(contents);
            }
            toClient.flush();
            bInputStream.close();
        }
        disconnect(clientSocket);
        return true;
    }

    public boolean recvFromNode(Socket clientSocket, String filepath) throws IOException, FileNotFoundException {
        InputStream fromClient = clientSocket.getInputStream();
        FileOutputStream fOutputStream = new FileOutputStream(filepath);
        try (BufferedOutputStream bOutputStream = new BufferedOutputStream(fOutputStream)) {
            byte[] contents = new byte[1000];

            int bytesRead = 0;
            while ((bytesRead = fromClient.read(contents)) != -1) {
                bOutputStream.write(contents, 0, bytesRead);
            }
            bOutputStream.flush();
            bOutputStream.close();
        }
        disconnect(clientSocket);
        return true;
    }

    public void disconnect(Socket clientSocket) throws IOException {
        clientSocket.close();
        clientSocket = null;
    }
}
