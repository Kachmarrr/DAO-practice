package org.example.controller.implementation;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import org.example.controller.AbstractServlet;
import org.example.controller.ServletService;
import org.example.model.Transaction;
import org.example.persistance.DatabaseConnectionManager;
import org.example.persistance.JdbcUnitOfWork;
import org.example.persistance.UnitOfWork;
import org.example.service.TransactionService;
import org.example.service.implementation.TransactionServiceImpl;

import java.util.List;

/**
 * REST-API servlet for work with Transaction Objects
 * <p>
 * support CRUD operations:
 * <ul>
 *     <li>GET /transactions — list of all transactions</li>
 *     <li>GET /transactions/{id} — get transaction by id</li>
 *     <li>POST /transactions — create new transaction between customers</li>
 *     <li>PUT /transactions/{id} — update transaction by id</li>
 *     <li>DELETE /transactions/{id} — delete transaction by id</li>
 * </ul>
 * <p>
 */
@WebServlet("/transactions/*")
public class TransactionServlet extends AbstractServlet<Transaction> {

    private TransactionService transactionService;
    private DatabaseConnectionManager databaseConnectionManager;
    private ServletService servletService;

    @Override
    public void init() throws ServletException {
        super.init();

        databaseConnectionManager = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

        UnitOfWork unitOfWork = new JdbcUnitOfWork(databaseConnectionManager);
        transactionService = new TransactionServiceImpl(unitOfWork);

        servletService = new ServletService<Transaction>() {

            @Override
            public List<Transaction> findAll() {
                return transactionService.getAllTrx();
            }

            @Override
            public Transaction findById(Long id) {
                return transactionService.getTrx(id);
            }

            @Override
            public Transaction create(Transaction entity) {
                return transactionService.transferTrxBetweenCustomers(entity.getSender_id(), entity.getRecipient_id(), entity.getAmount());
            }

            @Override
            public void update(Transaction entity) {
                transactionService.updateTrx(entity);
            }

            @Override
            public void delete(Long id) {
                transactionService.deleteTrx(id);
            }
        };
    }

    @Override
    protected ServletService getCRUD() {
        return servletService;
    }

    @Override
    protected Class getModelClass() {
        return Transaction.class;
    }
}
