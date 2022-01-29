package client.startup;


import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import client.view.View;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Evan
 */
import java.rmi.Naming;
import common.FCInterface;
public class Main {
    /**
     * Find server registery name in the local registery. 
     * I found use get all methods provided by the FCInterface to used by klient
     * @param args 
     */
    public static void main(String [] args){
        try{
            FCInterface server = (FCInterface) Naming.lookup(FCInterface.SERVER_NAME_IN_REGISTRY);
            new View().start(server);
            
        } catch(NotBoundException | MalformedURLException | RemoteException ex){
            System.out.println("Could not start client");
        }
    }
    
}
