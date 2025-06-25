package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.LoaiChiTieu;

public class LoaiChiTieuDAO implements DAO<LoaiChiTieu> {

	   private Connection conn;
	    
	    public LoaiChiTieuDAO() {
	        this.conn = DBConnection.getConnection();
	    }

		@Override
		public List<LoaiChiTieu> getAll() {
			List<LoaiChiTieu> list = new ArrayList<>();
			String sql = "SELECT * FROM loaichitieu";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					LoaiChiTieu lct = new LoaiChiTieu();
					lct.setId(rs.getInt("id"));
					lct.setTen_loai(rs.getString("ten_loai"));
					lct.setId_buoi(rs.getInt("id_buoi"));
					list.add(lct);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		@Override
		public LoaiChiTieu getById(int id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean add(LoaiChiTieu t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean update(LoaiChiTieu t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean delete(int id) {
			// TODO Auto-generated method stub
			return false;
		}
}
