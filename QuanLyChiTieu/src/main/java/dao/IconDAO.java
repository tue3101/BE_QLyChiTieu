package dao;

import config.DBConnection;
import model.Icon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IconDAO implements DAO<Icon> {
    private Connection conn;
    
    public IconDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<Icon> getAll() {
        List<Icon> list = new ArrayList<>();
        String sql = "SELECT * FROM icon";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Icon icon = new Icon();
                icon.setId_icon(rs.getInt("id_icon"));
                icon.setTen_icon(rs.getString("ten_icon"));
                list.add(icon);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public Icon getById(int id) {
        String sql = "SELECT * FROM icon WHERE id_icon = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                Icon icon = new Icon();
                icon.setId_icon(rs.getInt("id_icon"));
                icon.setTen_icon(rs.getString("ten_icon"));
                return icon;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(Icon icon) {
        String sql = "INSERT INTO icon(ten_icon) VALUES(?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, icon.getTen_icon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(Icon icon) {
        String sql = "UPDATE icon SET ten_icon=? WHERE id_icon=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, icon.getTen_icon());
            ps.setInt(2, icon.getId_icon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM icon WHERE id_icon=?";
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