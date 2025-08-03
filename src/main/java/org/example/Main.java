package org.example;


import org.example.dao.BankDAO;
import org.example.dao.CustomerDAO;
import org.example.models.Bank;
import org.example.models.Customer;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        try {
            Connection connection = dcm.getConnection();
            CustomerDAO customerDAO = new CustomerDAO(connection);

            System.out.println(customerDAO.findAll().stream().count());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}