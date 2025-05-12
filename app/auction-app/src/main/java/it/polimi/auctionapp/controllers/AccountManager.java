package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.UserDAO;
import it.polimi.auctionapp.beans.Address;
import it.polimi.auctionapp.beans.User;
import it.polimi.auctionapp.utils.MessageType;
import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                processTemplate(request, response, "/account/index");
            } else {
                sendRedirect(request, response, "/account/details");
            }
        }
    }

    @WebServlet("/account/details")
    public static class AccountDetailsServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            if (request.getSession().getAttribute("user") == null) {
                processTemplate(request, response, "/account/index");
            } else {
                processTemplate(request, response, "/account/details");
            }
        }
    }

    @WebServlet("/login")
    public static class LoginManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(request, response, "username", "password");
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                User user = userDataAccessObject.getUser(username, password);
                request.getSession().setAttribute("user", user);
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("User logged in successfully.")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                if (request.getSession().getAttribute("from") != null) {
                    sendRedirect(
                        request,
                        response,
                        "/" + request.getSession().getAttribute("from")
                    );
                    request.getSession().removeAttribute("from");
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
            contextAttributes.setFlash(
                "message",
                MessageType.SUCCESS.wrap("User logged out successfully.")
            );
            sendRedirect(request, response, "/");
        }
    }

    @WebServlet("/signup")
    public static class SignupManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(
                    request,
                    response,
                    "username",
                    "password",
                    "name",
                    "surname",
                    "country",
                    "zip_code",
                    "city",
                    "street",
                    "street_number"
                );
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
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap(
                        "User registered successfully. Now log in with your credentials"
                    )
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/update/delete")
    public static class DeleteAccountManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(request, response, "password");
                userDataAccessObject.deleteAccount(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    request.getParameter("password")
                );
                request.getSession().setAttribute("user", null);
                request.getSession().invalidate();
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Account deleted successfully")
                );
                sendRedirect(request, response, "/");
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
                sendRedirect(request, response, "/account");
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/update/username")
    public static class UpdateUsernameServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(request, response, "new-username", "password");
                if (
                    request
                        .getParameter("new-username")
                        .equals(((User) request.getSession().getAttribute("user")).getUsername())
                ) {
                    throw new SQLWarning("New username must be different from the old one");
                }
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
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Username updated successfully")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/update/password")
    public static class UpdatePasswordServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(request, response, "new-password", "password");
                if (
                    !request
                        .getParameter("new-password")
                        .equals(request.getParameter("confirm-new-password"))
                ) {
                    throw new SQLWarning("The two provided passwords are not equal");
                }
                userDataAccessObject.updatePassword(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    request.getParameter("password"),
                    request.getParameter("new-password")
                );
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Password updated successfully")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/account");
            }
        }
    }

    @WebServlet("/account/update/details")
    public static class UpdateManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) {
            try {
                checkAllRequiredParams(
                    request,
                    response,
                    "name",
                    "surname",
                    "country",
                    "zip-code",
                    "city",
                    "street",
                    "street-number"
                );

                if (Integer.parseInt(request.getParameter("street-number")) < 1) {
                    throw new SQLWarning("Street number must be a positive number");
                }

                if (
                    Integer.parseInt(request.getParameter("zip-code")) < 10000 ||
                    Integer.parseInt(request.getParameter("zip-code")) > 99999
                ) {
                    throw new SQLWarning("Zip code must be a positive 5 digit number ");
                }

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
                request.getSession().setAttribute("user", new_user);
                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Account details updated successfully")
                );
            } catch (NumberFormatException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.WARNING.wrap("Street number and zip code must be numbers")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/account");
            }
        }
    }
}
