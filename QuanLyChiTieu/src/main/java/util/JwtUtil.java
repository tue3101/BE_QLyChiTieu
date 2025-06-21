package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
//xác minh Json web token
public class JwtUtil {
	//ký và xác minh token JWT
    private static final String SECRET_KEY = "your_secret_key_here";
    private static final long EXPIRATION_TIME = 86400000; // hạn 24 giờ

    //phương thức nhận thông tin người dùng sau đó tạo chuỗi token có mã hóa 
    public static String generateToken(int userId, String email, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) //gán userId làm subject
                .claim("email", email)//gán thêm thông tin email và role
                .claim("role", role)
                .setIssuedAt(new Date()) //Ghi lại thời điểm token được tạo ra
                //System.currentTimeMillis() : tgian hiện tại 
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))//hạn sử dụng token
                //Dùng thuật toán HS256 để ký token JWT với khóa bí mật (SECRET_KEY).
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact(); // Tạo chuỗi token hoàn chỉnh
    }
//giải mã decode và xác minh tính hợp lệ của chuỗi JWT
    public static Claims validateToken(String token) {
        return Jwts.parser() //Tạo một trình phân tích (parser) JWT
                .setSigningKey(SECRET_KEY) //thiết lập khóa để kiểm tra chữ ký của token
                .parseClaimsJws(token) //phân tích token nếu hợp lệ thì sẽ giải mã thành đối tượng JWT
                .getBody(); //lấy ra phần payload trong token (thông tin người dùng)
    }

    //kiểm tra token có hết hạn chưa
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token); //Giải mã token, lấy phần Claims (dữ liệu bên trong)
            //Lấy thời gian hết hạn (exp) của token và so sánh exp có trước tgian hiện tại ko
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
} 