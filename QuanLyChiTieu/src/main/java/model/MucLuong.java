package model;

public class MucLuong {
	private int id;
	private double muc_luong;
	public MucLuong() {
		super();
	}
	public MucLuong(double muc_luong) {
		super();
		this.muc_luong = muc_luong;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getMuc_luong() {
		return muc_luong;
	}
	public void setMuc_luong(double muc_luong) {
		this.muc_luong = muc_luong;
	}
	@Override
	public String toString() {
		return "MucLuong [id=" + id + ", muc_luong=" + muc_luong + "]";
	}
	

}
