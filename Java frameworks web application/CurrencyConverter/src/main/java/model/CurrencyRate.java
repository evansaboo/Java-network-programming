/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Evan
 */
@Entity
public class CurrencyRate implements Serializable {

    @Id
    private String currencyName;
    private double currencyRate;

    public CurrencyRate() {
    }

    public CurrencyRate(String currencyName, double currencyRate) {
        this.currencyName = currencyName;
        this.currencyRate = currencyRate;
    }
    
    public String getCurrencyName(){
        return currencyName;
    }
    public double getCurrencyRate(){
        return currencyRate;
    }

}
