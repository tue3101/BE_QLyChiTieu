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
} 