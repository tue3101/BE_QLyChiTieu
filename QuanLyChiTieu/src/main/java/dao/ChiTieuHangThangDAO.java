package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.ChiTieuHangThang;
import model.GiaoDich;

public class ChiTieuHangThangDAO implements DAO<ChiTieuHangThang> {
	private Connection conn;

	public ChiTieuHangThangDAO() {
		this.conn = DBConnection.getConnection();
	}

	@Override
	public List<ChiTieuHangThang> getAll() {
		List<ChiTieuHangThang> list = new ArrayList<>();
		String sql = "SELECT * FROM chi_tieu_hang_thang";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ChiTieuHangThang ctht = new ChiTieuHangThang();
				ctht.setId(rs.getInt("id"));
				ctht.setId_nguoidung(rs.getInt("id_nguoidung"));
				ctht.setId_tennhom(rs.getInt("id_tennhom"));
				ctht.setSo_tien(rs.getDouble("so_tien"));
				ctht.setThang(rs.getInt("thang"));
				ctht.setNam(rs.getInt("nam"));
				list.add(ctht);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// lấy theo tháng
	public List<ChiTieuHangThang> getByMonth(int userId, int month, int year) {
		List<ChiTieuHangThang> list = new ArrayList<>();
		String sql = "SELECT * FROM chi_tieu_hang_thang WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setInt(2, month);
			ps.setInt(3, year);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ChiTieuHangThang ctht = new ChiTieuHangThang();
				ctht.setId(rs.getInt("id"));
				ctht.setId_nguoidung(rs.getInt("id_nguoidung"));
				ctht.setId_tennhom(rs.getInt("id_tennhom"));
				ctht.setSo_tien(rs.getDouble("so_tien"));
				ctht.setThang(rs.getInt("thang"));
				ctht.setNam(rs.getInt("nam"));
				list.add(ctht);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

//	public BigDecimal getOrInheritOrUpdate(int userId, int month, int year, Object newAmount, boolean isUpdate) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	// lấy số tiền của tháng cũ hoặc cập nhật lại tháng mới nhưng vẫn giữ lại được
	// dữ liệu tháng cũ
	public BigDecimal getOrInheritOrUpdate(int userId, int thang, int nam, BigDecimal newAmount, boolean isUpdate) {
		BigDecimal finalAmount = null;

		// 1. Kiểm tra tháng hiện tại đã có chưa
		String checkSql = "SELECT so_tien FROM chi_tieu_hang_thang WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
		try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, thang);
			stmt.setInt(3, nam);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// Đã có rồi
				finalAmount = rs.getBigDecimal("so_tien");
				if (isUpdate && newAmount != null) {
					// Cập nhật số tiền mới
					String updateSql = "UPDATE chi_tieu_hang_thang SET so_tien = ? WHERE id_nguoidung = ? AND thang = ? AND nam = ?";
					try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
						updateStmt.setBigDecimal(1, newAmount);
						updateStmt.setInt(2, userId);
						updateStmt.setInt(3, thang);
						updateStmt.setInt(4, nam);
						updateStmt.executeUpdate();
						finalAmount = newAmount;
					}
				}
			} else {
				// Chưa có → tìm tháng trước gần nhất
				String findSql = "SELECT so_tien FROM chi_tieu_hang_thang "
						+ "WHERE id_nguoidung = ? AND (nam < ? OR (nam = ? AND thang < ?)) "
						+ "ORDER BY nam DESC, thang DESC LIMIT 1";
				try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
					findStmt.setInt(1, userId);
					findStmt.setInt(2, nam);
					findStmt.setInt(3, nam);
					findStmt.setInt(4, thang);
					ResultSet prevRs = findStmt.executeQuery();
					if (prevRs.next()) {
						finalAmount = prevRs.getBigDecimal("so_tien");
					} else {
						finalAmount = BigDecimal.ZERO;
					}

					// Chèn dòng mới
					BigDecimal insertAmount = isUpdate && newAmount != null ? newAmount : finalAmount;
					String insertSql = "INSERT INTO chi_tieu_hang_thang (id_nguoidung, so_tien, thang, nam, id_tennhom) "
							+ "VALUES (?, ?, ?, ?, 1)";
					try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
						insertStmt.setInt(1, userId);
						insertStmt.setBigDecimal(2, insertAmount);
						insertStmt.setInt(3, thang);
						insertStmt.setInt(4, nam);
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

	@Override
	public ChiTieuHangThang getById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(ChiTieuHangThang ctht) {
		String sql = "INSERT INTO chi_tieu_hang_thang (id_nguoidung, id_tennhom, so_tien, thang, nam) VALUES (?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, ctht.getId_nguoidung());
			ps.setInt(2, ctht.getId_tennhom());
			ps.setDouble(3, ctht.getSo_tien());
			ps.setInt(4, ctht.getThang());
			ps.setInt(5, ctht.getNam());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean update(ChiTieuHangThang ctht) {
//		 String sql = "UPDATE chi_tieu_hang_thang SET id_nguoidung=?, id_tennhom=?, so_tien=?, thang=?, nam=?  WHERE id_GD=?";
//	        try {
//	            PreparedStatement ps = conn.prepareStatement(sql);
//	            ps.setInt(1, ctht.getId_nguoidung());
//	            ps.setInt(2, ctht.getId_tennhom());
//	            ps.setDouble(3, ctht.getSo_tien());
//	            ps.setInt(4, ctht.getThang());
//	            ps.setInt(5, ctht.getNam());
//	            ps.setInt(6, ctht.getId());
//	            return ps.executeUpdate() > 0;
//	        } catch (SQLException e) {
//	            e.printStackTrace();
//	        }
		return false;
	}

	@Override
	public boolean delete(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<ChiTieuHangThang> getAllByUserId(int userId) {
		List<ChiTieuHangThang> list = new ArrayList<>();
		String sql = "SELECT * FROM chi_tieu_hang_thang WHERE id_nguoidung = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ChiTieuHangThang ctht = new ChiTieuHangThang();
				ctht.setId(rs.getInt("id"));
				ctht.setId_nguoidung(rs.getInt("id_nguoidung"));
				ctht.setId_tennhom(rs.getInt("id_tennhom"));
				ctht.setSo_tien(rs.getDouble("so_tien"));
				ctht.setThang(rs.getInt("thang"));
				ctht.setNam(rs.getInt("nam"));
				list.add(ctht);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

}
