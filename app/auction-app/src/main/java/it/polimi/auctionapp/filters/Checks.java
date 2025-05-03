package it.polimi.auctionapp.filters;

import it.polimi.auctionapp.utils.MessageType;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class Checks {

    @WebFilter(urlPatterns = { "/buy/*", "/sell/*", "/account/update/*" })
    public static class LoginFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession session = req.getSession(false);

            if (session == null || session.isNew() || session.getAttribute("user") == null) {
                req
                    .getSession()
                    .setAttribute(
                        "FLASH_message",
                        MessageType.INFO.wrap(
                            "You were redirected here from " +
                            req.getRequestURI().substring(req.getContextPath().length()) +
                            ". Please log in to continue."
                        )
                    );
                req.getSession().setAttribute("from", req.getRequestURI().split("/")[2]);
                res.sendRedirect(req.getServletContext().getContextPath() + "/account");
                return;
            }
            chain.doFilter(request, response);
        }
    }
}
