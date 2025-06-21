package model;

public class DanhMuc {
	private int id_danhmuc, id_mau, id_icon, id_loai, id_tennhom;
	private Integer id_nguoidung;
	private String ten_danh_muc;
	public DanhMuc() {
		super();
	}
	public DanhMuc(Integer id_nguoidung, int id_mau, int id_icon,int id_loai,int id_tennhom, String ten_danh_muc) {
		super();
		this.id_nguoidung = id_nguoidung;
		this.id_mau = id_mau;
		this.id_icon = id_icon;
		this.id_loai =id_loai;
		this.id_tennhom=id_tennhom;
		this.ten_danh_muc = ten_danh_muc;
	}
	public int getId_danhmuc() {
		return id_danhmuc;
	}
	public void setId_danhmuc(int id_danhmuc) {
		this.id_danhmuc = id_danhmuc;
	}
	public Integer getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(Integer id_nguoidung) {
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
	public int getId_tennhom() {
		return id_tennhom;
	}
	public void setId_tennhom(int id_tennhom) {
		this.id_tennhom = id_tennhom;
	}
	@Override
	public String toString() {
		return "DanhMuc [id_danhmuc=" + id_danhmuc + ", id_mau=" + id_mau + ", id_icon=" + id_icon + ", id_loai="
				+ id_loai + ", id_tennhom=" + id_tennhom + ", id_nguoidung=" + id_nguoidung + ", ten_danh_muc="
				+ ten_danh_muc + "]";
	}
	
	

}
