package org.example;


import org.example.dao.CustomerDAO;
import org.example.models.Customer;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        try {
            Connection connection = dcm.getConnection();
            CustomerDAO customerDAO = new CustomerDAO(connection);
            Customer customer = new Customer("John", "Adams", "jadams.wh.gov");

            customerDAO.create(customer);
            System.out.println(customer);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}