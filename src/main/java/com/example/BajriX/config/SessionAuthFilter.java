package com.example.BajriX.config;

import com.example.BajriX.Entity.Seller;
import com.example.BajriX.Repo.SellerRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The "Wristband Checker" Filter:
 *
 * Plain-English Explanation:
 * When a seller logs in, we give them a wristband (a random token like "c7b1e4...").
 * Every time they try to view their dashboard, add a product, or edit a price,
 * this filter stands at the door and checks their wristband before they can enter.
 *
 * How it works:
 * 1. Intercepts incoming HTTP requests.
 * 2. If the request is for seller-scoped operations (/api/v1/sellers/**):
 *    - Reads the `X-Session-Token` header.
 *    - Looks up the seller in MySQL by that token.
 * 3. If token is valid:
 *    - Binds the real, verified Seller to `CurrentSellerContext`.
 *    - Passes the request forward to the controller.
 * 4. If token is missing or invalid:
 *    - Rejects the request immediately with HTTP 403 Forbidden.
 *    - The caller never even reaches the controller.
 * 5. When the request finishes, it cleans up the ThreadLocal so threads in the pool stay fresh.
 */
@Component
@Order(1)
public class SessionAuthFilter extends OncePerRequestFilter {

    private final SellerRepo sellerRepo;

    public SessionAuthFilter(SellerRepo sellerRepo) {
        this.sellerRepo = sellerRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only enforce token authentication on seller-scoped endpoints (/api/v1/sellers/**)
        // Public buyer endpoints (/api/v1/products/**) and auth endpoints (/api/v1/auth/**) are open.
        boolean isSellerEndpoint = path.startsWith("/api/v1/sellers");

        // Allow GET /api/v1/sellers (the public list used by the demo seller switcher dropdown)
        boolean isPublicSellerList = isSellerEndpoint && path.equals("/api/v1/sellers") && "GET".equalsIgnoreCase(method);

        // Allow CORS pre-flight OPTIONS requests
        boolean isOptions = "OPTIONS".equalsIgnoreCase(method);

        if (isSellerEndpoint && !isPublicSellerList && !isOptions) {
            String token = request.getHeader("X-Session-Token");

            if (token == null || token.trim().isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                        "Wristband missing: Please provide 'X-Session-Token' header. Log in first to receive a session token.");
                return;
            }

            // Look up the real seller who owns this token in the database
            Seller seller = sellerRepo.findBySessionToken(token.trim()).orElse(null);

            if (seller == null) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                        "Invalid or expired wristband: No seller found for this session token. Please log in again.");
                return;
            }

            try {
                // Attach the real seller to our ThreadLocal context
                CurrentSellerContext.set(seller);
                filterChain.doFilter(request, response);
            } finally {
                // Always clear ThreadLocal after request execution to prevent memory leaks in the thread pool
                CurrentSellerContext.clear();
            }
        } else {
            // Non-seller routes (buyer browse, login, registration) pass straight through
            filterChain.doFilter(request, response);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = "{\"success\":false,\"message\":\"" + message + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}