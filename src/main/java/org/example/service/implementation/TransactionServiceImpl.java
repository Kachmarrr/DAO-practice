package org.example.service.implementation;

import org.example.model.Customer;
import org.example.model.Transaction;
import org.example.persistance.JdbcUnitOfWork;
import org.example.persistance.UnitOfWork;
import org.example.service.TransactionService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final UnitOfWork unitOfWork;

    public TransactionServiceImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Transaction proccesTransaction(Long recipientId, BigDecimal amount) {
        try {
            unitOfWork.begin();

            Transaction transaction = Transaction.builder()
                    .sender_id(22L)
                    .recipient_id(recipientId)
                    .amount(amount)
                    .build();

            Customer recipient = unitOfWork.getCustomerDAO().findById(recipientId);
            recipient.setBalance(recipient.getBalance().add(amount));
            unitOfWork.getCustomerDAO().update(recipient);

            unitOfWork.getTransactionDAO().create(transaction);
            unitOfWork.commit();

            return transaction;
        } catch (Exception e) {
            try { unitOfWork.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction transferTrxBetweenCustomers(Long senderId, Long recipientId, BigDecimal amount) {
        try {
            unitOfWork.begin();

            Customer sender = unitOfWork.getCustomerDAO().findById(senderId);
            Customer recipient = unitOfWork.getCustomerDAO().findById(recipientId);

            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Not enough balance");
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            recipient.setBalance(recipient.getBalance().add(amount));

            unitOfWork.getCustomerDAO().update(sender);
            unitOfWork.getCustomerDAO().update(recipient);

            Transaction transaction = Transaction.builder()
                    .sender_id(senderId)
                    .recipient_id(recipientId)
                    .amount(amount)
                    .build();

            unitOfWork.getTransactionDAO().create(transaction);
            unitOfWork.commit();

            return transaction;
        } catch (Exception e) {
            try { unitOfWork.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction getTrx(Long transactionId) {
        try {
            unitOfWork.begin();
            return unitOfWork.getTransactionDAO().findById(transactionId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateTrx(Transaction newTransaction) {
        try {
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

            // Відкат старої транзакції
            sender.setBalance(sender.getBalance().add(oldTransaction.getAmount()));
            recipient.setBalance(recipient.getBalance().subtract(oldTransaction.getAmount()));

            // Перевірка нового балансу
            if (sender.getBalance().compareTo(newTransaction.getAmount()) < 0) {
                throw new IllegalArgumentException("Not enough balance");
            }

            // Застосування нової транзакції
            sender.setBalance(sender.getBalance().subtract(newTransaction.getAmount()));
            recipient.setBalance(recipient.getBalance().add(newTransaction.getAmount()));

            unitOfWork.getCustomerDAO().update(sender);
            unitOfWork.getCustomerDAO().update(recipient);
            unitOfWork.getTransactionDAO().update(newTransaction);

            unitOfWork.commit();
        } catch (SQLException e) {
            try { unitOfWork.rollback(); } catch (SQLException ignore) {}
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
            recipient.setBalance(recipient.getBalance().subtract(transaction.getAmount()));

            unitOfWork.getCustomerDAO().update(sender);
            unitOfWork.getCustomerDAO().update(recipient);
            unitOfWork.getTransactionDAO().delete(transactionId);

            unitOfWork.commit();
        } catch (SQLException e) {
            try { unitOfWork.rollback(); } catch (SQLException ignore) {}
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> getAllTrx() {
        try {
            unitOfWork.begin();
            return unitOfWork.getTransactionDAO().findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Transaction> getAllCustomerTransactions(Long customerId) {
        try {
            unitOfWork.begin();
            return unitOfWork.getTransactionDAO().findAllTransactionsByCustomerId(customerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
