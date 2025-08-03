package org.example;


import org.example.dao.CustomerDao;
import org.example.models.Customer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        try {
            Connection connection = dcm.getConnection();
            Statement statement = connection.createStatement();


            CustomerDao customerDao = new CustomerDao(connection);
            Customer customer = customerDao.findById(26);

            System.out.println(customer.getFirstName() + " " + customer.getLastName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}