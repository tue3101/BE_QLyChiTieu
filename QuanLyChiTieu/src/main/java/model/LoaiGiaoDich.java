package model;

public class LoaiGiaoDich {
	private int id_loai;
	private String ten_loai;
	public LoaiGiaoDich() {
		super();
	}
	public LoaiGiaoDich(String ten_loai) {
		super();
		this.ten_loai = ten_loai;
	}
	public int getId_loai() {
		return id_loai;
	}
	public void setId_loai(int id_loai) {
		this.id_loai = id_loai;
	}
	public String getTen_loai() {
		return ten_loai;
	}
	public void setTen_loai(String ten_loai) {
		this.ten_loai = ten_loai;
	}
	@Override
	public String toString() {
		return "LoaiGiaoDich [id_loai=" + id_loai + ", ten_loai=" + ten_loai + "]";
	}
	
	

}
