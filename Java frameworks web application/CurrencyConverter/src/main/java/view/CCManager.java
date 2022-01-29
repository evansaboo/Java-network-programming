/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import model.CurrencyDTO;

/**
 *
 * @author Evan
 */
@Named("ccManager")
@ConversationScoped
public class CCManager implements Serializable {

    @EJB
    private Controller contr;
    private CurrencyDTO currentCurrency;
    private String convertNameFrom;
    private String convertNameTo;
    private Double currencyAmount;
    private Exception exception;

    @Inject
    private Conversation conversation;

    private void startConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    private void stopConversation() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }

    public void setConvertFrom(String convertFrom) {
        this.convertNameFrom = convertFrom;
    }

    public void setConvertTo(String convertTo) {
        this.convertNameTo = convertTo;
    }

    public void setCurrencyAmount(Double currencyFromAmount) {
        this.currencyAmount = currencyFromAmount;
    }

    public String getConvertFrom() {
        return convertNameFrom;
    }

    public String getConvertTo() {
        return convertNameTo;
    }

    public Double getCurrencyAmount() {
        return null;
    }

    public void convertCurrency() {
        try {
            exception = null;
            startConversation();
            currentCurrency = contr.convertCurrency(convertNameFrom, convertNameTo, currencyAmount);
        } catch (Exception e) {
            exceptionHandler(new Exception("Input field is empty."));

        }
    }

    public CurrencyDTO getCurrentCurrency() {
        return currentCurrency;
    }

    private void exceptionHandler(Exception e) {
        stopConversation();
        e.printStackTrace(System.err);
        exception = e;
    }
    
    public boolean getFailure(){
        return exception != null;
    }
    
    public Exception getException(){
        return exception;
    }
}
