/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author Evan
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Mail.mailToReceiver",
            query = "SELECT m FROM Mail m WHERE m.mailTo = :to AND m.showReceiver = TRUE ORDER BY m.mailCreated DESC")
    ,
@NamedQuery(name = "Mail.mailToSender",
            query = "SELECT m FROM Mail m WHERE m.mailFrom = :from AND m.showSender = TRUE ORDER BY m.mailCreated DESC"),})

public class Mail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mailId;
    private String mailFrom;
    private String mailTo;
    private boolean showSender;
    private boolean showReceiver;

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date mailCreated;

    private String title;

    @Lob
    private String body;

    public Mail(String mailFrom, String mailTo, String title, String body) {
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailCreated = new Date();
        this.title = title;
        this.body = body;
        showSender = true;
        showReceiver = true;
    }

    public Mail() {

    }

    public String getMailFrom() {
        return mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void setReceiver(boolean b) {
        this.showReceiver = b;
    }

    public void setSender(boolean b) {
        this.showSender = b;
    }

    public boolean getSender() {
        return showSender;
    }

    public boolean getReceiver() {
        return showReceiver;
    }

    public JsonObjectBuilder toJson(boolean concatBody) {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
        JsonProvider provider = JsonProvider.provider();
        JsonObjectBuilder obj = provider.createObjectBuilder()
                .add("mailId", mailId)
                .add("mailFrom", mailFrom)
                .add("mailTo", mailTo)
                .add("mailCreated", df.format(mailCreated))
                .add("title", title);
        String b = body;
        if (concatBody && b.length() > 20) {
            b = b.substring(0, 20) + "...";
        }
        obj.add("body", b);
        return obj;
    }
    

}
