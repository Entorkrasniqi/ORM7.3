package dao;

import entity.Currency;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurrencyDao {
    private static final Logger LOGGER = Logger.getLogger(CurrencyDao.class.getName());

    public List<Currency> findAll() {
        EntityManager em = null;
        List<Currency> currencies = new ArrayList<>();
        
        try {
            em = datasource.MariaDbJpaConnection.getInstance();
            currencies = em.createQuery("SELECT c FROM Currency c", Currency.class).getResultList();
        } catch (PersistenceException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving currencies", e);
            throw e;
        }
        
        return currencies;
    }

    public void persist(Currency currency) {
        EntityManager em = null;
        EntityTransaction transaction = null;
        
        try {
            em = datasource.MariaDbJpaConnection.getInstance();
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(currency);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error persisting currency", e);
            throw e;
        }
    }
    
    public Currency findByCode(String code) {
        EntityManager em = null;
        Currency currency = null;
        
        try {
            em = datasource.MariaDbJpaConnection.getInstance();
            List<Currency> results = em.createQuery(
                "SELECT c FROM Currency c WHERE c.code = :code", 
                Currency.class)
                .setParameter("code", code)
                .getResultList();
                
            if (!results.isEmpty()) {
                currency = results.get(0);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding currency by code", e);
            throw e;
        }
        
        return currency;
    }
}
