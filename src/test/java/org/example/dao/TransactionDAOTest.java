package org.example.dao;

import org.example.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionDAOTest {

    private static final String READ_ONE_SQL = "SELECT id, amount, sender_id, recipient_id FROM transaction WHERE id = ?";
    private static final String READ_ALL_SQL = "SELECT id, amount, sender_id, recipient_id FROM transaction";
    private static final String INSERT_SQL = "INSERT INTO transaction (amount, sender_id, recipient_id) VALUES (?, ?, ?)";
    private static final String DELETE_SQL = "DELETE FROM transaction WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE transaction SET amount = ?, sender_id = ?, recipient_id = ? WHERE id = ?";
    private static final String READ_ALL_FOR_SQL = "SELECT id, amount, sender_id, recipient_id FROM transaction WHERE sender_id = ?";

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement stmt;
    @Mock
    private ResultSet rs;

    @Spy
    @InjectMocks
    private TransactionDAO transactionDAO;

    @BeforeEach
    void setUp() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(stmt.execute()).thenReturn(true);
    }

    @Test
    void testFindById_Found() throws Exception {
        long id = 10L;
        BigDecimal amount = BigDecimal.valueOf(123.45);
        long sender = 1L;
        long recipient = 2L;

        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getLong("id")).thenReturn(id);
        when(rs.getBigDecimal("amount")).thenReturn(amount);
        when(rs.getLong("sender_id")).thenReturn(sender);
        when(rs.getLong("recipient_id")).thenReturn(recipient);

        Transaction tx = transactionDAO.findById(id);

        assertNotNull(tx);
        assertEquals(id, tx.getId());
        assertEquals(amount, tx.getAmount());
        assertEquals(sender, tx.getSender_id());
        assertEquals(recipient, tx.getRecipient_id());

        verify(connection).prepareStatement(READ_ONE_SQL);
        verify(stmt).setLong(1, id);
        verify(stmt).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws Exception {
        when(rs.next()).thenReturn(false);

        Transaction tx = transactionDAO.findById(99L);

        assertNull(tx);
        verify(connection).prepareStatement(READ_ONE_SQL);
        verify(stmt).setLong(1, 99L);
        verify(stmt).executeQuery();
    }

    @Test
    void testFindById_SQLException() throws Exception {
        when(connection.prepareStatement(READ_ONE_SQL)).thenThrow(new SQLException("fail"));
        assertThrows(RuntimeException.class, () -> transactionDAO.findById(1L));
    }

    @Test
    void testFindAll_Empty() throws Exception {
        when(rs.next()).thenReturn(false);
        List<Transaction> list = transactionDAO.findAll();
        assertTrue(list.isEmpty());
        verify(connection).prepareStatement(READ_ALL_SQL);
        verify(stmt).executeQuery();
    }

    @Test
    void testFindAll_Multiple() throws Exception {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getBigDecimal("amount")).thenReturn(BigDecimal.ONE, BigDecimal.TEN);
        when(rs.getLong("sender_id")).thenReturn(100L, 200L);
        when(rs.getLong("recipient_id")).thenReturn(300L, 400L);

        List<Transaction> list = transactionDAO.findAll();

        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(BigDecimal.ONE, list.get(0).getAmount());

        assertEquals(2L, list.get(1).getId());
        assertEquals(BigDecimal.TEN, list.get(1).getAmount());

        verify(connection).prepareStatement(READ_ALL_SQL);
        verify(stmt).executeQuery();
    }

    @Test
    void testCreate() throws Exception {
        BigDecimal amount = BigDecimal.valueOf(50);
        long sender = 5L;
        long recipient = 6L;
        Transaction input = new Transaction().setAmount(amount).setSender_id(sender).setRecipient_id(recipient);
        long fakeId = 77L;
        Transaction created = new Transaction().setAmount(amount).setSender_id(sender).setRecipient_id(recipient);
        created.setId(fakeId);
        doReturn((int)fakeId).when(transactionDAO).getLastVal(anyString());
        doReturn(created).when(transactionDAO).findById(fakeId);

        Transaction tx = transactionDAO.create(input);

        assertNotNull(tx);
        assertEquals(fakeId, tx.getId());
        assertEquals(amount, tx.getAmount());
        assertEquals(sender, tx.getSender_id());
        assertEquals(recipient, tx.getRecipient_id());

        verify(connection).prepareStatement(INSERT_SQL);
        verify(stmt).setBigDecimal(1, amount);
        verify(stmt).setLong(2, sender);
        verify(stmt).setLong(3, recipient);
        verify(stmt).execute();
        verify(transactionDAO).getLastVal(anyString());
        verify(transactionDAO).findById(fakeId);
    }

    @Test
    void testUpdate() throws Exception {
        long id = 88L;
        BigDecimal newAmount = BigDecimal.valueOf(99);
        long sender = 10L;
        long recipient = 20L;
        Transaction input = new Transaction().setAmount(newAmount).setSender_id(sender).setRecipient_id(recipient);
        input.setId(id);
        Transaction updated = new Transaction().setAmount(newAmount).setSender_id(sender).setRecipient_id(recipient);
        updated.setId(id);

        doReturn(updated).when(transactionDAO).findById(id);

        Transaction result = transactionDAO.update(input);

        assertNotNull(result);
        assertEquals(updated, result);

        verify(connection).prepareStatement(UPDATE_SQL);
        verify(stmt).setBigDecimal(1, newAmount);
        verify(stmt).setLong(2, sender);
        verify(stmt).setLong(3, recipient);
        verify(stmt).setLong(4, id);
        verify(stmt).execute();
        verify(transactionDAO).findById(id);
    }

    @Test
    void testDelete() throws Exception {
        long id = 55L;
        transactionDAO.delete(id);

        verify(connection).prepareStatement(DELETE_SQL);
        verify(stmt).setLong(1, id);
        verify(stmt).execute();
    }

    @Test
    void testFindAllForCustomer_Empty() throws Exception {
        long sender = 123L;
        when(rs.next()).thenReturn(false);

        List<Transaction> list = transactionDAO.findAllTransactionsByCustomerId(sender);

        assertTrue(list.isEmpty());
        verify(connection).prepareStatement(READ_ALL_FOR_SQL);
        verify(stmt).setLong(1, sender);
        verify(stmt).executeQuery();
    }

    @Test
    void testFindAllForCustomer_Multiple() throws Exception {
        long sender = 321L;
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(10L, 20L);
        when(rs.getBigDecimal("amount")).thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(5));
        when(rs.getLong("sender_id")).thenReturn(sender, sender);
        when(rs.getLong("recipient_id")).thenReturn(100L, 200L);

        List<Transaction> list = transactionDAO.findAllTransactionsByCustomerId(sender);

        assertEquals(2, list.size());
        assertEquals(10L, list.get(0).getId());
        assertEquals(BigDecimal.ZERO, list.get(0).getAmount());
        assertEquals(20L, list.get(1).getId());
        assertEquals(BigDecimal.valueOf(5), list.get(1).getAmount());

        verify(connection).prepareStatement(READ_ALL_FOR_SQL);
        verify(stmt).setLong(1, sender);
        verify(stmt).executeQuery();
    }
}
