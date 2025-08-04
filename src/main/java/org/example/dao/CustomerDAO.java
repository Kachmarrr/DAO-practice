package org.example.dao;

import org.example.models.Customer;
import org.example.util.DataAccsessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO extends DataAccsessObject<Customer> {

    private static final String GET_ONE = "SELECT id, first_name, last_name, email, balance, bank_id FROM customer WHERE id=?";
    private static final String INSERT = "INSERT INTO customer (first_name, last_name, email, balance) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE customer SET first_name = ?, last_name = ?, email = ?, balance = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM customer WHERE id = ?";
    private static final String READ = "SELECT id, first_name, last_name, email, balance, bank_id FROM customer";


    public CustomerDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Customer findById(long id) {
        Customer customer = new Customer();

        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
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
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(READ)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Customer customer = new Customer();
                customer.setId(resultSet.getLong("id"));
                customer.setFirstName(resultSet.getString("first_name"));
                customer.setLastName(resultSet.getString("last_name"));
                customer.setEmail(resultSet.getString("email"));
                customer.setBalance(resultSet.getBigDecimal("balance"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return customers;
    }


    @Override
    public Customer update(Customer dto) {
        Customer customer = null;
        try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, dto.getFirstName());
            statement.setString(2, dto.getLastName());
            statement.setString(3, dto.getEmail());
            statement.setBigDecimal(4, dto.getBalance());
            statement.setLong(5, dto.getId());
            statement.execute();
            customer = this.findById(dto.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return customer;
    }

    @Override
    public Customer create(Customer dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT)) {

            statement.setString(1, dto.getFirstName());
            statement.setString(2, dto.getLastName());
            statement.setString(3, dto.getEmail());
            statement.setBigDecimal(4, dto.getBalance());

            statement.execute();

            long id = this.getLastVal(CUSTOMER_SEQUENCE);
            return this.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}

