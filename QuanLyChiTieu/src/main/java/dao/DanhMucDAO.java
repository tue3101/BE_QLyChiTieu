package dao;

import config.DBConnection;
import model.DanhMuc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO implements DAO<DanhMuc> {
	//khai báo biến instance 
	//Connection là lớp trong thư viện JDBC (để kết nối jva với Database
    private Connection conn;
    //khởi tạo DanhMucDAO kết nối CSDL
    public DanhMucDAO() {
    	//gọi phương thức getC từ lớp DBC để kết nối database
        this.conn = DBConnection.getConnection();
    }
    
    @Override //ghi đè phương thức getAll
    public List<DanhMuc> getAll() { //trả về list chứa các đối tượng DM
        List<DanhMuc> list = new ArrayList<>(); //tạo ds rỗng chứa các đối tượng từ CSDL
        String sql = "SELECT * FROM danhmuc";
        try {
        	//PreparedStatement dùng để chống mã độc và dữ liệu xấu khi làm việc với CSDL
        	//preparedStatement(sql) java sẽ biên dịch câu truy vấn một lần 
            PreparedStatement ps = conn.prepareStatement(sql);
             
            //executeQuery dùng để thực thi câu truy vấn SELECT
            //Resultset là đối tượng chứa kết quả
            ResultSet rs = ps.executeQuery();
            //next() dùng di chuyển con trỏ xuống dòng kế tiếp trong bảng
            //trở xuống từng dòng lấy giá trị rồi thêm vào ds đối tượng dm , lặp cho tới khi hết dòng dữ liệu trong bảng
            while(rs.next()) {
            	//tạo đối tượng DanhMuc rỗng  
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setId_tennhom(rs.getInt("id_tennhom"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);//dùng biến list để add dữ liệu từ đối tượng danh mục vào ds
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public DanhMuc getById(int id) {
        String sql = "SELECT * FROM danhmuc WHERE id_danhmuc = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) { //kiểm tra có dữ liệu trả về ko
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setId_tennhom(rs.getInt("id_tennhom"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                return dm; //có thì trả về đối tượng dm 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; //ko có thì trả về null
    }
    
    @Override
    public boolean add(DanhMuc dm) {
        String sql = "INSERT INTO danhmuc(id_nguoidung, id_mau, id_icon, id_loai, id_tennhom, ten_danh_muc) VALUES(?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            // Check if id_nguoidung is null
            if (dm.getId_nguoidung() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);//gán null cho tham số thứ 1 kiểu integer
            } else {
                ps.setInt(1, dm.getId_nguoidung());
            }
            ps.setInt(2, dm.getId_mau());
            ps.setInt(3, dm.getId_icon());
            ps.setInt(4, dm.getId_loai());
            ps.setInt(5, dm.getId_tennhom());
            ps.setString(6, dm.getTen_danh_muc());
            //thực thi lênh truy vấn và trả về số dòng bị ảnh hưởng 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;//nếu ko có dòng nào thay đổi
    }
    
    @Override
    public boolean update(DanhMuc dm) {
        String sql = "UPDATE danhmuc SET id_nguoidung=?, id_mau=?, id_icon=?, id_loai=?, id_tennhom=?, ten_danh_muc=? WHERE id_danhmuc=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            // Check if id_nguoidung is null
            if (dm.getId_nguoidung() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, dm.getId_nguoidung());
            }
            ps.setInt(2, dm.getId_mau());
            ps.setInt(3, dm.getId_icon());
            ps.setInt(4, dm.getId_loai());
            ps.setInt(5, dm.getId_tennhom());
            ps.setString(6, dm.getTen_danh_muc());
            ps.setInt(7, dm.getId_danhmuc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM danhmuc WHERE id_danhmuc=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<DanhMuc> getByUserId(int userId) {
        List<DanhMuc> list = new ArrayList<>();
        String sql = "SELECT * FROM danhmuc WHERE id_nguoidung = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId); //truyền id người dùng vào tham số dầu tiên
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setId_tennhom(rs.getInt("id_tennhom"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Phương thức để lấy tất cả danh mục mặc định (id_nguoidung IS NULL)
    public List<DanhMuc> getAllDefaultCategories() {
        List<DanhMuc> list = new ArrayList<>();
        String sql = "SELECT * FROM danhmuc WHERE id_nguoidung IS NULL";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                // Sử dụng getObject để lấy giá trị có thể là null
                Object idNguoiDungObj = rs.getObject("id_nguoidung");
                if (idNguoiDungObj != null) { //đoạn này dư vì đã có câu truy vấn lọc điều kiện 
                    dm.setId_nguoidung((Integer) idNguoiDungObj);
                } else {
                    dm.setId_nguoidung(null);
                }
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setId_tennhom(rs.getInt("id_tennhom"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Phương thức tìm kiếm danh mục theo tên và ID người dùng 
    public List<DanhMuc> searchByNameAndUserId(String nameQuery, Integer userId) {
        System.out.println("DEBUG: DAO nhận nameQuery: " + nameQuery + ", userId: " + userId);
        List<DanhMuc> list = new ArrayList<>();
        //tìm tên danh mục giống với từ khóa ?
        StringBuilder sql = new StringBuilder("SELECT * FROM danhmuc WHERE ten_danh_muc LIKE ?");

        if (userId != null) {
        	// sql.append() nối thêm điều kiện vào SQL ban đầu
            // Tìm kiếm danh mục của người dùng cụ thể HOẶC danh mục mặc định
            sql.append(" AND (id_nguoidung = ? OR id_nguoidung IS NULL)");
        } // Nếu userId là null (admin), không thêm điều kiện về id_nguoidung

        try {
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            System.out.println("DEBUG: SQL LIKE: %" + nameQuery + "%");
           //Thêm dấu % trước và sau từ khóa để tìm bất kỳ vị trí nào chứa chuỗi đó
            ps.setString(1, "%" + nameQuery + "%"); // Tìm kiếm chuỗi con

            if (userId != null) {
                ps.setInt(2, userId);
            }

            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                // Sử dụng getObject để lấy giá trị có thể là null
                Object idNguoiDungObj = rs.getObject("id_nguoidung");
                if (idNguoiDungObj != null) {
                    dm.setId_nguoidung((Integer) idNguoiDungObj);
                } else {
                    dm.setId_nguoidung(null);
                }
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setId_tennhom(rs.getInt("id_tennhom"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);
            }
            System.out.println("DEBUG: Số lượng danh mục tìm được: " + list.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 