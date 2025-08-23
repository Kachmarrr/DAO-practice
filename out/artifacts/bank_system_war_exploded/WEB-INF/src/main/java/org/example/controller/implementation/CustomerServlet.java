package org.example.controller.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dao.TransactionDAO;
import org.example.model.Customer;
import org.example.persistance.DatabaseConnectionManager;
import org.example.service.CustomerService;
import org.example.service.implementation.CustomerServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST-API servlet for work with Customer Objects
 * <p>
 * support CRUD operations:
 * <ul>
 *     <li>GET /customers — list of all customers</li>
 *     <li>GET /customers/{id} — get customer by id</li>
 *     <li>POST /customers — create new customer</li>
 *     <li>PUT /customers/{id} — update customer by id</li>
 *     <li>DELETE /customers/{id} — delete customer by id</li>
 * </ul>
 * <p>
 */
@WebServlet("/customers/*")
public class CustomerServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CustomerServlet.class.getName());

    private CustomerService customerService;
    private DatabaseConnectionManager databaseConnectionManager;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();

        Path path = Paths.get("/main/resources/db.properties");
        try {
            // consider moving DB config to web.xml / env vars
            databaseConnectionManager = DatabaseConnectionManager.fromClasspathResource("db.properties");
            Connection connection = databaseConnectionManager.getConnection();
            TransactionDAO transactionDAO = new TransactionDAO(connection);
            customerService = new CustomerServiceImpl(connection, transactionDAO);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to initialize DB connection", e);
            throw new ServletException("Cannot connect to database", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = normalizePath(request.getPathInfo());

            if (pathInfo == null) {
                List<Customer> customers = customerService.findAllCustomers();
                writeJson(response, HttpServletResponse.SC_OK, customers);
                return;
            }

            Long id = parseIdFromPath(pathInfo);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid id in path");
                return;
            }

            Customer customer = customerService.findCustomerById(id);
            if (customer == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                return;
            }

            writeJson(response, HttpServletResponse.SC_OK, customer);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "GET /customers error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Customer customer = readJson(request, Customer.class);
            if (customer == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty or invalid");
                return;
            }

            Customer created = customerService.createCustomer(customer);
            writeJson(response, HttpServletResponse.SC_CREATED, created);
        } catch (JsonSyntaxException jse) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "POST /customers error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            String pathInfo = normalizePath(request.getPathInfo());
            Long id = parseIdFromPath(pathInfo);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing id in path");
                return;
            }

            Customer bodyCustomer = readJson(request, Customer.class);
            if (bodyCustomer == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty or invalid");
                return;
            }

            // enforce id consistency
            bodyCustomer.setId(id);

            Customer existing = customerService.findCustomerById(id);
            if (existing == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                return;
            }

            customerService.updateCustomer(bodyCustomer);
            writeJson(response, HttpServletResponse.SC_OK, bodyCustomer);

        } catch (JsonSyntaxException jse) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "PUT /customers/{id} error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = normalizePath(request.getPathInfo());
            Long id = parseIdFromPath(pathInfo);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing id in path");
                return;
            }

            Customer existing = customerService.findCustomerById(id);
            if (existing == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                return;
            }

            customerService.deleteCustomer(id);
            // no body for 204
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DELETE /customers/{id} error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ---------- helpers ----------

    private String normalizePath(String pathInfo) {
        if (pathInfo == null || "/".equals(pathInfo)) return null;
        return pathInfo;
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null) return null;
        String p = pathInfo;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.isEmpty()) return null;
        try {
            return Long.valueOf(p);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private <T> T readJson(HttpServletRequest req, Class<T> cls) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            return gson.fromJson(reader, cls);
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object obj) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setStatus(status);
        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(obj));
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            Map<String, String> err = new HashMap<>();
            err.put("error", message == null ? "" : message);
            writeJson(resp, status, err);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, "Failed to write error response", ioe);
        }
    }

    private String safeMessage(Throwable t) {
        return t == null ? "" : (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
    }
}
