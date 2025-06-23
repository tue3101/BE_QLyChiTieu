package model;

import java.sql.Date;

public class ChiTieuHangThang {
	private int id , id_nguoidung, id_tennhom, thang, nam;
	private double so_tien;
	public ChiTieuHangThang() {
		super();
	}
	public ChiTieuHangThang(int id_nguoidung, int id_tennhom ,int thang, int nam, double so_tien) {
		super();
		this.id_nguoidung = id_nguoidung;
		this.id_tennhom = id_tennhom;
		this.thang = thang;
		this.nam = nam;
		this.so_tien = so_tien;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(int id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
	}
	public int getId_tennhom() {
		return id_tennhom;
	}
	public void setId_tennhom(int id_tennhom) {
		this.id_tennhom = id_tennhom;
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
	public double getSo_tien() {
		return so_tien;
	}
	public void setSo_tien(double so_tien) {
		this.so_tien = so_tien;
	}
	
	
	@Override
	public String toString() {
		return "ChiTieuHangThang [id=" + id + ", id_nguoidung=" + id_nguoidung + ", id_tennhom=" + id_tennhom +", ngay=" +  ", thang=" + thang + ", nam=" + nam + ", so_tien=" + so_tien + "]";
	}
	
	

}
