package dao;

import config.DBConnection;
import model.NganSach;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NganSachDAO implements DAO<NganSach> {
    private Connection conn;
    
    public NganSachDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<NganSach> getAll() {
        List<NganSach> list = new ArrayList<>();
        String sql = "SELECT * FROM ngansach";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                NganSach ns = new NganSach();
                ns.setId_ngansach(rs.getInt("id_ngansach"));
                ns.setId_nguoidung(rs.getInt("id_nguoidung"));
                ns.setThang(rs.getInt("thang"));
                ns.setNam(rs.getInt("nam"));
                ns.setNgansach(rs.getDouble("ngansach"));
                list.add(ns);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public NganSach getById(int id) {
        String sql = "SELECT * FROM ngansach WHERE id_ngansach = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                NganSach ns = new NganSach();
                ns.setId_ngansach(rs.getInt("id_ngansach"));
                ns.setId_nguoidung(rs.getInt("id_nguoidung"));
                ns.setThang(rs.getInt("thang"));
                ns.setNam(rs.getInt("nam"));
                ns.setNgansach(rs.getDouble("ngansach"));
                return ns;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(NganSach ns) {
        String sql = "INSERT INTO ngansach(id_nguoidung, thang, nam, ngansach) VALUES(?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, ns.getId_nguoidung());
            ps.setInt(2, ns.getThang());
            ps.setInt(3, ns.getNam());
            ps.setDouble(4, ns.getNgansach());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(NganSach ns) {
        String sql = "UPDATE ngansach SET id_nguoidung=?, thang=?, nam=?, ngansach=? WHERE id_ngansach=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, ns.getId_nguoidung());
            ps.setInt(2, ns.getThang());
            ps.setInt(3, ns.getNam());
            ps.setDouble(4, ns.getNgansach());
            ps.setInt(5, ns.getId_ngansach());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM ngansach WHERE id_ngansach=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public NganSach getByMonth(int userId, int month, int year) {
        String sql = "SELECT * FROM ngansach WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                NganSach ns = new NganSach();
                ns.setId_ngansach(rs.getInt("id_ngansach"));
                ns.setId_nguoidung(rs.getInt("id_nguoidung"));
                ns.setThang(rs.getInt("thang"));
                ns.setNam(rs.getInt("nam"));
                ns.setNgansach(rs.getDouble("ngansach"));
                return ns;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy số tiền ngân sách của tháng cũ hoặc cập nhật lại tháng mới nhưng vẫn giữ dữ liệu tháng cũ
    public double getOrInheritOrUpdate(int userId, int thang, int nam, Double newAmount, boolean isUpdate) {
        double finalAmount = 0;
        // 1. Kiểm tra tháng hiện tại đã có chưa
        String checkSql = "SELECT ngansach FROM ngansach WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, thang);
            stmt.setInt(3, nam);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Đã có rồi
                finalAmount = rs.getDouble("ngansach");
                if (isUpdate && newAmount != null) {
                    // Cập nhật số tiền mới
                    String updateSql = "UPDATE ngansach SET ngansach = ? WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setDouble(1, newAmount);
                        updateStmt.setInt(2, userId);
                        updateStmt.setInt(3, thang);
                        updateStmt.setInt(4, nam);
                        updateStmt.executeUpdate();
                        finalAmount = newAmount;
                    }
                }
            } else {
                // Chưa có → tìm tháng trước gần nhất
                String findSql = "SELECT ngansach FROM ngansach "
                        + "WHERE id_nguoidung = ? AND (nam < ? OR (nam = ? AND thang < ?)) "
                        + "ORDER BY nam DESC, thang DESC LIMIT 1";
                try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                    findStmt.setInt(1, userId);
                    findStmt.setInt(2, nam);
                    findStmt.setInt(3, nam);
                    findStmt.setInt(4, thang);
                    ResultSet prevRs = findStmt.executeQuery();
                    if (prevRs.next()) {
                        finalAmount = prevRs.getDouble("ngansach");
                    } else {
                        finalAmount = 0;
                    }
                    // Chèn dòng mới
                    double insertAmount = (isUpdate && newAmount != null) ? newAmount : finalAmount;
                    String insertSql = "INSERT INTO ngansach (id_nguoidung, thang, nam, ngansach) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, thang);
                        insertStmt.setInt(3, nam);
                        insertStmt.setDouble(4, insertAmount);
                        insertStmt.executeUpdate();
                        finalAmount = insertAmount;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return finalAmount;
    }
} 