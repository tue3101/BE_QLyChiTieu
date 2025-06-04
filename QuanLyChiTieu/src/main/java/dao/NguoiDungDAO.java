package dao;

import config.DBConnection;
import model.NguoiDung;
import util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO implements DAO<NguoiDung> {
    private Connection conn;
    
    public NguoiDungDAO() {
        this.conn = DBConnection.getConnection();
    }
    
    @Override
    public List<NguoiDung> getAll() {
        List<NguoiDung> list = new ArrayList<>();
        String sql = "SELECT * FROM nguoidung";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                NguoiDung nd = new NguoiDung();
                nd.setId_nguoidung(rs.getInt("id_nguoidung"));
                nd.setHoten(rs.getString("hoten"));
                nd.setEmail(rs.getString("email"));
                nd.setMatkhau(rs.getString("matkhau"));
                nd.setRole(rs.getString("role"));
                list.add(nd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    @Override
    public NguoiDung getById(int id) {
        String sql = "SELECT * FROM nguoidung WHERE id_nguoidung = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                NguoiDung nd = new NguoiDung();
                nd.setId_nguoidung(rs.getInt("id_nguoidung"));
                nd.setHoten(rs.getString("hoten"));
                nd.setEmail(rs.getString("email"));
                nd.setMatkhau(rs.getString("matkhau"));
                nd.setRole(rs.getString("role"));
                return nd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean add(NguoiDung nd) {
        String sql = "INSERT INTO nguoidung(hoten, email, matkhau, role) VALUES(?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nd.getHoten());
            ps.setString(2, nd.getEmail());
            ps.setString(3, PasswordUtil.hashPassword(nd.getMatkhau()));
            ps.setString(4, nd.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean update(NguoiDung nd) {
        // Phương thức này chỉ dùng để cập nhật mật khẩu
        String sql = "UPDATE nguoidung SET matkhau=? WHERE id_nguoidung=?";
        try {
            System.out.println("Executing update SQL: " + sql);
            PreparedStatement ps = conn.prepareStatement(sql);
            System.out.println("Setting parameters for update: matkhau=***masked***, id_nguoidung=" + nd.getId_nguoidung());
            // Mật khẩu đã được băm ở tầng Service, không băm lại ở đây
            ps.setString(1, nd.getMatkhau());
            ps.setInt(2, nd.getId_nguoidung());
            int rowsAffected = ps.executeUpdate();
            System.out.println("Update executed. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Exception during user update:");
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM nguoidung WHERE id_nguoidung=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Cập nhật thông tin người dùng (không bao gồm mật khẩu)
    public boolean updateUserInfo(NguoiDung nd) {
        String sql = "UPDATE nguoidung SET hoten=?, email=?, role=? WHERE id_nguoidung=?";
        try {
            System.out.println("Executing updateUserInfo SQL: " + sql);
            PreparedStatement ps = conn.prepareStatement(sql);
            System.out.println("Setting parameters for updateUserInfo: hoten=" + nd.getHoten() + ", email=" + nd.getEmail() + ", role=" + nd.getRole() + ", id_nguoidung=" + nd.getId_nguoidung());
            ps.setString(1, nd.getHoten());
            ps.setString(2, nd.getEmail());
            ps.setString(3, nd.getRole());
            ps.setInt(4, nd.getId_nguoidung());
            int rowsAffected = ps.executeUpdate();
            System.out.println("updateUserInfo executed. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Exception during user info update:");
            e.printStackTrace();
        }
        return false;
    }
    
    public NguoiDung login(String email, String password) {
        String sql = "SELECT * FROM nguoidung WHERE email=?";
        try {
            System.out.println("Attempting login for email: " + email);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                System.out.println("User found with email: " + email);
                String storedPassword = rs.getString("matkhau");
                if(PasswordUtil.verifyPassword(password, storedPassword)) {
                    System.out.println("Password verification successful for email: " + email);
                    NguoiDung nd = new NguoiDung();
                    nd.setId_nguoidung(rs.getInt("id_nguoidung"));
                    nd.setHoten(rs.getString("hoten"));
                    nd.setEmail(rs.getString("email"));
                    nd.setMatkhau(rs.getString("matkhau"));
                    nd.setRole(rs.getString("role"));
                    return nd;
                } else {
                    System.out.println("Password verification failed for email: " + email);
                }
            } else {
                System.out.println("User not found with email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception during login:");
            e.printStackTrace();
        }
        return null;
    }

    // Phương thức kiểm tra sự tồn tại của email
    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM nguoidung WHERE email = ? LIMIT 1";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // Trả về true nếu có ít nhất 1 dòng (email tồn tại)
        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý lỗi kết nối/truy vấn - có thể throw exception hoặc trả về false và log lỗi
            return false; // Giả định lỗi truy vấn nghĩa là không tìm thấy (hoặc xử lý lỗi khác)
        }
    }

    // Phương thức tìm kiếm người dùng theo tên 
    public List<NguoiDung> searchByName(String nameQuery) {
        List<NguoiDung> list = new ArrayList<>();
        String sql = "SELECT * FROM nguoidung WHERE hoten LIKE ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + nameQuery + "%"); // Tìm kiếm chuỗi con
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                NguoiDung nd = new NguoiDung();
                nd.setId_nguoidung(rs.getInt("id_nguoidung"));
                nd.setHoten(rs.getString("hoten"));
                nd.setEmail(rs.getString("email"));
                nd.setMatkhau(rs.getString("matkhau")); // Có thể cân nhắc không lấy mật khẩu ở đây
                nd.setRole(rs.getString("role"));
                list.add(nd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 