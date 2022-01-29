/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Random;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Evan
 */
@Entity
public class Account implements Serializable {
   
    @Id
    private String username;
    private String password;

    public Account(){
        
    }
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername(){
        return username;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public long genNewUserId(){
        Random generateUserId = new Random();
        return generateUserId.nextLong();
    }
}
