package org.example.dao;

import org.example.model.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @Spy
    @InjectMocks
    private BankDAO bankDAO;

    private static final String GET_ONE_SQL = "SELECT id, name FROM bank WHERE id=?";
    private static final String READ_ALL_SQL = "SELECT id, name FROM bank";
    private static final String CREATE_SQL = "INSERT INTO bank (name) VALUES (?)";
    private static final String UPDATE_SQL = "UPDATE bank SET name = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM bank WHERE id = ?";

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(connection.prepareStatement(anyString())).thenReturn(stmt);
        lenient().when(stmt.executeQuery()).thenReturn(rs);
        lenient().when(stmt.execute()).thenReturn(true);
    }

    @Test
    void testFindById_Found() throws Exception {
        long id = 5L;
        String name = "Test Bank";

        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getLong("id")).thenReturn(id);
        when(rs.getString("name")).thenReturn(name);

        Bank result = bankDAO.findById(id);

        assertEquals(id, result.getId());
        assertEquals(name, result.getName());

        verify(connection).prepareStatement(GET_ONE_SQL);
        verify(stmt).setLong(1, id);
        verify(stmt).executeQuery();
    }

    @Test
    void testFindById_NotFound() throws Exception {
        long id = 99L;
        when(rs.next()).thenReturn(false);

        Bank result = bankDAO.findById(id);

        assertNotNull(result);
        assertEquals(0, result.getId());
        assertNull(result.getName());

        verify(stmt).executeQuery();
    }

    @Test
    void testFindAll() throws Exception {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("name")).thenReturn("A", "B");

        var list = bankDAO.findAll();

        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals("A", list.get(0).getName());
        assertEquals(2L, list.get(1).getId());
        assertEquals("B", list.get(1).getName());

        verify(connection).prepareStatement(READ_ALL_SQL);
        verify(stmt).executeQuery();
    }

    @Test
    void testUpdate() throws Exception {
        long id = 7L;
        Bank input = new Bank().setName("NewName");
        input.setId(id);
        Bank updated = new Bank().setName("NewName");
        updated.setId(id);

        doReturn(updated).when(bankDAO).findById(id);

        Bank result = bankDAO.update(input);

        assertEquals(updated, result);

        verify(connection).prepareStatement(UPDATE_SQL);
        verify(stmt).setString(1, "NewName");
        verify(stmt).setLong(2, id);
        verify(stmt).execute();
        verify(bankDAO).findById(id);
    }

    @Test
    void testCreate() throws Exception {
        Bank input = new Bank().setName("CreatedBank");
        Long fakeId = 42L;

        doReturn(fakeId).when(bankDAO).getLastVal(anyString());
        Bank created = new Bank().setName("CreatedBank");
        created.setId(fakeId);
        doReturn(created).when(bankDAO).findById(fakeId);

        Bank result = bankDAO.create(input);

        assertEquals(fakeId, result.getId());
        assertEquals("CreatedBank", result.getName());

        verify(connection).prepareStatement(CREATE_SQL);
        verify(stmt).setString(1, "CreatedBank");
        verify(stmt).execute();
        verify(bankDAO).getLastVal(anyString());
        verify(bankDAO).findById(fakeId);
    }

    @Test
    void testDelete() throws Exception {
        long id = 13L;

        bankDAO.delete(id);

        verify(connection).prepareStatement(DELETE_SQL);
        verify(stmt).setLong(1, id);
        verify(stmt).execute();
    }
}
