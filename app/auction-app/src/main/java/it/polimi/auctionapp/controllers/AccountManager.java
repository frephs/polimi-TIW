package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.UserDAO;
import it.polimi.auctionapp.beans.Address;
import it.polimi.auctionapp.beans.User;
import it.polimi.auctionapp.utils.MessageType;
import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class AccountManager {

    static UserDAO userConnectionHandler = new UserDAO();

    @WebServlet("/account")
    public static class SessionManager extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            if(request.getSession().isNew()){
                request.getSession().setAttribute("user", null);
                // FIXME: This causes first-click bug
                response.sendRedirect(getServletContext().getContextPath() + "/");
            }

            if (request.getSession().getAttribute("user") == null) {
                processTemplate("/account", request, response);
            } else {
                processTemplate("/account", request, response);

            }

        }

    }

    @WebServlet("/login")
    public static class LoginManagerServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            try {
                User user = userConnectionHandler.getUser(username, password);
                request.getSession().setAttribute("user", user);
                request.getSession().setAttribute("message", MessageType.SUCCESS.wrap("User logged in successfully."));
            } catch(SQLWarning e) {
                request.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                request.setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
            }

        }
    }

    @WebServlet("/logout")
    public static class LogoutManagerServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            session.invalidate();
            try {
                session.setAttribute("message", MessageType.SUCCESS.wrap("User logged out successfully."));
                response.sendRedirect("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @WebServlet("/signup")
    public static class SignupManagerServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            try {
                userConnectionHandler.addUser(
                        new User(
                                request.getParameter("username"),
                                request.getParameter("name"),
                                request.getParameter("surname"),
                                new Address(
                                        request.getParameter("country"),
                                        Integer.parseInt(request.getParameter("zip_code")),
                                        request.getParameter("city"),
                                        request.getParameter("street"),
                                        Integer.parseInt(request.getParameter("street_number"))
                                )),
                        request.getParameter("password")
                );
                session.setAttribute("message", MessageType.SUCCESS.wrap("User registered successfully. Now log in with your credentials"));
                processTemplate("/account", request, response);

            }catch (SQLWarning e){
                session.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
                processTemplate("/account", request, response);
            } catch (SQLException e) {
                session.setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
                processTemplate("/account", request, response);
            }finally {
                session.removeAttribute("message");
            }

        }
    }

    @WebServlet("/account/delete")
    public static class DeleteAccountManagerServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {

        }
    }


    @WebServlet("/account/updateUsername")
    public static class UpdateUsernameServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            try {
                userConnectionHandler.updateUsername(
                        ((User) request.getSession().getAttribute("user")).getUsername(),
                        request.getParameter("newUsername"),
                        request.getParameter("password")
                );
            } catch (SQLWarning e) {
                request.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                request.setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
            }
        }
    }

    @WebServlet("/account/updatePassword")
    public static class UpdatePasswordServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            try {
                userConnectionHandler.updatePassword(((User) request.getSession().getAttribute("user")).getUsername(),
                        request.getParameter("newPassword"),
                        request.getParameter("oldPassword")
                );
            } catch (SQLWarning e) {
                request.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                request.setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
            }
        }
    }

    @WebServlet("/account/updateAccountDetails")
    public static class UpdateManagerServlet extends ThymeleafHTTPServlet {
        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            try {
                userConnectionHandler.updateAccountDetails(
                        new User(
                                ((User) request.getSession().getAttribute("user")).getUsername(),
                                request.getParameter("name"),
                                request.getParameter("surname"),
                                new Address(
                                        request.getParameter("country"),
                                        Integer.parseInt(request.getParameter("zipCode")),
                                        request.getParameter("city"),
                                        request.getParameter("street"),
                                        Integer.parseInt(request.getParameter("street_number"))
                                )
                        )
                );
            } catch (SQLException e) {
                request.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            }
        }


    }
}