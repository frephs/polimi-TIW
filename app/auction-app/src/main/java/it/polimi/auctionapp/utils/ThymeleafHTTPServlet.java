package it.polimi.auctionapp.utils;

import com.google.gson.Gson;
import it.polimi.auctionapp.beans.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.Serial;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ThymeleafHTTPServlet extends HttpServlet {

    private TemplateEngine templateEngine = new TemplateEngine();
    protected final ContextAttributes contextAttributes = new ContextAttributes();

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        SQLConnectionHandler.getConnection();
    }

    public void processTemplate(
        HttpServletRequest request,
        HttpServletResponse response,
        String path
    ) {
        try {
            final JakartaServletWebApplication jakartaServletWebApplication =
                JakartaServletWebApplication.buildApplication(getServletContext());

            final IWebExchange webExchange = jakartaServletWebApplication.buildExchange(
                request,
                response
            );
            final IWebRequest webRequest = webExchange.getRequest();

            WebContext context = new WebContext(webExchange);

            response.setCharacterEncoding("UTF-8");
            contextAttributes.set("page", path);
            contextAttributes.set("user", (User) request.getSession().getAttribute("user"));
            contextAttributes.updateContext(request);
            if (
                ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) &&
                    response.getStatus() == HttpServletResponse.SC_OK &&
                    (path.startsWith("/sell") || path.startsWith("/buy"))) ||
                path.startsWith("/account/details")
            ) {
                response.setContentType("application/json");
                String json = new Gson().toJson(contextAttributes.attributesMap);
                response.getWriter().write(json);
                //templateEngine.process("controller", context, response.getWriter());
            } else {
                response.setContentType("text/html");
                if (
                    (path.startsWith("/sell") || path.startsWith("/buy")) ||
                    path.startsWith("/account/details")
                ) {
                    request
                        .getSession()
                        .setAttribute("from", request.getRequestURI().split("/")[2]);
                    sendRedirect(request, response, "/controller");
                    //templateEngine.process("controller", context, response.getWriter());
                } else {
                    templateEngine.process(path, context, response.getWriter());
                }
            }

            contextAttributes.clearContext(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRedirect(
        HttpServletRequest request,
        HttpServletResponse response,
        String path
    ) {
        try {
            contextAttributes.updateContext(request);
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("location", path);
            response.sendRedirect(getServletContext().getContextPath() + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalStateException ignored) {}
    }

    public void checkAllRequiredParams(
        HttpServletRequest request,
        HttpServletResponse response,
        String... requiredParams
    ) throws SQLException {
        List<String> missingParamList = new ArrayList<>();
        for (String param : requiredParams) {
            if (request.getParameter(param) == null || request.getParameter(param).isEmpty()) {
                missingParamList.add(param);
            }
        }

        if (!missingParamList.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new SQLException(
                "Bad request: missing required parameter: " + String.join(", ", missingParamList)
            );
        }
    }

    public static String breadCrumb(HttpServletRequest request) {
        String[] splitPath =
            (request.getContextPath() +
                request.getServletPath() +
                (request.getQueryString() != null ? ("?" + request.getQueryString()) : "")).split(
                    "/"
                );
        StringBuilder cumulativePath = new StringBuilder();
        return Arrays.stream(splitPath)
            .filter(s -> !s.isEmpty())
            .map(s -> {
                cumulativePath.append("/").append(s);
                return (
                    "<a href='" +
                    cumulativePath +
                    "'>" +
                    (s.contains("=")
                            ? (s.split("\\?")[0] +
                                (s.split("=").length > 1 ? (": " + s.split("=")[1]) : ""))
                            : s) +
                    "</a>"
                );
            })
            .collect(Collectors.joining(" > "));
    }

    public static class ContextAttributes {

        Map<String, Object> attributesMap = new HashMap<>();

        public void set(String attribute, Object value) {
            attributesMap.put(attribute, value);
        }

        public void setFlash(String attribute, Object value) {
            attributesMap.put("FLASH_" + attribute, value);
        }

        public void updateContext(HttpServletRequest request) {
            set("breadcrumb", breadCrumb(request));
            for (Map.Entry<String, Object> entry : attributesMap.entrySet()) {
                if (entry.getKey().startsWith("FLASH_")) {
                    request.getSession().setAttribute(entry.getKey(), entry.getValue());
                } else {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }

        public void clearContext(HttpServletRequest request) {
            attributesMap = new HashMap<>();
            if (request.getSession(false) != null) request
                .getSession()
                .getAttributeNames()
                .asIterator()
                .forEachRemaining(name -> {
                    if (name.startsWith("FLASH_")) {
                        request.getSession().removeAttribute(name);
                    }
                });
        }
    }
}
