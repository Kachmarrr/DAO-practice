package org.example;


import org.example.dao.BankDAO;
import org.example.dao.CustomerDAO;
import org.example.dao.TransactionDAO;
import org.example.models.Bank;
import org.example.models.Customer;
import org.example.models.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        try {
            Connection connection = dcm.getConnection();
            TransactionDAO trxDao = new TransactionDAO(connection);

            trxDao.findAllTransactionsByCustomerId(25).forEach(System.out::println);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}