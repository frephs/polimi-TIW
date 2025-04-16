package it.polimi.auctionapp.utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;


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

    public void processTemplate(String path, HttpServletRequest request, HttpServletResponse response) {
        try {
            final JakartaServletWebApplication jakartaServletWebApplication = JakartaServletWebApplication.buildApplication(getServletContext());

            final IWebExchange webExchange = jakartaServletWebApplication.buildExchange(request, response);
            final IWebRequest webRequest = webExchange.getRequest();

            WebContext context = new WebContext(webExchange);

            templateEngine.process(path, context, response.getWriter());
            response.setContentType("text/html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        }

    }
