package model;

public class ThongKe {
    private double tongThuNhap;
    private double tongChiTieu;
    private boolean vuotNganSach;
    
    public ThongKe(double tongThuNhap, double tongChiTieu, boolean vuotNganSach) {
        this.tongThuNhap = tongThuNhap;
        this.tongChiTieu = tongChiTieu;
        this.vuotNganSach = vuotNganSach;
    }
    
    public double getTongThuNhap() {
        return tongThuNhap;
    }
    
    public void setTongThuNhap(double tongThuNhap) {
        this.tongThuNhap = tongThuNhap;
    }
    
    public double getTongChiTieu() {
        return tongChiTieu;
    }
    
    public void setTongChiTieu(double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }
    
    public boolean isVuotNganSach() {
        return vuotNganSach;
    }
    
    public void setVuotNganSach(boolean vuotNganSach) {
        this.vuotNganSach = vuotNganSach;
    }
} 