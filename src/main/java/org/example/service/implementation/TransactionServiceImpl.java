package org.example.service.implementation;

import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.model.Transaction;
import org.example.persistance.JdbcUnitOfWork;
import org.example.service.TransactionService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final JdbcUnitOfWork unitOfWork;
    private final TransactionDAO readOnlyTransactionDAO;

    public TransactionServiceImpl(JdbcUnitOfWork unitOfWork, TransactionDAO readOnlyTransactionDAO) {
        this.unitOfWork = unitOfWork;
        this.readOnlyTransactionDAO = readOnlyTransactionDAO;
    }

    @Override
    public Transaction proccesTransaction(Long recipientId, BigDecimal amount) {
        try {
            unitOfWork.begin(); // <- Обов’язково починаємо транзакцію

            Transaction transaction = Transaction.builder()
                    .sender_id(22L)
                    .recipient_id(recipientId)
                    .amount(amount)
                    .build();

            Customer recipient = unitOfWork.getCustomerDAO().findById(recipientId);
            recipient.setBalance(recipient.getBalance().add(amount));

            unitOfWork.getCustomerDAO().update(recipient);
            unitOfWork.getTransactionDAO().create(transaction);

            unitOfWork.commit(); // ← Фіксуємо зміни

            return transaction;
        } catch (Exception e) {
            try { unitOfWork.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction transferTrxBetweenCustomers(Long senderId, Long recipientId, BigDecimal amount) {
        try (unitOfWork) {
            unitOfWork.begin();
            Customer sender = unitOfWork.getCustomerDAO().findById(senderId);
            Customer recipient = unitOfWork.getCustomerDAO().findById(recipientId);

            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException();
            }

            // Зменшуємо баланс sender
            sender.setBalance(sender.getBalance().subtract(amount));
            unitOfWork.getCustomerDAO().update(sender);

            // Збільшуємо баланс recipient (правильно)
            recipient.setBalance(recipient.getBalance().add(amount));
            unitOfWork.getCustomerDAO().update(recipient);

            Transaction transaction = Transaction.builder()
                    .amount(amount)
                    .sender_id(senderId)
                    .recipient_id(recipientId)
                    .build();

            unitOfWork.getTransactionDAO().create(transaction);
            unitOfWork.commit();
            return transaction;

        } catch (SQLException e) {
            try {
                unitOfWork.rollback();
            } catch (SQLException ex) {}
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction getTrx(Long transactionId) {
        return readOnlyTransactionDAO.findById(transactionId);
    }

    @Override
    public void updateTrx(Transaction newTransaction) { // ?

        try (unitOfWork) {
            unitOfWork.begin();
            Transaction oldTransaction = unitOfWork.getTransactionDAO().findById(newTransaction.getId());
            if (oldTransaction == null) {
                throw new IllegalArgumentException("Transaction not found");
            }

            Customer sender = unitOfWork.getCustomerDAO().findById(newTransaction.getSender_id());
            Customer recipient = unitOfWork.getCustomerDAO().findById(newTransaction.getRecipient_id());
            if (sender == null || recipient == null) {
                throw new IllegalArgumentException("Sender or recipient not found");
            }

            sender.setBalance(sender.getBalance().add(oldTransaction.getAmount()));
            recipient.setBalance(recipient.getBalance().subtract(oldTransaction.getAmount()));

            if (sender.getBalance().compareTo(newTransaction.getAmount()) < 0) {
                throw new IllegalArgumentException("Not enough balance");
            }

            sender.setBalance(sender.getBalance().subtract(newTransaction.getAmount()));
            recipient.setBalance(recipient.getBalance().add(newTransaction.getAmount()));

            unitOfWork.getCustomerDAO().update(sender);
            unitOfWork.getCustomerDAO().update(recipient);

            unitOfWork.getTransactionDAO().update(newTransaction);

            unitOfWork.commit();
        } catch (SQLException e) {
            try {
                unitOfWork.rollback();
            } catch (SQLException ex) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTrx(Long transactionId) {
        try {
            unitOfWork.begin();
            Transaction transaction = unitOfWork.getTransactionDAO().findById(transactionId);
            Customer sender = unitOfWork.getCustomerDAO().findById(transaction.getSender_id());
            Customer recipient = unitOfWork.getCustomerDAO().findById(transaction.getRecipient_id());

            sender.setBalance(sender.getBalance().add(transaction.getAmount()));
            unitOfWork.getCustomerDAO().update(sender);

            recipient.setBalance(recipient.getBalance().subtract(transaction.getAmount()));
            unitOfWork.getCustomerDAO().update(recipient);

            unitOfWork.getTransactionDAO().delete(transactionId);

            unitOfWork.commit();
        } catch (SQLException e) {
            try {
                unitOfWork.rollback();
            } catch (SQLException ex) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> getAllTrx() {
        return readOnlyTransactionDAO.findAll();
    }

    public List<Transaction> getAllCustomerTransactions(Long customerId) {
        return readOnlyTransactionDAO.findAllTransactionsByCustomerId(customerId);
    }
}
