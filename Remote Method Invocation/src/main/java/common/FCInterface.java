/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *Interface implemented by the server.
 * Methods in the interface are being used by the client over RMI
 * @author Evan
 */
public interface FCInterface extends Remote {
    
    public static final String SERVER_NAME_IN_REGISTRY = "fileCatalog";
    
     
    public long login(ClientInterface remoteNode, String username, String password) throws RemoteException;
    
    public String register(String username, String password) throws RemoteException;
    
    public boolean uploadFile(int sessionId, long userId, String filename, long fileSize, boolean isPublic, boolean canWrite) throws RemoteException;
    
    public boolean downloadFile(long userId, String filename, int sessionId) throws RemoteException;
    
    public String deleteFile(long userId, String filename) throws RemoteException;
    
    public String unregister(String username, String password) throws RemoteException;
    
    public void logout(long userId) throws RemoteException;
    
    public List<? extends FileDTO> listAllFiles(long userId) throws RemoteException;
    
    public String updateFile(int sessionId, long userId, String filename, long fileSize, boolean isPublic, boolean canWrite, boolean changePermissions) throws RemoteException;
}
