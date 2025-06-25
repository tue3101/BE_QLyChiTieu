package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import config.DBConnection;
import model.ChiTieuMau;

public class ChiTieuMauDAO implements DAO<ChiTieuMau>  {
	   private Connection conn;
	    
	    public ChiTieuMauDAO() {
	        this.conn = DBConnection.getConnection();
	        try {
				if (conn == null || conn.isClosed()) {
					System.err.println("Lỗi kết nối DB trong ChiTieuMauDAO!");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    
	    public ChiTieuMauDAO(Connection conn) {
	        this.conn = conn;
	    }

		@Override
		public List<ChiTieuMau> getAll() {
			List<ChiTieuMau> list = new ArrayList<>();
			String sql = "SELECT * FROM chitieu30ngay";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ChiTieuMau ctm = new ChiTieuMau();
					ctm.setId(rs.getInt("id"));
					ctm.setNgay(rs.getInt("ngay"));
					ctm.setId_muc_luong(rs.getInt("id_muc_luong"));
					ctm.setId_buoi(rs.getInt("id_buoi"));
					ctm.setId_loai_chi(rs.getInt("id_loai_chi"));
					ctm.setId_goi_y(rs.getInt("id_goi_y"));
					ctm.setTen_chi_tieu_mau(rs.getString("ten_chi_tieu_mau"));
					list.add(ctm);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		@Override
		public ChiTieuMau getById(int id) {
			String sql = "SELECT * FROM chitieu30ngay WHERE id = ?";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					ChiTieuMau ctm = new ChiTieuMau();
					ctm.setId(rs.getInt("id"));
					ctm.setNgay(rs.getInt("ngay"));
					ctm.setId_muc_luong(rs.getInt("id_muc_luong"));
					ctm.setId_buoi(rs.getInt("id_buoi"));
					ctm.setId_loai_chi(rs.getInt("id_loai_chi"));
					ctm.setId_goi_y(rs.getInt("id_goi_y"));
					ctm.setTen_chi_tieu_mau(rs.getString("ten_chi_tieu_mau"));
					ctm.setId_nguoidung(rs.getInt("id_nguoidung"));
					return ctm;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public boolean add(ChiTieuMau t) {
			// This implementation depends on the full object. 
			// The new method is more specific for the export logic.
			return addChiTieu30Ngay(t.getId_nguoidung(), t.getId_muc_luong(), t.getId_buoi(), t.getId_loai_chi(), t.getId_goi_y(), t.getTen_chi_tieu_mau(), t.getNgay());
		}
		
		public boolean addChiTieu30Ngay(int idNguoiDung, int idMucLuong, int idBuoi, int idLoaiChi, int idGoiY, String tenChiTieuMau, int ngay) {
			String sql = "INSERT INTO chitieu30ngay(id_nguoidung, id_muc_luong, id_buoi, id_loai_chi, id_goi_y, ten_chi_tieu_mau, ngay) VALUES(?, ?, ?, ?, ?, ?, ?)";
			try {
				System.out.println("Insert chitieu30ngay: " + idNguoiDung + ", " + idMucLuong + ", " + idBuoi + ", " + idLoaiChi + ", " + idGoiY + ", " + tenChiTieuMau + ", " + ngay);
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, idNguoiDung);
				ps.setInt(2, idMucLuong);
				ps.setInt(3, idBuoi);
				ps.setInt(4, idLoaiChi);
				ps.setInt(5, idGoiY);
				ps.setString(6, tenChiTieuMau);
				ps.setInt(7, ngay);
				boolean result = ps.executeUpdate() > 0;
				System.out.println("Insert result: " + result);
				return result;
			} catch (SQLException e) {
				System.err.println("Lỗi SQL khi insert chitieu30ngay: " + e.getMessage());
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public boolean update(ChiTieuMau t) {
			String sql = "UPDATE chitieu30ngay SET ten_chi_tieu_mau = ? WHERE id = ?";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, t.getTen_chi_tieu_mau());
				ps.setInt(2, t.getId());
				int rows = ps.executeUpdate();
				return rows > 0;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public boolean delete(int id) {
			String sql = "DELETE FROM chitieu30ngay WHERE id = ?";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, id);
				return ps.executeUpdate() > 0;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		// Cập nhật tất cả các bản ghi thành tên chi tiêu mẫu mới
		public boolean updateAllTenChiTieuMau(String tenMoi) {
			String sql = "UPDATE chitieu30ngay SET ten_chi_tieu_mau = ?";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, tenMoi);
				int rows = ps.executeUpdate();
				return rows > 0;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}

		// Cập nhật tên chi tiêu mẫu cho một người dùng cụ thể
		public boolean updateTenChiTieuMauByUserId(String tenMoi, int userId) {
			String sql = "UPDATE chitieu30ngay SET ten_chi_tieu_mau = ? WHERE id_nguoidung = ?";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, tenMoi);
				ps.setInt(2, userId);
				// executeUpdate() trả về số dòng bị ảnh hưởng, 
				// nếu > 0 tức là có ít nhất 1 dòng đã được cập nhật
				return ps.executeUpdate() > 0;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}

		// Export các khoản chi tiêu từ giao_dich sang chitieu30ngay theo tháng, lấy tên chi tiêu mẫu từ tên danh mục, số tiền từ giao dịch
		// public List<ChiTieuMau> exportFromGiaoDich(int thang) {
		// 	System.out.println("[LOG TEST] Vào ChiTieuMauDAO.exportFromGiaoDich");
		// 	System.out.println("Tháng truyền vào DAO: " + thang);
		// 	System.out.println("==> Đã vào Service.exportChiTieuMauFromGiaoDich");
		// 	System.out.println("==> Đã vào DAO.exportFromGiaoDich");
		// 	System.out.println("Tháng truyền vào DAO: " + thang);
		// 	List<ChiTieuMau> exported = new ArrayList<>();
		// 	String sql = "SELECT g.id_nguoidung, g.id_loai AS id_loai_chi, d.ten_danh_muc, g.so_tien, g.ngay " +
		// 		"FROM giaodich g JOIN danhmuc d ON g.id_danhmuc = d.id_danhmuc " +
		// 		"WHERE g.id_loai = 2 AND MONTH(g.ngay) = ?";
		// 	System.out.println("SQL Query: " + sql + " | Tham số: " + thang);
		// 	try {
		// 		PreparedStatement ps = conn.prepareStatement(sql);
		// 		ps.setInt(1, thang);
		// 		ResultSet rs = ps.executeQuery();
		// 		System.out.println("Bắt đầu truy vấn export...");
		// 		int count = 0;
		// 		System.out.println("Auto-commit: " + conn.getAutoCommit());
		// 		while (rs.next()) {
		// 			count++;
		// 			ChiTieuMau ctm = new ChiTieuMau();
		// 			ctm.setId_nguoidung(rs.getInt("id_nguoidung"));
		// 			ctm.setId_loai_chi(rs.getInt("id_loai_chi"));
		// 			ctm.setTen_chi_tieu_mau(rs.getString("ten_danh_muc"));
		// 			ctm.setSo_tien(rs.getDouble("so_tien"));
		// 			Date sqlDate = rs.getDate("ngay");
		// 			if (sqlDate != null) {
		// 				Calendar cal = Calendar.getInstance();
		// 				cal.setTime(sqlDate);
		// 				int ngayTrongThang = cal.get(Calendar.DAY_OF_MONTH);
		// 				ctm.setNgay(ngayTrongThang);
		// 			}
		// 			// Insert vào chitieu30ngay (truyền đủ các trường NOT NULL)
		// 			String insert = "INSERT INTO chitieu30ngay (id_nguoidung, id_muc_luong, id_buoi, id_loai_chi, id_goi_y, ten_chi_tieu_mau, so_tien, ngay) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		// 			PreparedStatement ps2 = conn.prepareStatement(insert);
		// 			ps2.setInt(1, ctm.getId_nguoidung());
		// 			ps2.setInt(2, 1); // id_muc_luong mặc định
		// 			ps2.setInt(3, 5); // id_buoi mặc định
		// 			ps2.setInt(4, ctm.getId_loai_chi());
		// 			ps2.setInt(5, 1); // id_goi_y mặc định
		// 			ps2.setString(6, ctm.getTen_chi_tieu_mau());
		// 			ps2.setDouble(7, ctm.getSo_tien());
		// 			ps2.setInt(8, ctm.getNgay());
		// 			System.out.println("Insert: " + ctm.getId_nguoidung() + ", " + ctm.getId_loai_chi() + ", " + ctm.getTen_chi_tieu_mau() + ", " + ctm.getSo_tien() + ", " + ctm.getNgay());
		// 			int rows = ps2.executeUpdate();
		// 			System.out.println("Rows inserted: " + rows);
		// 			if (rows > 0) exported.add(ctm);
		// 		}
		// 		System.out.println("Số bản ghi lấy được: " + count);
		// 		if (count == 0) System.out.println("Không có bản ghi nào được trả về từ truy vấn!");
		// 		conn.commit();
		// 	} catch (SQLException e) {
		// 		System.err.println("Lỗi SQL khi insert: " + e.getMessage());
		// 		e.printStackTrace();
		// 		throw new RuntimeException("SQL Error: " + e.getMessage());
		// 	}
		// 	return exported;
		// }

//		public List<ChiTieuMau> exportGiaoDichToChiTieu30Ngay(int thang) {
//			List<ChiTieuMau> exportedList = new ArrayList<>();
//			String selectSql = "SELECT g.id_nguoidung, g.so_tien, g.ngay, d.ten_danh_muc, g.id_loai " +
//							   "FROM giaodich g JOIN danhmuc d ON g.id_danhmuc = d.id_danhmuc " +
//							   "WHERE g.id_loai = 2 AND g.thang = ?";
//			try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
//				ps.setInt(1, thang);
//				ResultSet rs = ps.executeQuery();
//				while (rs.next()) {
//					int idNguoiDung = rs.getInt("id_nguoidung");
//					double soTien = rs.getDouble("so_tien");
//					Date sqlDate = rs.getDate("ngay");
//					int ngay = 1;
//					if (sqlDate != null) {
//						ngay = sqlDate.toLocalDate().getDayOfMonth();
//					}
//					String tenChiTieuMau = rs.getString("ten_danh_muc");
//					int idLoaiChi = rs.getInt("id_loai");
//
//					// Insert vào chitieu30ngay
//					String insertSql = "INSERT INTO chitieu30ngay (ngay, id_muc_luong, id_buoi, id_loai_chi, id_goi_y, ten_chi_tieu_mau, id_nguoidung, so_tien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//					try (PreparedStatement ps2 = conn.prepareStatement(insertSql)) {
//						ps2.setInt(1, ngay);
//						ps2.setInt(2, 1); // id_muc_luong mặc định
//						ps2.setInt(3, 5); // id_buoi mặc định
//						ps2.setInt(4, idLoaiChi);
//						ps2.setInt(5, 1); // id_goi_y mặc định
//						ps2.setString(6, tenChiTieuMau);
//						ps2.setInt(7, idNguoiDung);
//						ps2.setDouble(8, soTien);
//						int rows = ps2.executeUpdate();
//						if (rows > 0) {
//							// Tạo object chi tiết để trả về
//							ChiTieuMau ctm = new ChiTieuMau();
//							ctm.setId_nguoidung(idNguoiDung);
//							ctm.setSo_tien(soTien);
//							ctm.setNgay(ngay);
//							ctm.setTen_chi_tieu_mau(tenChiTieuMau);
//							ctm.setId_loai_chi(idLoaiChi);
//							exportedList.add(ctm);
//						}
//					}
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			return exportedList;
//		}

	// Lấy chi tiêu mẫu mặc định (public)
	public List<ChiTieuMau> getDefaultTemplates() {
		List<ChiTieuMau> list = new ArrayList<>();
		String sql = "SELECT * FROM chitieu30ngay WHERE id_nguoidung IS NULL ORDER BY ngay ASC";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ChiTieuMau ctm = new ChiTieuMau();
					ctm.setId(rs.getInt("id"));
					ctm.setNgay(rs.getInt("ngay"));
					ctm.setId_muc_luong(rs.getInt("id_muc_luong"));
					ctm.setId_buoi(rs.getInt("id_buoi"));
					ctm.setId_loai_chi(rs.getInt("id_loai_chi"));
					ctm.setId_goi_y(rs.getInt("id_goi_y"));
					ctm.setTen_chi_tieu_mau(rs.getString("ten_chi_tieu_mau"));
					Object idNguoiDungObj = rs.getObject("id_nguoidung");
					ctm.setId_nguoidung(idNguoiDungObj == null ? null : (Integer) idNguoiDungObj);
					list.add(ctm);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// Lấy chi tiêu mẫu của chỉ userId
	public List<ChiTieuMau> getByUserId(int userId) {
		List<ChiTieuMau> list = new ArrayList<>();
		String sql = "SELECT * FROM chitieu30ngay WHERE id_nguoidung = ? ORDER BY ngay ASC";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ChiTieuMau ctm = new ChiTieuMau();
					ctm.setId(rs.getInt("id"));
					ctm.setNgay(rs.getInt("ngay"));
					ctm.setId_muc_luong(rs.getInt("id_muc_luong"));
					ctm.setId_buoi(rs.getInt("id_buoi"));
					ctm.setId_loai_chi(rs.getInt("id_loai_chi"));
					ctm.setId_goi_y(rs.getInt("id_goi_y"));
					ctm.setTen_chi_tieu_mau(rs.getString("ten_chi_tieu_mau"));
					ctm.setId_nguoidung(rs.getInt("id_nguoidung"));
					list.add(ctm);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// Xóa tất cả chi tiêu mẫu của một người dùng
	public boolean deleteByUserId(int userId) {
		List<Integer> goiYIds = new ArrayList<>();
		// 1. Lấy tất cả id_goi_y của user
		String selectSql = "SELECT id_goi_y FROM chitieu30ngay WHERE id_nguoidung = ?";
		try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int idGoiY = rs.getInt("id_goi_y");
					if (idGoiY > 0) goiYIds.add(idGoiY);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		// 2. Xóa chi tiêu mẫu
		String deleteChiTieuSql = "DELETE FROM chitieu30ngay WHERE id_nguoidung = ?";
		try (PreparedStatement ps = conn.prepareStatement(deleteChiTieuSql)) {
			ps.setInt(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		// 3. Xóa các gợi ý liên quan
		if (!goiYIds.isEmpty()) {
			StringBuilder sb = new StringBuilder("DELETE FROM goiy WHERE id IN (");
			for (int i = 0; i < goiYIds.size(); i++) {
				sb.append("?");
				if (i < goiYIds.size() - 1) sb.append(",");
			}
			sb.append(")");
			try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
				for (int i = 0; i < goiYIds.size(); i++) {
					ps.setInt(i + 1, goiYIds.get(i));
				}
				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				// Không return false ở đây, vì có thể chi tiêu mẫu đã xóa thành công
			}
		}
		return true;
	}
}
