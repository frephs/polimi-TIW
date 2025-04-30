package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class Landing {

    @WebServlet("/")
    public static class DefaultRedirectServlet extends ThymeleafHTTPServlet {

        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.sendRedirect("welcome");
        }
    }

    @WebServlet("/welcome")
    public static class LandingPageServlet extends ThymeleafHTTPServlet {

        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            processTemplate(request, response, "index");
        }
    }

    @WebServlet("/design")
    public static class DesignTemplateServlet extends ThymeleafHTTPServlet {

        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            processTemplate(request, response, "design");
        }
    }

    @WebServlet("/template/")
    public static class TemplateServlet extends ThymeleafHTTPServlet {

        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            processTemplate(request, response, "page-template");
        }
    }
}
