package org.example.service.implementation;

import org.example.dao.CustomerDAO;
import org.example.model.Customer;
import org.example.service.CustomerService;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private CustomerDAO customerDAO;

    @Override
    public List<Customer> findAllCustomers() {
        return customerDAO.findAll();
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        return customerDAO.create(customer);
    }

    @Override
    public Customer findCustomerById(Long customerId) {
        return customerDAO.findById(customerId);
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
