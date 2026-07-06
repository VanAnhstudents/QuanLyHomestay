package com.example.ql_homestay.repository;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.ChiTietPhuThuDAO;
import com.example.ql_homestay.data.dao.HoaDonDAO;
import com.example.ql_homestay.model.ChiTietPhuThu;
import com.example.ql_homestay.model.HoaDon;

import java.util.List;

/**
 * InvoiceRepository – lớp trung gian giữa UI và tầng DAO cho module Thanh toán.
 * Đóng gói toàn bộ logic nghiệp vụ liên quan đến HoaDon và ChiTietPhuThu.
 */
public class InvoiceRepository {

    private final HoaDonDAO hoaDonDAO;
    private final ChiTietPhuThuDAO chiTietPhuThuDAO;

    public InvoiceRepository(DatabaseHelper dbHelper) {
        this.hoaDonDAO        = new HoaDonDAO(dbHelper);
        this.chiTietPhuThuDAO = new ChiTietPhuThuDAO(dbHelper);
    }

    // ─── HÓA ĐƠN ─────────────────────────────────────────────────────────────

    /** Lấy tất cả hóa đơn, mới nhất trước. */
    public List<HoaDon> getAllInvoices() {
        return hoaDonDAO.getAll();
    }

    /**
     * Lọc hóa đơn theo trạng thái.
     * @param trangThai "DaThanhToan" | "ChuaThanhToan" | "HoanTien"
     */
    public List<HoaDon> getInvoicesByTrangThai(String trangThai) {
        return hoaDonDAO.filterByTrangThai(trangThai);
    }

    /** Lấy chi tiết một hóa đơn kèm JOIN. */
    public HoaDon getInvoiceById(int maHD) {
        return hoaDonDAO.findById(maHD);
    }

    /** Tìm hóa đơn theo mã đặt phòng. */
    public HoaDon getInvoiceByDatPhong(int maDatPhong) {
        return hoaDonDAO.findByDatPhong(maDatPhong);
    }

    /** Lấy danh sách hóa đơn trong khoảng ngày lập. */
    public List<HoaDon> getInvoicesByDateRange(String from, String to) {
        return hoaDonDAO.getByDateRange(from, to);
    }

    /**
     * Tạo hóa đơn mới kèm các dòng phụ thu.
     * Thực hiện insert HoaDon trước, sau đó insert ChiTietPhuThu theo MaHD vừa tạo.
     *
     * @param hd          đối tượng HoaDon cần tạo (MaHD chưa set)
     * @param chiTietList danh sách dòng phụ thu (MaHD sẽ được gán tự động)
     * @return MaHD của hóa đơn vừa tạo, -1 nếu lỗi.
     */
    public long createInvoice(HoaDon hd, List<ChiTietPhuThu> chiTietList) {
        long maHD = hoaDonDAO.insert(hd);
        if (maHD > 0 && chiTietList != null) {
            for (ChiTietPhuThu ct : chiTietList) {
                ct.setMaHD((int) maHD);
                chiTietPhuThuDAO.insert(ct);
            }
        }
        return maHD;
    }

    /**
     * Xác nhận đã thanh toán hóa đơn.
     * @param maHD       mã hóa đơn
     * @param ngayTT     ngày thanh toán "yyyy-MM-dd"
     * @param phuongThuc phương thức thanh toán
     * @return số dòng bị ảnh hưởng
     */
    public int confirmPayment(int maHD, String ngayTT, String phuongThuc) {
        return hoaDonDAO.confirmPayment(maHD, ngayTT, phuongThuc);
    }

    /**
     * Cập nhật trạng thái hóa đơn (hoàn tiền…).
     * @param trangThai "DaThanhToan" | "ChuaThanhToan" | "HoanTien"
     */
    public int updateTrangThai(int maHD, String trangThai) {
        return hoaDonDAO.updateTrangThai(maHD, trangThai);
    }

    /** Xóa hóa đơn (cascade xóa ChiTietPhuThu). */
    public int deleteInvoice(int maHD) {
        return hoaDonDAO.delete(maHD);
    }

    // ─── PHỤ THU ─────────────────────────────────────────────────────────────

    /** Lấy tất cả dòng phụ thu của một hóa đơn. */
    public List<ChiTietPhuThu> getChiTietByHoaDon(int maHD) {
        return chiTietPhuThuDAO.getByHoaDon(maHD);
    }

    /** Thêm một dòng phụ thu. */
    public long addChiTiet(ChiTietPhuThu ct) {
        return chiTietPhuThuDAO.insert(ct);
    }

    /** Xóa một dòng phụ thu. */
    public int deleteChiTiet(int maChiTiet) {
        return chiTietPhuThuDAO.delete(maChiTiet);
    }

    // ─── THỐNG KÊ NHANH ──────────────────────────────────────────────────────

    /** Doanh thu hôm nay. */
    public double getTodayRevenue(String today) {
        return hoaDonDAO.getTotalRevenueByDate(today);
    }

    /** Số hóa đơn lập trong ngày. */
    public int getTodayInvoiceCount(String today) {
        return hoaDonDAO.countByDateRange(today, today);
    }
}
