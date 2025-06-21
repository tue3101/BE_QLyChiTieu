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

import model.ChiTieuHangThang;
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
import java.util.Arrays;
import java.util.Calendar; // Vẫn cần Calendar cho các phương thức khác, nhưng không dùng cho LocalDate

@WebServlet("/api/*")
public class QuanLyChiTieuServlet extends HttpServlet {

	// serialVersionUID là mã phiên bản của một class dùng khi chuyển đối tượng
	// thành byte (tuần tự hóa) để lưu hoặc truyền đi.
	// Nó giúp Java biết class khi ghi và class khi đọc có giống nhau không.
	// Nếu khác, sẽ lỗi khi đọc lại đối tượng.
	private static final long serialVersionUID = 1L;
	private QuanLyChiTieuService service; // dùng xử lý nghiệp vụ từ các DAO

//gson là một đối tượng của thư viện Gson (Google) – dùng để chuyển đổi giữa JSON và Java object.
	private Gson gson;

	// khai báo hằng số lưu tên thuộc tính userId
	private static final String USER_ID_ATTRIBUTE = "userId";

	// dùng để đại diện cho dữ liệu đầu vào khi người dùng đăng nhập.
	private static class LoginRequest {
		String email;
		String matkhau;
	}

	// Định nghĩa lớp POJO cho Change Password Request
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
		String hoten; // Tên hiển thị
		String role;

		public String getEmail() {
			return email;
		}

		public String getMatkhau() {
			return matkhau;
		}

		public String getHoten() {
			return hoten;
		}

		public String getRole() {
			return role;
		}
	}

	public QuanLyChiTieuServlet() {
		super();// gọi constructor của lớp cha httpServlet
		// service để xử lý các nghiệp vụ như đăng nhập, đăng ký, giao dịch, danh
		// mục,...
		service = new QuanLyChiTieuService();

		// Cấu hình Gson để xử lý LocalDate
		// khởi tạo một "bộ cấu hình" để tạo ra đối tượng Gson
		GsonBuilder gsonBuilder = new GsonBuilder();

		// Đăng ký một bộ chuyển đổi tùy chỉnh (adapter) để Gson hiểu và xử lý kiểu
		// LocalDate, vì Gson không hỗ trợ sẵn LocalDate
		gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
		gson = gsonBuilder.create(); // để tạo ra đối tượng Gson thực tế có thể dùng để chuyển đổi Java ↔ JSON.
	}

	// Đây là một lớp chuyển đổi tùy chỉnh (TypeAdapter) dùng để hướng dẫn Gson cách
	// chuyển đổi LocalDate ↔ JSON
	private static class LocalDateAdapter extends TypeAdapter<LocalDate> {

		// bộ định dạng ngày tháng (DateTimeFormatter)
		private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// Hàm write() giúp ghi đối tượng LocalDate thành chuỗi ngày tháng kiểu
		// dd/MM/yyyy trong JSON
		@Override
		public void write(JsonWriter out, LocalDate value) throws IOException {
			if (value == null) { // nếu ngày tháng là null
				out.nullValue(); // ghi null vào json
			} else {
				// nếu có giá trị thì định dạng thành dd/mm/yyyy rồi chuyển thành chuỗi
				out.value(value.format(formatter));
			}
		}

		@Override
		public LocalDate read(JsonReader in) throws IOException {// JsonReader in dùng để đọc dữ liệu JSON và trả về
																	// kiểu LocalDate

			// in.peek()cho biết token kế tiếp trong JSON mà không tiêu thụ nó (peek = nhìn
			// trước).
			// JsonToken.NULL là một hằng số đại diện cho giá trị null trong JSON.
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
				in.nextNull();// đọc và bỏ qua giá trị null trong JSON
				return null;// trả về giá trị null trong Java
			}
			// đọc một chuỗi ngày từ JSON và chuyển nó thành một đối tượng LocalDate
			// in.nextString() Đọc giá trị kế tiếp trong JSON (ở vị trí hiện tại) dưới dạng
			// String.
			// LocalDate.parse(...) pthuc tĩnh để phân tích cú pháp một chuỗi thành đối
			// tượng LocalDate
			return LocalDate.parse(in.nextString(), formatter);
		}
	}

	// Add CORS headers before handling any request
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		setCorsHeaders(request, response); // dùng để thiết lập các header CORS
		response.setStatus(HttpServletResponse.SC_OK); // thông báo rằng yêu cầu OPTIONS được xử lý thành công.
	}

	// pthuc xử lý CORS
	private void setCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
		// Lấy giá trị của header Origin từ yêu cầu HTTP do trình duyệt gửi
		String origin = request.getHeader("Origin");
		if (origin != null && !origin.isEmpty()) { // ko null, ko rỗng
			// phản hồi sẽ đặt Access-Control-Allow-Origin bằng đúng giá trị origin đó
			response.setHeader("Access-Control-Allow-Origin", origin);
		}

		// xác định pthuc HTTP được phép từ client
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

		// Cho phép client gửi các header tùy chỉnh như Authorization hoặc Content-Type.
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

		// Cho phép trình duyệt gửi cookie hoặc thông tin xác thực (ví dụ JWT lưu trong
		// cookie).
		response.setHeader("Access-Control-Allow-Credentials", "true");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("DEBUG: Received GET request to path: " + request.getPathInfo());
		setCorsHeaders(request, response); // thiết lập các HTTP headers cần thiết để hỗ trợ CORS
		String pathInfo = request.getPathInfo(); // giúp lấy phần đường dẫn sau servlet path

		// Thiết lập Content-Type của HTTP response là application/json.
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8"); // Đặt mã hóa ký tự cho nội dung phản hồi là UTF-8 như t.viet, và
												// unicode

		if (pathInfo == null || pathInfo.equals("/")) { // null hoặc ko có gì sau /
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);// trạng thái notfound
			// dùng để ghi nội dung JSON vào response body
			response.getWriter()// lấy ra một printwriter để ghi dữ liệu phản hồi dạng văn bản
					.write( // ghi chuỗi json vào body
							"{\"error\": \"Endpoint not specified\"}");// dấu \" thoát ký tự " bên trong chuỗi jva
			return;
		}
		// vd: "/user/123" -> ["","user","123"]
		String[] pathParts = pathInfo.split("/"); // có tác dụng tách chuỗi pathInfo thành mảng các phần đường dẫn (path
													// segments), dựa trên dấu gạch chéo /.

		// dùng để lấy ra phần tử thứ hai trong mảng pathParts
		String resource = pathParts.length > 1 ? pathParts[1] : null;

		// Đặt kiểm tra /api/categories/search lên trước điều kiện /api/categories chung
		if ("categories".equals(resource) // Kiểm tra tên tài nguyên trong URL có phải là "categories" không.
				&& pathParts.length > 2 // Đảm bảo rằng pathParts có ít nhất 3 phần tử.
				&& "search".equals(pathParts[2])) { // Kiểm tra xem phần tử thứ 3 của đường dẫn có đúng là "search" hay
													// không.
			handleSearchCategories(request, response); // xử lý logic tìm kiếm danh mục (categories/search)
		} else if ("categories".equals(resource)) {
			handleGetCategories(request, response, pathParts); // phân nhánh các route
		} else if ("transactions".equals(resource)) {
			// Đặt kiểm tra /api/transactions/search lên trước các đường dẫn transactions
			if (pathParts.length > 2 // URL có ít nhất 3 phần
					&& "search".equals(pathParts[2])) {// phần thứ ba
				handleSearchTransactions(request, response);
			} else if (pathParts.length > 3 //Kiểm tra mảng có đủ phần tử để lấy được pathParts[4]
					&& "user".equals(pathParts[2]) //Xác định rằng URL đang nói đến người dùng (user).
					&& "all".equals(pathParts[4])) {//Xác định yêu cầu là lấy tất cả danh mục của người dùng đó
				handleGetAllTransactionsByUserId(request, response, pathParts);
			} else {
				//chuyển sang xử lý giao dịch (transactions).
				handleGetTransactions(request, response, pathParts);
			}
		} else if ("budget".equals(resource)) {// Kiểm tra tên tài nguyên trong URL có phải "budget"
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
		} else if ("users".equals(resource) && pathParts.length > 2 && !"search".equals(pathParts[2])) {
			// Expecting /api/users/{id} hoặc /api/users/{id}/password
			try {
				// Chuyển chuỗi pathParts[2] thành một số nguyên (int) và gán vào biến userId
				int userId = Integer.parseInt(pathParts[2]);
				if (pathParts.length > 3 && "password".equals(pathParts[3])) {
					// Handle change password
					handleChangePassword(request, response, userId);
				} else {
					// Handle update user info
					handleGetUserById(request, response, userId);
				}
			} catch (NumberFormatException e) {
				//trạng thái 400 (ko hợp lệ)
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Invalid user ID\"}");
			} catch (Exception e) {
				e.printStackTrace();//In ra ngăn xếp lỗi
				//Thiết lập mã lỗi HTTP 500 – Internal Server Error
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


			/////các endpoint mới 
//		} else if ("balance".equals(resource)) { 
//			handleGetBalance(request, response, pathParts);
		} else if ("nhom-loai".equals(resource)) {
			// Nếu có dạng /api/nhom-loai/user/{userId}
			if (pathParts.length > 3 && "user".equals(pathParts[2])) {
				try {
					System.out.println("DEBUG nhom-loai: pathParts[3] = " + pathParts[3]);
					int requestedUserId = Integer.parseInt(pathParts[3]);
					Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
					if (authenticatedUserId == null || authenticatedUserId != requestedUserId) {
						System.out.println("DEBUG nhom-loai: authenticatedUserId = " + authenticatedUserId + ", requestedUserId = " + requestedUserId);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' nhom-loai\"}");
						return;
					}
					List<model.NhomLoaiGD> list = service.getNhomLoaiGDByUserId(requestedUserId);
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("application/json");
					response.getWriter().write(gson.toJson(list));
				} catch (NumberFormatException e) {
					System.out.println("ERROR nhom-loai: Invalid user ID, pathParts[3] = " + pathParts[3]);
					e.printStackTrace();
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().write("{\"error\": \"Invalid user ID\"}");
				} catch (Exception e) {
					System.out.println("ERROR nhom-loai: Exception khi lấy nhom-loai theo userId " + pathParts[3]);
					e.printStackTrace();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().write("{\"error\": \"Internal server error\"}");
				}
				return;
			} else if (request.getMethod().equals("GET")) {
				handleGetNhomLoai(request, response);
				return;
			}
		} else if ("chi-tieu-hang-thang".equals(resource)) {
			if (pathParts.length > 3 && "user".equals(pathParts[2]) && "all".equals(pathParts[4])) {
//				int userId = Integer.parseInt(pathParts[3]);
//				List<ChiTieuHangThang> list = service.getAllCTHangThangByUserId(userId);
//				response.setStatus(HttpServletResponse.SC_OK);
//				response.setContentType("application/json");
//				response.getWriter().write(gson.toJson(list));
				handleGetAllChiTieuHangThangByUserId(request, response, pathParts);
				return;
			} else if (pathParts.length > 2 && "user".equals(pathParts[2]) && pathParts.length > 6 && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
				if (pathParts.length > 8 && "amount".equals(pathParts[8])) {
					if (request.getMethod().equals("GET")) {
						handleGetChiTieuHangThangAmount(request, response, pathParts);
						return;
					} 
				} else {
					handleGetChiTieuHangThangByMonth(request, response, pathParts);
					return; // <-- THÊM DÒNG NÀY
				}
			} else if (request.getMethod().equals("GET")) {
				handleGetAllChiTieuHangThang(request, response);
				return; // <-- THÊM DÒNG NÀY
			}
//		} else if ("income".equals(resource)) {
//			if (pathParts.length > 7 && "user".equals(pathParts[2]) && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
//				if (pathParts.length > 8 && "before".equals(pathParts[8])) {
//					handleGetIncomeBefore(request, response, pathParts);
//				} else if (pathParts.length > 8 && "after".equals(pathParts[8])) {
//					handleGetIncomeAfter(request, response, pathParts);
//				}
//			}
//		} else if ("expense".equals(resource)) {
//			if (pathParts.length > 7 && "user".equals(pathParts[2]) && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
//				handleGetExpense(request, response, pathParts);
//			}
		} else {
			//404 Tài nguyên được yêu cầu không tồn tại trên server.
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().write("{\"error\": \"Endpoint not found\"}");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//CORS là cơ chế bảo mật trình duyệt chặn các yêu cầu AJAX từ origin khác
		//dùng để thiết lập các header CORS cho phép trình duyệt từ domain khác gọi API của bạn một cách hợp lệ.
		setCorsHeaders(request, response); // 
		request.setCharacterEncoding("UTF-8");//Thiết lập bảng mã ký tự cho dữ liệu gửi từ client 
		String pathInfo = request.getPathInfo();//Lấy phần đường dẫn còn lại sau servlet path.
		response.setContentType("application/json");//Xác định rằng dữ liệu trả về sẽ ở định dạng JSON
		response.setCharacterEncoding("UTF-8");//Đảm bảo phản hồi gửi về sẽ dùng bảng mã UTF-8

		if (pathInfo == null || pathInfo.equals("/")) {//// null hoặc ko có gì sau /
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);//lỗi 404 not found
			response.getWriter().write("{\"error\": \"Endpoint not specified\"}");
			return;
		}

		String[] pathParts = pathInfo.split("/");//Tách pathInfo thành các phần nhỏ bằng dấu /
		String endpoint = pathParts.length > 1 ? pathParts[1] : null; //Nếu mảng có ít nhất 2 phần tử, lấy phần tử thứ hai (index 1) làm endpoint.

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
		} 
//		else if (request.getMethod().equals("POST")) {
//				handlePostNhomLoai(request, response);
//			}else {
//			//404
//			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//			response.getWriter().write("{\"error\": \"Endpoint not found\"}");
//		}
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		setCorsHeaders(request, response); // thiết lập các HTTP headers cần thiết để hỗ trợ CORS
		request.setCharacterEncoding("UTF-8");//Thiết lập bảng mã ký tự cho dữ liệu gửi từ client 
		String pathInfo = request.getPathInfo();// giúp lấy phần đường dẫn sau servlet path		
		response.setContentType("application/json");// Thiết lập Content-Type của HTTP response là application/json.
		response.setCharacterEncoding("UTF-8");// Đặt mã hóa ký tự cho nội dung phản hồi là UTF-8 như t.viet, và  unicode 

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
				//400 ko hợp lệ
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Invalid category ID\"}");
			} catch (Exception e) {
				e.printStackTrace();
				//500 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
		}else if ("chi-tieu-hang-thang".equals(resource)) {
			if (pathParts.length > 2 && "user".equals(pathParts[2]) && pathParts.length > 6 && "month".equals(pathParts[4]) && "year".equals(pathParts[6])) {
				if (pathParts.length > 8 && "amount".equals(pathParts[8])) {
					handlePutChiTieuHangThangAmount(request, response, pathParts);
					return;
				}
			} 
		else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().write("{\"error\": \"Endpoint not found or missing ID\"}");
		}

		System.out.println("DEBUG doPut pathInfo: " + pathInfo);
		System.out.println("DEBUG doPut pathParts: " + Arrays.toString(pathParts));
		System.out.println("DEBUG doPut resource: " + resource);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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

	
	//nơi xử lý các y/c lấy danh mục 
	private void handleGetCategories(HttpServletRequest request, HttpServletResponse response, String[] pathParts)
			throws IOException {
		// Expecting /api/categories/user/{userId}
		if (pathParts.length > 3 && "user".equals(pathParts[2])) {
			try {
				int requestedUserId = Integer.parseInt(pathParts[3]);
				// Lấy userId từ request attribute (do AuthFilter đặt)
				Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

				if (authenticatedUserId == null || authenticatedUserId != requestedUserId) {
					// Người dùng yêu cầu danh mục của người khác hoặc không xác thực
					response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Yêu cầu đã được xác thực, nhưng người dùng không có quyền truy cập tài nguyên này.
					response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' categories\"}");
					return;
				}

				List<DanhMuc> categories = service.getDanhMucByUserId(requestedUserId);
				response.setStatus(HttpServletResponse.SC_OK);
				//Bạn đang chuyển danh sách categories sang định dạng JSON và ghi nó vào phản hồi HTTP để gửi về client.
				response.getWriter().write(gson.toJson(categories));
			} catch (NumberFormatException e) {
				//400 request ko hợp lệ
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Invalid user ID\"}");
			} catch (Exception e) {
				e.printStackTrace();
				//500 lỗi server
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("{\"error\": \"Internal server error\"}");
			}
		} else {
			//400 ko tìm thấy trên server
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/categories/user/{userId}\"}");
		}
	}

	private void handlePostCategories(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Lấy userId từ attribute của request (được AuthFilter set)
		Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);

		if (userId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"User not authenticated to add categories\"}");
			return;
		}

		try {
			// Đọc JSON từ request body và chuyển thành đối tượng DanhMuc
			DanhMuc newCategory = gson.fromJson(request.getReader(), DanhMuc.class);

			// Xử lý id_tennhom dựa trên loại danh mục
			if (newCategory.getId_loai() == 2) {
				// Nếu danh mục thuộc loại "Chi tiêu" (id_loai = 2),
				// mặc định gán nó vào nhóm "Chi tiêu phát sinh" (id_tennhom = 2).
				newCategory.setId_tennhom(2);
			} else if (newCategory.getId_loai() == 1) {
				// Nếu danh mục thuộc loại "Thu nhập" (id_loai = 1),
				// mặc định gán nó vào nhóm "Thu nhập" (id_tennhom = 1).
				newCategory.setId_tennhom(1);
			}
			// Nếu id_tennhom vẫn là null hoặc 0, có thể set giá trị mặc định khác nếu cần

			// Gán userId đã xác thực cho danh mục mới.
			// Điều này đảm bảo người dùng chỉ có thể thêm danh mục cho chính họ.
			newCategory.setId_nguoidung(userId);

			// Bạn đang gọi phương thức addDanhMuc(...) trong lớp service để thêm một danh mục mới
			boolean success = service.addDanhMuc(newCategory);

			if (success) {
				response.setStatus(HttpServletResponse.SC_CREATED);
				// Trả về danh mục đã được tạo (có thể đã có id mới từ DB)
				response.getWriter().write(gson.toJson(newCategory));
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("{\"error\": \"Failed to add category\"}");
			}
		} catch (JsonSyntaxException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}");
			e.printStackTrace(); // Log lỗi ra console server để debug
		}
	}

	private void handlePutCategories(HttpServletRequest request, HttpServletResponse response, int categoryId)
			throws IOException {
		// Lấy userId từ attribute của request (được AuthFilter set)
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

			if (updatedDanhMuc == null || updatedDanhMuc.getTen_danh_muc() == null
					|| updatedDanhMuc.getTen_danh_muc().isEmpty() || updatedDanhMuc.getId_icon() == 0
					|| updatedDanhMuc.getId_mau() == 0 || updatedDanhMuc.getId_loai() == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Invalid category data\"}");
				return;
			}

			updatedDanhMuc.setId_danhmuc(categoryId); // Gán categoryId từ URL

			// Xử lý id_tennhom dựa trên loại danh mục (tương tự như thêm mới)
			if (updatedDanhMuc.getId_loai() == 2) {
				// Nếu danh mục thuộc loại "Chi tiêu" (id_loai = 2),
				// mặc định gán nó vào nhóm "Chi tiêu phát sinh" (id_tennhom = 2).
				updatedDanhMuc.setId_tennhom(2);
			} else if (updatedDanhMuc.getId_loai() == 1) {
				// Nếu danh mục thuộc loại "Thu nhập" (id_loai = 1),
				// mặc định gán nó vào nhóm "Thu nhập" (id_tennhom = 1).
				updatedDanhMuc.setId_tennhom(1);
			}
			// Nếu id_tennhom vẫn là null hoặc 0, có thể set giá trị mặc định khác nếu cần

			// Lấy thông tin người dùng để kiểm tra role
			NguoiDung updatingUser = service.getUserById(userId);

			if (updatingUser != null && "admin".equals(updatingUser.getRole())) {
				// Nếu người dùng là admin, đặt id_nguoidung là null khi cập nhật
				updatedDanhMuc.setId_nguoidung(null); // Sử dụng null cho danh mục mặc định
			} else { // Nếu không phải admin (người dùng bình thường), đảm bảo id_nguoidung không bị
						// thay đổi (giữ nguyên của user)
				// Hoặc có thể kiểm tra xem danh mục này có thuộc về user đó không trước khi
				// cho phép cập nhật
				// Logic kiểm tra quyền sở hữu danh mục đã có trong handleDeleteCategories, có
				// thể áp dụng tương tự ở đây
				DanhMuc existingCategory = service.getDanhMucByIdAndUserId(categoryId, userId);
				if (existingCategory == null) {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Hoặc SC_NOT_FOUND
					response.getWriter().write("{\"error\": \"Category not found or does not belong to user\"}");
					return;
				}
				// Giữ nguyên id_nguoidung của danh mục gốc hoặc set lại userId nếu cần cập nhật
				// data khác
				// updatedDanhMuc.setId_nguoidung(userId); // Dòng này không cần thiết nếu chỉ
				// update data khác và giữ nguyên quyền sở hữu
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

	private void handleDeleteCategories(HttpServletRequest request, HttpServletResponse response, int categoryId)
			throws IOException {
		Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
		if (userId == null) { // Should not happen if AuthFilter works correctly
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"Unauthorized\"}");
			return;
		}

		// Get the requesting user to check their role
		NguoiDung requestingUser = service.getUserById(userId);

		// If the user is admin, allow them to delete any category. Otherwise, check
		// ownership.
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

	private void handleGetTransactions(HttpServletRequest request, HttpServletResponse response, String[] pathParts)
			throws IOException {
		// Expecting /api/transactions/user/{userId}/month/{month}/year/{year}
		if (pathParts.length > 5 && "user".equals(pathParts[2]) && "month".equals(pathParts[4])
				&& "year".equals(pathParts[6])) {
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
			response.getWriter().write(
					"{\"error\": \"Endpoint not found. Use /api/transactions/user/{userId}/month/{month}/year/{year}\"}");
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

			if (newGiaoDich == null || newGiaoDich.getId_danhmuc() == null || newGiaoDich.getId_loai() == null
					|| newGiaoDich.getSo_tien() <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Invalid transaction data\"}");
				return;
			}

			newGiaoDich.setId_nguoidung(String.valueOf(userId)); // Gán userId từ token

			// bất kỳ giao dịch nào nếu không truyền id_tennhom thì mặc định gán id_tennhom = "1"
			if (newGiaoDich.getId_tennhom() == null || newGiaoDich.getId_tennhom().isEmpty()) {
				newGiaoDich.setId_tennhom("2");
			}

			// Đảm bảo ngày, tháng, năm được thiết lập nếu cần (từ request hoặc hệ thống)
			// Logic xử lý ngày, tháng, năm đã được chuyển sang Service để tập trung
			// Nếu ngày không được cung cấp, Service sẽ sử dụng ngày hiện tại và thiết lập
			// tháng/năm
			// Nếu ngày được cung cấp, Service sẽ trích xuất tháng/năm
			// Không cần xử lý logic này ở đây nữa.
			// Đảm bảo rằng trong Service, bạn đã xử lý đúng việc gán thang/nam từ ngay nếu
			// ngay != null
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

	private void handlePutTransactions(HttpServletRequest request, HttpServletResponse response, int transactionId)
			throws IOException {
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

			if (updatedGiaoDich == null || updatedGiaoDich.getId_danhmuc() == null
					|| updatedGiaoDich.getId_loai() == null || updatedGiaoDich.getSo_tien() <= 0) {
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

	private void handleDeleteTransactions(HttpServletRequest request, HttpServletResponse response, int transactionId)
			throws IOException {
		// Lấy userId từ request attribute (do AuthFilter đặt)
		Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
		if (userId == null) { // Should not happen if AuthFilter works correctly
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"Unauthorized\"}");
			return;
		}

		// Kiểm tra xem giao dịch có thuộc về người dùng đang đăng nhập không trước khi
		// xóa
		GiaoDich transactionToDelete = service.getGiaoDichByIdAndUserId(transactionId, userId); // Sử dụng phương thức
																								// mới

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
				System.out.println("Email: " + (loginRequest.email != null ? loginRequest.email : "null")
						+ ", Password: " + (loginRequest.matkhau != null ? "masked" : "null")); // Log password cẩn thận
			}

			// Kiểm tra xem loginRequest có null không hoặc các trường cần thiết có null
			// không
			if (loginRequest == null || loginRequest.email == null || loginRequest.email.isEmpty()
					|| loginRequest.matkhau == null || loginRequest.matkhau.isEmpty()) {
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

			// Debug log
			System.out.println("Register request received: " + gson.toJson(registerRequest));

			if (registerRequest == null || registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()
					|| registerRequest.getMatkhau() == null || registerRequest.getMatkhau().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
				response.getWriter().write("{\"error\": \"Email and password are required\"}");
				return;
			}

			// Việc kiểm tra email đã tồn tại được xử lý trong Service.register
			NguoiDung newUser = new NguoiDung();
			newUser.setEmail(registerRequest.getEmail());
			newUser.setMatkhau(registerRequest.getMatkhau()); // Mật khẩu sẽ được mã hóa trong Service

			// Xử lý tên hiển thị
			String tenHienThi = registerRequest.getHoten();
			System.out.println("Ten hien thi from request: " + tenHienThi);

			if (tenHienThi != null && !tenHienThi.trim().isEmpty()) {
				newUser.setHoten(tenHienThi.trim());
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Tên hiển thị không được để trống\"}");
				return;
			}

			// Gán role mặc định nếu không được cung cấp hoặc không hợp lệ
			if (registerRequest.getRole() != null
					&& (registerRequest.getRole().equals("user") || registerRequest.getRole().equals("admin"))) {
				newUser.setRole(registerRequest.getRole());
			} else {
				newUser.setRole("user"); // Role mặc định
			}

			boolean success = service.register(newUser);

			if (success) {
				// Đăng ký thành công, có thể trả về thông báo hoặc tự động đăng nhập và trả về
				// token
				response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
				response.getWriter().write("{\"message\": \"Registration successful\"}");
			} else {
				// Nếu Service.register trả về false (ví dụ: email đã tồn tại), trả về 409
				// Conflict
				response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
				response.getWriter().write("{\"error\": \"Registration failed - Email may already exist\"}");
			}
		} catch (JsonSyntaxException e) {
			System.err.println("Json Syntax Error during register parsing: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
			response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
			response.getWriter().write("{\"error\": \"An error occurred during registration\"}");
		}
	}

	private void handleGetBudget(HttpServletRequest request, HttpServletResponse response, String[] pathParts)
			throws IOException {
		// Expecting /api/budget/user/{userId}/month/{month}/year/{year}
		if (pathParts.length > 5 && "user".equals(pathParts[2]) && "month".equals(pathParts[4])
				&& "year".equals(pathParts[6])) {
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
			response.getWriter().write(
					"{\"error\": \"Endpoint not found. Use /api/budget/user/{userId}/month/{month}/year/{year}\"}");
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

			if (newNganSach == null || newNganSach.getNgansach() <= 0 || newNganSach.getThang() == 0
					|| newNganSach.getNam() == 0) {
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

	private void handlePutBudget(HttpServletRequest request, HttpServletResponse response, int budgetId)
			throws IOException {
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

			if (updatedNganSach == null || updatedNganSach.getNgansach() <= 0 || updatedNganSach.getThang() == 0
					|| updatedNganSach.getNam() == 0) {
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

	private void handleDeleteBudget(HttpServletRequest request, HttpServletResponse response, int budgetId)
			throws IOException {
		Integer userId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
		if (userId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"Unauthorized\"}");
			return;
		}

		// Kiểm tra xem ngân sách có thuộc về người dùng đang đăng nhập không trước khi
		// xóa
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

	private void handleGetTransactionTypes(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
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

	private void handlePutUser(HttpServletRequest request, HttpServletResponse response, int requestedUserId)
			throws IOException {
		System.out.println("Inside handlePutUser for user ID: " + requestedUserId);
		Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
		if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
			response.getWriter().write("{\"error\": \"Unauthorized\"}");
			return;
		}

		// Get the requesting user to check their role
		NguoiDung requestingUser = service.getUserById(authenticatedUserId);

		// Allow admin to update any user, but non-admin users can only update
		// themselves
		if (requestingUser == null
				|| (!"admin".equals(requestingUser.getRole()) && !authenticatedUserId.equals(requestedUserId))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
			response.getWriter().write("{\"error\": \"You are not authorized to update this user\"}");
			return;
		}

		NguoiDung updatedUser = null;
		try {
			// Đọc trực tiếp từ request reader vào Gson
			updatedUser = gson.fromJson(request.getReader(), NguoiDung.class);

			if (updatedUser == null || updatedUser.getHoten() == null || updatedUser.getHoten().isEmpty()
					|| updatedUser.getEmail() == null || updatedUser.getEmail().isEmpty()) {
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
					// Trường hợp không tìm thấy user sau khi update (rất khó xảy ra nếu update
					// thành công)
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

	private void handleGetUserById(HttpServletRequest request, HttpServletResponse response, int requestedUserId)
			throws IOException {
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

	private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, int requestedUserId)
			throws IOException {
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

			if (passwordRequest == null || passwordRequest.getOldPassword() == null
					|| passwordRequest.getOldPassword().isEmpty() || passwordRequest.getNewPassword() == null
					|| passwordRequest.getNewPassword().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("{\"error\": \"Old password and new password are required\"}");
				return;
			}

			// Gọi service để đổi mật khẩu
			boolean success = service.changePassword(requestedUserId, passwordRequest.getOldPassword(),
					passwordRequest.getNewPassword());

			if (success) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write("{\"message\": \"Password changed successfully\"}");
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Hoặc SC_FORBIDDEN nếu mật khẩu cũ sai
				response.getWriter()
						.write("{\"error\": \"Failed to change password - Incorrect old password or user not found\"}");
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
		// Trong trường hợp sử dụng JWT không có state trên server, chỉ cần thông báo
		// thành công để client xóa token
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

			if (newMauSac == null || newMauSac.getTen_mau() == null || newMauSac.getTen_mau().isEmpty()
					|| newMauSac.getMa_mau() == null || newMauSac.getMa_mau().isEmpty()) {
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

	private void handleDeleteIcons(HttpServletRequest request, HttpServletResponse response, int iconId)
			throws IOException {
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

	private void handleDeleteColors(HttpServletRequest request, HttpServletResponse response, int colorId)
			throws IOException {
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

	private void handlePutIcons(HttpServletRequest request, HttpServletResponse response, int iconId)
			throws IOException {
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

	private void handlePutColors(HttpServletRequest request, HttpServletResponse response, int colorId)
			throws IOException {
		// Logic để cập nhật Màu sắc
		MauSac updatedMauSac = null;
		try {
			updatedMauSac = gson.fromJson(request.getReader(), MauSac.class);

			if (updatedMauSac == null || updatedMauSac.getTen_mau() == null || updatedMauSac.getTen_mau().isEmpty()
					|| updatedMauSac.getMa_mau() == null || updatedMauSac.getMa_mau().isEmpty()) {
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

	private void handleGetDefaultCategories(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
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
			if (newUser == null || newUser.getEmail() == null || newUser.getEmail().isEmpty()
					|| newUser.getMatkhau() == null || newUser.getMatkhau().isEmpty() || newUser.getHoten() == null
					|| newUser.getHoten().isEmpty() || newUser.getRole() == null || newUser.getRole().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
				response.getWriter()
						.write("{\"error\": \"Missing required user data (email, password, hoten, role)\"}");
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
	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, int requestedUserId)
			throws IOException {
		Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
		if (authenticatedUserId == null) { // Should not happen if AuthFilter works correctly
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
			response.getWriter().write("{\"error\": \"Unauthorized\"}");
			return;
		}

		// Get the requesting user to check their role
		NguoiDung requestingUser = service.getUserById(authenticatedUserId);

		// Only admin can delete users, and admin cannot delete their own account
		if (requestingUser == null || !"admin".equals(requestingUser.getRole())
				|| authenticatedUserId.equals(requestedUserId)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
			response.getWriter().write(
					"{\"error\": \"You are not authorized to delete this user or cannot delete your own account\"}");
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

	// --- Phương thức xử lý tìm kiếm danh mục theo tên (Cho người dùng và admin)
	// ---
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
	private void handleGetAllTransactionsByUserId(HttpServletRequest request, HttpServletResponse response,
			String[] pathParts) throws IOException {
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
				if (requestingUser == null || (!"admin".equals(requestingUser.getRole())
						&& !authenticatedUserId.equals(requestedUserId))) {
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

	// --- Nhóm loại giao dịch ---
	private void handleGetNhomLoai(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<model.NhomLoaiGD> list = service.getALLNhomLoaiGD();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().write(gson.toJson(list));
	}

	// --- Chi tiêu hàng tháng ---
	private void handleGetAllChiTieuHangThang(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<model.ChiTieuHangThang> list = service.getAllCTHangThang();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().write(gson.toJson(list));
	}
	
	//lấy chi tiêu hàng tháng bằng tháng , năm
	private void handleGetChiTieuHangThangByMonth(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
		int userId = Integer.parseInt(pathParts[3]);
		int month = Integer.parseInt(pathParts[5]);
		int year = Integer.parseInt(pathParts[7]);
		// Gọi service để lấy số tiền
		java.math.BigDecimal amount = service.getSoTien(userId, month, year);
		
		// Chuyển BigDecimal thành int (cắt bỏ phần thập phân)
		int amountAsInt = (amount != null) ? amount.intValue() : 0;
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		// Trả về JSON chứa số tiền dạng int
		response.getWriter().write("{\"amount\": " + amountAsInt + "}");
	}
	
	//lấy số tiền chi tiêu hàng tháng
	private void handleGetChiTieuHangThangAmount(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
		int userId = Integer.parseInt(pathParts[3]);
		int month = Integer.parseInt(pathParts[5]);
		int year = Integer.parseInt(pathParts[7]);
		java.math.BigDecimal amount = service.getSoTien(userId, month, year);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.getWriter().write("{\"amount\": " + amount + "}");
	}
	
	//cập nhật số tiền hàng tháng
	private void handlePutChiTieuHangThangAmount(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
		// Đọc body 1 lần duy nhất
		String body = request.getReader().lines().reduce("", (acc, line) -> acc + line);
		System.out.println("DEBUG PUT ChiTieuHangThang:");
		System.out.println("userId: " + pathParts[3]);
		System.out.println("month: " + pathParts[5]);
		System.out.println("year: " + pathParts[7]);
		System.out.println("amount (body): " + body);

		int userId = Integer.parseInt(pathParts[3]);
		int month = 0;
		int year = 0;
		try {
			month = Integer.parseInt(pathParts[5]);
			year = Integer.parseInt(pathParts[7]);
		} catch (Exception e) {
			// Nếu không parse được thì để 0, sẽ lấy ngày hiện tại bên dưới
		}
		// Nếu không truyền hoặc truyền 0 thì lấy tháng/năm hiện tại
		if (month <= 0 || year <= 0) {
			java.time.LocalDate now = java.time.LocalDate.now();
			month = now.getMonthValue();
			year = now.getYear();
		}
		com.google.gson.JsonObject json = gson.fromJson(body, com.google.gson.JsonObject.class);
		java.math.BigDecimal newAmount = json.get("amount").getAsBigDecimal();
		boolean success = service.updateSoTien(userId, month, year, newAmount);
		if (success) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write("{\"message\": \"Cập nhật số tiền thành công\"}");
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"error\": \"Cập nhật số tiền thất bại\"}");
		}
	}

	// --- lấy tất cả Chi tiêu hàng tháng ---
	private void handleGetAllChiTieuHangThangByUserId(HttpServletRequest request, HttpServletResponse response, String[] pathParts) throws IOException {
		// Endpoint: /api/chi-tieu-hang-thang/user/{userId}/all
		if (pathParts.length > 4 && "user".equals(pathParts[2]) && "all".equals(pathParts[4])) {
			try {
				int requestedUserId = Integer.parseInt(pathParts[3]);
				Integer authenticatedUserId = (Integer) request.getAttribute(USER_ID_ATTRIBUTE);
				if (authenticatedUserId == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("{\"error\": \"Unauthorized\"}");
					return;
				}
				// Chỉ cho phép xem của chính mình 
				NguoiDung requestingUser = service.getUserById(authenticatedUserId);
				if (requestingUser == null || (!"admin".equals(requestingUser.getRole()) && !authenticatedUserId.equals(requestedUserId))) {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write("{\"error\": \"Forbidden - Cannot access other users' monthly expenses\"}");
					return;
				}
				List<model.ChiTieuHangThang> list = service.getAllCTHangThangByUserId(requestedUserId);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.getWriter().write(gson.toJson(list));
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
			response.getWriter().write("{\"error\": \"Endpoint not found. Use /api/chi-tieu-hang-thang/user/{userId}/all\"}");
		}
	}
}