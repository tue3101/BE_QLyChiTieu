package api;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.JwtUtil;
import model.NguoiDung;
import service.QuanLyChiTieuService;

@WebFilter("/api/*") // Áp dụng cho tất cả các đường dẫn dưới /api
public class AuthFilter implements Filter {

    private QuanLyChiTieuService service;
    private static final String USER_ID_ATTRIBUTE = "userId";

    public void init(FilterConfig fConfig) throws ServletException {
        service = new QuanLyChiTieuService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Cho phép truy cập các endpoint login và register mà không cần xác thực
        if (path.equals("/api/login") || path.equals("/api/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Lấy token từ header Authorization (ví dụ: Bearer <token>)
        String authorizationHeader = httpRequest.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        // Xác thực token
        if (token != null && service.validateToken(token)) {
            // Token hợp lệ, lấy thông tin người dùng và đặt vào request attribute
            NguoiDung user = service.getUserFromToken(token);
            if (user != null) {
                httpRequest.setAttribute(USER_ID_ATTRIBUTE, user.getId_nguoidung());
                // Cho phép request đi tiếp
                chain.doFilter(request, response);
            } else {
                // Token hợp lệ nhưng không tìm thấy người dùng (có thể token cũ)
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                httpResponse.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            }
        } else {
            // Token không hợp lệ hoặc không có token
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            httpResponse.getWriter().write("{\"error\": \"Unauthorized - Missing or invalid token\"}");
        }
    }

    @Override
    public void destroy() {
        // Cleanup resources if needed
    }
} 