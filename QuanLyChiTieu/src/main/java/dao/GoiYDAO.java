package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.GoiY;

public class GoiYDAO implements DAO<GoiY> {

	private Connection conn;

	public GoiYDAO() {
		this.conn = DBConnection.getConnection();
	}

	public GoiYDAO(Connection conn) {
		this.conn = conn;
	}

	@Override
	public List<GoiY> getAll() {
		List<GoiY> list = new ArrayList<>();
		String sql = "SELECT * FROM goiy";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				GoiY gy = new GoiY();
				gy.setId(rs.getInt("id"));
				gy.setGoi_y(rs.getString("goi_y"));
				gy.setGia(rs.getDouble("gia"));
				gy.setId_loai_chi(rs.getInt("id_loai_chi"));
				list.add(gy);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public GoiY getById(int id) {
		String sql = "SELECT * FROM goiy WHERE id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				GoiY gy = new GoiY();
				gy.setId(rs.getInt("id"));
				gy.setGoi_y(rs.getString("goi_y"));
				gy.setGia(rs.getDouble("gia"));
				gy.setId_loai_chi(rs.getInt("id_loai_chi"));
				return gy;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean add(GoiY t) {
		return addGoiYAndGetId(t) > 0;
	}
	
	public int addGoiYAndGetId(GoiY gy) {
	    String sql = "INSERT INTO goiy(goi_y, gia, id_loai_chi) VALUES(?, ?, ?)";
	    int generatedId = -1;
	    try {
	        PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, gy.getGoi_y());
	        ps.setDouble(2, gy.getGia());
	        ps.setInt(3, gy.getId_loai_chi());
	        
	        int affectedRows = ps.executeUpdate();

	        if (affectedRows > 0) {
	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) {
	                    generatedId = rs.getInt(1);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return generatedId;
	}

	@Override
	public boolean update(GoiY t) {
		String sql = "UPDATE goiy SET goi_y = ?, gia = ?, id_loai_chi = ? WHERE id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, t.getGoi_y());
			ps.setDouble(2, t.getGia());
			ps.setInt(3, t.getId_loai_chi());
			ps.setInt(4, t.getId());
			int rows = ps.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete(int id) {
		String sql = "DELETE FROM goiy WHERE id = ?";
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
