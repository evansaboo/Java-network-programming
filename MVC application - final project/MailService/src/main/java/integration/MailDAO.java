/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import model.Account;
import model.Mail;

/**
 *
 * @author Evan
 */
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Stateless
public class MailDAO {

    @PersistenceContext(unitName = "mailPU")
    private EntityManager em;

    public Account findAccount(String username) {

        try {
            return em.find(Account.class, username);
        } catch (Exception e) {
            return null;
        }
    }

    public void addNewAccount(Account acc) {
        em.persist(acc);
    }

    public void addNewMail(Mail mail) {
        em.persist(mail);
    }

    public List<Mail> getMailByTo(String username) {
        TypedQuery<Mail> query
                = em.createNamedQuery("Mail.mailToReceiver", Mail.class)
                        .setParameter("to", username);
        return query.getResultList();
    }
    
    public List<Mail> getMailByFrom(String username) {
        TypedQuery<Mail> query
                = em.createNamedQuery("Mail.mailToSender", Mail.class)
                        .setParameter("from", username);
        return query.getResultList();
    }
    public Mail getMailById(long mailId) {
        return em.find(Mail.class, mailId);
    }
    public void deleteMail(long mailId){
        Mail mail = getMailById(mailId);
        em.remove(mail);   
    }

}
