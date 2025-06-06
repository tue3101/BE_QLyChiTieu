package api;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException; // Import JsonSyntaxException
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.DanhMuc;
import model.GiaoDich;
import model.Icon;
import model.LoginResponse;
import model.MauSac;
import model.NguoiDung;
import model.NganSach;
import service.QuanLyChiTieuService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar; // Vẫn cần Calendar cho các phương thức khác, nhưng không dùng cho LocalDate

@WebServlet("/api/*")
public class QuanLyChiTieuServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private QuanLyChiTieuService service;
    private Gson gson;
    private static final String USER_ID_ATTRIBUTE = "userId";

    // Định nghĩa lớp POJO cho Login Request ở đây
    private static class LoginRequest {
        String email;
        String matkhau;
    }

    // Định nghĩa lớp POJO cho Change Password Request ở đây
     private static class ChangePasswordRequest {
         String oldPassword;
         String newPassword;
         
         public String getOldPassword() {
             return oldPassword;
         }
         
         public String getNewPassword() {
             return newPassword;
         }
     }
     
    // Định nghĩa lớp POJO cho Register Request ở đây
    private static class RegisterRequest {
        String email;
        String matkhau;
        String ten_hien_thi; // Tên hiển thị
        String role;
        
        public String getEmail() {
            return email;
        }
        
        public String getMatkhau() {
            return matkhau;
        }
        
        public String getTen_hien_thi() {
            return ten_hien_thi;
        }
        
        public String getRole() {
            return role;
        }
    }

    public QuanLyChiTieuServlet() {
        super();
        service = new QuanLyChiTieuService();
        // Cấu hình Gson để xử lý LocalDate
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Đăng ký TypeAdapter cho LocalDate
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gson = gsonBuilder.create();
    }

    // Type adapter cho LocalDate
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString(), formatter);
        }
    }
    
    // Add CORS headers before handling any request
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        // Read the Origin header from the request and set it in the response
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("DEBUG: Received GET request to path: " + request.getPathInfo());
        setCorsHeaders(request, response); // Also add headers to actual responses
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not specified\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        String resource = pathParts.length > 1 ? pathParts[1] : null;

        // Đặt kiểm tra /api/categories/search lên trước điều kiện /api/categories chung
        if ("categories".equals(resource) && pathParts.length > 2 && "search".equals(pathParts[2])) {
             handleSearchCategories(request, response);
        } else if ("categories".equals(resource)) {
            handleGetCategories(request, response, pathParts);
        } else if ("transactions".equals(resource)) {
        	// Đặt kiểm tra /api/transactions/search lên trước các đường dẫn transactions khác
            if (pathParts.length > 2 && "search".equals(pathParts[2])) {
                handleSearchTransactions(request, response);
            } else if (pathParts.length > 3 && "user".equals(pathParts[2]) && "all".equals(pathParts[4])) { // New endpoint for all transactions by user ID
                 handleGetAllTransactionsByUserId(request, response, pathParts);
            } else {
                handleGetTransactions(request, response, pathParts);
            }
        } else if ("budget".equals(resource)) {
             handleGetBudget(request, response, pathParts);
        // Đặt kiểm tra /api/colors/search lên trước điều kiện /api/colors chung
        } else if ("colors".equals(resource) && pathParts.length > 2 && "search".equals(pathParts[2])) {
             handleSearchColors(request, response);
        } else if ("colors".equals(resource)) {
             handleGetColors(request, response);
        } else if ("icons".equals(resource)) {
             handleGetIcons(request, response);
        } else if ("transaction-types".equals(resource)) {
             handleGetTransactionTypes(request, response);
        } else if ("users".equals(resource) && pathParts.length > 2 && !"search".equals(pathParts[2])) { // Thêm điều kiện không phải search
             // Expecting /api/users/{id} hoặc /api/users/{id}/password
             try {
                 int userId = Integer.parseInt(pathParts[2]);
                 if (pathParts.length > 3 && "password".equals(pathParts[3])) {
                     // Handle change password
                     handleChangePassword(request, response, userId);
                 } else {
                     // Handle update user info
                      handleGetUserById(request, response, userId);
                 }
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid user ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
        } else if ("default-categories".equals(resource)) {
            handleGetDefaultCategories(request, response);
        } else if ("users".equals(resource) && pathParts.length == 2) {
            handleGetAllUsers(request, response);
        } else if ("users".equals(resource) && pathParts.length > 2 && "search".equals(pathParts[2])) {
             handleSearchUsers(request, response);
        } else if ("categories".equals(resource) && pathParts.length > 2 && "search".equals(pathParts[2])) {
             handleSearchCategories(request, response);
        } else if ("icons".equals(resource) && pathParts.length > 2 && "search".equals(pathParts[2])) {
             handleSearchIcons(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not found\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(request, response); // Add CORS headers
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not specified\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        String endpoint = pathParts.length > 1 ? pathParts[1] : null;

        if ("login".equals(endpoint)) {
            handleLogin(request, response);
        } else if ("register".equals(endpoint)) {
            handleRegister(request, response);
        } else if ("categories".equals(endpoint)) {
             handlePostCategories(request, response);
         } else if ("transactions".equals(endpoint)) {
             handlePostTransactions(request, response);
         } else if ("budget".equals(endpoint)) {
             handlePostBudget(request, response);
         } else if ("icons".equals(endpoint)) {
             handlePostIcons(request, response);
         } else if ("colors".equals(endpoint)) {
             handlePostColors(request, response);
         } else if ("logout".equals(endpoint)) {
             handleLogout(request, response);
         } else if ("admin-add-user".equals(endpoint)) {
             handleAdminAddUser(request, response);
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Endpoint not found\"}");
         }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(request, response); // Add CORS headers
        request.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not specified\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        String resource = pathParts.length > 1 ? pathParts[1] : null;

        if ("categories".equals(resource) && pathParts.length > 2) {
            // Expecting /api/categories/{id}
            try {
                int categoryId = Integer.parseInt(pathParts[2]);
                handlePutCategories(request, response, categoryId);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid category ID\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal server error\"}");
            }
        } else if ("transactions".equals(resource) && pathParts.length > 2) {
             // Expecting /api/transactions/{id}
             try {
                 int transactionId = Integer.parseInt(pathParts[2]);
                 handlePutTransactions(request, response, transactionId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid transaction ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else if ("budget".equals(resource) && pathParts.length > 2) {
              // Expecting /api/budget/{id}
             try {
                 int budgetId = Integer.parseInt(pathParts[2]);
                 handlePutBudget(request, response, budgetId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid budget ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else if ("icons".equals(resource) && pathParts.length > 2) {
             // Expecting /api/icons/{id}
             try {
                 int iconId = Integer.parseInt(pathParts[2]);
                 handlePutIcons(request, response, iconId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid icon ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else if ("colors".equals(resource) && pathParts.length > 2) {
             // Expecting /api/colors/{id}
             try {
                 int colorId = Integer.parseInt(pathParts[2]);
                 handlePutColors(request, response, colorId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid color ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else if ("users".equals(resource) && pathParts.length > 2) {
              // Expecting /api/users/{id} hoặc /api/users/{id}/password
             try {
                 int userId = Integer.parseInt(pathParts[2]);
                 if (pathParts.length > 3 && "password".equals(pathParts[3])) {
                     // Handle change password
                     handleChangePassword(request, response, userId);
                 } else {
                     // Handle update user info
                      handlePutUser(request, response, userId);
                 }
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid user ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Endpoint not found or missing ID\"}");
         }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(request, response); // Add CORS headers
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not specified\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        String resource = pathParts.length > 1 ? pathParts[1] : null;

        if ("categories".equals(resource) && pathParts.length > 2) {
            // Expecting /api/categories/{id}
            try {
                int categoryId = Integer.parseInt(pathParts[2]);
                handleDeleteCategories(request, response, categoryId);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid category ID\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal server error\"}");
            }
        } else if ("transactions".equals(resource) && pathParts.length > 2) {
             // Expecting /api/transactions/{id}
             try {
                 int transactionId = Integer.parseInt(pathParts[2]);
                 handleDeleteTransactions(request, response, transactionId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid transaction ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
        } else if ("budget".equals(resource) && pathParts.length > 2) {
             // Expecting /api/budget/{id}
             try {
                 int budgetId = Integer.parseInt(pathParts[2]);
                 handleDeleteBudget(request, response, budgetId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid budget ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
        } else if ("icons".equals(resource) && pathParts.length > 2) {
             // Expecting /api/icons/{id}
             try {
                 int iconId = Integer.parseInt(pathParts[2]);
                 handleDeleteIcons(request, response, iconId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid icon ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
        } else if ("colors".equals(resource) && pathParts.length > 2) {
             // Expecting /api/colors/{id}
             try {
                 int colorId = Integer.parseInt(pathParts[2]);
                 handleDeleteColors(request, response, colorId);
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid color ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
        } else if ("users".equals(resource) && pathParts.length > 2) {
              // Expecting /api/users/{id} hoặc /api/users/{id}/password
             try {
                 int userId = Integer.parseInt(pathParts[2]);
                 if (pathParts.length > 3 && "password".equals(pathParts[3])) {
                     // Handle change password
                     handleChangePassword(request, response, userId);
                 } else {
                     // Handle update user info
                      handleDeleteUser(request, response, userId);
                 }
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid user ID\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Endpoint not found or missing ID\"}");
         }
    }

    private void handleGetCategories(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
        // Expecting /api/categories/user/{userId}
        if (pathParts.length > 3 && "user".equals(pathParts[2])) {
            try {
                int requestedUserId = Integer.parseInt(pathParts[3]);
                 // Lấy userId từ request attribute (do AuthFilter đặt)
                Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

                if (authenticatedUserId == null || authenticatedUserId != requestedUserId) {
                     // Người dùng yêu cầu danh mục của người khác hoặc không xác thực
                     response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Hoặc SC_UNAUTHORIZED
                     response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' categories\"}");
                     return;
                }

                List<DanhMuc> categories = service.getDanhMucByUserId(requestedUserId);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(categories));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid user ID\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal server error\"}");
            }
        } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/categories/user/{userId}\"}");
        }
    }

    private void handlePostCategories(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) { // Should not happen if AuthFilter works correctly
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        DanhMuc newDanhMuc = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            newDanhMuc = gson.fromJson(request.getReader(), DanhMuc.class);

            if (newDanhMuc == null || newDanhMuc.getTen_danh_muc() == null || newDanhMuc.getTen_danh_muc().isEmpty() || newDanhMuc.getId_icon() == 0 || newDanhMuc.getId_mau() == 0 || newDanhMuc.getId_loai() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid category data\"}");
                return;
            }

            // Lấy thông tin người dùng để kiểm tra role
            NguoiDung creatingUser = service.getUserById(userId);

            if (creatingUser != null && "admin".equals(creatingUser.getRole())) {
                // Nếu người dùng là admin, đặt id_nguoidung là null cho danh mục mặc định
                newDanhMuc.setId_nguoidung(null); // Sử dụng null cho danh mục mặc định
            } else {
                // Nếu không phải admin (người dùng bình thường), gán id_nguoidung của họ
                newDanhMuc.setId_nguoidung(userId); // Gán userId từ token
            }

            boolean success = service.addDanhMuc(newDanhMuc);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Category added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to add category\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during category parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePutCategories(HttpServletRequest request, HttpServletResponse response, int categoryId) throws IOException {
        // Lấy userId từ request attribute (do AuthFilter đặt)
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) { // Should not happen if AuthFilter works correctly
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        DanhMuc updatedDanhMuc = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            updatedDanhMuc = gson.fromJson(request.getReader(), DanhMuc.class);

             if (updatedDanhMuc == null || updatedDanhMuc.getTen_danh_muc() == null || updatedDanhMuc.getTen_danh_muc().isEmpty() || updatedDanhMuc.getId_icon() == 0 || updatedDanhMuc.getId_mau() == 0 || updatedDanhMuc.getId_loai() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid category data\"}");
                return;
            }

            updatedDanhMuc.setId_danhmuc(categoryId); // Gán categoryId từ URL

            // Lấy thông tin người dùng để kiểm tra role
            NguoiDung updatingUser = service.getUserById(userId);

            if (updatingUser != null && "admin".equals(updatingUser.getRole())) {
                // Nếu người dùng là admin, đặt id_nguoidung là null khi cập nhật
                updatedDanhMuc.setId_nguoidung(null); // Sử dụng null cho danh mục mặc định
            } else { // Nếu không phải admin (người dùng bình thường), đảm bảo id_nguoidung không bị thay đổi (giữ nguyên của user)
                // Hoặc bạn có thể kiểm tra xem danh mục này có thuộc về user đó không trước khi cho phép cập nhật
                // Logic kiểm tra quyền sở hữu danh mục đã có trong handleDeleteCategories, có thể áp dụng tương tự ở đây
                 DanhMuc existingCategory = service.getDanhMucByIdAndUserId(categoryId, userId);
                 if (existingCategory == null) {
                      response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Hoặc SC_NOT_FOUND
                      response.getWriter().write("{\"error\": \"Category not found or does not belong to user\"}");
                      return;
                 }
                // Giữ nguyên id_nguoidung của danh mục gốc hoặc set lại userId nếu cần cập nhật data khác
                // updatedDanhMuc.setId_nguoidung(userId); // Dòng này không cần thiết nếu chỉ update data khác và giữ nguyên quyền sở hữu
                 updatedDanhMuc.setId_nguoidung(existingCategory.getId_nguoidung()); // Giữ nguyên id_nguoidung hiện tại
            }

            boolean success = service.updateDanhMuc(updatedDanhMuc);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Category updated successfully\"}");
            } else {
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Hoặc SC_FORBIDDEN nếu không thuộc về user
                 response.getWriter().write("{\"error\": \"Category not found or does not belong to user\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during category update parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleDeleteCategories(HttpServletRequest request, HttpServletResponse response, int categoryId) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) { // Should not happen if AuthFilter works correctly
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        // Get the requesting user to check their role
        NguoiDung requestingUser = service.getUserById(userId);

        // If the user is admin, allow them to delete any category. Otherwise, check ownership.
        DanhMuc categoryToDelete = null;
        if (requestingUser != null && "admin".equals(requestingUser.getRole())) {
            // Admin can delete any category - just check if it exists
            categoryToDelete = service.getDanhMucById(categoryId); // Need a service method to get category by ID only
        } else {
            // Non-admin users can only delete their own categories
            categoryToDelete = service.getDanhMucByIdAndUserId(categoryId, userId); // Existing check
        }

        if (categoryToDelete == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Category not found or does not belong to user\"}");
            return;
        }

        try {
            boolean success = service.deleteDanhMuc(categoryId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Category deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to delete category\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

     private void handleGetTransactions(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
         // Expecting /api/transactions/user/{userId}/month/{month}/year/{year}
         if (pathParts.length > 5 && "user".equals(pathParts[2]) && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
             try {
                 int requestedUserId = Integer.parseInt(pathParts[3]);
                 int month = Integer.parseInt(pathParts[5]);
                 int year = Integer.parseInt(pathParts[7]);

                 // Lấy userId từ request attribute (do AuthFilter đặt)
                 Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

                 if (authenticatedUserId == null || authenticatedUserId != requestedUserId) {
                     // Người dùng yêu cầu giao dịch của người khác hoặc không xác thực
                     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                     response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' transactions\"}");
                     return;
                 }

                 List<GiaoDich> transactions = service.getGiaoDichByMonth(requestedUserId, month, year);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write(gson.toJson(transactions));
             } catch (NumberFormatException e) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid user ID, month, or year\"}");
             } catch (Exception e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.getWriter().write("{\"error\": \"Internal server error\"}");
             }
         } else {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/transactions/user/{userId}/month/{month}/year/{year}\"}");
         }
     }

     private void handlePostTransactions(HttpServletRequest request, HttpServletResponse response) throws IOException {
         // Lấy userId từ request attribute (do AuthFilter đặt)
         Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
         if (userId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
         }

         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = request.getReader().readLine()) != null) {
             sb.append(line);
         }
         String jsonRequest = sb.toString();

         // --- DEBUG LOG ---
         System.out.println("Received JSON Request Body in handlePostTransactions: " + jsonRequest);
         // -----------------

         GiaoDich newGiaoDich = null;
         try {
             newGiaoDich = gson.fromJson(jsonRequest, GiaoDich.class);

             if (newGiaoDich == null || newGiaoDich.getId_danhmuc() == null || newGiaoDich.getId_loai() == null || newGiaoDich.getSo_tien() <= 0) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  response.getWriter().write("{\"error\": \"Invalid transaction data\"}");
                  return;
             }

             newGiaoDich.setId_nguoidung(String.valueOf(userId)); // Gán userId từ token
             // Đảm bảo ngày, tháng, năm được thiết lập nếu cần (từ request hoặc hệ thống)
             // Logic xử lý ngày, tháng, năm đã được chuyển sang Service để tập trung
             // Nếu ngày không được cung cấp, Service sẽ sử dụng ngày hiện tại và thiết lập tháng/năm
             // Nếu ngày được cung cấp, Service sẽ trích xuất tháng/năm
             // Không cần xử lý logic này ở đây nữa.
             // Đảm bảo rằng trong Service, bạn đã xử lý đúng việc gán thang/nam từ ngay nếu ngay != null
             // và gán ngay = LocalDate.now() nếu ngay == null.

             // Log debug tạm thời trước khi gọi service
             System.out.println("Attempting to add transaction: " + newGiaoDich);
             boolean success = service.addGiaoDich(newGiaoDich);

             // --- DEBUG LOG SAU KHI GỌI SERVICE ---
             System.out.println("Result of addGiaoDich: " + success);
             // -----------------------------------

             if (success) {
                 response.setStatus(HttpServletResponse.SC_CREATED);
                 response.getWriter().write("{\"message\": \"Transaction added successfully\"}");
             } else {
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  response.getWriter().write("{\"error\": \"Failed to add transaction\"}");
             }
         } catch (JsonSyntaxException e) {
             System.err.println("Json Syntax Error during transaction parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
         } catch (Exception e) {
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             response.getWriter().write("{\"error\": \"Internal server error\"}");
         }
     }

     private void handlePutTransactions(HttpServletRequest request, HttpServletResponse response, int transactionId) throws IOException {
         // Lấy userId từ request attribute (do AuthFilter đặt)
         Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
         if (userId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
         }

         GiaoDich updatedGiaoDich = null;
         try {
             // Đọc trực tiếp từ request reader vào Gson
             updatedGiaoDich = gson.fromJson(request.getReader(), GiaoDich.class);

             if (updatedGiaoDich == null || updatedGiaoDich.getId_danhmuc() == null || updatedGiaoDich.getId_loai() == null || updatedGiaoDich.getSo_tien() <= 0) {
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  response.getWriter().write("{\"error\": \"Invalid transaction data\"}");
                  return;
             }

             updatedGiaoDich.setId_GD(String.valueOf(transactionId));
             updatedGiaoDich.setId_nguoidung(String.valueOf(userId)); // Đảm bảo userId đúng
             // Cập nhật tháng/năm nếu ngày được cung cấp
              // Tương tự như POST, logic xử lý tháng/năm từ ngày nên ở Service
              // Đảm bảo Service xử lý cập nhật tháng/năm khi ngày được cung cấp.

             // Log debug tạm thời trước khi gọi service
             System.out.println("Attempting to update transaction: " + updatedGiaoDich);
             boolean success = service.updateGiaoDich(updatedGiaoDich);

             // --- DEBUG LOG SAU KHI GỌI SERVICE ---
             System.out.println("Result of updateGiaoDich: " + success);
             // -------------------------------------

             if (success) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write("{\"message\": \"Transaction updated successfully\"}");
             } else {
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Hoặc SC_FORBIDDEN nếu không thuộc về user
                  response.getWriter().write("{\"error\": \"Transaction not found or update failed\"}");
             }
         } catch (JsonSyntaxException e) {
             System.err.println("Json Syntax Error during transaction update parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
         } catch (Exception e) {
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             response.getWriter().write("{\"error\": \"Internal server error\"}");
         }
     }

     private void handleDeleteTransactions(HttpServletRequest request, HttpServletResponse response, int transactionId) throws IOException {
         // Lấy userId từ request attribute (do AuthFilter đặt)
         Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
         if (userId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
         }

         // Kiểm tra xem giao dịch có thuộc về người dùng đang đăng nhập không trước khi xóa
         GiaoDich transactionToDelete = service.getGiaoDichByIdAndUserId(transactionId, userId); // Sử dụng phương thức mới

         if (transactionToDelete == null) {
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             response.getWriter().write("{\"error\": \"Transaction not found or does not belong to user\"}");
             return;
         }

         try {
             boolean success = service.deleteGiaoDich(transactionId);

             if (success) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write("{\"message\": \"Transaction deleted successfully\"}");
             } else {
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  response.getWriter().write("{\"error\": \"Failed to delete transaction\"}");
             }
         } catch (Exception e) {
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             response.getWriter().write("{\"error\": \"Internal server error\"}");
         }
     }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequest loginRequest = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            loginRequest = gson.fromJson(request.getReader(), LoginRequest.class);

            System.out.println("LoginRequest object after gson.fromJson: " + loginRequest); // Giữ lại debug log
            if (loginRequest != null) {
                 System.out.println("Email: " + (loginRequest.email != null ? loginRequest.email : "null") + ", Password: " + (loginRequest.matkhau != null ? "masked" : "null")); // Log password cẩn thận
            }

            // Kiểm tra xem loginRequest có null không hoặc các trường cần thiết có null không
            if (loginRequest == null || loginRequest.email == null || loginRequest.email.isEmpty() || loginRequest.matkhau == null || loginRequest.matkhau.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                response.getWriter().write("{\"error\": \"Email and password are required\"}");
                return; // Thoát khỏi phương thức
            }

            // Gọi service để xác thực người dùng
            LoginResponse loginResponse = service.login(loginRequest.email, loginRequest.matkhau);

            // Xử lý kết quả từ service
            if (loginResponse != null && loginResponse.getToken() != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(loginResponse));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                response.getWriter().write("{\"error\": \"Invalid email or password\"}");
            }
        } catch (JsonSyntaxException e) {
            // Bắt ngoại lệ nếu JSON không hợp lệ
             System.err.println("Json Syntax Error during login parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
             // Bắt các lỗi khác trong quá trình đọc/parse
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             response.getWriter().write("{\"error\": \"Internal server error during login\"}");
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RegisterRequest registerRequest = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            registerRequest = gson.fromJson(request.getReader(), RegisterRequest.class);

            if (registerRequest == null || registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty() || registerRequest.getMatkhau() == null || registerRequest.getMatkhau().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                response.getWriter().write("{\"error\": \"Email and password are required\"}");
                return;
            }

            // Việc kiểm tra email đã tồn tại được xử lý trong Service.register
            NguoiDung newUser = new NguoiDung();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setMatkhau(registerRequest.getMatkhau()); // Mật khẩu sẽ được mã hóa trong Service
            // Gán tên mặc định nếu không được cung cấp
            if (registerRequest.getTen_hien_thi() != null && !registerRequest.getTen_hien_thi().isEmpty()) {
                newUser.setHoten(registerRequest.getTen_hien_thi());
            } else {
                newUser.setHoten("Người dùng mới"); // Tên mặc định
            }
            // Gán role mặc định nếu không được cung cấp hoặc không hợp lệ
             if (registerRequest.getRole() != null && (registerRequest.getRole().equals("user") || registerRequest.getRole().equals("admin"))) {
                 newUser.setRole(registerRequest.getRole());
             } else {
                 newUser.setRole("user"); // Role mặc định
             }

            boolean success = service.register(newUser);

            if (success) {
                // Đăng ký thành công, có thể trả về thông báo hoặc tự động đăng nhập và trả về token
                 response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                 response.getWriter().write("{\"message\": \"Registration successful\"}");
            } else {
                // Nếu Service.register trả về false (ví dụ: email đã tồn tại), trả về 409 Conflict
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                response.getWriter().write("{\"error\": \"Registration failed - Email may already exist\"}");
            }
        }  catch (JsonSyntaxException e) {
             System.err.println("Json Syntax Error during register parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred during registration\"}");
        }
    }

    private void handleGetBudget(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
        // Expecting /api/budget/user/{userId}/month/{month}/year/{year}
        if (pathParts.length > 5 && "user".equals(pathParts[2]) && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
            try {
                int requestedUserId = Integer.parseInt(pathParts[3]);
                int month = Integer.parseInt(pathParts[5]);
                int year = Integer.parseInt(pathParts[7]);

                // Lấy userId từ request attribute (do AuthFilter đặt)
                Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

                if (authenticatedUserId == null || authenticatedUserId != requestedUserId) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' budget\"}");
                    return;
                }

                NganSach budget = service.getNganSachByMonth(requestedUserId, month, year);
                if (budget != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(gson.toJson(budget));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"message\": \"Budget not found for this period\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid user ID, month, or year\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal server error\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/budget/user/{userId}/month/{month}/year/{year}\"}");
        }
    }

    private void handlePostBudget(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        NganSach newNganSach = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            newNganSach = gson.fromJson(request.getReader(), NganSach.class);

            if (newNganSach == null || newNganSach.getNgansach() <= 0 || newNganSach.getThang() == 0 || newNganSach.getNam() == 0) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid budget data\"}");
                 return;
            }

            newNganSach.setId_nguoidung(userId); // Gán userId từ token (sử dụng Integer)

             // Kiểm tra trùng ngân sách cho tháng/năm của người dùng
            if (service.isNganSachExistForMonthYear(userId, newNganSach.getThang(), newNganSach.getNam())) {
                 response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                 response.getWriter().write("{\"error\": \"Budget for this month and year already exists\"}");
                 return;
            }

            boolean success = service.addNganSach(newNganSach);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Budget added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to add budget\"}");
            }
        } catch (JsonSyntaxException e) {
             System.err.println("Json Syntax Error during budget parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePutBudget(HttpServletRequest request, HttpServletResponse response, int budgetId) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        NganSach updatedNganSach = null;
        try {
             // Đọc trực tiếp từ request reader vào Gson
             updatedNganSach = gson.fromJson(request.getReader(), NganSach.class);

             if (updatedNganSach == null || updatedNganSach.getNgansach() <= 0 || updatedNganSach.getThang() == 0 || updatedNganSach.getNam() == 0) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid budget data\"}");
                 return;
            }

            updatedNganSach.setId_ngansach(budgetId); // Gán budgetId từ URL
            updatedNganSach.setId_nguoidung(userId); // Đảm bảo userId đúng (sử dụng Integer)

            // Log debug tạm thời trước khi gọi service
            System.out.println("Attempting to update budget: " + updatedNganSach);
            boolean success = service.updateNganSach(updatedNganSach);

            // --- DEBUG LOG SAU KHI GỌI SERVICE ---
            System.out.println("Result of updateNganSach: " + success);
            // -------------------------------------

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Budget updated successfully\"}");
            } else {
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Hoặc SC_FORBIDDEN nếu không thuộc về user
                 response.getWriter().write("{\"error\": \"Budget not found or does not belong to user\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during budget update parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleDeleteBudget(HttpServletRequest request, HttpServletResponse response, int budgetId) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        // Kiểm tra xem ngân sách có thuộc về người dùng đang đăng nhập không trước khi xóa
        NganSach budgetToDelete = service.getNganSachByIdAndUserId(budgetId, userId);

        if (budgetToDelete == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Budget not found or does not belong to user\"}");
            return;
        }

        try {
            boolean success = service.deleteNganSach(budgetId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Budget deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to delete budget\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetIcons(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<model.Icon> icons = service.getAllIcon();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(icons));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetColors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<model.MauSac> colors = service.getAllMauSac();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(colors));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetTransactionTypes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<model.LoaiGiaoDich> transactionTypes = service.getAllLoaiGiaoDich();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(transactionTypes));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePutUser(HttpServletRequest request, HttpServletResponse response, int requestedUserId) throws IOException {
        System.out.println("Inside handlePutUser for user ID: " + requestedUserId);
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        // Get the requesting user to check their role
        NguoiDung requestingUser = service.getUserById(authenticatedUserId);

        // Allow admin to update any user, but non-admin users can only update themselves
        if (requestingUser == null || (!"admin".equals(requestingUser.getRole()) && !authenticatedUserId.equals(requestedUserId))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"You are not authorized to update this user\"}");
            return;
        }

        NguoiDung updatedUser = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            updatedUser = gson.fromJson(request.getReader(), NguoiDung.class);

             if (updatedUser == null || updatedUser.getHoten() == null || updatedUser.getHoten().isEmpty() || updatedUser.getEmail() == null || updatedUser.getEmail().isEmpty()) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Invalid user data\"}");
                 return;
            }

            updatedUser.setId_nguoidung(requestedUserId); // Gán ID từ URL
            // Không cho phép cập nhật mật khẩu và role qua endpoint này
            updatedUser.setMatkhau(null); // Đảm bảo mật khẩu không bị cập nhật

             // Kiểm tra trùng email khi cập nhật
            if (service.isEmailExistsExcludingId(updatedUser.getEmail(), requestedUserId)) {
                 response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                 response.getWriter().write("{\"error\": \"Email already exists\"}");
                 return;
            }

            // Log debug tạm thời trước khi gọi service
            System.out.println("Attempting to update user: " + updatedUser);
            boolean success = service.updateUser(updatedUser);

            // --- DEBUG LOG SAU KHI GỌI SERVICE ---
            System.out.println("Result of updateUser: " + success);
            // -------------------------------------

            if (success) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 // Sau khi cập nhật thành công, lấy lại thông tin user mới nhất và trả về
                 NguoiDung latestUser = service.getUserById(requestedUserId);
                 if (latestUser != null) {
                      latestUser.setMatkhau(null); // Đảm bảo không trả về mật khẩu
                      response.getWriter().write(gson.toJson(latestUser));
                 } else {
                      // Trường hợp không tìm thấy user sau khi update (rất khó xảy ra nếu update thành công)
                      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                      response.getWriter().write("{\"error\": \"Failed to retrieve updated user data\"}");
                 }
            } else {
                 // Service sẽ trả về false nếu user không tồn tại hoặc email trùng
                 // Cần refine phản hồi tùy theo lý do thất bại (user not found vs email exists)
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Hoặc SC_CONFLICT nếu email trùng
                 response.getWriter().write("{\"error\": \"Failed to update user\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during user update parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetUserById(HttpServletRequest request, HttpServletResponse response, int requestedUserId) throws IOException {
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null || !authenticatedUserId.equals(requestedUserId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"You are not authorized to view this user\"}");
            return;
        }

        try {
            NguoiDung user = service.getUserById(requestedUserId);
            if (user != null) {
                 // Lưu ý: Không trả về mật khẩu trong response
                 user.setMatkhau(null); // Xóa mật khẩu trước khi gửi về client
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write(gson.toJson(user));
            } else {
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                 response.getWriter().write("{\"error\": \"User not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, int requestedUserId) throws IOException {
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null || !authenticatedUserId.equals(requestedUserId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"You are not authorized to change password for this user\"}");
            return;
        }

        ChangePasswordRequest passwordRequest = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            passwordRequest = gson.fromJson(request.getReader(), ChangePasswordRequest.class);

             if (passwordRequest == null || passwordRequest.getOldPassword() == null || passwordRequest.getOldPassword().isEmpty() || passwordRequest.getNewPassword() == null || passwordRequest.getNewPassword().isEmpty()) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().write("{\"error\": \"Old password and new password are required\"}");
                 return;
            }

             // Gọi service để đổi mật khẩu
            boolean success = service.changePassword(requestedUserId, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());

            if (success) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write("{\"message\": \"Password changed successfully\"}");
            } else {
                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Hoặc SC_FORBIDDEN nếu mật khẩu cũ sai
                 response.getWriter().write("{\"error\": \"Failed to change password - Incorrect old password or user not found\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during password change parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleSearchTransactions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing or empty keyword parameter\"}");
            return;
        }

        try {
            List<model.GiaoDich> searchResults = service.searchTransactionsByKeyword(userId, keyword);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(searchResults));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Logic logout phía server (nếu cần, ví dụ: invalidate session)
        // Trong trường hợp sử dụng JWT không có state trên server, chỉ cần thông báo thành công để client xóa token
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"message\": \"Logout successful\"}");
    }

    private void handlePostIcons(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Logic để thêm Icon mới
        Icon newIcon = null;
        try {
            newIcon = gson.fromJson(request.getReader(), Icon.class);

            if (newIcon == null || newIcon.getTen_icon() == null || newIcon.getTen_icon().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid icon data\"}");
                return;
            }

            boolean success = service.addIcon(newIcon);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Icon added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to add icon\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during icon parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePostColors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Logic để thêm Màu sắc mới
        MauSac newMauSac = null;
        try {
            newMauSac = gson.fromJson(request.getReader(), MauSac.class);

            if (newMauSac == null || newMauSac.getTen_mau() == null || newMauSac.getTen_mau().isEmpty() || newMauSac.getMa_mau() == null || newMauSac.getMa_mau().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid color data\"}");
                return;
            }

            boolean success = service.addMauSac(newMauSac);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Color added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to add color\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during color parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleDeleteIcons(HttpServletRequest request, HttpServletResponse response, int iconId) throws IOException {
        // Logic để xóa Icon
        try {
            boolean success = service.deleteIcon(iconId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Icon deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Icon not found or delete failed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleDeleteColors(HttpServletRequest request, HttpServletResponse response, int colorId) throws IOException {
        // Logic để xóa Màu sắc
        try {
            boolean success = service.deleteMauSac(colorId);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Color deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Color not found or delete failed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePutIcons(HttpServletRequest request, HttpServletResponse response, int iconId) throws IOException {
        // Logic để cập nhật Icon
        Icon updatedIcon = null;
        try {
            updatedIcon = gson.fromJson(request.getReader(), Icon.class);

            if (updatedIcon == null || updatedIcon.getTen_icon() == null || updatedIcon.getTen_icon().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid icon data\"}");
                return;
            }

            updatedIcon.setId_icon(iconId); // Gán iconId từ URL

            boolean success = service.updateIcon(updatedIcon);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Icon updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Icon not found or update failed\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during icon update parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handlePutColors(HttpServletRequest request, HttpServletResponse response, int colorId) throws IOException {
        // Logic để cập nhật Màu sắc
        MauSac updatedMauSac = null;
        try {
            updatedMauSac = gson.fromJson(request.getReader(), MauSac.class);

            if (updatedMauSac == null || updatedMauSac.getTen_mau() == null || updatedMauSac.getTen_mau().isEmpty() || updatedMauSac.getMa_mau() == null || updatedMauSac.getMa_mau().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid color data\"}");
                return;
            }

            updatedMauSac.setId_mau(colorId); // Gán colorId từ URL

            boolean success = service.updateMauSac(updatedMauSac);

            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Color updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Color not found or update failed\"}");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Json Syntax Error during color update parsing: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    // Thêm các phương thức doGet, doPut, doDelete cho các loại request khác nếu cần

    private void handleGetDefaultCategories(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<DanhMuc> defaultCategories = service.getAllDefaultCategories();
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(defaultCategories));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        NguoiDung requestingUser = service.getUserById(authenticatedUserId);

        if (requestingUser == null || !"admin".equals(requestingUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"Forbidden - Only admin can access all users\"}");
            return;
        }

        try {
            List<NguoiDung> allUsers = service.getAllUsers(); // Service đã set mật khẩu = null
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(allUsers));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    private void handleAdminAddUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        NguoiDung requestingUser = service.getUserById(authenticatedUserId);

        if (requestingUser == null || !"admin".equals(requestingUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"Forbidden - Only admin can add users\"}");
            return;
        }

        NguoiDung newUser = null;
        try {
            // Đọc trực tiếp từ request reader vào Gson
            newUser = gson.fromJson(request.getReader(), NguoiDung.class);

            // Kiểm tra dữ liệu cần thiết
            if (newUser == null || newUser.getEmail() == null || newUser.getEmail().isEmpty() || newUser.getMatkhau() == null || newUser.getMatkhau().isEmpty() || newUser.getHoten() == null || newUser.getHoten().isEmpty() || newUser.getRole() == null || newUser.getRole().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                response.getWriter().write("{\"error\": \"Missing required user data (email, password, hoten, role)\"}");
                return;
            }

            // Validate role value (ensure it's either 'user' or 'admin')
            if (!"user".equals(newUser.getRole()) && !"admin".equals(newUser.getRole())) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                 response.getWriter().write("{\"error\": \"Invalid role value. Must be 'user' or 'admin'.\"}");
                 return;
            }

            boolean success = service.adminAddUser(newUser);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                response.getWriter().write("{\"message\": \"User added successfully by admin\"}");
            } else {
                // Service.adminAddUser trả về false nếu email đã tồn tại
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                response.getWriter().write("{\"error\": \"Failed to add user - Email may already exist\"}");
            }
        } catch (JsonSyntaxException e) {
             System.err.println("Json Syntax Error during admin add user parsing: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
             response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred while adding user\"}");
        }
    }

    // Phương thức mới để xử lý yêu cầu xóa người dùng (chỉ admin)
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, int requestedUserId) throws IOException {
         Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
         if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
              response.getWriter().write("{\"error\": \"Unauthorized\"}");
              return;
         }

         // Get the requesting user to check their role
         NguoiDung requestingUser = service.getUserById(authenticatedUserId);

         // Only admin can delete users, and admin cannot delete their own account
         if (requestingUser == null || !"admin".equals(requestingUser.getRole()) || authenticatedUserId.equals(requestedUserId)) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
             response.getWriter().write("{\"error\": \"You are not authorized to delete this user or cannot delete your own account\"}");
             return;
         }

         try {
             boolean success = service.deleteUser(requestedUserId); // <-- Call service to delete user

             if (success) {
                 response.setStatus(HttpServletResponse.SC_OK); // 200 OK
                 response.getWriter().write("{\"message\": \"User deleted successfully\"}");
             } else {
                 response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Hoặc SC_INTERNAL_SERVER_ERROR nếu lỗi khác
                 response.getWriter().write("{\"error\": \"Failed to delete user or user not found\"}");
             }
         } catch (Exception e) {
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
             response.getWriter().write("{\"error\": \"An error occurred while deleting user\"}");
         }
    }

    // --- Phương thức xử lý tìm kiếm người dùng theo tên (Chỉ admin) ---
    private void handleSearchUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        NguoiDung requestingUser = service.getUserById(authenticatedUserId);

        if (requestingUser == null || !"admin".equals(requestingUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.getWriter().write("{\"error\": \"Forbidden - Only admin can search users\"}");
            return;
        }

        String nameQuery = request.getParameter("name");
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("{\"error\": \"Missing 'name' parameter\"}");
            return;
        }

        try {
            List<NguoiDung> users = service.searchUsersByName(nameQuery.trim()); // Service đã set mật khẩu = null
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(gson.toJson(users));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred while searching users\"}");
        }
    }

    // --- Phương thức xử lý tìm kiếm danh mục theo tên (Cho người dùng và admin) ---
    private void handleSearchCategories(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("name"); // hoặc "query" tùy frontend
        System.out.println("DEBUG: Searching categories with query: " + keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing 'name' parameter\"}");
            return;
        }

        Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
        if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        NguoiDung requestingUser = service.getUserById(authenticatedUserId);
        if (requestingUser == null) { // User not found after auth, should not happen
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
             response.getWriter().write("{\"error\": \"Unauthorized\"}");
             return;
        }

        try {
            List<DanhMuc> categories = service.searchCategoriesByName(keyword.trim(), requestingUser);
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(gson.toJson(categories));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred while searching categories\"}");
        }
    }

    // --- Phương thức xử lý tìm kiếm icon theo tên ---
    private void handleSearchIcons(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nameQuery = request.getParameter("name");
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("{\"error\": \"Missing 'name' parameter\"}");
            return;
        }

        try {
            List<Icon> icons = service.searchIconsByName(nameQuery.trim());
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(gson.toJson(icons));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred while searching icons\"}");
        }
    }

    // --- Phương thức xử lý tìm kiếm màu sắc theo tên ---
    private void handleSearchColors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nameQuery = null;
        
        // Thử lấy từ query parameter trước
        nameQuery = request.getParameter("name");
        
        // Nếu không có trong query parameter, thử đọc từ body
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = request.getReader().readLine()) != null) {
                    sb.append(line);
                }
                String jsonBody = sb.toString();
                if (!jsonBody.isEmpty()) {
                    // Parse JSON body để lấy tham số name
                    com.google.gson.JsonObject jsonObject = gson.fromJson(jsonBody, com.google.gson.JsonObject.class);
                    if (jsonObject.has("name")) {
                        nameQuery = jsonObject.get("name").getAsString();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error reading request body: " + e.getMessage());
            }
        }

        System.out.println("DEBUG: Received nameQuery in Servlet: " + nameQuery);

        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
            response.getWriter().write("{\"error\": \"Missing 'name' parameter in query or request body\"}");
            return;
        }

        try {
            List<MauSac> colors = service.searchColorsByName(nameQuery.trim());
            response.setStatus(HttpServletResponse.SC_OK); // 200 OK
            response.getWriter().write(gson.toJson(colors));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("{\"error\": \"An error occurred while searching colors\"}");
        }
    }

    // Phương thức xử lý lấy tất cả giao dịch theo ID người dùng
    private void handleGetAllTransactionsByUserId(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
        // Endpoint: /api/transactions/user/{userId}/all
        if (pathParts.length > 3 && "user".equals(pathParts[2]) && "all".equals(pathParts[4])) {
            try {
                int requestedUserId = Integer.parseInt(pathParts[3]);

                // Lấy userId từ request attribute (do AuthFilter đặt)
                Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

                if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    return;
                }

                // Lấy thông tin người dùng yêu cầu để kiểm tra role
                NguoiDung requestingUser = service.getUserById(authenticatedUserId);

                // Chỉ cho phép xem giao dịch của chính mình hoặc nếu là admin
                if (requestingUser == null || (!"admin".equals(requestingUser.getRole()) && !authenticatedUserId.equals(requestedUserId))) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' transactions\"}");
                    return;
                }

                // Lấy tất cả giao dịch của người dùng được yêu cầu
                List<GiaoDich> transactions = service.getAllGiaoDichByUserId(requestedUserId);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(transactions));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid user ID\"}");
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Internal server error\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/transactions/user/{userId}/all\"}");
        }
    }
} 