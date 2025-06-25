package service;

import dao.*;
import model.*;
import util.JwtUtil;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import config.DBConnection;

//tầng service là nơi sử lý nghiệp vụ: xthuc , mã hóa...
public class QuanLyChiTieuService {
	// Khai báo các DAO tương ứng với từng bảng trong CSDL.
	private NguoiDungDAO nguoiDungDAO;
	private DanhMucDAO danhMucDAO;
	private GiaoDichDAO giaoDichDAO;
	private NganSachDAO nganSachDAO;
	private IconDAO iconDAO;
	private MauSacDAO mauSacDAO;
	private LoaiGiaoDichDAO loaiGiaoDichDAO;
	private NhomLoaiDAO nhomLoaiDAO; // khai báo thêm nhóm loại
	private ChiTieuHangThangDAO chiTieuHangThangDAO; // khai báo chi tiêu hàng tháng

	
	//============các DAO mới======//
	private BuoiDAO buoiDAO;
	private MucLuongDAO mucLuongDAO;
	private ChiTieuMauDAO chiTieuMauDAO;
	private GoiYDAO goiyDAO;
	private LoaiChiTieuDAO loaiChiTieuDAO;
	
	
	
	public QuanLyChiTieuService() {
		// đối tượng mới từ lớp DAO và gán cho biến để dùng các hàm có trong lớp DAO
		this.nguoiDungDAO = new NguoiDungDAO();
		this.danhMucDAO = new DanhMucDAO();
		this.giaoDichDAO = new GiaoDichDAO();
		this.nganSachDAO = new NganSachDAO();
		this.iconDAO = new IconDAO();
		this.mauSacDAO = new MauSacDAO();
		this.loaiGiaoDichDAO = new LoaiGiaoDichDAO();
		this.nhomLoaiDAO = new NhomLoaiDAO();// thêm đối tượng và gán cho biến
		this.chiTieuHangThangDAO = new ChiTieuHangThangDAO();// thêm
	//================các đối tượng mới======//
		this.buoiDAO = new BuoiDAO();
		this.mucLuongDAO = new MucLuongDAO();
		this.chiTieuMauDAO = new ChiTieuMauDAO();
		this.goiyDAO = new GoiYDAO();
		this.loaiChiTieuDAO = new LoaiChiTieuDAO();
	
	
	}

	// Xử lý đăng nhập
	public LoginResponse login(String email, String password) {
		NguoiDung user = nguoiDungDAO.login(email, password);
		if (user != null) {
			String token = JwtUtil.generateToken(user.getId_nguoidung(), user.getEmail(), user.getRole());
			return new LoginResponse(user, token);
		}
		return null;
	}

	// Xử lý đăng ký
	// Trả về false nếu email đã tồn tại hoặc lỗi khi thêm vào DB
	public boolean register(NguoiDung nguoiDung) {
		// Kiểm tra xem email đã tồn tại chưa
		if (nguoiDungDAO.isEmailExists(nguoiDung.getEmail())) {
			// Email đã tồn tại, không đăng ký
			return false; // Hoặc bạn có thể throw một exception riêng ở đây
		}

		// Nếu email chưa tồn tại, tiến hành thêm vào DB
		boolean success = nguoiDungDAO.add(nguoiDung);
		if (success) {
			// Sau khi thêm user thành công, tạo bản ghi chi tiêu hàng tháng mặc định
			NguoiDung newUser = nguoiDungDAO.login(nguoiDung.getEmail(), nguoiDung.getMatkhau()); // Lấy lại user để lấy id
			if (newUser != null) {
				LocalDate today = LocalDate.now();
				model.ChiTieuHangThang ctht = new model.ChiTieuHangThang(
					newUser.getId_nguoidung(),
					1, // id_tennhom = 1 (chi tiêu hàng tháng)
					today.getMonthValue(),
					today.getYear(),
					0.0 // số tiền mặc định = 0
				);
				chiTieuHangThangDAO.add(ctht);
			}
		}
		return success;
	}

	// Cập nhật thông tin người dùng
	public boolean updateUser(NguoiDung nguoiDung) {
		// Gọi phương thức mới chỉ cập nhật thông tin (không mật khẩu)
		return nguoiDungDAO.updateUserInfo(nguoiDung);
	}

	// Đổi mật khẩu người dùng
	public boolean changePassword(int userId, String oldPassword, String newPassword) {
		NguoiDung user = nguoiDungDAO.getById(userId);
		if (user != null) {
			// Xác thực mật khẩu cũ
			if (util.PasswordUtil.verifyPassword(oldPassword, user.getMatkhau())) {
				// Mã hóa mật khẩu mới
				String hashedNewPassword = util.PasswordUtil.hashPassword(newPassword);
				// Cập nhật mật khẩu mới vào database
				user.setMatkhau(hashedNewPassword);
				return nguoiDungDAO.update(user); // Sử dụng lại phương thức update
			}
		}
		return false; // Xác thực mật khẩu cũ thất bại hoặc người dùng không tồn tại
	}

	// Xác thực token còn hạn ko
	public boolean validateToken(String token) {
		try {
			return !JwtUtil.isTokenExpired(token);
		} catch (Exception e) {
			return false;
		}
	}

	// Lấy thông tin người dùng từ token
	public NguoiDung getUserFromToken(String token) {
		try {
			// gán thông tin mã hóa vào đối tượng claims
			io.jsonwebtoken.Claims claims = JwtUtil.validateToken(token);
			// getSubject() Lấy ra thông tin chính định danh người dùng

			int userId = Integer.parseInt(claims.getSubject());
			return nguoiDungDAO.getById(userId);
		} catch (Exception e) {
			return null;
		}
	}

	// Lấy thông tin người dùng theo ID
	public NguoiDung getUserById(int userId) {
		return nguoiDungDAO.getById(userId);
	}

	// Lấy danh sách danh mục của người dùng
	public List<DanhMuc> getDanhMucByUserId(int userId) {
		return danhMucDAO.getByUserId(userId);
	}

	// Lấy danh mục theo ID và kiểm tra người dùng sở hữu
	public DanhMuc getDanhMucByIdAndUserId(int categoryId, int userId) {
		DanhMuc danhMuc = danhMucDAO.getById(categoryId);
		if (danhMuc != null && danhMuc.getId_nguoidung() == userId) {
			return danhMuc;
		}
		return null;
	}

	// Phương thức mới để lấy danh mục theo ID (không kiểm tra người dùng sở hữu)
	public DanhMuc getDanhMucById(int categoryId) {
		return danhMucDAO.getById(categoryId);
	}

	// Phương thức mới để lấy tất cả danh mục mặc định
	public List<DanhMuc> getAllDefaultCategories() {
		return danhMucDAO.getAllDefaultCategories();
	}

	// Thêm danh mục mới
	public boolean addDanhMuc(DanhMuc danhMuc) {
		return danhMucDAO.add(danhMuc);
	}

	// Cập nhật danh mục
	public boolean updateDanhMuc(DanhMuc danhMuc) {
		return danhMucDAO.update(danhMuc);
	}

	// Xóa danh mục
	public boolean deleteDanhMuc(int id) {
		return danhMucDAO.delete(id);
	}

	// Thêm giao dịch mới
	public boolean addGiaoDich(GiaoDich giaoDich) {
		// Xử lý gán tháng/năm từ ngày nếu chưa có
		if (giaoDich.getNgay() != null) {
			if (giaoDich.getThang() == 0 || giaoDich.getNam() == 0) {
				giaoDich.setThang(giaoDich.getNgay().getMonthValue());
				giaoDich.setNam(giaoDich.getNgay().getYear());
			}
		} else { // Nếu ngày không được cung cấp, gán ngày hiện tại và trích xuất tháng/năm
			LocalDate today = LocalDate.now();
			giaoDich.setNgay(today);
			giaoDich.setThang(today.getMonthValue());
			giaoDich.setNam(today.getYear());
		}
		return giaoDichDAO.add(giaoDich);
	}

	// Cập nhật giao dịch
	public boolean updateGiaoDich(GiaoDich giaoDich) {
		// Xử lý cập nhật tháng/năm nếu ngày được cung cấp
		if (giaoDich.getNgay() != null) {
			giaoDich.setThang(giaoDich.getNgay().getMonthValue());
			giaoDich.setNam(giaoDich.getNgay().getYear());
		}
		return giaoDichDAO.update(giaoDich);
	}

	// Xóa giao dịch
	public boolean deleteGiaoDich(int id) {
		return giaoDichDAO.delete(id);
	}

	// Lấy danh sách giao dịch của người dùng theo tháng
	public List<GiaoDich> getGiaoDichByMonth(int userId, int month, int year) {
		return giaoDichDAO.getByMonth(userId, month, year);
	}

	// Lấy giao dịch theo ID và kiểm tra người dùng sở hữu
	public GiaoDich getGiaoDichByIdAndUserId(int transactionId, int userId) {
		GiaoDich giaoDich = giaoDichDAO.getById(transactionId);
		if (giaoDich != null && giaoDich.getId_nguoidung().equals(String.valueOf(userId))) {
			return giaoDich;
		}
		return null;
	}

	// Tìm kiếm giao dịch theo từ khóa và người dùng
	public List<GiaoDich> searchTransactionsByKeyword(int userId, String keyword) {
		return giaoDichDAO.searchByKeyword(userId, keyword);
	}

	// Thêm ngân sách mới
	public boolean addNganSach(NganSach nganSach) {
		return nganSachDAO.add(nganSach);
	}

	// Cập nhật ngân sách
	public boolean updateNganSach(NganSach nganSach) {
		return nganSachDAO.update(nganSach);
	}

	// Lấy ngân sách theo ID
	public NganSach getNganSachById(int budgetId) {
		return nganSachDAO.getById(budgetId);
	}

	// Lấy ngân sách theo ID và kiểm tra người dùng sở hữu
	public NganSach getNganSachByIdAndUserId(int budgetId, int userId) {
		NganSach nganSach = nganSachDAO.getById(budgetId);
		if (nganSach != null && nganSach.getId_nguoidung() == userId) {
			return nganSach;
		}
		return null;
	}

	// Lấy ngân sách theo tháng
	public NganSach getNganSachByMonth(int userId, int month, int year) {
		return nganSachDAO.getByMonth(userId, month, year);
	}

	// Xóa ngân sách
	public boolean deleteNganSach(int id) {
		return nganSachDAO.delete(id);
	}

	// Lấy danh sách icon
	public List<Icon> getAllIcon() {
		return iconDAO.getAll();
	}

	// Lấy danh sách màu sắc
	public List<MauSac> getAllMauSac() {
		return mauSacDAO.getAll();
	}

	// Lấy danh sách loại giao dịch
	public List<LoaiGiaoDich> getAllLoaiGiaoDich() {
		return loaiGiaoDichDAO.getAll();
	}

	// Lấy danh sách nhóm loại giao dịch
	public List<NhomLoaiGD> getALLNhomLoaiGD() {
		return nhomLoaiDAO.getAll();
	}

	// Thêm nhóm loại giao dịch
//	public boolean addNhomLoaiGD(NhomLoaiGD nlgd) {
//		return nhomLoaiDAO.add(nlgd);
//	}
//
//	// update nhóm loại giao dịch
//	public boolean updateNhomLoaiGD(NhomLoaiGD nlgd) {
//		return nhomLoaiDAO.update(nlgd);
//	}
//
//	// delete nhóm loại giao dịch
//	public boolean deleteNhomLoaiGD(int id) {
//		return nhomLoaiDAO.delete(id);
//	}

	// lấy tất cả ds chi tiêu hàng tháng
	public List<ChiTieuHangThang> getAllCTHangThang() {
		return chiTieuHangThangDAO.getAll();
	}

	// Lấy chi tiêu theo tháng của người dùng
	public List<ChiTieuHangThang> getByMonth(int userId, int month, int year) {
		return chiTieuHangThangDAO.getByMonth(userId, month, year);
	}

	// Lấy số tiền của người dùng trong tháng, tự động kế thừa nếu chưa có
	public BigDecimal getSoTien(int userId, int month, int year) {
		return chiTieuHangThangDAO.getOrInheritOrUpdate(userId, month, year, null, false);
	}

	// Cập nhật chi tiêu của tháng này (nếu có thì cập nhật, chưa có thì thêm mới)
	public boolean updateSoTien(int userId, int month, int year, BigDecimal newAmount) {
		BigDecimal result = chiTieuHangThangDAO.getOrInheritOrUpdate(userId, month, year, newAmount, true);
		return result != null;
	}

	// Kiểm tra email đã tồn tại cho người dùng khác (dùng khi cập nhật thông tin
	// người dùng)
	public boolean isEmailExistsExcludingId(String email, int userId) {
		// TODO: Thêm phương thức isEmailExistsExcludingId(String email, int userId) vào
		// NguoiDungDAO
		// Hiện tại tạm thời luôn trả về false
		System.err.println("TODO: Implement isEmailExistsExcludingId in NguoiDungDAO");
		return false;
	}

	// Kiểm tra trùng ngân sách cho tháng/năm của người dùng
	public boolean isNganSachExistForMonthYear(int userId, int month, int year) {
		// Phương thức getNganSachByMonth đã có sẵn trong DAO và Service
		return nganSachDAO.getByMonth(userId, month, year) != null;
	}

	// Kiểm tra trùng ngân sách cho tháng/năm của người dùng (loại trừ một ngân sách
	// cụ thể)
	public boolean isNganSachExistForMonthYearExcludingId(int userId, int month, int year, int budgetId) {
		// TODO: Thêm phương thức isNganSachExistForMonthYearExcludingId(int userId, int
		// month, int year, int budgetId) vào NganSachDAO
		// Hiện tại tạm thời luôn trả về false
		System.err.println("TODO: Implement isNganSachExistForMonthYearExcludingId in NganSachDAO");
		return false;
	}

	// Tính tổng thu nhập trong tháng
	public double tinhTongThuNhapBefore(int userId, int month, int year) {
		List<GiaoDich> giaoDichs = giaoDichDAO.getByMonth(userId, month, year);
		double tongThuNhap = 0;
		for (GiaoDich gd : giaoDichs) {
			if (gd.getId_loai().equals("1")) { // 1 là id của loại giao dịch thu nhập
				tongThuNhap += gd.getSo_tien();
			}
		}
		return tongThuNhap;
	}

	//Tổng thu nhập sau khi trừ chi phí hàng tháng
//	public double tinhTongThuNhapAfter(int userId, int month, int year) {
//	    // 1. Lấy danh sách giao dịch trong tháng
//	    List<GiaoDich> giaoDichs = giaoDichDAO.getByMonth(userId, month, year);
//	    double tongThuNhap = 0;
//	    boolean daCoThuNhap = false;
//
//	    for (GiaoDich gd : giaoDichs) {
//	        if (gd.getId_loai().equals("1")) { // 1 là ID loại giao dịch thu nhập
//	            tongThuNhap += gd.getSo_tien();
//	            daCoThuNhap = true;
//	        }
//	    }
//
//	    // 2. Lấy số tiền chi tiêu hàng tháng (tự kế thừa từ tháng trước nếu chưa có)
//	    BigDecimal chiTieuHangThang = chiTieuHangThangDAO.getOrInheritOrUpdate(userId, month, year, null, false);
//	    double chiPhi = chiTieuHangThang != null ? chiTieuHangThang.doubleValue() : 0;
//
//	    // 3. Nếu chưa có giao dịch thu nhập, mặc định tổng là âm chi phí
//	    if (!daCoThuNhap) {
//	        return -chiPhi;
//	    }
//
//	    // 4. Nếu có thu nhập, thì trừ chi tiêu hàng tháng ra
//	    return tongThuNhap - chiPhi;
//	}
//
//	
	
	
	
	
	
	
	
	
	// Tính tổng chi tiêu trong tháng
	public double tinhTongChiTieu(int userId, int month, int year) {
		List<GiaoDich> giaoDichs = giaoDichDAO.getByMonth(userId, month, year);
		double tongChiTieu = 0;
		for (GiaoDich gd : giaoDichs) {
			if (gd.getId_loai().equals("2")) { // 2 là id của loại giao dịch chi tiêu
				tongChiTieu += gd.getSo_tien();
			}
		}
		return tongChiTieu;
	}

	// Kiểm tra vượt ngân sách
	public boolean kiemTraVuotNganSach(int userId, int month, int year) {
		NganSach nganSach = nganSachDAO.getByMonth(userId, month, year);
		if (nganSach != null) {
			double tongChiTieu = tinhTongChiTieu(userId, month, year);
			return tongChiTieu > nganSach.getNgansach();
		}
		return false;
	}

	// Thêm Icon mới
	public boolean addIcon(Icon icon) {
		return iconDAO.add(icon);
	}

	// Cập nhật Icon
	public boolean updateIcon(Icon icon) {
		return iconDAO.update(icon);
	}

	// Xóa Icon
	public boolean deleteIcon(int id) {
		return iconDAO.delete(id);
	}

	// Thêm Màu sắc mới
	public boolean addMauSac(MauSac mauSac) {
		return mauSacDAO.add(mauSac);
	}

	// Cập nhật Màu sắc
	public boolean updateMauSac(MauSac mauSac) {
		return mauSacDAO.update(mauSac);
	}

	// Xóa Màu sắc
	public boolean deleteMauSac(int id) {
		return mauSacDAO.delete(id);
	}

	// Phương thức để lấy tất cả người dùng (dành cho admin)
	public List<NguoiDung> getAllUsers() {
		List<NguoiDung> users = nguoiDungDAO.getAll();
		// Đảm bảo không trả về mật khẩu
		for (NguoiDung user : users) {
			user.setMatkhau(null);
		}
		return users;
	}

	// Phương thức mới để admin thêm người dùng (có thể thiết lập role ban đầu)
	public boolean adminAddUser(NguoiDung nguoiDung) {
		// Kiểm tra email đã tồn tại chưa
		if (nguoiDungDAO.isEmailExists(nguoiDung.getEmail())) {
			return false; // Email đã tồn tại
		}

		// Mật khẩu cần được thiết lập trước khi gọi DAO (sẽ được hash trong DAO add
		// method)
		if (nguoiDung.getMatkhau() == null || nguoiDung.getMatkhau().isEmpty()) {
			// Có thể tạo mật khẩu mặc định hoặc yêu cầu mật khẩu khi admin thêm
			// Tạm thời yêu cầu mật khẩu phải được cung cấp trong request
			System.err.println("Password must be provided when admin adds user.");
			return false; // Hoặc throw exception
		}

		// Thêm vào DB (DAO sẽ băm mật khẩu)
		return nguoiDungDAO.add(nguoiDung);
	}

	// Phương thức mới để xóa người dùng (dành cho admin)
	public boolean deleteUser(int userId) {
		return nguoiDungDAO.delete(userId);
	}

	// Phương thức tìm kiếm người dùng theo tên (dành cho admin)
	public List<NguoiDung> searchUsersByName(String nameQuery) {
		List<NguoiDung> users = nguoiDungDAO.searchByName(nameQuery);
		// Đảm bảo không trả về mật khẩu
		for (NguoiDung user : users) {
			user.setMatkhau(null);
		}
		return users;
	}

	// Phương thức tìm kiếm danh mục theo tên cho người dùng hoặc admin
	public List<DanhMuc> searchCategoriesByName(String nameQuery, NguoiDung currentUser) {
		System.out.println("DEBUG: Service nhận nameQuery: " + nameQuery + ", user: "
				+ (currentUser != null ? currentUser.getEmail() : "null"));
		if ("admin".equals(currentUser.getRole())) {
			// Admin tìm kiếm tất cả danh mục
			return danhMucDAO.searchByNameAndUserId(nameQuery, null);
		} else {
			// Người dùng thường chỉ tìm kiếm danh mục của họ hoặc mặc định
			return danhMucDAO.searchByNameAndUserId(nameQuery, currentUser.getId_nguoidung());
		}
	}

	// Phương thức tìm kiếm icon theo tên
	public List<Icon> searchIconsByName(String nameQuery) {
		return iconDAO.searchByName(nameQuery);
	}

	// Phương thức tìm kiếm màu sắc theo tên
	public List<MauSac> searchColorsByName(String nameQuery) {
		System.out.println("DEBUG: Received nameQuery in Service: " + nameQuery);
		return mauSacDAO.searchByName(nameQuery);
	}

	// Lấy danh sách giao dịch của người dùng theo ID
	public List<GiaoDich> getAllGiaoDichByUserId(int userId) {
		return giaoDichDAO.getByUserId(userId);
	}

	public List<NhomLoaiGD> getNhomLoaiGDByUserId(int requestedUserId) {
		return nhomLoaiDAO.getByUserId(requestedUserId);
	}

	public List<ChiTieuHangThang> getAllCTHangThangByUserId(int userId) {
		return chiTieuHangThangDAO.getAllByUserId(userId);
	}

	// Lấy hoặc kế thừa/cập nhật ngân sách tháng
	public double getOrInheritOrUpdateNganSach(int userId, int thang, int nam, Double newAmount, boolean isUpdate) {
		return nganSachDAO.getOrInheritOrUpdate(userId, thang, nam, newAmount, isUpdate);
	}
	
	
	
	
	//======cac service moi====//
	public List<Buoi> getAllBuoi(){
		return buoiDAO.getAll();
	}
	public List<MucLuong> getAllMucLuong(){
		return mucLuongDAO.getAll();
	}
	public List<LoaiChiTieu> getAllLoaiChiTieu(){
		return loaiChiTieuDAO.getAll();
		
	}
	public List<GoiY>  getAllGoiY(){
		return goiyDAO.getAll();
	}
	public List<ChiTieuMau> getAllChiTieuMau(){
		return chiTieuMauDAO.getAll();
	}

	// Cập nhật tên chi tiêu mẫu
	public boolean updateChiTieuMauTen(int id, String tenMoi) {
		model.ChiTieuMau ctm = new model.ChiTieuMau();
		ctm.setId(id);
		ctm.setTen_chi_tieu_mau(tenMoi);
		return chiTieuMauDAO.update(ctm);
	}

	// Cập nhật tất cả tên chi tiêu mẫu thành tên mới
	public boolean updateAllTenChiTieuMau(String tenMoi) {
		return chiTieuMauDAO.updateAllTenChiTieuMau(tenMoi);
	}

	// Export các khoản chi tiêu từ giao_dich sang chitieu30ngay theo tháng
	// public List<model.ChiTieuMau> exportChiTieuMauFromGiaoDich(int thang) {
	// 	return chiTieuMauDAO.exportFromGiaoDich(thang);
	// }

	// Export các khoản chi tiêu từ giao_dich sang chitieu30ngay theo tháng (trả về danh sách chi tiết)
//	public List<model.ChiTieuMau> exportGiaoDichToChiTieu30Ngay(int thang) {
//		return chiTieuMauDAO.exportGiaoDichToChiTieu30Ngay(thang);
//	}

	public int processGiaoDichChiTieu(int thang, int idNguoiDung) {
		Connection conn = null;
		int processedCount = 0;
		try {
			// Get a single connection for the entire transaction
			conn = DBConnection.getConnection();
			if (conn == null) {
				throw new SQLException("Không thể kết nối tới cơ sở dữ liệu.");
			}
			// Disable auto-commit to start a transaction
			conn.setAutoCommit(false);

			// Instantiate DAOs with the shared connection
			GiaoDichDAO transGiaoDichDAO = new GiaoDichDAO(conn);
			DanhMucDAO transDanhMucDAO = new DanhMucDAO(conn);
			GoiYDAO transGoiYDAO = new GoiYDAO(conn);
			ChiTieuMauDAO transChiTieuMauDAO = new ChiTieuMauDAO(conn);

			// 1. Get the current year
			int nam = java.time.LocalDate.now().getYear();

			System.out.println("Gọi processGiaoDichChiTieu với thang=" + thang + ", idNguoiDung=" + idNguoiDung + ", nam=" + nam);

			// 2. Get all expense transactions for the given month, year, and user
			List<GiaoDich> giaoDichList = transGiaoDichDAO.getChiTieuByThangAndNguoiDung(idNguoiDung, thang, nam);

			System.out.println("Số lượng giao dịch chi tiêu lấy được: " + giaoDichList.size());

			// 3. Process each transaction
			for (GiaoDich gd : giaoDichList) {
				System.out.println("Đang xử lý giao dịch ID: " + gd.getId_GD());
				DanhMuc dm = transDanhMucDAO.getById(Integer.parseInt(gd.getId_danhmuc()));
				if (dm == null) {
					System.err.println("Không tìm thấy danh mục ID: " + gd.getId_danhmuc() + ". Bỏ qua giao dịch ID: " + gd.getId_GD());
					continue;
				}
				System.out.println("Tìm thấy danh mục: " + dm.getTen_danh_muc());
				int idLoaiChi = dm.getId_loai();

				GoiY newGoiY = new GoiY();
				newGoiY.setGoi_y(dm.getTen_danh_muc());
				newGoiY.setGia(gd.getSo_tien());
				newGoiY.setId_loai_chi(idLoaiChi);

				int newGoiYId = transGoiYDAO.addGoiYAndGetId(newGoiY);
				System.out.println("ID gợi ý mới: " + newGoiYId);
				if (newGoiYId == -1) {
					System.err.println("Không thể tạo bản ghi Gợi Ý cho giao dịch ID: " + gd.getId_GD());
					continue;
				}

				int ngayTrongThang = gd.getNgay().getDayOfMonth();
				String tenChiTieuMau = "Chi tiêu tháng " + thang;
				int idMucLuongDefault = 1;
				int idBuoiDefault = 5;

				System.out.println("Chuẩn bị insert chitieu30ngay cho giao dịch ID: " + gd.getId_GD());
				boolean success = transChiTieuMauDAO.addChiTieu30Ngay(
					idNguoiDung, idMucLuongDefault, idBuoiDefault, idLoaiChi, newGoiYId, tenChiTieuMau, ngayTrongThang
				);
				System.out.println("Kết quả insert: " + success);

				if (success) {
					processedCount++;
				} else {
					System.err.println("Không thể tạo bản ghi chitieu30ngay cho giao dịch ID: " + gd.getId_GD());
				}
			}

			// If all operations are successful, commit the transaction
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Lỗi SQL: " + e.getMessage());
			if (conn != null) {
				try {
					System.err.println("Lỗi xảy ra, giao dịch đang được rollback...");
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return -1; // Indicate error
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close(); // Return connection to the pool if pooling is used, otherwise close it.
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return processedCount;
	}
}