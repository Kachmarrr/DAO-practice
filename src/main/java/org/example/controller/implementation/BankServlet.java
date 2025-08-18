package org.example.controller.implementation;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import org.example.controller.AbstractServlet;
import org.example.controller.ServletService;
import org.example.dao.BankDAO;
import org.example.dao.CustomerDAO;
import org.example.model.Bank;
import org.example.persistance.DatabaseConnectionManager;
import org.example.service.BankService;
import org.example.service.implementation.BankServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * REST-API servlet for work with Bank Objects
 * <p>
 * support CRUD operations:
 * <ul>
 *     <li>GET /banks — list of all bank</li>
 *     <li>GET /banks/{id} — get bank by id</li>
 *     <li>POST /banks — create new bank</li>
 *     <li>PUT /banks/{id} — update bank by id</li>
 *     <li>DELETE /banks/{id} — delete bank by id</li>
 * </ul>
 * <p>
 */
@WebServlet("/banks/*")
public class BankServlet extends AbstractServlet<Bank> {

    private BankService bankService;
    private ServletService<Bank> servletService;
    private DatabaseConnectionManager databaseConnectionManager;

    @Override
    public void init() throws ServletException {
        super.init();

        databaseConnectionManager = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        Connection connection;
        try {
            connection = databaseConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        BankDAO bankDAO = new BankDAO(connection);
        CustomerDAO customerDAO = new CustomerDAO(connection);
        bankService = new BankServiceImpl(bankDAO, customerDAO);

        servletService = new ServletService<Bank>() {
            @Override
            public List<Bank> findAll() {
               return bankService.findAllBanks();
            }

            @Override
            public Bank findById(Long id) {
                return bankService.findBankById(id);
            }

            @Override
            public Bank create(Bank model) {
                return bankService.createBank(model);
            }

            @Override
            public void update(Bank model) {
                bankService.updateBank(model);
            }

            @Override
            public void delete(Long id) {
                bankService.deleteBank(id);
            }
        };
    }
    @Override
    protected ServletService<Bank> getCRUD() {
        return servletService;
    }

    @Override
    protected Class<Bank> getModelClass() {
        return Bank.class;
    }


}
