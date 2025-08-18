package org.example;



import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.persistance.DatabaseConnectionManager;
import org.example.persistance.JdbcUnitOfWork;
import org.example.persistance.UnitOfWork;
import org.example.service.CustomerService;
import org.example.service.TransactionService;
import org.example.service.implementation.CustomerServiceImpl;
import org.example.service.implementation.TransactionServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        try {
            Connection connection = dcm.getConnection();
            TransactionDAO trxDao = new TransactionDAO(connection);

            JdbcUnitOfWork jdbcUnitOfWork = new JdbcUnitOfWork(dcm);
            TransactionService trxSer = new TransactionServiceImpl(jdbcUnitOfWork);

            TransactionDAO transactionDAO = new TransactionDAO(connection);
            CustomerService customerService = new CustomerServiceImpl(connection,transactionDAO);

            Customer customer = customerService.findCustomerById(1L);

            System.out.println(customer);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}