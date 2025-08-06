package org.example.dao;

import org.example.model.Bank;
import org.example.model.Customer;
import org.example.util.DataAccsessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankDAO extends DataAccsessObject<Bank> {

    private static final String GET_ONE = "SELECT id, name FROM bank WHERE id=?";
    private static final String CREATE = "INSERT INTO bank (name) VALUES (?)";
    private static final String DELETE = "DELETE FROM bank WHERE id = ?";
    private static final String UPDATE = "UPDATE bank SET name = ? WHERE id = ?";
    private static final String READ_ALL = "SELECT id, name FROM bank";

    public BankDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Bank findById(long id) {
        Bank bank = new Bank();
        try (PreparedStatement statement = this.connection.prepareStatement(GET_ONE)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {

                bank.setId(resultSet.getInt("id"));
                bank.setName(resultSet.getString("name"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return bank;
    }

    @Override
    public List<Bank> findAll() {
        List<Bank> banks = new ArrayList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Bank bank = new Bank();
                bank.setId(resultSet.getLong("id"));
                bank.setName(resultSet.getString("name"));
                banks.add(bank);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return banks;
    }

    @Override
    public Bank update(Bank dto) {
        Bank bank = null;
        try (PreparedStatement statement = this.connection.prepareStatement(UPDATE)) {

            statement.setString(1, dto.getName());
            statement.setLong(2, dto.getId());
            statement.execute();
            bank = this.findById(dto.getId());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return bank;
    }

    @Override
    public Bank create(Bank dto) {
        try (PreparedStatement statement = this.connection.prepareStatement(CREATE)) {
            statement.setString(1, dto.getName());
            statement.execute();

            int id = this.getLastVal(BANK_SEQUENCE);
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
}
