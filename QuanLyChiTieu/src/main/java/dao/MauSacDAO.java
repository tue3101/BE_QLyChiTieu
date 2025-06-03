package dao;

import config.DBConnection;
import model.MauSac;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MauSacDAO implements DAO<MauSac> {
    private Connection conn;
    
    public MauSacDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<MauSac> getAll() {
        List<MauSac> list = new ArrayList<>();
        String sql = "SELECT * FROM mausac";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                MauSac ms = new MauSac();
                ms.setId_mau(rs.getInt("id_mau"));
                ms.setMa_mau(rs.getString("ma_mau"));
                ms.setTen_mau(rs.getString("ten_mau"));
                list.add(ms);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public MauSac getById(int id) {
        String sql = "SELECT * FROM mausac WHERE id_mau = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                MauSac ms = new MauSac();
                ms.setId_mau(rs.getInt("id_mau"));
                ms.setMa_mau(rs.getString("ma_mau"));
                ms.setTen_mau(rs.getString("ten_mau"));
                return ms;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(MauSac ms) {
        String sql = "INSERT INTO mausac(ma_mau, ten_mau) VALUES(?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ms.getMa_mau());
            ps.setString(2, ms.getTen_mau());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(MauSac ms) {
        String sql = "UPDATE mausac SET ma_mau=?, ten_mau=? WHERE id_mau=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ms.getMa_mau());
            ps.setString(2, ms.getTen_mau());
            ps.setInt(3, ms.getId_mau());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM mausac WHERE id_mau=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 