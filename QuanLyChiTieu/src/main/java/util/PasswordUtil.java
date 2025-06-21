package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

	// mã hóa mật khẩu
	public static String hashPassword(String password) {
		try {
			// MessageDigest thực hiện các thuật toán băm
			// Tạo một đối tượng để mã hóa dữ liệu bằng thuật toán SHA-256.
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] salt = generateSalt(); // Tạo một chuỗi salt ngẫu nhiên
			md.update(salt);// trộn salt với mật khẩu trước khi mã hóa
			byte[] hashedPassword = md.digest(password.getBytes()); // thực thi băm mật khẩu
			// trả về kq có cấu trúc <salt base64> : <hashedPassword base64>
			return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Lỗi khi mã hóa mật khẩu", e);
		}
	}

	// xác minh mật khẩu
	public static boolean verifyPassword(String password, String storedPassword) {
		try {
			// Tách chuỗi đã lưu (định dạng "salt:hash") thành 2 phần:
			// parts[0]: salt (đã mã hóa Base64)
			// parts[1]: hashed password (đã mã hóa Base64)
			String[] parts = storedPassword.split(":");

			// Giải mã Base64 để lấy lại salt và hash gốc đã lưu ở CSDL
			byte[] salt = Base64.getDecoder().decode(parts[0]);
			byte[] storedHash = Base64.getDecoder().decode(parts[1]);

			// Băm lại mật khẩu người dùng vừa nhập (sử dụng lại salt cũ) để tạo
			// hashedPassword mới
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt);
			byte[] hashedPassword = md.digest(password.getBytes());

			// So sánh 2 mảng byte:
			// storedHash (mã đã lưu trong DB)
			// hashedPassword (vừa băm lại từ mật khẩu nhập)
			return MessageDigest.isEqual(storedHash, hashedPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Lỗi khi xác thực mật khẩu", e);
		}
	}

	
	//tạo salt bảo mật tỏng quá trình băm mk
	private static byte[] generateSalt() {
		SecureRandom random = new SecureRandom(); //Tạo bộ sinh số ngẫu nhiên an toàn
		byte[] salt = new byte[16]; //Tạo mảng 16 byte = 128 bit (đủ mạnh cho salt)
		random.nextBytes(salt); //Ghi các giá trị ngẫu nhiên vào mảng salt
		return salt;
	}
}