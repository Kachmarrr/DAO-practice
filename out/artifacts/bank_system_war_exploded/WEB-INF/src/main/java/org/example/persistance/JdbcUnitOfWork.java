package org.example.persistance;

import org.example.dao.CustomerDAO;
import org.example.dao.TransactionDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcUnitOfWork implements UnitOfWork, AutoCloseable {

    private final DatabaseConnectionManager dcm;
    private Connection connection;
    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;
    private boolean committed = false;

    public JdbcUnitOfWork(DatabaseConnectionManager dcm) {
        this.dcm = dcm;
    }

    public void begin() throws SQLException {
        connection = dcm.getConnection();
        connection.setAutoCommit(false);

        transactionDAO = new TransactionDAO(connection);
        customerDAO = new CustomerDAO(connection);

        committed = false;
    }

    @Override
    public void commit() throws SQLException {
        if (connection == null) {
            throw new SQLException("No active transaction to commit");
        }
        connection.commit();
        committed = true;
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.isClosed()) {
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
        if (connection == null) return;

        try {
            if (!committed && !connection.getAutoCommit()) {
                connection.rollback(); // автоматичний rollback якщо commit не викликано
            }
            connection.setAutoCommit(true);
        } finally {
            if (!connection.isClosed()) {
                connection.close();
            }
            connection = null;
        }
    }
}
