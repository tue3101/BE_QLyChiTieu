package model;

public class ChiTieuMau {
	private int id, ngay, id_muc_luong, id_buoi, id_loai_chi, id_goi_y;
	private String ten_chi_tieu_mau;
	private Integer id_nguoidung;

	public ChiTieuMau() {
		super();
	}

	

	


	public ChiTieuMau(int ngay, int id_muc_luong, int id_buoi, int id_loai_chi, int id_goi_y, String ten_chi_tieu_mau,
			Integer id_nguoidung) {
		super();
		this.ngay = ngay;
		this.id_muc_luong = id_muc_luong;
		this.id_buoi = id_buoi;
		this.id_loai_chi = id_loai_chi;
		this.id_goi_y = id_goi_y;
		this.ten_chi_tieu_mau = ten_chi_tieu_mau;
		this.id_nguoidung = id_nguoidung;
	}






	public String getTen_chi_tieu_mau() {
		return ten_chi_tieu_mau;
	}

	public void setTen_chi_tieu_mau(String ten_chi_tieu_mau) {
		this.ten_chi_tieu_mau = ten_chi_tieu_mau;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNgay() {
		return ngay;
	}

	public void setNgay(int ngay) {
		this.ngay = ngay;
	}

	public int getId_muc_luong() {
		return id_muc_luong;
	}

	public void setId_muc_luong(int id_muc_luong) {
		this.id_muc_luong = id_muc_luong;
	}

	public int getId_buoi() {
		return id_buoi;
	}

	public void setId_buoi(int id_buoi) {
		this.id_buoi = id_buoi;
	}

	public int getId_loai_chi() {
		return id_loai_chi;
	}

	public void setId_loai_chi(int id_loai_chi) {
		this.id_loai_chi = id_loai_chi;
	}

	public int getId_goi_y() {
		return id_goi_y;
	}

	public void setId_goi_y(int id_goi_y) {
		this.id_goi_y = id_goi_y;
	}

	public Integer getId_nguoidung() {
		return id_nguoidung;
	}

	public void setId_nguoidung(Integer id_nguoidung) {
		this.id_nguoidung = id_nguoidung;
	}

	

	@Override
	public String toString() {
		return "ChiTieuMau [id=" + id + ", ngay=" + ngay + ", id_muc_luong=" + id_muc_luong + ", id_buoi=" + id_buoi
				+ ", id_loai_chi=" + id_loai_chi + ", id_goi_y=" + id_goi_y + ", ten_chi_tieu_mau=" + ten_chi_tieu_mau
				+ ", id_nguoidung=" + id_nguoidung + ", so_tien=" + "]";
	}
	
	

}
