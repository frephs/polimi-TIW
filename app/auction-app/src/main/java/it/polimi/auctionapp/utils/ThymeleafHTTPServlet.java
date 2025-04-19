package it.polimi.auctionapp.utils;

import jakarta.servlet.ServletException;
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

    public void processTemplate(HttpServletRequest request, HttpServletResponse response, String path) {
        try {
            final JakartaServletWebApplication jakartaServletWebApplication = JakartaServletWebApplication.buildApplication(getServletContext());

            final IWebExchange webExchange = jakartaServletWebApplication.buildExchange(request, response);
            final IWebRequest webRequest = webExchange.getRequest();

            WebContext context = new WebContext(webExchange);

            response.setContentType("text/html");
            request.getSession().setAttribute("breadcrumb", breadCrumb(request.getServletPath()));
            templateEngine.process(path, context, response.getWriter());

            request.getSession().setAttribute("message", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String path) {
        try{
            response.sendRedirect(getServletContext().getContextPath()  + path);
            //request.getSession().setAttribute("message", null);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    //TODO: add get parameters handling 
    public String breadCrumb(String servletPath) {
        String[] splitPath = (getServletContext().getContextPath() + "/" + servletPath).split("/");

        return Arrays.stream(splitPath).filter(
                s -> !s.isEmpty()
        ).flatMap(
                s -> Arrays.stream(s.split("="))
        ).map(
                s -> "<a>"

                        + s + "</a>"
        ).collect(Collectors.joining(">"));
    }


}
