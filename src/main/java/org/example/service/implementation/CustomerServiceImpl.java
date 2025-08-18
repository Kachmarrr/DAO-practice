package org.example.service.implementation;

import org.example.dao.CustomerDAO;
import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.service.CustomerService;

import java.sql.Connection;
import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;

    public CustomerServiceImpl(Connection connection, TransactionDAO transactionDAO) {
        this.customerDAO = new CustomerDAO(connection);
        this.transactionDAO = transactionDAO;
    }

    @Override
    public List<Customer> findAllCustomers() {
        return customerDAO.findAll().stream()
                .peek(customer -> customer // new peek operator for streams
                        .setTransactions(transactionDAO
                                .findAllTransactionsByCustomerId(customer
                                        .getId()))).toList();
    }

    @Override
    public Customer createCustomer(Customer customer) {
        return customerDAO.create(customer);
    }

    @Override
    public Customer findCustomerById(Long customerId) {
        Customer customer = customerDAO.findById(customerId);
        customer.setTransactions(transactionDAO.findAllTransactionsByCustomerId(customerId));
        return customer;
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerDAO.update(customer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerDAO.delete(customerId);
    }
}
