package dao;

import config.DBConnection;
import model.DanhMuc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO implements DAO<DanhMuc> {
    private Connection conn;
    
    public DanhMucDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<DanhMuc> getAll() {
        List<DanhMuc> list = new ArrayList<>();
        String sql = "SELECT * FROM danhmuc";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);
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
            if(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                return dm;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(DanhMuc dm) {
        String sql = "INSERT INTO danhmuc(id_nguoidung, id_mau, id_icon, id_loai, ten_danh_muc) VALUES(?,?,?,?,?)";
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
            ps.setString(5, dm.getTen_danh_muc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(DanhMuc dm) {
        String sql = "UPDATE danhmuc SET id_nguoidung=?, id_mau=?, id_icon=?, id_loai=?, ten_danh_muc=? WHERE id_danhmuc=?";
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
            ps.setString(5, dm.getTen_danh_muc());
            ps.setInt(6, dm.getId_danhmuc());
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
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setId_danhmuc(rs.getInt("id_danhmuc"));
                dm.setId_nguoidung(rs.getInt("id_nguoidung"));
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
                dm.setTen_danh_muc(rs.getString("ten_danh_muc"));
                list.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Phương thức mới để lấy tất cả danh mục mặc định (id_nguoidung IS NULL)
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
                if (idNguoiDungObj != null) {
                    dm.setId_nguoidung((Integer) idNguoiDungObj);
                } else {
                    dm.setId_nguoidung(null);
                }
                dm.setId_mau(rs.getInt("id_mau"));
                dm.setId_icon(rs.getInt("id_icon"));
                dm.setId_loai(rs.getInt("id_loai"));
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
        StringBuilder sql = new StringBuilder("SELECT * FROM danhmuc WHERE ten_danh_muc LIKE ?");

        if (userId != null) {
            // Tìm kiếm danh mục của người dùng cụ thể HOẶC danh mục mặc định
            sql.append(" AND (id_nguoidung = ? OR id_nguoidung IS NULL)");
        } // Nếu userId là null (admin), không thêm điều kiện về id_nguoidung

        try {
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            System.out.println("DEBUG: SQL LIKE: %" + nameQuery + "%");
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