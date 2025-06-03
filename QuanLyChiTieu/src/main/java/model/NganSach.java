package model;

public class NganSach {
	private int id_ngansach, id_nguoidung, thang, nam;
	private double ngansach;
	public NganSach() {
		super();
	}
	public NganSach(int id_nguoidung, int thang, int nam, double ngansach) {
		super();
		this.id_nguoidung = id_nguoidung;
		this.thang = thang;
		this.nam = nam;
		this.ngansach = ngansach;
	}
	public int getId_ngansach() {
		return id_ngansach;
	}
	public void setId_ngansach(int id_ngansach) {
		this.id_ngansach = id_ngansach;
	}
	public int getId_nguoidung() {
		return id_nguoidung;
	}
	public void setId_nguoidung(int id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
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
	public double getNgansach() {
		return ngansach;
	}
	public void setNgansach(double ngansach) {
		this.ngansach = ngansach;
	}
	@Override
	public String toString() {
		return "NganSach [id_ngansach=" + id_ngansach + ", id_nguoidung=" + id_nguoidung + ", thang=" + thang + ", nam="
				+ nam + ", ngansach=" + ngansach + "]";
	}
	

}
