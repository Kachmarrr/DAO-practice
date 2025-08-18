package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.util.DataTransferObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractServlet<T extends DataTransferObject> extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AbstractServlet.class.getName());
    protected Gson gson;

    protected abstract CRUD<T> getCRUD();
    protected abstract Class<T> getModelClass();


    @Override
    public void init() throws ServletException {
        super.init();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String pathInfo = checkPath(request.getPathInfo());

            if (pathInfo == null) {
                List<T> all = getCRUD().findAll();
                writeJson(response, HttpServletResponse.SC_OK, all);
                return;
            }

            Long id = parseIdFromPath(pathInfo);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid id in path");
                return;
            }

            T found = getCRUD().findById(id);
            if (found == null) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
                return;
            }

            writeJson(response, HttpServletResponse.SC_OK, found);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "GET error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, safeMessage(e));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            T body = readJson(request, getModelClass());
            if (body == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty or invalid");
                return;
            }

            T created = getCRUD().create(body);
            writeJson(response, HttpServletResponse.SC_CREATED, created);
        } catch (JsonSyntaxException jse) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "POST error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, safeMessage(e));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String path = checkPath(req.getPathInfo());
            Long id = parseIdFromPath(path);
            if (id == null) { writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing id"); return; }

            T body = readJson(req, getModelClass());
            if (body == null) { writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Empty body"); return; }

            // Ось воно — встановлюємо id з URL, ігноруючи те, що прийшло в JSON
            body.setId(id);

            T existing = getCRUD().findById(id);
            if (existing == null) { writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found"); return; }

            getCRUD().update(body);
            writeJson(resp, HttpServletResponse.SC_OK, body);

        } catch (JsonSyntaxException jse) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + safeMessage(jse));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "PUT error", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, safeMessage(e));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String pathInfo = checkPath(request.getPathInfo());
            Long id = parseIdFromPath(pathInfo);
            if (id == null) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing id in path");
                return;
            }

            getCRUD().delete(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "DELETE error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, safeMessage(e));
        }
    }

    protected String checkPath(String pathInfo) {
        if (pathInfo == null || "/".equals(pathInfo)) return null;
        return pathInfo;
    }

    protected Long parseIdFromPath(String pathInfo) {
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

    protected T readJson(HttpServletRequest req, Class<T> tClass) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            return gson.fromJson(reader, tClass);
        }
    }

    protected void writeJson(HttpServletResponse response, int status, Object object) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(object));
        }
    }

    protected void writeError(HttpServletResponse response, int status, String message) {
        try {
            Map<String, String> err = new HashMap<>();
            err.put("error", message == null ? "" : message);
            writeJson(response, status, err);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, "Failed to write error response", ioe);
        }
    }

    protected String safeMessage(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

}
