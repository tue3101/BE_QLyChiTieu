package model;

public class MauSac {
	private int id_mau;
	private String ma_mau, ten_mau;
	public MauSac() {
		super();
	}
	public MauSac(String ma_mau, String ten_mau) {
		super();
		this.ma_mau = ma_mau;
		this.ten_mau = ten_mau;
	}
	public int getId_mau() {
		return id_mau;
	}
	public void setId_mau(int id_mau) {
		this.id_mau = id_mau;
	}
	public String getMa_mau() {
		return ma_mau;
	}
	public void setMa_mau(String ma_mau) {
		this.ma_mau = ma_mau;
	}
	public String getTen_mau() {
		return ten_mau;
	}
	public void setTen_mau(String ten_mau) {
		this.ten_mau = ten_mau;
	}
	@Override
	public String toString() {
		return "MauSac [id_mau=" + id_mau + ", ma_mau=" + ma_mau + ", ten_mau=" + ten_mau + "]";
	}
	
	

}
