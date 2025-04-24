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

    static UserDAO userDataAccessObject = new UserDAO();

    @WebServlet("/account")
    public static class SessionManager extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            if (request.getSession().getAttribute("user") == null) {
                processTemplate(request, response, "/account");
            } else {
                processTemplate(request, response, "/account-details");
            }
        }
    }

    @WebServlet("/login")
    public static class LoginManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            try {
                User user = userDataAccessObject.getUser(username, password);
                request.getSession().setAttribute("user", user);
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.SUCCESS.wrap("User logged in successfully.")
                    );
            } catch (SQLWarning e) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                //FIXME: This hides error warnings
                if (request.getSession().getAttribute("from") != null) {
                    sendRedirect(
                        request,
                        response,
                        "/" + request.getSession().getAttribute("from")
                    );
                } else {
                    sendRedirect(request, response, "/account");
                }
            }
        }
    }

    @WebServlet("/account/logout")
    public static class LogoutManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            request.getSession().invalidate();
            request.getSession().setAttribute("user", null);
            request
                .getSession()
                .setAttribute("message", MessageType.SUCCESS.wrap("User logged out successfully."));
            sendRedirect(request, response, "/");
        }
    }

    @WebServlet("/signup")
    public static class SignupManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            HttpSession session = request.getSession();
            try {
                userDataAccessObject.addUser(
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
                        )
                    ),
                    request.getParameter("password")
                );
                session.setAttribute(
                    "message",
                    MessageType.SUCCESS.wrap(
                        "User registered successfully. Now log in with your credentials"
                    )
                );
            } catch (SQLWarning e) {
                session.setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                session.setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/delete")
    public static class DeleteAccountManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                userDataAccessObject.deleteAccount(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    request.getParameter("password")
                );
                request.getSession().setAttribute("user", null);
                request.getSession().invalidate();
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.SUCCESS.wrap("Account deleted successfully")
                    );
                sendRedirect(request, response, "/");
            } catch (SQLWarning e) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
                sendRedirect(request, response, "/account");
            } catch (SQLException e) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/update-username")
    public static class UpdateUsernameServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            if (
                request
                    .getParameter("new-username")
                    .equals(((User) request.getSession().getAttribute("user")).getUsername())
            ) {
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.WARNING.wrap("New username must be different from the old one")
                    );
            } else if (request.getParameter("new-username").isEmpty()) {
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.WARNING.wrap("New username cannot be empty")
                    );
            } else if (request.getParameter("password").isEmpty()) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.WARNING.wrap("Password cannot be empty"));
            } else {
                try {
                    userDataAccessObject.updateUsername(
                        ((User) request.getSession().getAttribute("user")).getUsername(),
                        request.getParameter("new-username"),
                        request.getParameter("password")
                    );
                    request
                        .getSession()
                        .setAttribute(
                            "user",
                            userDataAccessObject.getUser(
                                request.getParameter("new-username"),
                                request.getParameter("password")
                            )
                        );
                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            MessageType.SUCCESS.wrap("Username updated successfully")
                        );
                } catch (SQLWarning e) {
                    request
                        .getSession()
                        .setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
                } catch (SQLException e) {
                    request
                        .getSession()
                        .setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
                }
            }
            sendRedirect(request, response, "/account");
        }
    }

    @WebServlet("/account/update-password")
    public static class UpdatePasswordServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            if (
                !request
                    .getParameter("new-password")
                    .equals(request.getParameter("confirm-new-password"))
            ) {
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.WARNING.wrap("New password and confirmation do not match")
                    );
            } else if (
                request.getParameter("new-password").isEmpty() ||
                request.getParameter("confirm-new-password").isEmpty() ||
                request.getParameter("password").isEmpty()
            ) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.WARNING.wrap("Password cannot be empty"));
            } else {
                try {
                    userDataAccessObject.updatePassword(
                        ((User) request.getSession().getAttribute("user")).getUsername(),
                        request.getParameter("password"),
                        request.getParameter("new-password")
                    );
                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            MessageType.SUCCESS.wrap("Password updated successfully")
                        );
                } catch (SQLWarning e) {
                    request
                        .getSession()
                        .setAttribute("message", MessageType.WARNING.wrap(e.getMessage()));
                } catch (SQLException e) {
                    request
                        .getSession()
                        .setAttribute("message", MessageType.ERROR.wrap(e.getMessage()));
                }
            }
            sendRedirect(request, response, "/account");
        }
    }

    @WebServlet("/account/update-details")
    public static class UpdateManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            if (
                request.getParameter("name").isEmpty() ||
                request.getParameter("surname").isEmpty() ||
                request.getParameter("country").isEmpty() ||
                request.getParameter("zip-code").isEmpty() ||
                request.getParameter("city").isEmpty() ||
                request.getParameter("street").isEmpty() ||
                request.getParameter("street-number").isEmpty()
            ) {
                request
                    .getSession()
                    .setAttribute("message", MessageType.WARNING.wrap("All fields must be filled"));
            } else {
                try {
                    if (Integer.parseInt(request.getParameter("street-number")) < 1) {
                        request
                            .getSession()
                            .setAttribute(
                                "message",
                                MessageType.WARNING.wrap("Street number must be a positive number")
                            );
                    } else if (
                        Integer.parseInt(request.getParameter("zip-code")) < 10000 ||
                        Integer.parseInt(request.getParameter("zip-code")) > 99999
                    ) {
                        request
                            .getSession()
                            .setAttribute(
                                "message",
                                MessageType.WARNING.wrap(
                                    "Zip code must be a positive 5 digit number "
                                )
                            );
                    } else {
                        User new_user = new User(
                            ((User) request.getSession().getAttribute("user")).getUsername(),
                            request.getParameter("name"),
                            request.getParameter("surname"),
                            new Address(
                                request.getParameter("country"),
                                Integer.parseInt(request.getParameter("zip-code")),
                                request.getParameter("city"),
                                request.getParameter("street"),
                                Integer.parseInt(request.getParameter("street-number"))
                            )
                        );
                        userDataAccessObject.updateAccountDetails(new_user);
                        request
                            .getSession()
                            .setAttribute(
                                "message",
                                MessageType.SUCCESS.wrap("Account details updated successfully")
                            );
                        request.getSession().setAttribute("user", new_user);
                    }
                } catch (SQLException e) {
                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            "Something went wrong while updating your details. " +
                            MessageType.ERROR.wrap(e.getMessage())
                        );
                } catch (NumberFormatException e) {
                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            MessageType.WARNING.wrap("Street number and zip code must be numbers")
                        );
                }
            }
            sendRedirect(request, response, "/account");
        }
    }
}
