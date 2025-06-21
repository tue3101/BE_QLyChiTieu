package dao;

import config.DBConnection;
import model.GiaoDich;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.sql.Date;

public class GiaoDichDAO implements DAO<GiaoDich> {
    private Connection conn;
    
    public GiaoDichDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<GiaoDich> getAll() {
        List<GiaoDich> list = new ArrayList<>();
        String sql = "SELECT * FROM giaodich";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setId_GD(String.valueOf(rs.getInt("id_GD")));
                gd.setId_danhmuc(String.valueOf(rs.getInt("id_danhmuc")));
                gd.setId_nguoidung(String.valueOf(rs.getInt("id_nguoidung")));
                gd.setId_loai(String.valueOf(rs.getInt("id_loai")));
                gd.setId_tennhom(rs.getString("id_tennhom"));
                gd.setSo_tien(rs.getDouble("so_tien"));
                Date sqlDate = rs.getDate("ngay");
                if (sqlDate != null) {
                	//java.sql.Date = 2024-06-01 còn toLocalDate() sẽ cho ra LocalDate.of(2024, 6, 1)
                    gd.setNgay(sqlDate.toLocalDate());
                }
                gd.setThang(rs.getInt("thang"));
                gd.setNam(rs.getInt("nam"));
                gd.setGhi_chu(rs.getString("ghi_chu"));
                list.add(gd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public GiaoDich getById(int id) {
        String sql = "SELECT * FROM giaodich WHERE id_GD = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setId_GD(String.valueOf(rs.getInt("id_GD")));
                gd.setId_danhmuc(String.valueOf(rs.getInt("id_danhmuc")));
                gd.setId_nguoidung(String.valueOf(rs.getInt("id_nguoidung")));
                gd.setId_loai(String.valueOf(rs.getInt("id_loai")));
                gd.setId_tennhom(rs.getString("id_tennhom"));
                gd.setSo_tien(rs.getDouble("so_tien"));
                Date sqlDate = rs.getDate("ngay");
                if (sqlDate != null) {
                    gd.setNgay(sqlDate.toLocalDate());
                }
                gd.setThang(rs.getInt("thang"));
                gd.setNam(rs.getInt("nam"));
                gd.setGhi_chu(rs.getString("ghi_chu"));
                return gd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(GiaoDich gd) {
        String sql = "INSERT INTO giaodich(id_danhmuc, id_nguoidung, id_loai, id_tennhom, so_tien, ngay, thang, nam, ghi_chu) VALUES(?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(gd.getId_danhmuc()));
            ps.setInt(2, Integer.parseInt(gd.getId_nguoidung()));
            ps.setInt(3, Integer.parseInt(gd.getId_loai()));
            ps.setString(4, gd.getId_tennhom());
            ps.setDouble(5, gd.getSo_tien());
            if (gd.getNgay() != null) {
            	//Chuyển từ LocalDate sang java.sql.Date
                ps.setDate(6, java.sql.Date.valueOf(gd.getNgay()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, gd.getThang());
            ps.setInt(8, gd.getNam());
            ps.setString(9, gd.getGhi_chu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(GiaoDich gd) {
        String sql = "UPDATE giaodich SET id_danhmuc=?, id_nguoidung=?, id_loai=?, id_tennhom=?, so_tien=?, ngay=?, thang=?, nam=?, ghi_chu=? WHERE id_GD=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(gd.getId_danhmuc()));
            ps.setInt(2, Integer.parseInt(gd.getId_nguoidung()));
            ps.setInt(3, Integer.parseInt(gd.getId_loai()));
            ps.setString(4, gd.getId_tennhom());
            ps.setDouble(5, gd.getSo_tien());
            if (gd.getNgay() != null) {
                ps.setDate(6, java.sql.Date.valueOf(gd.getNgay()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, gd.getThang());
            ps.setInt(8, gd.getNam());
            ps.setString(9, gd.getGhi_chu());
            ps.setInt(10, Integer.parseInt(gd.getId_GD()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM giaodich WHERE id_GD=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<GiaoDich> getByUserId(int userId) {
        List<GiaoDich> list = new ArrayList<>();
        String sql = "SELECT * FROM giaodich WHERE id_nguoidung = ? ORDER BY ngay DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setId_GD(String.valueOf(rs.getInt("id_GD")));
                gd.setId_danhmuc(String.valueOf(rs.getInt("id_danhmuc")));
                gd.setId_nguoidung(String.valueOf(rs.getInt("id_nguoidung")));
                gd.setId_loai(String.valueOf(rs.getInt("id_loai")));
                gd.setId_tennhom(rs.getString("id_tennhom"));
                gd.setSo_tien(rs.getDouble("so_tien"));
                Date sqlDate = rs.getDate("ngay");
                if (sqlDate != null) {
                    gd.setNgay(sqlDate.toLocalDate());
                }
                gd.setThang(rs.getInt("thang"));
                gd.setNam(rs.getInt("nam"));
                gd.setGhi_chu(rs.getString("ghi_chu"));
                list.add(gd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<GiaoDich> getByMonth(int userId, int month, int year) {
        List<GiaoDich> list = new ArrayList<>();
        String sql = "SELECT * FROM giaodich WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setId_GD(String.valueOf(rs.getInt("id_GD")));
                gd.setId_danhmuc(String.valueOf(rs.getInt("id_danhmuc")));
                gd.setId_nguoidung(String.valueOf(rs.getInt("id_nguoidung")));
                gd.setId_loai(String.valueOf(rs.getInt("id_loai")));
                gd.setId_tennhom(rs.getString("id_tennhom"));
                gd.setSo_tien(rs.getDouble("so_tien"));
                Date sqlDate = rs.getDate("ngay");
                if (sqlDate != null) {
                    gd.setNgay(sqlDate.toLocalDate());
                }
                gd.setThang(rs.getInt("thang"));
                gd.setNam(rs.getInt("nam"));
                gd.setGhi_chu(rs.getString("ghi_chu"));
                list.add(gd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Tìm kiếm giao dịch theo từ khóa trong ghi chú của người dùng
    public List<GiaoDich> searchByKeyword(int userId, String keyword) {
        List<GiaoDich> list = new ArrayList<>();
        String sql = "SELECT * FROM giaodich WHERE id_nguoidung = ? AND ghi_chu LIKE ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, "%" + keyword + "%"); // Tìm kiếm từ khóa xuất hiện ở bất kỳ đâu trong ghi_chu
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setId_GD(String.valueOf(rs.getInt("id_GD")));
                gd.setId_danhmuc(String.valueOf(rs.getInt("id_danhmuc")));
                gd.setId_nguoidung(String.valueOf(rs.getInt("id_nguoidung")));
                gd.setId_loai(String.valueOf(rs.getInt("id_loai")));
                gd.setId_tennhom(rs.getString("id_tennhom"));
                gd.setSo_tien(rs.getDouble("so_tien"));
                Date sqlDate = rs.getDate("ngay");
                if (sqlDate != null) {
                    gd.setNgay(sqlDate.toLocalDate());
                }
                gd.setThang(rs.getInt("thang"));
                gd.setNam(rs.getInt("nam"));
                gd.setGhi_chu(rs.getString("ghi_chu"));
                list.add(gd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 