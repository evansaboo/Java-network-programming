/* 
 * Created and modified by Evan Saboo
 */
package client.view;

import client.net.FileTransferClient;
import java.util.Scanner;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import common.FCInterface;
import common.ClientInterface;
import common.FileDTO;
import common.netFunctions;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Evan
 */
public class View implements Runnable {

    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private final synchronizedPrintLine printOut = new synchronizedPrintLine();
    private FCInterface server;
    private ClientInterface myRemoteObj;
    FileTransferClient transfer = new FileTransferClient();
    File file;
    boolean fileIsPublic = false;
    boolean fileIsWritable = false;
    boolean updatePremissions = false;
    long clientId = -1;
    Random rand = new Random();
    int sessionId;
    
    public void start(FCInterface server) {
        this.server = server;
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        boolean success;
        printOut("Welcome to file catalog. To list all commands type 'help'.");
        while (receivingCmds) {
            try {

                String[] cmdLine = readNextLine().split(" ");

                Commands cmd = Commands.valueOf(cmdLine[0].toUpperCase());

                switch (cmd) {
                    case REGISTER:
                        if (!checkUserAndPassParams(cmdLine)) {
                            wrongInputMsg();
                        }
                        printOut(server.register(cmdLine[1], cmdLine[2]));
                        break;
                    case UNREGISTER:
                        if (!checkUserAndPassParams(cmdLine)) {
                            wrongInputMsg();
                            continue;
                        }
                        printOut(server.unregister(cmdLine[1], cmdLine[2]));
                        break;
                    case LOGIN:
                        myRemoteObj = new ConsoleOutput();
                        if (!checkUserAndPassParams(cmdLine)) {
                            wrongInputMsg();
                        }
                        clientId = server.login(myRemoteObj, cmdLine[1].toLowerCase(), cmdLine[2]);
                        if (clientId == -1) {
                            printOut("Wrong username or password.");
                            continue;
                        }else if(clientId == -2){
                            printOut("User is already logged in.");
                            continue; 
                        }
                        printOut("Welcome " + cmdLine[1]);

                        break;
                    case LOGOUT:
                        if (!checkifUserIsLoggedIn()) continue;
                        server.logout(clientId);
                        printOut("You have successfully logged out.");
                        UnicastRemoteObject.unexportObject(myRemoteObj, false);
                        myRemoteObj = null;
                        clientId = -1;
                        break;
                    case LIST:
                        if (!checkifUserIsLoggedIn()) continue;
                        List<? extends FileDTO> files = server.listAllFiles(clientId);
                        
                        final Object[][] table = new String[files.size() + 1][];

                        table[0] = new String[]{"Name:", "Owner:", "Size:", "Is Public:", "Is Writable:"};
                        for (int i = 0; i < files.size(); i++) {
                            FileDTO currentFile = files.get(i);

                            table[i + 1] = new String[]{currentFile.getFileName(),
                                currentFile.getFileOwner(),
                                sizeFormat(currentFile.size()),
                                String.valueOf(currentFile.isPublic()),
                                String.valueOf(currentFile.isWritable())};
                        }
                        for (final Object[] row : table) {
                            System.out.format("%-15s%-15s%-15s%-15s%-15s\n", row);
                        }
                        break;
                    case UPLOAD:
                        if (!checkifUserIsLoggedIn()) continue;
                        if (!checkUploadParams(cmdLine)) {
                            printOut("Usage: upload <filepath> <public/private> <read/write (if public)>");
                            continue;
                        }
                        file = new File(cmdLine[1]);

                        if (!file.exists() || file.isDirectory()) {
                            printOut("File not found.");
                            continue;
                        }
                        
                        sessionId = rand.nextInt();
                        success = server.uploadFile(sessionId, clientId, file.getName(), file.length(), fileIsPublic, fileIsWritable);
                        if (!success) {
                            printOut("File already exists");
                            continue;
                        }

                        transfer.connect("127.0.0.1", netFunctions.NETWORK_PORT, sessionId);

                        transfer.sendToServer(file);
                        printOut("File uploaded successfully");

                        break;
                    case DOWNLOAD:
                        if (!checkifUserIsLoggedIn()) continue;
                        if (!checkDownloadDeleteParams(cmdLine)) {
                            printOut("wrong input, try again.");
                            continue;
                        }
                        file = new File(cmdLine[1]);
                        sessionId = rand.nextInt();
                        success = server.downloadFile(clientId, file.getName(), sessionId);
                        if (!success) {
                            printOut("Couldn't find the file in the file catalog.");
                            continue;
                        }
                        
                        transfer.connect("127.0.0.1", netFunctions.NETWORK_PORT, sessionId);

                        transfer.recvFromServer(cmdLine[1]);
                        printOut("File downloaded successfully");
                        break;
                    case UPDATE:
                        if (!checkifUserIsLoggedIn()) continue;
                        if (!checkUpdateParams(cmdLine)) {
                            wrongInputMsg();
                            continue;
                        }
                        file = new File(cmdLine[1]);

                        if (!file.exists() || file.isDirectory()) {
                            printOut("File not found.");
                            continue;
                        }
                        sessionId = rand.nextInt();
                        String feedback = server.updateFile(sessionId, clientId, file.getName(), file.length(), fileIsPublic, fileIsWritable, updatePremissions);
                        String[] feedbackParams = feedback.split("##");

                        if (feedbackParams[0].equals("False")) {
                            printOut(feedbackParams[1]);
                        } else if (feedbackParams[0].equals("True")) {

                            transfer.connect("127.0.0.1", netFunctions.NETWORK_PORT, sessionId);
                            transfer.sendToServer(file);
                            printOut(feedbackParams[1]);
                        }
                        break;
                    case DELETE:
                        if (!checkifUserIsLoggedIn()) continue;
                        else if (!checkDownloadDeleteParams(cmdLine)) {
                            continue;
                        }
                        printOut(server.deleteFile(clientId, cmdLine[1]));

                        break;
                    default:
                        printOut("REGISTER user - usage: register <username> <password>");
                        printOut("UNREGISTER user - usage: unregister <username> <password>");
                        printOut("LOGIN user - usage: login <username> <password>");
                        printOut("LOGOUT user - usage: logout");
                        printOut("LIST files - usage: list");
                        printOut("UPLOAD file - usage: upload <filepath and/or filename> <choose: public/private> <if public: write/read>");
                        printOut("DOWNLOAD file - usage: download <filepath and/or filename>");
                        printOut("UPDATE file - usage: update <filepath and /or filename> <if owner choose: public/private> <if owner: write/read>");
                        printOut("DELETE file - usage: delete <filename>");
                        break;
                }
            } catch (Exception e) {
                printOut(e.toString());
            }
        }
    }

    private boolean checkifUserIsLoggedIn() {
        if (clientId == -1 || clientId == -2) {
            printOut("You haven't logged in yet");
            return false;
        }
        return true;
    }

    /**
     * Prints out the symbol '>' and waits for user input
     *
     * @return Scanner nextline for user input
     */
    private String readNextLine() {
        printOut.print(PROMPT);
        return console.nextLine();
    }

    private String sizeFormat(long size) {
        if (size < 1000) {
            return size + " B";
        } else if (size < 1000000) {
            return (int) Math.round(size / 1000) + " KB";
        } else if (size < 1000000000) {
            return (int) Math.round(size / 1000000) + " MB";
        } else {
            return (int) Math.round(size / 1000000000) + "MB";
        }
    }

    private void printOut(String msg) {
        printOut.print(PROMPT);
        printOut.println(msg);
    }

    /**
     * Check if string array size is between 1 and 2
     *
     * @param cmd string array
     * @return true if array size is between 1 and 2, else false
     */
    private boolean checkDownloadDeleteParams(String[] params) {
        return (params.length == 2);
    }

    private boolean checkUserAndPassParams(String[] params) {
        if (!(params.length > 1 && params.length < 4)) {
            return false;
        }

        for (String param : params) {
            if (param.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUploadParams(String[] params) {
        if (params.length > 2 && params.length < 5) {
            if (params[2].toLowerCase().equals("public")) {
                fileIsPublic = true;
            } else if (params[2].toLowerCase().equals("private")) {
                fileIsPublic = false;

            } else {
                return false;
            }
            if (params.length == 4) {
                if (params[3].toLowerCase().equals("read")) {
                    fileIsWritable = false;
                } else if (params[3].toLowerCase().equals("write")) {
                    fileIsWritable = true;
                } else {
                    return false;
                }
            }

        } else {
            return false;
        }
        return true;
    }

    private boolean checkUpdateParams(String[] params) {
        if (params.length > 1 && params.length < 5) {
            if (params.length == 2) {
                fileIsPublic = false;
                fileIsWritable = false;
                updatePremissions = false;
            } else {
                updatePremissions = true;
                fileIsWritable = false;
                switch (params[2].toLowerCase()) {
                    case "public":
                        fileIsPublic = true;
                        break;
                    case "private":
                        fileIsPublic = false;
                        break;
                    default:
                        return false;
                }
                if (params.length == 4) {
                    switch (params[3].toLowerCase()) {
                        case "read":
                            fileIsWritable = false;
                            break;
                        case "write":
                            fileIsWritable = true;
                            break;
                        default:
                            return false;
                    }
                }
            }

        } else {
            return false;
        }
        return true;
    }

    private void wrongInputMsg() {
        printOut.print(PROMPT);
        printOut.println("Worng input, try again");
    }

    private class ConsoleOutput extends UnicastRemoteObject implements ClientInterface {

        public ConsoleOutput() throws RemoteException {

        }

        @Override
        public void recvMsg(String msg) {
            printOut.println((String) msg);
            printOut.print(PROMPT);
        }
    }

}
