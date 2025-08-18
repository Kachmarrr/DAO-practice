package org.example.persistance;

import org.example.dao.CustomerDAO;
import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.model.Transaction;

import java.sql.SQLException;

public interface UnitOfWork extends AutoCloseable {

    void begin() throws SQLException;
    void commit() throws SQLException;
    void rollback() throws SQLException;

    CustomerDAO getCustomerDAO();
    TransactionDAO getTransactionDAO();

    @Override
    void close() throws SQLException;
}
