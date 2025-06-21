package model;

import java.time.LocalDate;

public class GiaoDich {
	private String id_GD, id_danhmuc, id_nguoidung, id_loai, id_tennhom;
	private double so_tien;
	private LocalDate ngay;
	private int thang, nam;
	private String ghi_chu;
	public GiaoDich() {
		super();
	}
	public GiaoDich(String id_danhmuc, String id_nguoidung, String id_loai, String id_tennhom, double so_tien, LocalDate ngay, int thang,
			int nam, String ghi_chu) {
		super();
		this.id_danhmuc = id_danhmuc;
		this.id_nguoidung = id_nguoidung;
		this.id_loai = id_loai;
		this.id_tennhom = id_tennhom;
		this.so_tien = so_tien;
		this.ngay = ngay;
		this.thang = thang;
		this.nam = nam;
		this.ghi_chu = ghi_chu;
	}
	public String getId_GD() {
		return id_GD;
	}
	public void setId_GD(String id_GD) {
		this.id_GD = id_GD;
	}
	public String getId_danhmuc() {
		return id_danhmuc;
	}
	public void setId_danhmuc(String id_danhmuc) {
		this.id_danhmuc = id_danhmuc;
	}
	public String getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(String id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
	}
	public String getId_loai() {
		return id_loai;
	}
	public void setId_loai(String id_loai) {
		this.id_loai = id_loai;
	}
	public double getSo_tien() {
		return so_tien;
	}
	public void setSo_tien(double so_tien) {
		this.so_tien = so_tien;
	}
	public LocalDate getNgay() {
		return ngay;
	}
	public void setNgay(LocalDate ngay) {
		this.ngay = ngay;
	}
	public int getThang() {
		return thang;
	}
	public void setThang(int thang) {
		this.thang = thang;
	}
	public int getNam() {
		return nam;
	}
	public void setNam(int nam) {
		this.nam = nam;
	}
	public String getGhi_chu() {
		return ghi_chu;
	}
	public void setGhi_chu(String ghi_chu) {
		this.ghi_chu = ghi_chu;
	}
	public String getId_tennhom() {
		return id_tennhom;
	}
	public void setId_tennhom(String id_tennhom) {
		this.id_tennhom = id_tennhom;
	}
	@Override
	public String toString() {
		return "GiaoDich [id_GD=" + id_GD + ", id_danhmuc=" + id_danhmuc + ", id_nguoidung=" + id_nguoidung
				+ ", id_loai=" + id_loai + ", id_tennhom=" + id_tennhom + ", so_tien=" + so_tien + ", ngay=" + ngay + ", thang=" + thang + ", nam="
				+ nam + ", ghi_chu=" + ghi_chu + "]";
	}
	
	
	

}
