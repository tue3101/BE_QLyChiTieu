package model;

public class LoginResponse {
    private NguoiDung user;
    private String token;
    
    public LoginResponse(NguoiDung user, String token) {
        this.user = user;
        this.token = token;
    }
    
    public NguoiDung getUser() {
        return user;
    }
    
    public void setUser(NguoiDung user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
} 