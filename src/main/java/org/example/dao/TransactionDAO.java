package org.example.dao;

import org.example.model.Transaction;
import org.example.util.DataAccsessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO extends DataAccsessObject<Transaction> {

    private static final String READ_ONE = "SELECT id, amount, sender_id, recipient_id FROM transaction WHERE id = ?";
    private static final String READ_ALL = "SELECT id, amount, sender_id, recipient_id FROM transaction";
    private static final String INSERT = "INSERT INTO transaction (amount, sender_id, recipient_id) VALUES (?, ?, ?)";
    private static final String DELETE = "DELETE FROM transaction WHERE id = ?";
    private static final String UPDATE = "UPDATE transaction SET amount = ?, sender_id = ?, recipient_id = ? WHERE id = ?";
    private static final String READ_ALL_FOR = "SELECT id, amount, sender_id, recipient_id FROM transaction WHERE sender_id = ?";

    public TransactionDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Transaction findById(long id) {
        Transaction transaction = new Transaction();
        try (PreparedStatement statement = this.connection.prepareStatement(READ_ONE)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                transaction.setId(resultSet.getLong("id"));
                transaction.setAmount(resultSet.getBigDecimal("amount"));
                transaction.setSender_id(resultSet.getLong("sender_id"));
                transaction.setRecipient_id(resultSet.getLong("recipient_id"));
                return transaction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement statement = this.connection.prepareStatement(READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(resultSet.getLong("id"));
                transaction.setAmount(resultSet.getBigDecimal("amount"));
                transaction.setSender_id(resultSet.getLong("sender_id"));
                transaction.setRecipient_id(resultSet.getLong("recipient_id"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return transactions;
    }

    @Override
    public Transaction update(Transaction dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(UPDATE)) {
            statement.setBigDecimal(1, dto.getAmount());
            statement.setLong(2, dto.getSender_id());
            statement.setLong(3, dto.getRecipient_id());
            statement.setLong(4, dto.getId());
            statement.execute();

            Transaction transaction = this.findById(dto.getId());
            return transaction;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public Transaction create(Transaction dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(INSERT)) {

            statement.setBigDecimal(1, dto.getAmount());
            statement.setLong(2, dto.getSender_id());
            statement.setLong(3, dto.getRecipient_id());
            statement.execute();

            long id = this.getLastVal(TRANSACTION_SEQUENCE);
            return this.findById(id);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(long id) {
        try (PreparedStatement statement = this.connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public List<Transaction> findAllTransactionsByCustomerId(long sender_id) {
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement statement = this.connection.prepareStatement(READ_ALL_FOR)) {
            statement.setLong(1, sender_id);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Transaction transaction = new Transaction();

                transaction.setId(resultSet.getLong("id"));
                transaction.setAmount(resultSet.getBigDecimal("amount"));
                transaction.setSender_id(resultSet.getLong("sender_id"));
                transaction.setRecipient_id(resultSet.getLong("recipient_id"));
                transactions.add(transaction);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return transactions;


    }
}
