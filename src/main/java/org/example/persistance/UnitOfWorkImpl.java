package org.example.persistance;

import lombok.Getter;
import org.example.dao.CustomerDAO;
import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class UnitOfWorkImpl implements UnitOfWork {

    private final DatabaseConnectionManager dcm;
    private Connection connection;
    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;

    public UnitOfWorkImpl(DatabaseConnectionManager dcm) throws SQLException {
        this.dcm = dcm;
    }

    @Override
    public void begin() throws SQLException {
        connection = dcm.getConnection();
        connection.setAutoCommit(false);

        customerDAO = new CustomerDAO(connection);
        transactionDAO = new TransactionDAO(connection);
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null) {
            connection.rollback();
        }
    }

    @Override
    public CustomerDAO getCustomerDAO() {
        return customerDAO;
    }

    @Override
    public TransactionDAO getTransactionDAO() {
        return transactionDAO;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
