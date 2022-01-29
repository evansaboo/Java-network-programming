/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface implemented in the client
 * Used to send recvMsg method over RMI to be used by server
 * @author Evan
 */
public interface ClientInterface extends Remote{
    
    public void recvMsg(String msg) throws RemoteException;
    
}
