package model;

public class GoiY {
	private int id, id_loai_chi;
	private String goi_y;
	private double gia;
	public GoiY() {
		super();
	}
	public GoiY(int id_loai_chi, String goi_y, double gia) {
		super();
		this.id_loai_chi = id_loai_chi;
		this.goi_y = goi_y;
		this.gia = gia;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId_loai_chi() {
		return id_loai_chi;
	}
	public void setId_loai_chi(int id_loai_chi) {
		this.id_loai_chi = id_loai_chi;
	}
	public String getGoi_y() {
		return goi_y;
	}
	public void setGoi_y(String goi_y) {
		this.goi_y = goi_y;
	}
	public double getGia() {
		return gia;
	}
	public void setGia(double gia) {
		this.gia = gia;
	}
	@Override
	public String toString() {
		return "GoiY [id=" + id + ", id_loai_chi=" + id_loai_chi + ", goi_y=" + goi_y + ", gia=" + gia + "]";
	} 
	

}
