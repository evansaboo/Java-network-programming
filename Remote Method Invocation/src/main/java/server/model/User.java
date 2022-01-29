/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import server.integration.FileCatalogDAO;
import server.net.FileTransferServer;
import common.ClientInterface;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Evan
 */
public class User {

    private final String username;
    private final ClientInterface remoteNode;
    private final long userId;
    private final FileCatalogDAO fileCatalogDb;
    String filepath = "recources/DB_Files/";
    UserManager userManager;
    private final FileTransferServer transfer;

    public User(long userId, String username, ClientInterface remoteNode, UserManager userManager, FileTransferServer transfer, FileCatalogDAO fileCatalogDb) {
        this.userId = userId;
        this.username = username;
        this.remoteNode = remoteNode;
        this.userManager = userManager;
        this.transfer = transfer;
        this.fileCatalogDb = fileCatalogDb;
    }

    public String getUsername() {
        return username;
    }
    
    public long getUserId() {
        return userId;
    }
    /**
     * Calls on a method inthe object retrieved from client and 
     * passes a msg to printed out to the clients interface.
     * @param msg Message to be printed out.
     * @throws RemoteException 
     */
    public void sendMsg(String msg) throws RemoteException {
        remoteNode.recvMsg(msg);
    }
    
    /**
     * Adds specific file info to the database and retrieves a file from the client via sockets.
     * @param sessionId random int provided by the client
     * @param filename 
     * @param filesize
     * @param fileIsPublic
     * @param fileIsWritable
     * @return true if the file was successfully added to the database, else false
     */
    public boolean uploadFile(int sessionId, String filename, long filesize, boolean fileIsPublic, boolean fileIsWritable) {
        if (fileCatalogDb.findFileByName(filename) != null) {
            return false;
        }

        fileCatalogDb.addFileToDB(username, filename, filesize, fileIsPublic, fileIsWritable);
        CompletableFuture.runAsync(() -> {

            try {
                transfer.recvFromClient(filepath + filename, sessionId);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });
        return true;
    }
    
    /**
     * Retrieves file info from the database Send a file to the client via sockets
     * @param filename provided filename
     * @param sessionId client socket id
     * @return true if can be sent to the klient, else false
     */
    public boolean downloadFile(String filename, int sessionId) {
        FileHandler file = fileCatalogDb.findFileByName(filename);
        if (file == null) {
            return false;
        } else if (!file.isPublic() && !file.getFileOwner().equals(username)) {
            return false;
        }
        CompletableFuture.runAsync(() -> {
            try {
                File fileHandler = new File(filepath + filename);
                transfer.sendToClient(fileHandler, sessionId);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });
        doNotify(file, "Your public file '" + filename + "' was downloaded by "+username+".");
        return true;
    }
    
    /**
     * Delete specific file info from the database and delete the file from local file system
     * @param filename file to be deleted
     * @return Message to the user if file was deleted successfully
     */
    public String deleteFile(String filename) {
        FileHandler file = fileCatalogDb.findFileByName(filename);
        if (file == null) {
            return "File doesn't exist.";
        } else if ((file.isPublic() && !file.isWritable()) && !file.getFileOwner().equals(username)) {
            return "File is set to Read-Only";
        } else if (file.isPublic() && !file.isWritable()) {
            return "File is set to Read-Only";
        }

        fileCatalogDb.deleteFileFromCatalog(filename);
        doNotify(file, "Your public file '" + filename + "' was deleted by "+username+".");
        file.deleteFile(filepath);
        return "The file has successfully been deleted from file catalog.";
    }
    
    /**
     * @return  a list info on every accessible from the database
     */
    public List<FileHandler> listAllFiles() {
        return fileCatalogDb.getAllFiles(username);
    }


    /**
     * Updates a specific file and its information.
     * The information gets updated in the database.
     * The file retrieved from the client overwrites the previous file in the file system.
     * @param sessionId random int provided by the client for socket identification
     * @param filename filename of the file to be updated
     * @param fileSize update file size in the database
     * @param isPublic update the parameter isPublic in the db
     * @param isWritable update the parameter isWritable in the db
     * @param changePermissions checks if the client wants to update the premissions
     * @return true if the file was successfully updated in the database, else false
     */
    public String updateFile(int sessionId, String filename, long fileSize, boolean isPublic, boolean isWritable, boolean changePermissions) {
        FileHandler file = fileCatalogDb.findFileByName(filename);

        if (file == null || (!file.getFileOwner().equals(username) && !file.isPublic())) {
            return "False##File doesn't exist";
        } else if ((file.isPublic() && !file.isWritable()) && !file.getFileOwner().equals(username)) {
            return "False##File is set to Read-Only.";
        }
        if (file.getFileOwner().equals(username) && changePermissions) {
            if (!fileCatalogDb.updateFile(filename, fileSize, isPublic, isWritable)) {

                return "False##Failed to update file";
            }
        } else {
            if (!fileCatalogDb.updateFile(filename, fileSize, file.isPublic(), file.isWritable())) {
                return "False##Failed to update file";
            }
        }
        CompletableFuture.runAsync(() -> {
            try {
                transfer.recvFromClient(filepath + filename, sessionId);
                
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });

        doNotify(file, "Your public file '" + filename + "' was updated by "+username+".");
        return "True##File updated successfully";
    }
    
    /**
     * Sends a notification to the file owner when her/his file has been changed in any way. 
     * @param file File information, contains username of the file owner
     * @param msg notification to be sent.
     */
    private void doNotify(FileHandler file, String msg) {
        CompletableFuture.runAsync(() -> {
            if (!file.getFileOwner().equals(username)) {
                try {
                    userManager.notifyUser(file.getFileOwner(), msg);
                } catch (RemoteException e) {
                    System.err.println("Failed to notify User");
                }
            }
        });
    }
}
