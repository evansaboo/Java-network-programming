/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import common.netFunctions;

/**
 *
 * @author Evan
 */
public class FileTransferClient {

    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    netFunctions func = new netFunctions();

    private OutputStream toServer;

    public void connect(String host, int port, int sessionId) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        toServer = socket.getOutputStream();
        toServer.write(ByteBuffer.allocate(4).putInt(sessionId).array());
        toServer = null;
    }

    public boolean sendToServer(File file) throws FileNotFoundException, IOException {
        return func.sendToNode(socket, file);
    }

    public boolean recvFromServer(String filepath) throws FileNotFoundException, IOException {
        return func.recvFromNode(socket, filepath);
    }
}
