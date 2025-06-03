package model;

public class Icon {
	private int id_icon;
	private String ten_icon;
	public Icon() {
		super();
	}
	public Icon(String ten_icon) {
		super();
		this.ten_icon = ten_icon;
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
	@Override
	public String toString() {
		return "Icon [id_icon=" + id_icon + ", ten_icon=" + ten_icon + "]";
	}
	
	

}
