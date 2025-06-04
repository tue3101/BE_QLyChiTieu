package model;

public class Icon {
	private int id_icon;
	private String ten_icon;
	private String ma_icon;
	public Icon() {
		super();
	}
	public Icon(String ten_icon, String ma_icon) {
		super();
		this.ten_icon = ten_icon;
		this.ma_icon = ma_icon;
	}
	public int getId_icon() {
		return id_icon;
	}
	public void setId_icon(int id_icon) {
		this.id_icon = id_icon;
	}
	public String getTen_icon() {
		return ten_icon;
	}
	public void setTen_icon(String ten_icon) {
		this.ten_icon = ten_icon;
	}
	public String getMa_icon() {
		return ma_icon;
	}
	public void setMa_icon(String ma_icon) {
		this.ma_icon = ma_icon;
	}
	@Override
	public String toString() {
		return "Icon [id_icon=" + id_icon + ", ten_icon=" + ten_icon + ", ma_icon=" + ma_icon + "]";
	}
	
	

}
