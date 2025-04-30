package it.polimi.auctionapp.utils;

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
import java.util.Arrays;
import java.util.stream.Collectors;

public class ThymeleafHTTPServlet extends HttpServlet {

    TemplateEngine templateEngine = new TemplateEngine();

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
            response.setContentType("text/html");

            request.getSession().setAttribute("breadcrumb", breadCrumb(request));
            templateEngine.process(path, context, response.getWriter());
            //TODO: fix
            request.getSession().setAttribute("message", null);
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
            response.sendRedirect(getServletContext().getContextPath() + path);
            //request.getSession().setAttribute("message", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String breadCrumb(HttpServletRequest request) {
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
                                ": " +
                                (s.split("=").length > 1 ? s.split("=")[1] : ""))
                            : s) +
                    "</a>"
                );
            })
            .collect(Collectors.joining(" > "));
    }
}
