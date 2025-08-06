package org.example.dao;

import org.example.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerDAOTest {

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        // general behaviour for tests
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        customerDAO = new CustomerDAO(connection);

    }

    @Test
    void testFindByIdReturnsCustomer() throws SQLException {
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("first_name")).thenReturn("Andrii");
        when(resultSet.getString("last_name")).thenReturn("Kachmar");
        when(resultSet.getString("email")).thenReturn("kachmar@gmail.com");
        when(resultSet.getBigDecimal("balance")).thenReturn(new BigDecimal("9400"));

        Customer customer = customerDAO.findById(1L);

        assertNotNull(customer);
        assertEquals(1L, customer.getId());
        assertEquals("Andrii", customer.getFirstName());
        assertEquals("Kachmar", customer.getLastName());
        assertEquals("kachmar@gmail.com", customer.getEmail());
        assertEquals(new BigDecimal("9400"), customer.getBalance());

        verify(statement).setLong(1, 1L);
        verify(statement).executeQuery();
    }

    @Test
    void testFindAllReturnsList() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("first_name")).thenReturn("John", "Jane");
        when(resultSet.getString("last_name")).thenReturn("Doe", "Smith");
        when(resultSet.getString("email")).thenReturn("john@example.com", "jane@example.com");
        when(resultSet.getBigDecimal("balance")).thenReturn(
                new BigDecimal("100.00"),
                new BigDecimal("200.00")
        );

        List<Customer> customers = customerDAO.findAll();

        assertEquals(2, customers.size());
        assertEquals("John", customers.get(0).getFirstName());
        assertEquals("Jane", customers.get(1).getFirstName());
        verify(statement, times(1)).executeQuery();
    }


    @Test
    void testUpdateCustomer() throws Exception {
        Customer input = new Customer();
        input.setId(1L);
        input.setFirstName("John");
        input.setLastName("Doe");
        input.setEmail("john@example.com");
        input.setBalance(new BigDecimal("100.00"));

        Customer updated = new Customer();
        updated.setId(1L);
        updated.setFirstName("Johnny");
        updated.setLastName("Doe");
        updated.setEmail("johnny@example.com");
        updated.setBalance(new BigDecimal("200.00"));

        CustomerDAO spyDao = spy(customerDAO);

        when(statement.execute()).thenReturn(true);
        doReturn(updated).when(spyDao).findById(1L);

        Customer result = spyDao.update(input);

        assertEquals("Johnny", result.getFirstName());
        assertEquals("johnny@example.com", result.getEmail());
        assertEquals(new BigDecimal("200.00"), result.getBalance());

        verify(statement).setString(1, "John");
        verify(statement).setString(2, "Doe");
        verify(statement).setString(3, "john@example.com");
        verify(statement).setBigDecimal(4, new BigDecimal("100.00"));
        verify(statement).setLong(5, 1L);
    }

    @Test
    void testCreateCustomer() throws SQLException {
        Customer input = new Customer();
        input.setFirstName("Andrii");
        input.setLastName("Kachmar");
        input.setEmail("kachmar@gmail.com");
        input.setBalance(new BigDecimal("1000.00"));

        CustomerDAO spyDao = spy(customerDAO);

        when(statement.execute()).thenReturn(true);
        doReturn(1).when(spyDao).getLastVal(anyString());
        doReturn(input).when(spyDao).findById(1L);

        Customer created = spyDao.create(input);

        assertNotNull(created);
        verify(statement).setString(1, "Andrii");
        verify(statement).setString(2, "Kachmar");
        verify(statement).setString(3, "kachmar@gmail.com");
        verify(statement).setBigDecimal(4, new BigDecimal("1000.00"));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        when(statement.execute()).thenReturn(true);

        customerDAO.delete(1L);

        verify(statement).setLong(1, 1L);
        verify(statement).execute();
    }

    @Test
    void testFindByIdWhenNoResult() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        Customer customer = customerDAO.findById(999L);

        assertNotNull(customer);
        assertNull(customer.getFirstName());
        verify(statement).setLong(1, 999L);
    }
}