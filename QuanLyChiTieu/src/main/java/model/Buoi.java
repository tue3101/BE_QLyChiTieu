package model;

public class Buoi {
	private int id;
	private String ten_buoi;
	public Buoi() {
		super();
	}
	public Buoi(String ten_buoi) {
		super();
		this.ten_buoi = ten_buoi;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTen_buoi() {
		return ten_buoi;
	}
	public void setTen_buoi(String ten_buoi) {
		this.ten_buoi = ten_buoi;
	}
	@Override
	public String toString() {
		return "Buoi [id=" + id + ", ten_buoi=" + ten_buoi + "]";
	}
	

}
