package service;

import dao.*;
import model.*;
import util.JwtUtil;
import java.util.List;
import java.sql.Date;
import java.time.LocalDate;

public class QuanLyChiTieuService {
    private NguoiDungDAO nguoiDungDAO;
    private DanhMucDAO danhMucDAO;
    private GiaoDichDAO giaoDichDAO;
    private NganSachDAO nganSachDAO;
    private IconDAO iconDAO;
    private MauSacDAO mauSacDAO;
    private LoaiGiaoDichDAO loaiGiaoDichDAO;
    
    public QuanLyChiTieuService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.danhMucDAO = new DanhMucDAO();
        this.giaoDichDAO = new GiaoDichDAO();
        this.nganSachDAO = new NganSachDAO();
        this.iconDAO = new IconDAO();
        this.mauSacDAO = new MauSacDAO();
        this.loaiGiaoDichDAO = new LoaiGiaoDichDAO();
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
        return nguoiDungDAO.add(nguoiDung);
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
    
    // Xác thực token
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
            io.jsonwebtoken.Claims claims = JwtUtil.validateToken(token);
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
    
    // Kiểm tra email đã tồn tại cho người dùng khác (dùng khi cập nhật thông tin người dùng)
    public boolean isEmailExistsExcludingId(String email, int userId) {
        // TODO: Thêm phương thức isEmailExistsExcludingId(String email, int userId) vào NguoiDungDAO
        // Hiện tại tạm thời luôn trả về false
        System.err.println("TODO: Implement isEmailExistsExcludingId in NguoiDungDAO");
        return false;
    }
    
    // Kiểm tra trùng ngân sách cho tháng/năm của người dùng
    public boolean isNganSachExistForMonthYear(int userId, int month, int year) {
        // Phương thức getNganSachByMonth đã có sẵn trong DAO và Service
        return nganSachDAO.getByMonth(userId, month, year) != null;
    }
    
    // Kiểm tra trùng ngân sách cho tháng/năm của người dùng (loại trừ một ngân sách cụ thể)
    public boolean isNganSachExistForMonthYearExcludingId(int userId, int month, int year, int budgetId) {
        // TODO: Thêm phương thức isNganSachExistForMonthYearExcludingId(int userId, int month, int year, int budgetId) vào NganSachDAO
        // Hiện tại tạm thời luôn trả về false
        System.err.println("TODO: Implement isNganSachExistForMonthYearExcludingId in NganSachDAO");
        return false;
    }
    
    // Tính tổng thu nhập trong tháng
    public double tinhTongThuNhap(int userId, int month, int year) {
        List<GiaoDich> giaoDichs = giaoDichDAO.getByMonth(userId, month, year);
        double tongThuNhap = 0;
        for(GiaoDich gd : giaoDichs) {
            if(gd.getId_loai().equals("1")) { // 1 là id của loại giao dịch thu nhập
                tongThuNhap += gd.getSo_tien();
            }
        }
        return tongThuNhap;
    }
    
    // Tính tổng chi tiêu trong tháng
    public double tinhTongChiTieu(int userId, int month, int year) {
        List<GiaoDich> giaoDichs = giaoDichDAO.getByMonth(userId, month, year);
        double tongChiTieu = 0;
        for(GiaoDich gd : giaoDichs) {
            if(gd.getId_loai().equals("2")) { // 2 là id của loại giao dịch chi tiêu
                tongChiTieu += gd.getSo_tien();
            }
        }
        return tongChiTieu;
    }
    
    // Kiểm tra vượt ngân sách
    public boolean kiemTraVuotNganSach(int userId, int month, int year) {
        NganSach nganSach = nganSachDAO.getByMonth(userId, month, year);
        if(nganSach != null) {
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

    // Phương thức mới để lấy tất cả người dùng (dành cho admin)
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

        // Mật khẩu cần được thiết lập trước khi gọi DAO (sẽ được hash trong DAO add method)
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
        System.out.println("DEBUG: Service nhận nameQuery: " + nameQuery + ", user: " + (currentUser != null ? currentUser.getEmail() : "null"));
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
} 