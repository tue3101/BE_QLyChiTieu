package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Buoi;


public class BuoiDAO implements DAO<Buoi> {
	   private Connection conn;
	    
	    public BuoiDAO(){
	        this.conn = DBConnection.getConnection();
	    }

		@Override
		public List<Buoi> getAll() {
			List<Buoi> list = new ArrayList<>();
			String sql = "SELECT * FROM buoi";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					Buoi b = new Buoi();
					b.setId(rs.getInt("id"));
					b.setTen_buoi(rs.getString("ten_buoi"));
					list.add(b);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		}

		@Override
		public Buoi getById(int id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean add(Buoi t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean update(Buoi t) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean delete(int id) {
			// TODO Auto-generated method stub
			return false;
		}

}
