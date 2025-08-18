package org.example.controller.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dao.BankDAO;
import org.example.dao.CustomerDAO;
import org.example.model.Bank;
import org.example.persistance.DatabaseConnectionManager;
import org.example.service.BankService;
import org.example.service.implementation.BankServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST-API servlet for work with Bank Objects
 * <p>
 * support CRUD operations:
 * <ul>
 *     <li>GET /banks — list of all banks</li>
 *     <li>GET /banks/{id} — get bank by id</li>
 *     <li>POST /banks — create new bank</li>
 *     <li>PUT /banks/{id} — update bank by id</li>
 *     <li>DELETE /banks/{id} — delete bank by id</li>
 * </ul>
 * <p>
 */
//@WebServlet("/banks/*")
public class BankServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BankServlet.class.getName());

    private BankService bankService;
    private DatabaseConnectionManager databaseConnectionManager;
    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        gson = new Gson();

        try {
            databaseConnectionManager = new DatabaseConnectionManager("localhost", "postgres", "postgres", "1234");

            Connection connection = databaseConnectionManager.getConnection();

            BankDAO bankDAO = new BankDAO(connection);
            CustomerDAO customerDAO = new CustomerDAO(connection);

            bankService = new BankServiceImpl(bankDAO, customerDAO);

        } catch (SQLException e) {
            throw new ServletException("Cannot connect to database", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String path = checkPath(request.getPathInfo());

            if (path == null) {
                List<Bank> banks = bankService.findAllBanks();
                writeJson(response, HttpServletResponse.SC_OK, banks);
            }

            Long id = parseIdFromPath(path);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid id in path");
            }

            Bank bank = bankService.findBankById(id);
            if (bank == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Bank not found");
            }

            writeJson(response, HttpServletResponse.SC_OK, bank);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "GET /banks error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Bank bank = readJson(request, Bank.class);
            if (bank == null) {
                writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request body is empty");
                return;
            }

            Bank created = bankService.createBank(bank);

            writeJson(response, HttpServletResponse.SC_CREATED, created);
        } catch (JsonSyntaxException jse) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"POST /bank error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String path = checkPath(request.getPathInfo());
            Long id = parseIdFromPath(path);

            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing id in path");
                return;
            }

            Bank bank = readJson(request, Bank.class);
            if (bank == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty or invalid");
                return;
            }

            bank.setId(id);

            Bank bankToExist = bankService.findBankById(id);
            if (bankToExist == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Bank not found");
                return;
            }

            bankService.updateBank(bank);
            writeJson(response, HttpServletResponse.SC_OK, bank);

        } catch (JsonSyntaxException jse) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"PUT /bank error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String path = checkPath(request.getPathInfo());
            Long id = parseIdFromPath(path);
            if (path == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing path");
                return;
            }

            Bank bank = bankService.findBankById(id);
            if (bank == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Bank not found");
                return;
            }

            bankService.deleteBank(id);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DELETE /banks/{id} error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    private void writeJson(HttpServletResponse response, int status, Object object) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(status);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(gson.toJson(object));
        }

    }

    private Bank readJson(HttpServletRequest req, Class<Bank> bank) throws IOException {

        BufferedReader reader = req.getReader();
        return gson.fromJson(reader, bank);

    }


    private Long parseIdFromPath(String path) {

        if (path == null) return null;

        if (path.startsWith("/")) path = path.substring(1);

        if (path.isEmpty()) return null;

        return Long.valueOf(path);
    }

    private String checkPath(String path) {
        if (path == null) {
            return null;
        } else if (path.equals("/")) {
            return null;
        } else {
            return path;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) {
        try {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", message);
            writeJson(response, status, errorMap);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, "Failed to write error response", ioe);
        }
    }

    /**
     * Returns a “safe” message for the exception.
     *
     * - If the passed object == null, returns an empty string.
     * - If the message in the exception == null or empty, returns the simple name of the exception class.
     * - Otherwise, returns message.
     *
     * This is useful for logging or responding to the client when we don't want to get null values.
     */
    private String safeMessage(Throwable t) {
        if (t == null) {
            return "";
        }

        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) {
            return t.getClass().getSimpleName();
        }

        return msg;
    }


}
