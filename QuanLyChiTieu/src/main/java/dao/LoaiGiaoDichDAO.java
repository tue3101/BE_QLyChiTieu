package dao;

import config.DBConnection;
import model.LoaiGiaoDich;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiGiaoDichDAO implements DAO<LoaiGiaoDich> {
    private Connection conn;
    
    public LoaiGiaoDichDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<LoaiGiaoDich> getAll() {
        List<LoaiGiaoDich> list = new ArrayList<>();
        String sql = "SELECT * FROM loai_giao_dich";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                LoaiGiaoDich lgd = new LoaiGiaoDich();
                lgd.setId_loai(rs.getInt("id_loai"));
                lgd.setTen_loai(rs.getString("ten_loai"));
                list.add(lgd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public LoaiGiaoDich getById(int id) {
        String sql = "SELECT * FROM loai_giao_dich WHERE id_loai = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                LoaiGiaoDich lgd = new LoaiGiaoDich();
                lgd.setId_loai(rs.getInt("id_loai"));
                lgd.setTen_loai(rs.getString("ten_loai"));
                return lgd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(LoaiGiaoDich lgd) {
        String sql = "INSERT INTO loai_giao_dich(ten_loai) VALUES(?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, lgd.getTen_loai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(LoaiGiaoDich lgd) {
        String sql = "UPDATE loai_giao_dich SET ten_loai=? WHERE id_loai=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, lgd.getTen_loai());
            ps.setInt(2, lgd.getId_loai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM loai_giao_dich WHERE id_loai=?";
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