/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import integration.CConverterDAO;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import model.Currency;
import model.CurrencyRate;
import model.CurrencyDTO;

/**
 *
 * @author Evan
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class Controller {
    @EJB CConverterDAO ccDAO;

    
    public void addNewCurrencyRate(String rateName, double rateId){
        ccDAO.storeCurrencyRate(new CurrencyRate(rateName, rateId));
    }
    
    /*public CurrencyDTO finCurreny(int rateId){
        ccDAO
    }*/

    public CurrencyDTO convertCurrency(String rateNameFrom, String rateNameTo, double currencyAmount) {
        CurrencyRate cRateFrom = ccDAO.findCurrencyRate(rateNameFrom);
        CurrencyRate cRateTo = ccDAO.findCurrencyRate(rateNameTo);
        
        Currency currency = new Currency(cRateFrom.getCurrencyName(), 
                                            cRateTo.getCurrencyName());
        currency.convertCurrency(cRateFrom.getCurrencyRate(), cRateTo.getCurrencyRate(), currencyAmount);
        return currency;
    }

    
}
