/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import server.integration.FileCatalogDAO;
import common.ClientInterface;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import server.net.FileTransferServer;

/**
 *
 * @author Evan
 */
public class UserManager {

    private final FileCatalogDAO fileCatalogDb = new FileCatalogDAO();
    private final Random generateUserId = new Random();
    private final Map<Long, User> users = Collections.synchronizedMap(new HashMap<>());
    String filepath = "recources/DB_Files/";

    public UserManager() {
    }
    /**
     * Checks if a user who want to login has the correct credentials by checking the the db. 
     * If the user is registered in the db then create a new User object and add it to the hashmap which contains all logged in users.
     * The key is a random generated long which for user identification.
     * @param remoteNode an object which contains printout method to the klient
     * @param username provided username
     * @param password provided password
     * @param transfer object which contains method to transfer files to the klient
     * @return unique user identifier.
     */
    public long setUserAsLoggedIn(ClientInterface remoteNode, String username, String password, FileTransferServer transfer) {
        if (!fileCatalogDb.checkIfClientExists(username, password)) {
            return -1;
        }
        long userId = generateUserId.nextLong();
        synchronized (users) {
            users.put(userId, new User(userId, username, remoteNode, this, transfer, fileCatalogDb));
        }
        return userId;
    }

    public void removeUser(Long userId) {
        synchronized (users) {
            users.remove(userId);
        }
    }
    
    /**
     * Get User object by searching for user identification key. 
     * @param userId provided user id
     * @return User object
     */
    public User findUserByUserId(long userId) {
        User user;
        synchronized (users) {
            user = users.get(userId);
        }
        return user;
    }
    /**
     * Loop throughout the hashmap and find the user object which contains the correct provided username.
     * @param username 
     * @return User object
     */
    public User findUserByUsername(String username) {
        for (Map.Entry<Long, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (user.getUsername().equals(username)) {
                return user;
            }

        }
        return null;
    }
    
    /**
     * 
     * Find User object by providing the username and 
     * notify the client by using the sendMsg method in the User object
     * @param username provided username
     * @param msg meeasge to send to client
     * @throws RemoteException RMI exception
     */
    public void notifyUser(String username, String msg) throws RemoteException {
        User user;
        if ((user = findUserByUsername(username)) != null) {
            user.sendMsg(msg);
        }
    }
    
    /**
     * Adds a new user in the database.
     * @param username provided username
     * @param password provided password
     * @return return a message if the user user has been add or not.
     */
    public String register(String username, String password) {
        try {
            if (fileCatalogDb.fyndClientByUsername(username)) {
                return "Username already exists, try with another username.";
            }
            fileCatalogDb.createClient(username, password);
            return "Registration was successful, you can now login.";
        } catch (Exception e) {
            return "Something went wrong, please try again.";
        }
    }
    
    /**
     * Removes user from the database if the provided username and password is correct.
     * Also delete all private files owned by the user
     * @param username
     * @param password
     * @return return message if successfully deleted user fom db.
     */
    public String unregister(String username, String password) {
        if (!fileCatalogDb.checkIfClientExists(username, password)) {
            return "Wrong username or password";
        }

        if (fileCatalogDb.deleteClient(username)) {
            CompletableFuture.runAsync(() -> {
                List<String> userFiles = fileCatalogDb.getAllPrivateClientFiles(username);
                fileCatalogDb.deletePrivateClientFiles(username);
                for (String userFile : userFiles) {
                    File file = new File(filepath + userFile);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
            return "User unregistration successful";
        }
        return "User unregistration unsuccessful, please try again later.";
    }

}
