package org.example.service.implementation;

import org.example.model.Customer;
import org.example.model.Transaction;
import org.example.persistance.UnitOfWorkImpl;
import org.example.service.TransactionService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final UnitOfWorkImpl unitOfWork;

    public TransactionServiceImpl(UnitOfWorkImpl unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Transaction proccesTransaction(Long recipientId, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .recipient_id(recipientId)
                .amount(amount)
                .build();

        unitOfWork.getTransactionDAO().create(transaction);

        return transaction;
    }

    @Override
    public Transaction transferTrxBetweenCustomers(Long senderId, Long recipientId, BigDecimal amount) {

        Customer sender = unitOfWork.getCustomerDAO().findById(senderId);
        Customer recipient = unitOfWork.getCustomerDAO().findById(senderId);

        Transaction transaction;

        try (unitOfWork) {

            unitOfWork.begin();
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException();
            }

            sender.setBalance(sender.getBalance().subtract(amount)); // decrease sender balance
            unitOfWork.getCustomerDAO().update(sender);

            recipient.setBalance(sender.getBalance().add(amount)); // increase recipient balance
            unitOfWork.getCustomerDAO().update(recipient);


            transaction = Transaction.builder()
                    .amount(amount)
                    .sender_id(senderId)
                    .recipient_id(recipientId)
                    .build();

            unitOfWork.commit();

        } catch (SQLException e) {
            try {
                unitOfWork.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return transaction;
    }

    @Override
    public List<Transaction> getTrx(Long customerId) {
        return unitOfWork.getTransactionDAO().findAllTransactionsByCustomerId(customerId);
    }

    @Override
    public void updateTrx(Transaction newTransaction) { // ?
        unitOfWork.getTransactionDAO().update(newTransaction);
    }

    @Override
    public void deleteTrx(Long transactionId) {
        unitOfWork.getTransactionDAO().delete(transactionId);
    }

    @Override
    public List<Transaction> getAllTrx() {
        return unitOfWork.getTransactionDAO().findAll();
    }
}
