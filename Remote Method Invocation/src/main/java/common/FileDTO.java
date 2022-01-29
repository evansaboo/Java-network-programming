/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Interace extends Serializable to be sent over RMI and used as an object extention
 * @author Evan
 */
public interface FileDTO extends Serializable {

    public String getFileName() throws RemoteException;
    public String getFileOwner() throws RemoteException;
    public boolean isPublic() throws RemoteException;
    public boolean isWritable() throws RemoteException;
    public long size() throws RemoteException;
}
