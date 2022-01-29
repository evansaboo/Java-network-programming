/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.controller.Controller;
import common.FCInterface;

/**
 *
 * @author Evan
 */
public class Server {
    private final String fileCatalogName = FCInterface.SERVER_NAME_IN_REGISTRY;
    public static void main(String[] args){
        try{
            Server server = new Server();
            server.startRMI();
            System.out.println("File Catalog Server has started.");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Failed to start server" +e);
        }

        
    }
    /**
     * Gets a list of all available registeries in the 
     * default registery port and rebinds a registery name with controller object to one of them registeries.
     * @throws RemoteException if no registery exists, create a new registery 
     * @throws MalformedURLException rebind failure
     */
    private void startRMI() throws RemoteException, MalformedURLException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        Controller contr = new Controller();
        Naming.rebind(fileCatalogName, contr);
    }
}
