/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import model.CurrencyRate;

/**
 *
 * @author Evan
 */
@Singleton
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Stateless
public class CConverterDAO {

    @PersistenceContext(unitName = "cConverterPU")
    private EntityManager em;

    @PostConstruct
    void init() {
        if (findCurrencyRate("SEK") == null) {
            storeCurrencyRate(new CurrencyRate("SEK", 11.3169));
        }
        if (findCurrencyRate("USD") == null) {
            storeCurrencyRate(new CurrencyRate("USD", 1.34029));
        }
        if (findCurrencyRate("EUR") == null) {
            storeCurrencyRate(new CurrencyRate("EUR", 1.13783));
        }
        if (findCurrencyRate("AUD") == null) {
            storeCurrencyRate(new CurrencyRate("AUD", 1.4));
        }
        if (findCurrencyRate("GBP") == null) {
            storeCurrencyRate(new CurrencyRate("GBP", 1));
        }
    }

    public CurrencyRate findCurrencyRate(String currencyName) {
        try {
            return em.find(CurrencyRate.class, currencyName);
        } catch (Exception e) {
            return null;
        }
    }

    public void storeCurrencyRate(CurrencyRate cc) {
        em.persist(cc);
    }

}
