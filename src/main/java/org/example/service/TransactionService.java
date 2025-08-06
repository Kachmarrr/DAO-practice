package org.example.service;

import org.example.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    Transaction proccesTransaction(Long customerId, BigDecimal amount);
    Transaction transferTrxBetweenCustomers(Long senderId, Long recipientId, BigDecimal amount);
    Transaction getTrx(Long transactionId);
    void updateTrx(Transaction newTransaction);
    void deleteTrx(Long transactionId);
    List<Transaction> getAllTrx();

}
