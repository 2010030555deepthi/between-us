package com.betweenus.between_us.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String requestURI = request.getRequestURI();

        /*
         * Public authentication APIs
         */
        if (requestURI.startsWith("/api/auth")) {
            return true;
        }

        /*
         * Public files
         */
      if (
        requestURI.equals("/") ||
        requestURI.equals("/index.html") ||
        requestURI.equals("/login.html") ||
        requestURI.equals("/favicon.ico") ||
        requestURI.startsWith("/css/") ||
        requestURI.startsWith("/js/")
) {
    return true;
} 

        /*
         * Check session
         */
        HttpSession session =
                request.getSession(false);

        boolean loggedIn =
                session != null &&
                session.getAttribute("username") != null;

        if (loggedIn) {
            return true;
        }

        /*
         * API requests return 401
         */
        if (requestURI.startsWith("/api/")) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return false;
        }

        /*
         * Protected pages go to login
         */
        response.sendRedirect("/login.html");

        return false;
    }
}