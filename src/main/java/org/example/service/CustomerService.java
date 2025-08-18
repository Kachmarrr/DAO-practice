package org.example.service;

import org.example.model.Customer;

import java.util.List;

public interface CustomerService {

    List<Customer> findAllCustomers();
    Customer createCustomer(Customer customer);
    Customer findCustomerById(Long customerId);
    void updateCustomer(Customer customer);
    void deleteCustomer(Long customerId);

}
