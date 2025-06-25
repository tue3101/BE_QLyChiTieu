package model;

public class LoaiChiTieu {
	private int id, id_buoi;
	private String ten_loai;
	public LoaiChiTieu() {
		super();
	}
	public LoaiChiTieu(int id_buoi, String ten_loai) {
		super();
		this.id_buoi = id_buoi;
		this.ten_loai = ten_loai;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId_buoi() {
		return id_buoi;
	}
	public void setId_buoi(int id_buoi) {
		this.id_buoi = id_buoi;
	}
	public String getTen_loai() {
		return ten_loai;
	}
	public void setTen_loai(String ten_loai) {
		this.ten_loai = ten_loai;
	}
	@Override
	public String toString() {
		return "LoaiChiTieu [id=" + id + ", id_buoi=" + id_buoi + ", ten_loai=" + ten_loai + "]";
	}
	

}
