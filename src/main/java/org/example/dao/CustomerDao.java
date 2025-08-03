package org.example.dao;

import org.example.models.Customer;
import org.example.util.DataAccsessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomerDao extends DataAccsessObject<Customer> {

    private static final String GET_ONE = "SELECT id, first_name, last_name, email, balance, bank_id " +
            "FROM customer WHERE id=?";

    public CustomerDao(Connection connection) {
        super(connection);
    }

    @Override
    public Customer findById(long id) {
        Customer customer = new Customer();

         try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {

             statement.setLong(1, id);
             ResultSet resultSet = statement.executeQuery();
             while (resultSet.next()){
                 customer.setId(resultSet.getLong("id"));
                 customer.setFirstName(resultSet.getString("first_name"));
                 customer.setLastName(resultSet.getString("last_name"));
                 customer.setEmail(resultSet.getString("email"));
                // customer.setTransactions(resultSet.getString(""));
             }

         } catch (SQLException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
        return customer;
    }

    @Override
    public List<Customer> findAll() {
        return List.of();
    }

    @Override
    public Customer update(Customer dto) {
        return null;
    }

    @Override
    public Customer create(Customer dto) {
        return null;
    }

    @Override
    public void delete(long id) {

    }


}

