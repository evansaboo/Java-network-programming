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
public interface CurrencyDTO {
    
    String getCurrencyFrom();

    String getCurrencyTo();
    double getCurrencyFromAmount();
    double getCurrencyToAmount();
    
}
