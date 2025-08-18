package org.example.service;

import org.example.model.Bank;
import org.example.model.Customer;

import java.util.List;

public interface BankService {

    Bank createBank(Bank bank);
    void addCustomerToBank(Long bankId, Long customerId);
    List<Bank> findAllBanks();
    Bank findBankById(Long bankId);
    List<Customer> findAllCustomersInBankById(Long bankId);
    void updateBank(Bank bank);
    void deleteBank(Long bankId);

}
