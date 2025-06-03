package model;

public class NguoiDung {
	private int id_nguoidung;
	private String hoten, email, matkhau, role;
	public NguoiDung() {
		super();
	}
	public NguoiDung(String hoten, String email, String matkhau, String role) {
		super();
		this.hoten = hoten;
		this.email = email;
		this.matkhau = matkhau;
		this.role = role;
	}
	public int getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(int id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
	}
	public String getHoten() {
		return hoten;
	}
	public void setHoten(String hoten) {
		this.hoten = hoten;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMatkhau() {
		return matkhau;
	}
	public void setMatkhau(String matkhau) {
		this.matkhau = matkhau;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	@Override
	public String toString() {
		return "NguoiDung [id_nguoidung=" + id_nguoidung + ", hoten=" + hoten + ", email=" + email + ", matkhau="
				+ matkhau + ", role=" + role + "]";
	}
	
	

}
