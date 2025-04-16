package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class Landing {
    @WebServlet("/welcome")
    public static class LandingPageServlet extends ThymeleafHTTPServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            processTemplate("index", request, response);
        }
    }

    @WebServlet("/")
    public static class DefaultRedirectServlet extends ThymeleafHTTPServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.sendRedirect("welcome");
        }
    }

    @WebServlet("/design")
    public static class DesignTemplateServlet extends ThymeleafHTTPServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            processTemplate("page-template", request, response);
        }
    }
}