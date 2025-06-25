package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.MucLuong;

public class MucLuongDAO implements DAO<MucLuong> {
	   private Connection conn;
	    
	    public MucLuongDAO() {
	        this.conn = DBConnection.getConnection();
	    }

		@Override
		public List<MucLuong> getAll() {
			List<MucLuong> list = new ArrayList<>();
			String sql = "SELECT * FROM mucluong";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					MucLuong ml = new MucLuong();
					ml.setId(rs.getInt("id"));
					ml.setMuc_luong(rs.getDouble("muc_luong"));
					list.add(ml);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		@Override
		public MucLuong getById(int id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean add(MucLuong t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean update(MucLuong t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean delete(int id) {
			// TODO Auto-generated method stub
			return false;
		}

}
