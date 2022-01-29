/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import integration.MailDAO;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import model.Account;
import model.Mail;

/**
 *
 * @author Evan
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class Controller {

    @EJB
    MailDAO mailDAO;

    public long addNewAccount(String username, String password) {
        if (mailDAO.findAccount(username) != null) {
            return -1;
        }

        Account acc = new Account(username, password);
        long userId = acc.genNewUserId();

        try {
            mailDAO.addNewAccount(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    public long checkAccCredential(String username, String password) {
        Account acc = mailDAO.findAccount(username);

        if (acc == null) {
            return -1;
        } else if (!acc.getPassword().equals(password)) {
            return -1;
        }
        long userId = acc.genNewUserId();
        return userId;
    }
    
    public List<Mail> getAllMailByTo(String username){
        return mailDAO.getMailByTo(username);
    }
    public List<Mail> getAllMailByFrom(String username){
        return mailDAO.getMailByFrom(username);
    }
    public boolean insertNewMsg(Account acc, String toUser, String subject, String msgContent) {
        if(mailDAO.findAccount(toUser) == null){
            return false;
        }    
        mailDAO.addNewMail(new Mail(acc.getUsername(), toUser, subject, msgContent));   
        return true;
    }

    public void deleteMsg(long userId, long mailId, boolean isReceiver) {
        Mail mail = mailDAO.getMailById(mailId);
        if(mail == null){
            return;
        }
        if(isReceiver)
            mail.setReceiver(false);
        else
            mail.setSender(false);
        if(!mail.getReceiver() && !mail.getSender()){
            mailDAO.deleteMail(mailId);
        }
    }
    
 
    public Account getAccount(String username){
        return mailDAO.findAccount(username);
    }
    public Mail getMail(long mailId){
        return mailDAO.getMailById(mailId);
    }
}
