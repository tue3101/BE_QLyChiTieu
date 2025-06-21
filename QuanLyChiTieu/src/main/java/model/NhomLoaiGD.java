package model;

public class NhomLoaiGD {
	private int id_tennhom, id_loai;
	private String tennhom;
	public NhomLoaiGD() {
		super();
	}
	public NhomLoaiGD(int id_loai, String tennhom) {
		super();
		this.id_loai = id_loai;
		this.tennhom = tennhom;
	}
	public int getId_tennhom() {
		return id_tennhom;
	}
	public void setId_tennhom(int id_tennhom) {
		this.id_tennhom = id_tennhom;
	}
	public int getId_loai() {
		return id_loai;
	}
	public void setId_loai(int id_loai) {
		this.id_loai = id_loai;
	}
	public String getTennhom() {
		return tennhom;
	}
	public void setTennhom(String tennhom) {
		this.tennhom = tennhom;
	}
	@Override
	public String toString() {
		return "NhomLoaiGD [id_tennhom=" + id_tennhom + ", id_loai=" + id_loai + ", tennhom=" + tennhom + "]";
	}
	
	

}
