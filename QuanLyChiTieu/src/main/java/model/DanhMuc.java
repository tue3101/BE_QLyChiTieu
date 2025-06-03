package model;

public class DanhMuc {
	private int id_danhmuc, id_nguoidung, id_mau, id_icon, id_loai;
	private String ten_danh_muc;
	public DanhMuc() {
		super();
	}
	public DanhMuc(int id_nguoidung, int id_mau, int id_icon,int id_loai, String ten_danh_muc) {
		super();
		this.id_nguoidung = id_nguoidung;
		this.id_mau = id_mau;
		this.id_icon = id_icon;
		this.id_loai =id_loai;
		this.ten_danh_muc = ten_danh_muc;
	}
	public int getId_danhmuc() {
		return id_danhmuc;
	}
	public void setId_danhmuc(int id_danhmuc) {
		this.id_danhmuc = id_danhmuc;
	}
	public int getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(int id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
	}
	public int getId_mau() {
		return id_mau;
	}
	public void setId_mau(int id_mau) {
		this.id_mau = id_mau;
	}
	public int getId_icon() {
		return id_icon;
	}
	public void setId_icon(int id_icon) {
		this.id_icon = id_icon;
	}
	public String getTen_danh_muc() {
		return ten_danh_muc;
	}
	public void setTen_danh_muc(String ten_danh_muc) {
		this.ten_danh_muc = ten_danh_muc;
	}
	public int getId_loai() {
		return id_loai;
	}
	public void setId_loai(int id_loai) {
		this.id_loai = id_loai;
	}
	@Override
	public String toString() {
		return "DanhMuc [id_danhmuc=" + id_danhmuc + ", id_nguoidung=" + id_nguoidung + ", id_mau=" + id_mau
				+ ", id_icon=" + id_icon + ", loai_danh_muc=" + id_loai + ", ten_danh_muc=" + ten_danh_muc + "]";
	}
	
	

}
