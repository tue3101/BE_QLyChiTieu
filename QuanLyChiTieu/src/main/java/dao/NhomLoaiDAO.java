package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.NhomLoaiGD;

public class NhomLoaiDAO implements DAO<NhomLoaiGD> {

	private Connection conn;

	public NhomLoaiDAO() {
		this.conn = DBConnection.getConnection();
	}

	@Override
	public List<NhomLoaiGD> getAll() {
		List<NhomLoaiGD> list = new ArrayList<>();
		String sql = "SELECT * FROM nhom_loai_giao_dich";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				NhomLoaiGD nlgd = new NhomLoaiGD();
				nlgd.setId_tennhom(rs.getInt("id_tennhom"));
				nlgd.setId_loai(rs.getInt("id_loai"));
				nlgd.setTennhom(rs.getString("ten_nhom"));
				list.add(nlgd);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public NhomLoaiGD getById(int id) {
		String sql = "SELECT * FROM nhom_loai_giao_dich WHERE id_tennhom = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				NhomLoaiGD nlgd = new NhomLoaiGD();
				nlgd.setId_tennhom(rs.getInt("id_tennhom"));
				nlgd.setId_loai(rs.getInt("id_loai"));
				nlgd.setTennhom(rs.getString("ten_nhom"));
				return nlgd;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean add(NhomLoaiGD nlgd) {
		String sql = "INSERT INTO nhom_loai_giao_dich(ten_nhom) VALUES(?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, nlgd.getTennhom());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(NhomLoaiGD nlgd) {
		String sql = "UPDATE nhom_loai_giao_dich SET id_loai =?, ten_nhom=? WHERE id_tennhom=?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, nlgd.getId_loai());
			ps.setString(2, nlgd.getTennhom());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean delete(int id) {
		String sql = "DELETE FROM nhom_loai_giao_dich WHERE id_tennhom=?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<NhomLoaiGD> getByUserId(int userId) {
		List<NhomLoaiGD> list = new ArrayList<>();
		String sql = "SELECT * FROM nhom_loai_giao_dich WHERE id_nguoidung = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				NhomLoaiGD nlgd = new NhomLoaiGD();
				nlgd.setId_tennhom(rs.getInt("id_tennhom"));
				nlgd.setId_loai(rs.getInt("id_loai"));
				nlgd.setTennhom(rs.getString("ten_nhom"));
				list.add(nlgd);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	
}
