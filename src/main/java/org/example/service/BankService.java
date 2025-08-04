package org.example.service;

import org.example.model.Bank;
import org.example.model.Customer;

import java.util.List;

public interface BankService {

    Bank createBank(Bank bank);
    void addCustomerToBank(Customer customer);
    List<Bank> findAllBanks();
    Bank findBankById(Long bankId);
    List<Customer> findAllCustomersById(Long bankId);
    void updateBank(Bank bank);
    void deleteBank(Long bankId);

}
