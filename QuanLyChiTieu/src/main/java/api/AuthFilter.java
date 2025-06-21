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
//Filter là trung gian dùng chặn, check và fix các yêu cầu request và response
public class AuthFilter implements Filter {

    private QuanLyChiTieuService service;
    
    //khai báo hằng số lưu tên thuộc tính userId
    private static final String USER_ID_ATTRIBUTE = "userId";

    public void init(FilterConfig fConfig) throws ServletException {
        service = new QuanLyChiTieuService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
       //ép kiểu các đối tượng request và response từ tổng quát servlet về http
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        
        
        //options: Chuỗi biểu thị phương thức HTTP OPTIONS
        // .equalsIgnoreCase(...) so sánh ko phân biệt hoa thường
        //kiểm tra request có phải phương thức options ko
        //httpRequest.getMethod() :Lấy ra phương thức HTTP (như GET, POST, PUT, DELETE, OPTIONS) từ request
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK); //trạng thái có hằng số 200 nghĩa là OK , thành công
            chain.doFilter(request, response); // cho phép request tiếp tục đi tới servlet
            return;
        }

        //httpRequest.getRequestURI(): trả về toàn bộ URI của reques vd /myapp/api/user
        //.substring(httpRequest.getContextPath().length()) vd /api/user bỏ myapp
        // lấy phần "đường dẫn cụ thể" của request, không bao gồm context path.
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // kiểm tra request hiện tại có truy cập vào 2 đường dẫn này ko
        if (path.equals("/api/login") || path.equals("/api/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Lấy token từ header Authorization (ví dụ: Bearer <token>)
        String authorizationHeader = httpRequest.getHeader("Authorization");
        String token = null;
        
        //khác null và chuỗi bđ = bearer
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);//cắt bearer lấy token
        }

        // Xác thực token
        if (token != null && service.validateToken(token)) {
            // Token hợp lệ, lấy thông tin người dùng và đặt vào request attribute
            NguoiDung user = service.getUserFromToken(token); //gọi pthuc kiểm tra tính hợp lệ token
            if (user != null) {
            	//dùng setAttribute để gắn dữ liệu vào request theo cặp key:value
            	//	Dùng lại userId trong controller/service, không cần giải mã token lại
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
    }
} 