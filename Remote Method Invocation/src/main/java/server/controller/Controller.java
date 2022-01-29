/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import server.model.User;
import server.model.UserManager;
import common.FCInterface;
import common.ClientInterface;
import common.FileDTO;
import java.util.List;
import server.net.FileTransferServer;

/**
 *
 * @author Evan
 */
public class Controller extends UnicastRemoteObject implements FCInterface {

    private final UserManager  userManager;
    private final FileTransferServer transfer;

    public Controller() throws RemoteException {
        super();
        userManager = new UserManager();
        transfer = new FileTransferServer();
        new Thread(transfer).start();
    }

    @Override
    public synchronized long login(ClientInterface remoteNode, String username, String password) {
        if(userManager.findUserByUsername(username) != null){
            return -2;
        }
        return userManager.setUserAsLoggedIn(remoteNode, username, password, transfer);
    }

    @Override
    public synchronized String register(String username, String password) {
        return userManager.register(username, password);
    }
    
    @Override
    public synchronized String unregister(String username, String password ) {
        if(userManager.findUserByUsername(username) != null)
            return "You have to logout before unregistering.";
        return userManager.unregister(username, password);
    }
    
    @Override
    public synchronized boolean uploadFile(int sessionId, long userId, String filename, long filesize, boolean fileIsPublic, boolean fileIsWritable) {
        User user;
        if((user = userManager.findUserByUserId(userId)) == null)
                return false;
        return user.uploadFile(sessionId, filename, filesize, fileIsPublic, fileIsWritable);      
    }

    @Override
    public boolean downloadFile(long userId, String filename, int sessionId) {
        User user;
        if((user = userManager.findUserByUserId(userId)) == null)
                return false;
        return user.downloadFile(filename, sessionId);
    }
    
    @Override
    public synchronized String deleteFile(long userId, String filename){
        return userManager.findUserByUserId(userId).deleteFile(filename);
    }
    
    @Override
    public void logout(long userId) {
        userManager.removeUser(userId);
        
    }
    
    @Override
    public List<? extends FileDTO> listAllFiles(long userId){
        return userManager.findUserByUserId(userId).listAllFiles();
    } 

    @Override
    public synchronized String updateFile(int sessionId,long userId, String filename, long fileSize, boolean isPublic, boolean isWritable, boolean changePermissions) throws RemoteException {
        return userManager.findUserByUserId(userId).updateFile(sessionId, filename, fileSize, isPublic, isWritable, changePermissions);
        
    }
    
    

}
