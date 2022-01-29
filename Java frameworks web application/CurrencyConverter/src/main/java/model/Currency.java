/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;


/**
 *
 * @author Evan
 */

public class Currency implements CurrencyDTO {

    String currencyFromName;
    String currencyToName;
    double currencyFromAmount;
    double currencyToAmount;

    public Currency() {
    }

    public Currency(String currencyFromName, String currencyToName) {
        this.currencyFromName = currencyFromName;
        this.currencyToName = currencyToName;
    }

    @Override
    public String getCurrencyFrom() {
        return currencyFromName;
    }

    @Override
    public String getCurrencyTo() {
        return currencyToName;
    }

    @Override
    public double getCurrencyFromAmount() {
        return currencyFromAmount;
    }

    @Override
    public double getCurrencyToAmount() {
        return currencyToAmount;
    }
    
    public void convertCurrency(double currencyRateFrom, double currencyRateTo, double currencyAmount){
        currencyFromAmount = Math.floor(currencyAmount * 100) / 100;
        
        currencyToAmount = (currencyRateTo/currencyRateFrom) * currencyAmount;
        currencyToAmount = Math.floor(currencyToAmount * 100) / 100;
    }
}
