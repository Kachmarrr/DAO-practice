package org.example.service.implementation;

import org.example.dao.BankDAO;
import org.example.dao.CustomerDAO;
import org.example.model.Bank;
import org.example.model.Customer;
import org.example.service.BankService;

import java.util.List;

public class BankServiceImpl implements BankService {

    private final BankDAO bankDAO;
    private final CustomerDAO customerDAO;

    public BankServiceImpl(BankDAO bankDAO, CustomerDAO customerDAO) {
        this.bankDAO = bankDAO;
        this.customerDAO = customerDAO;
    }

    @Override
    public Bank createBank(Bank bank) {
        return bankDAO.create(bank);
    }

    @Override
    public void addCustomerToBank(Long bankId, Long customerId) {
        Customer customer = customerDAO.findById(customerId);
        customer.setBankId(bankId);
        customerDAO.update(customer);
    }

    @Override
    public List<Bank> findAllBanks() {
        return bankDAO.findAll();
    }

    @Override
    public Bank findBankById(Long bankId) {
        return bankDAO.findById(bankId);
    }

    @Override
    public List<Customer> findAllCustomersInBankById(Long bankId) {
        return customerDAO.findAllCustomersInBank(bankId);
    }

    @Override
    public void updateBank(Bank bank) {
        bankDAO.update(bank);
    }

    @Override
    public void deleteBank(Long bankId) {
        bankDAO.delete(bankId);
    }
}
