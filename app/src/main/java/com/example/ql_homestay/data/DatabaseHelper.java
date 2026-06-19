package com.example.ql_homestay.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * DatabaseHelper – Lala House
 * --------------------------------------------------------------------------------
 * Lớp duy nhất chịu trách nhiệm:
 *   1) Định nghĩa schema (CREATE TABLE) cho toàn bộ 17 bảng theo đúng RM.md,
 *      tôn trọng thứ tự khóa ngoại: bảng cha được tạo trước bảng con.
 *   2) Seed dữ liệu mẫu ngay trong onCreate() để có dữ liệu demo offline ngay
 *      từ lần chạy đầu tiên, không cần nhập tay, không cần file .db rời.
 * Theo guide.md: kiến trúc 3 lớp khuyến nghị là
 *      Model (POJO) -> DAO (CRUD) -> DatabaseHelper (SQLiteOpenHelper)
 * Lớp này chỉ đóng vai trò "DatabaseHelper" — không chứa Model/DAO/UI.
 * Quy ước seed:
 *   - 3 bảng hệ thống cố định (Quyen, Module, PhanQuyen_VaiTro) seed đúng
 *     seed data đã có sẵn trong RM.md.
 *   - Dữ liệu nghiệp vụ (TaiKhoan, NhanVien, Phong, KhachHang, DatPhong,
 *     CheckInOut, HoaDon, ChiTietPhuThu, ThongBao...) được seed bằng code,
 *     số lượng ở mức trung bình (10–15 dòng/bảng chính) để demo UI phong phú
 *     nhưng vẫn dễ kiểm soát tính nhất quán (trạng thái Phong khớp DatPhong,
 *     SoDem khớp NgayCheckIn/NgayCheckOut, TongCong khớp TienPhong + PhuThu
 *     - GiamGia, ChiTietPhuThu khớp tổng PhuThuDichVu của hóa đơn, v.v.)
 *   - Mốc thời gian "hiện tại" dùng để tạo dữ liệu tương đối là 17/06/2026,
 *     vì vậy các đặt phòng "DangO"/"SapDen"/"DaTraPhong"/"DaHuy" được rải
 *     quanh mốc này để khi mở app, danh sách trông như một hệ thống đang
 *     hoạt động thật.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "lalahouse.db";
    private static final int DB_VERSION = 2; // Đã tăng DB_VESION từ 1 -> 2 để onUpgrade() chạy lại và tạo bảng mới ==> tăng dần lên nếu có sự thay đổi ở đây!
    private static DatabaseHelper instance;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bật ràng buộc khóa ngoại cho phiên làm việc hiện tại của onCreate.
        // (Android không tự bật FK theo mặc định; mỗi lần mở DB ở DAO cũng
        // nên gọi db.execSQL("PRAGMA foreign_keys = ON;") tương tự.)
        db.execSQL("PRAGMA foreign_keys = ON;");

        createTables(db);

        // ----- Seed 3 bảng hệ thống cố định (đúng dữ liệu trong RM.md) -----
        seedQuyen(db);
        seedModule(db);
        seedPhanQuyenVaiTro(db);

        // ----- Seed dữ liệu nền (không phụ thuộc nghiệp vụ runtime) -----
        seedCaLamViec(db);
        seedLoaiPhong(db);
        seedTienNghi(db);

        // ----- Seed dữ liệu nghiệp vụ (đã build sẵn quan hệ nhất quán) -----
        seedTaiKhoan(db);
        seedNhanVien(db);
        seedPhanCongCa(db);
        seedKhachHang(db);
        seedPhong(db);
        seedPhongTienNghi(db);
        seedDatPhong(db);
        seedCheckInOut(db);
        seedHoaDon(db);
        seedChiTietPhuThu(db);
        seedThongBao(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Giai đoạn phát triển: đơn giản nhất là drop toàn bộ rồi tạo lại.
        // Thứ tự DROP ngược với thứ tự CREATE để tránh vướng khóa ngoại
        // (bảng con drop trước, bảng cha drop sau).
        db.execSQL("DROP TABLE IF EXISTS ChiTietPhuThu");
        db.execSQL("DROP TABLE IF EXISTS HoaDon");
        db.execSQL("DROP TABLE IF EXISTS CheckInOut");
        db.execSQL("DROP TABLE IF EXISTS DatPhong");
        db.execSQL("DROP TABLE IF EXISTS ThongBao");
        db.execSQL("DROP TABLE IF EXISTS Phong_TienNghi");
        db.execSQL("DROP TABLE IF EXISTS PhanCongCa");
        db.execSQL("DROP TABLE IF EXISTS PhanQuyen_VaiTro");
        db.execSQL("DROP TABLE IF EXISTS Phong");
        db.execSQL("DROP TABLE IF EXISTS NhanVien");
        db.execSQL("DROP TABLE IF EXISTS Quyen");
        db.execSQL("DROP TABLE IF EXISTS Module");
        db.execSQL("DROP TABLE IF EXISTS TienNghi");
        db.execSQL("DROP TABLE IF EXISTS LoaiPhong");
        db.execSQL("DROP TABLE IF EXISTS KhachHang");
        db.execSQL("DROP TABLE IF EXISTS CaLamViec");
        db.execSQL("DROP TABLE IF EXISTS TaiKhoan");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Đảm bảo FK luôn được bật mỗi khi DB được mở (mặc định Android là OFF).
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    // =========================================================================
    // TẠO SCHEMA – 17 BẢNG, ĐÚNG THỨ TỰ CHA -> CON
    // =========================================================================
    private void createTables(SQLiteDatabase db) {

        // ---------- NHÓM 1: bảng gốc, không phụ thuộc FK ----------

        db.execSQL("CREATE TABLE TaiKhoan (" +
                "MaTK INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenDangNhap TEXT NOT NULL UNIQUE," +
                "Email TEXT," +
                "MatKhau TEXT NOT NULL," +
                "VaiTro TEXT NOT NULL CHECK (VaiTro IN ('Admin','LeTan','KeToan','NhanVien'))," +
                "TrangThai TEXT NOT NULL DEFAULT 'HoatDong' CHECK (TrangThai IN ('HoatDong','Khoa'))," +
                "NgayTao TEXT NOT NULL," +
                "Avatar TEXT)");

        db.execSQL("CREATE TABLE CaLamViec (" +
                "MaCa INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenCa TEXT NOT NULL CHECK (TenCa IN ('Sang','Chieu','Toi'))," +
                "GioBatDau TEXT NOT NULL," +
                "GioKetThuc TEXT NOT NULL)");

        db.execSQL("CREATE TABLE KhachHang (" +
                "MaKH INTEGER PRIMARY KEY AUTOINCREMENT," +
                "HoTen TEXT NOT NULL," +
                "SDT TEXT," +
                "Email TEXT," +
                "CCCD TEXT," +
                "DiaChi TEXT," +
                "NgaySinh TEXT," +
                "GioiTinh TEXT CHECK (GioiTinh IN ('Nam','Nu','Khac'))," +
                "Avatar TEXT," +
                "SoLanThue INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE LoaiPhong (" +
                "MaLoaiPhong INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenLoai TEXT NOT NULL CHECK (TenLoai IN ('Standard','Deluxe','Suite'))," +
                "GiaCoBan REAL NOT NULL)");

        db.execSQL("CREATE TABLE TienNghi (" +
                "MaTienNghi INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenTienNghi TEXT NOT NULL UNIQUE)");

        db.execSQL("CREATE TABLE Module (" +
                "MaModule INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenModule TEXT NOT NULL UNIQUE CHECK (TenModule IN (" +
                "'TrangChu','QuanLyPhong','QuanLyDatPhong','QuanLyKhachHang'," +
                "'HoaDonThanhToan','QuanLyNhanVien','BaoCaoThongKe','CaiDatHeThong'))," +
                "Icon TEXT)");

        db.execSQL("CREATE TABLE Quyen (" +
                "MaQuyen INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TenQuyen TEXT NOT NULL UNIQUE CHECK (TenQuyen IN " +
                "('ToanQuyen','ChiXem','XemVaTao','KhongTruyCap')))");

        // ---------- NHÓM 2: phụ thuộc Nhóm 1 ----------

        db.execSQL("CREATE TABLE NhanVien (" +
                "MaNV INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaTK INTEGER UNIQUE," +
                "HoTen TEXT NOT NULL," +
                "ChucVu TEXT NOT NULL CHECK (ChucVu IN ('QuanLy', 'LeTan','KeToan','DonPhong','BaoVe'))," +
                "SDT TEXT," +
                "Email TEXT," +
                "CCCD TEXT," +
                "DiaChi TEXT," +
                "NgayVaoLam TEXT," +
                "Avatar TEXT," +
                "FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK) ON DELETE SET NULL)");

        db.execSQL("CREATE TABLE Phong (" +
                "MaPhong INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaLoaiPhong INTEGER NOT NULL," +
                "TenPhong TEXT NOT NULL," +
                "GiaMoiDem REAL NOT NULL," +
                "SucChua INTEGER," +
                "DienTich REAL," +
                "Tang INTEGER," +
                "TrangThai TEXT NOT NULL CHECK (TrangThai IN ('Trong','DangThue','DaDat'))," +
                "HinhAnh TEXT," +
                "MoTa TEXT," +
                "FOREIGN KEY (MaLoaiPhong) REFERENCES LoaiPhong(MaLoaiPhong))");

        db.execSQL("CREATE TABLE PhanQuyen_VaiTro (" +
                "MaPhanQuyen INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaVaiTro TEXT NOT NULL CHECK (MaVaiTro IN ('Admin','LeTan','KeToan','NhanVien'))," +
                "MaModule INTEGER NOT NULL," +
                "MaQuyen INTEGER NOT NULL," +
                "FOREIGN KEY (MaModule) REFERENCES Module(MaModule)," +
                "FOREIGN KEY (MaQuyen) REFERENCES Quyen(MaQuyen)," +
                "UNIQUE (MaVaiTro, MaModule))");

        // ---------- NHÓM 3: phụ thuộc Nhóm 2 ----------

        db.execSQL("CREATE TABLE PhanCongCa (" +
                "MaPhanCong INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaNV INTEGER NOT NULL," +
                "MaCa INTEGER NOT NULL," +
                "ThuTrongTuan INTEGER NOT NULL CHECK (ThuTrongTuan BETWEEN 1 AND 7)," +
                "FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) ON DELETE CASCADE," +
                "FOREIGN KEY (MaCa) REFERENCES CaLamViec(MaCa))");

        db.execSQL("CREATE TABLE Phong_TienNghi (" +
                "MaPhong INTEGER NOT NULL," +
                "MaTienNghi INTEGER NOT NULL," +
                "PRIMARY KEY (MaPhong, MaTienNghi)," +
                "FOREIGN KEY (MaPhong) REFERENCES Phong(MaPhong) ON DELETE CASCADE," +
                "FOREIGN KEY (MaTienNghi) REFERENCES TienNghi(MaTienNghi) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE ThongBao (" +
                "MaTB INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaTK INTEGER NOT NULL," +
                "NoiDung TEXT NOT NULL," +
                "DaDoc INTEGER NOT NULL DEFAULT 0 CHECK (DaDoc IN (0,1))," +
                "ThoiGian TEXT NOT NULL," +
                "FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE DatPhong (" +
                "MaDatPhong INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaKH INTEGER NOT NULL," +
                "MaPhong INTEGER NOT NULL," +
                "MaNV INTEGER," +
                "NgayCheckIn TEXT NOT NULL," +
                "NgayCheckOut TEXT NOT NULL," +
                "SoLuongKhach INTEGER NOT NULL DEFAULT 1," +
                "SoDem INTEGER NOT NULL," +
                "TrangThai TEXT NOT NULL CHECK (TrangThai IN ('SapDen','DangO','DaTraPhong','DaHuy'))," +
                "PhuongThucThanhToan TEXT," +
                "GhiChu TEXT," +
                "NgayTao TEXT NOT NULL," +
                "FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH)," +
                "FOREIGN KEY (MaPhong) REFERENCES Phong(MaPhong)," +
                "FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) ON DELETE SET NULL)");

        // ---------- NHÓM 4: phụ thuộc Nhóm 3 ----------

        db.execSQL("CREATE TABLE CheckInOut (" +
                "MaCheckLog INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaDatPhong INTEGER NOT NULL," +
                "MaNV INTEGER," +
                "Loai TEXT NOT NULL CHECK (Loai IN ('CheckIn','CheckOut'))," +
                "ThoiGian TEXT NOT NULL," +
                "GhiChuDacBiet TEXT," +
                "FOREIGN KEY (MaDatPhong) REFERENCES DatPhong(MaDatPhong) ON DELETE CASCADE," +
                "FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) ON DELETE SET NULL)");

        // HoaDon–DatPhong là quan hệ 1–1 nhưng KHÔNG bắt buộc với mọi DatPhong:
        // một đặt phòng "SắpĐến" (chưa diễn ra) hoặc "ĐãHủy" (không phát sinh
        // dịch vụ) có thể chưa có hóa đơn nào. Vì vậy MaDatPhong là UNIQUE
        // (đảm bảo 1 đặt phòng không thể có 2 hóa đơn) nhưng cho phép NULL.
        db.execSQL("CREATE TABLE HoaDon (" +
                "MaHD INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaDatPhong INTEGER UNIQUE," +
                "NgayLap TEXT NOT NULL," +
                "TienPhong REAL NOT NULL DEFAULT 0," +
                "PhuThuDichVu REAL NOT NULL DEFAULT 0," +
                "GiamGia REAL NOT NULL DEFAULT 0," +
                "TongCong REAL NOT NULL DEFAULT 0," +
                "TrangThai TEXT NOT NULL CHECK (TrangThai IN ('DaThanhToan','ChuaThanhToan','HoanTien'))," +
                "PhuongThucTT TEXT," +
                "NgayTT TEXT," +
                "MaNV INTEGER," +
                "FOREIGN KEY (MaDatPhong) REFERENCES DatPhong(MaDatPhong) ON DELETE CASCADE," +
                "FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) ON DELETE SET NULL)");

        // ---------- NHÓM 5: phụ thuộc Nhóm 4 ----------

        db.execSQL("CREATE TABLE ChiTietPhuThu (" +
                "MaChiTiet INTEGER PRIMARY KEY AUTOINCREMENT," +
                "MaHD INTEGER NOT NULL," +
                "TenPhuThu TEXT NOT NULL," +
                "SoTien REAL NOT NULL DEFAULT 0," +
                "FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD) ON DELETE CASCADE)");
    }

    // =========================================================================
    // SEED 1: Quyen (4 dòng cố định)
    // =========================================================================
    private void seedQuyen(SQLiteDatabase db) {
        String[] tenQuyenList = {"ToanQuyen", "ChiXem", "XemVaTao", "KhongTruyCap"};
        for (String ten : tenQuyenList) {
            ContentValues cv = new ContentValues();
            cv.put("TenQuyen", ten);
            db.insert("Quyen", null, cv);
        }
    }

    // =========================================================================
    // SEED 2: Module (8 dòng cố định, đúng tên trong RM.md)
    // =========================================================================
    private void seedModule(SQLiteDatabase db) {
        String[][] modules = {
                {"TrangChu",        "ic_home"},
                {"QuanLyPhong",     "ic_bed"},
                {"QuanLyDatPhong",  "ic_calendar"},
                {"QuanLyKhachHang", "ic_person"},
                {"HoaDonThanhToan", "ic_invoice"},
                {"QuanLyNhanVien",  "ic_staff"},
                {"BaoCaoThongKe",   "ic_chart"},
                {"CaiDatHeThong",   "ic_settings"},
        };
        for (String[] m : modules) {
            ContentValues cv = new ContentValues();
            cv.put("TenModule", m[0]);
            cv.put("Icon", m[1]);
            db.insert("Module", null, cv);
        }
    }

    /**
     * Tìm MaModule theo TenModule, MaQuyen theo TenQuyen.
     * Dùng nội bộ khi seed PhanQuyen_VaiTro để không phải hardcode ID,
     * tránh sai lệch nếu thứ tự insert ở seedModule()/seedQuyen() thay đổi.
     */
    private long getIdByName(SQLiteDatabase db, String table, String idColumn,
                             String nameColumn, String name) {
        android.database.Cursor cursor = db.query(table, new String[]{idColumn},
                nameColumn + " = ?", new String[]{name}, null, null, null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }

    // =========================================================================
    // SEED 3: PhanQuyen_VaiTro (32 dòng = 4 vai trò x 8 module)
    // =========================================================================
    private void seedPhanQuyenVaiTro(SQLiteDatabase db) {
        // {MaVaiTro, TenModule, TenQuyen}
        String[][] rows = {
                // Admin: ToanQuyen cho toàn bộ 8 module
                {"Admin", "TrangChu",        "ToanQuyen"},
                {"Admin", "QuanLyPhong",     "ToanQuyen"},
                {"Admin", "QuanLyDatPhong",  "ToanQuyen"},
                {"Admin", "QuanLyKhachHang", "ToanQuyen"},
                {"Admin", "HoaDonThanhToan", "ToanQuyen"},
                {"Admin", "QuanLyNhanVien",  "ToanQuyen"},
                {"Admin", "BaoCaoThongKe",   "ToanQuyen"},
                {"Admin", "CaiDatHeThong",   "ToanQuyen"},

                // LeTan
                // Ghi chú: RM.md ghi "Xem" cho TrangChu, nhưng bảng Quyen chỉ có
                // đúng 4 giá trị chuẩn (ToanQuyen/ChiXem/XemVaTao/KhongTruyCap).
                // "Xem" được ánh xạ vào "ChiXem" cho nhất quán dữ liệu.
                {"LeTan", "TrangChu",        "ChiXem"},
                {"LeTan", "QuanLyPhong",     "ChiXem"},
                {"LeTan", "QuanLyDatPhong",  "ToanQuyen"},
                {"LeTan", "QuanLyKhachHang", "ToanQuyen"},
                {"LeTan", "HoaDonThanhToan", "XemVaTao"},
                {"LeTan", "QuanLyNhanVien",  "KhongTruyCap"},
                {"LeTan", "BaoCaoThongKe",   "KhongTruyCap"},
                {"LeTan", "CaiDatHeThong",   "KhongTruyCap"},

                // KeToan
                {"KeToan", "TrangChu",        "ChiXem"},
                {"KeToan", "QuanLyPhong",     "ChiXem"},
                {"KeToan", "QuanLyDatPhong",  "ChiXem"},
                {"KeToan", "QuanLyKhachHang", "ChiXem"},
                {"KeToan", "HoaDonThanhToan", "ToanQuyen"},
                {"KeToan", "QuanLyNhanVien",  "ChiXem"},
                {"KeToan", "BaoCaoThongKe",   "ToanQuyen"},
                {"KeToan", "CaiDatHeThong",   "KhongTruyCap"},

                // NhanVien
                {"NhanVien", "TrangChu",        "ChiXem"},
                {"NhanVien", "QuanLyPhong",     "ChiXem"},
                {"NhanVien", "QuanLyDatPhong",  "ChiXem"},
                {"NhanVien", "QuanLyKhachHang", "ChiXem"},
                {"NhanVien", "HoaDonThanhToan", "KhongTruyCap"},
                {"NhanVien", "QuanLyNhanVien",  "KhongTruyCap"},
                {"NhanVien", "BaoCaoThongKe",   "KhongTruyCap"},
                {"NhanVien", "CaiDatHeThong",   "KhongTruyCap"},
        };

        for (String[] r : rows) {
            long maModule = getIdByName(db, "Module", "MaModule", "TenModule", r[1]);
            long maQuyen = getIdByName(db, "Quyen", "MaQuyen", "TenQuyen", r[2]);
            ContentValues cv = new ContentValues();
            cv.put("MaVaiTro", r[0]);
            cv.put("MaModule", maModule);
            cv.put("MaQuyen", maQuyen);
            db.insert("PhanQuyen_VaiTro", null, cv);
        }
    }

    // =========================================================================
    // SEED 4: CaLamViec (3 ca: Sáng / Chiều / Tối)
    // MaCa sẽ được AUTOINCREMENT: Sang=1, Chieu=2, Toi=3
    // =========================================================================
    private void seedCaLamViec(SQLiteDatabase db) {
        // {TenCa, GioBatDau, GioKetThuc}
        String[][] cas = {
                {"Sang",  "06:00", "14:00"},
                {"Chieu", "14:00", "22:00"},
                {"Toi",   "22:00", "06:00"},
        };
        for (String[] c : cas) {
            ContentValues cv = new ContentValues();
            cv.put("TenCa",       c[0]);
            cv.put("GioBatDau",   c[1]);
            cv.put("GioKetThuc",  c[2]);
            db.insert("CaLamViec", null, cv);
        }
    }

    // =========================================================================
    // SEED 5: LoaiPhong (Standard / Deluxe / Suite)
    // MaLoaiPhong: Standard=1, Deluxe=2, Suite=3
    // =========================================================================
    private void seedLoaiPhong(SQLiteDatabase db) {
        // {TenLoai, GiaCoBan}
        Object[][] loais = {
                {"Standard", 500_000.0},
                {"Deluxe",   900_000.0},
                {"Suite",  1_800_000.0},
        };
        for (Object[] l : loais) {
            ContentValues cv = new ContentValues();
            cv.put("TenLoai",   (String) l[0]);
            cv.put("GiaCoBan",  (Double) l[1]);
            db.insert("LoaiPhong", null, cv);
        }
    }

    // =========================================================================
    // SEED 6: TienNghi (WiFi, TV, DieuHoa, TuLanh, BonTam, Ban, TuQuan, BonTam)
    // MaTienNghi: WiFi=1, TV=2, DieuHoa=3, TuLanh=4, BonTam=5, Ban=6, TuQuan=7
    // =========================================================================
    private void seedTienNghi(SQLiteDatabase db) {
        String[] items = {"WiFi", "TV", "DieuHoa", "TuLanh", "BonTam", "BanLamViec", "TuQuan"};
        for (String t : items) {
            ContentValues cv = new ContentValues();
            cv.put("TenTienNghi", t);
            db.insert("TienNghi", null, cv);
        }
    }

    // =========================================================================
    // SEED 7: TaiKhoan
    // Mật khẩu để dạng plain-text (demo); production cần hash.
    // MaTK sẽ là: admin=1, letan1=2, letan2=3, ketoan1=4, nv1=5, nv2=6, nv3=7
    // =========================================================================
    private void seedTaiKhoan(SQLiteDatabase db) {
        // {TenDangNhap, Email, MatKhau, VaiTro, TrangThai, Avatar}
        String[][] tks = {
                {"admin",  "admin@lalahouse.vn",        "admin@1001",        "Admin",    "HoatDong", "2025-01-10", "avatar_admin"},
                {"lt.nva", "letan_nva@lalahouse.vn",    "letan_nva@1001",    "LeTan",    "HoatDong", "2025-02-01", "avatar_nam"},
                {"lt.dtt", "letan_dtt@lalahouse.vn",    "letan_dtt@1002",    "LeTan",    "HoatDong", "2025-03-15", "avatar_nu"},
                {"kt.dth", "ketoan_dth@lalahouse.vn",   "ketoan_dth@1001",   "KeToan",   "HoatDong", "2025-02-10", "avatar_nu"},
                {"nv.nva", "nhanvien_nva@lalahouse.vn", "nhanvien_nva@1001", "NhanVien", "HoatDong", "2025-04-01", "avatar_nam"},
                {"nv.dtt", "nhanvien_dtt@lalahouse.vn", "nhanvien_dtt@1002", "NhanVien", "HoatDong", "2025-05-01", "avatar_nu"},
                {"nv.dth", "nhanvien_dth@lalahouse.vn", "nhanvien_dth@1003", "NhanVien", "Khoa",     "2025-06-01", "avatar_nu"},
        };
        for (String[] t : tks) {
            ContentValues cv = new ContentValues();
            cv.put("TenDangNhap", t[0]);
            cv.put("Email",       t[1]);
            cv.put("MatKhau",     t[2]);
            cv.put("VaiTro",      t[3]);
            cv.put("TrangThai",   t[4]);
            cv.put("NgayTao",     t[5]);
            cv.put("Avatar",      t[6]);
            db.insert("TaiKhoan", null, cv);
        }
    }

    // =========================================================================
    // SEED 8: NhanVien (7 nhân viên, liên kết MaTK từ SEED 7)
    // MaNV: 1..7 (tương ứng MaTK 1..7)
    // =========================================================================
    private void seedNhanVien(SQLiteDatabase db) {
        // {MaTK, HoTen, ChucVu, SDT, Email, CCCD, DiaChi, NgayVaoLam, Avatar}
        String[][] nvs = {
                {"1", "Nguyen Van Anh", "QuanLy",   "0901111001", "admin@lalahouse.vn",        "001080012345", "Hoang Tien, Thanh Hoa", "2025-01-10", "avatar_admin"},
                {"2", "Nguyen Van An",  "LeTan",    "0901111002", "letan_nva@lalahouse.vn",    "001090023456", "Hoang Tien, Thanh Hoa", "2025-02-01", "avatar_nam"},
                {"3", "Dinh Thi Truc",  "LeTan",    "0901111003", "letan_dtt@lalahouse.vn",    "001070034567", "Hau Loc, Thanh Hoa",    "2025-03-15", "avatar_nu"},
                {"4", "Dinh Thi Ha",    "KeToan",   "0901111004", "ketoan_dth@lalahouse.vn",   "001085045678", "Yen Dinh, Thanh Hoa",   "2025-02-10", "avatar_nu"},
                {"5", "Nguyen Van Anh", "DonPhong", "0901111005", "nhanvien_nva@lalahouse.v",  "001095056789", "Hoang Tien, Thanh Hoa", "2025-04-01", "avatar_nam"},
                {"6", "Dinh Thi Truc",  "DonPhong", "0901111006", "nhanvien_dtt@lalahouse.vn", "001075067890", "Hau Loc, Thanh Hoa",    "2025-05-01", "avatar_nu"},
                {"7", "Dinh Thi Ha",    "BaoVe",    "0901111007", "nhanvien_dth@lalahouse.vn", "001088078901", "Yen Dinh, Thanh Hoa",   "2025-06-01", "avatar_nu"},
        };
        for (String[] nv : nvs) {
            ContentValues cv = new ContentValues();
            cv.put("MaTK",       Integer.parseInt(nv[0]));
            cv.put("HoTen",      nv[1]);
            cv.put("ChucVu",     nv[2]);
            cv.put("SDT",        nv[3]);
            cv.put("Email",      nv[4]);
            cv.put("CCCD",       nv[5]);
            cv.put("DiaChi",     nv[6]);
            cv.put("NgayVaoLam", nv[7]);
            cv.put("Avatar",     nv[8]);
            db.insert("NhanVien", null, cv);
        }
    }

    // =========================================================================
    // SEED 9: PhanCongCa
    // Phân công tuần lặp cho 6 nhân viên (MaNV 2–7; Admin/MaNV 1 không phân ca)
    // Thu 1=T2, 2=T3, 3=T4, 4=T5, 5=T6, 6=T7, 7=CN
    // MaCa: Sang=1, Chieu=2, Toi=3
    // =========================================================================
    private void seedPhanCongCa(SQLiteDatabase db) {
        // {MaNV, MaCa, ThuTrongTuan}
        int[][] rows = {
                // NV 2 (Lê tân): T2-T4-T6 ca Sáng
                {2, 1, 1}, {2, 1, 3}, {2, 1, 5},
                // NV 3 (Lê tân): T3-T5-T7 ca Sáng
                {3, 1, 2}, {3, 1, 4}, {3, 1, 6},
                // NV 4 (Kế toán): T2-T3-T4-T5-T6 ca Chiều
                {4, 2, 1}, {4, 2, 2}, {4, 2, 3}, {4, 2, 4}, {4, 2, 5},
                // NV 5 (Dọn phòng): T2-T3-T4-T5-T6 ca Sáng
                {5, 1, 1}, {5, 1, 2}, {5, 1, 3}, {5, 1, 4}, {5, 1, 5},
                // NV 6 (Dọn phòng): T3-T5-T7 ca Chiều
                {6, 2, 2}, {6, 2, 4}, {6, 2, 6},
                // NV 7 (Bảo vệ): toàn tuần ca Tối
                {7, 3, 1}, {7, 3, 2}, {7, 3, 3}, {7, 3, 4}, {7, 3, 5}, {7, 3, 6}, {7, 3, 7},
        };
        for (int[] r : rows) {
            ContentValues cv = new ContentValues();
            cv.put("MaNV",         r[0]);
            cv.put("MaCa",         r[1]);
            cv.put("ThuTrongTuan", r[2]);
            db.insert("PhanCongCa", null, cv);
        }
    }

    // =========================================================================
    // SEED 10: KhachHang (12 khách hàng)
    // MaKH: 1..12
    // =========================================================================
    private void seedKhachHang(SQLiteDatabase db) {
        // {HoTen, SDT, Email, CCCD, DiaChi, NgaySinh, GioiTinh, SoLanThue, Avatar}
        String[][] khs = {
                {"Nguyen Thi Mai",   "0912345601", "mai.nguyen@gmail.com",  "030190001001", "10 Tran Hung Dao, Hoan Kiem, HN",  "1990-05-12", "Nu",  "5", "avatar_nu"},
                {"Tran Van Hung",    "0912345602", "hung.tran@gmail.com",   "038085001002", "22 Ly Thuong Kiet, Q10, HCM",      "1985-11-20", "Nam", "3", "avatar_nam"},
                {"Le Thi Lan",       "0912345603", "lan.le@yahoo.com",      "001092001003", "15 Nguyen Trai, Thanh Xuan, HN",   "1992-07-08", "Nu",  "2", "avatar_nu"},
                {"Pham Quoc Bao",    "0912345604", "bao.pham@gmail.com",    "079080001004", "88 Hoang Van Thu, Phu Nhuan, HCM", "1980-03-25", "Nam", "7", "avatar_nam"},
                {"Hoang Thi Thu",    "0912345605", "thu.hoang@hotmail.com", "001095001005", "5 Hang Bai, Hoan Kiem, HN",        "1995-09-14", "Nu",  "1", "avatar_nu"},
                {"Vu Minh Duc",      "0912345606", "duc.vu@gmail.com",      "034088001006", "40 Bach Dang, Hai Chau, Da Nang",  "1988-12-30", "Nam", "4", "avatar_nam"},
                {"Dang Thi Hoa",     "0912345607", "hoa.dang@gmail.com",    "001093001007", "27 Phan Chu Trinh, HK, HN",        "1993-04-18", "Nu",  "2", "avatar_nu"},
                {"Bui Van Thanh",    "0912345608", "thanh.bui@gmail.com",   "026087001008", "60 Tran Phu, Nha Trang, KH",       "1987-08-05", "Nam", "6", "avatar_nam"},
                {"Do Thi Ngoc",      "0912345609", "ngoc.do@gmail.com",     "001091001009", "3 Ly Tu Trong, Q1, HCM",           "1991-01-22", "Nu",  "3", "avatar_nu"},
                {"Nguyen Duc Kien",  "0912345610", "kien.nd@gmail.com",     "027096001010", "100 Le Duan, Hai Chau, Da Nang",   "1996-06-11", "Nam", "1", "avatar_nam"},
                {"Cao Thi Bich Van", "0912345611", "van.cao@gmail.com",     "001089001011", "18 Chua Lang, Dong Da, HN",        "1989-02-28", "Nu",  "4", "avatar_nu"},
                {"Ly Hoang Nam",     "0912345612", "nam.ly@gmail.com",      "079094001012", "55 Vo Thi Sau, Q3, HCM",           "1994-10-07", "Nam", "2", "avatar_nam"},
        };
        for (String[] kh : khs) {
            ContentValues cv = new ContentValues();
            cv.put("HoTen",      kh[0]);
            cv.put("SDT",        kh[1]);
            cv.put("Email",      kh[2]);
            cv.put("CCCD",       kh[3]);
            cv.put("DiaChi",     kh[4]);
            cv.put("NgaySinh",   kh[5]);
            cv.put("GioiTinh",   kh[6]);
            cv.put("SoLanThue",  Integer.parseInt(kh[7]));
            cv.put("Avatar",     kh[8]);
            db.insert("KhachHang", null, cv);
        }
    }

    // =========================================================================
    // SEED 11: Phong (12 phòng, trạng thái khớp với seedDatPhong bên dưới)
    //
    // Tổng quan trạng thái (nhất quán với DatPhong):
    //   Trong    : P101, P102, P204, P301, P304, P402
    //   DangThue : P103, P201, P203, P302
    //   DaDat    : P202, P401
    //
    // MaLoaiPhong: Standard=1, Deluxe=2, Suite=3
    // MaPhong AUTOINCREMENT sẽ là 1..12 theo thứ tự insert
    // =========================================================================
    private void seedPhong(SQLiteDatabase db) {
        // {MaLoaiPhong, TenPhong, GiaMoiDem, SucChua, DienTich, Tang, TrangThai, MoTa, HinhAnh}
        Object[][] phongs = {
                // --- Tầng 1 (Standard) ---
                {1, "P101", 550_000.0,  2, 22.0, 1, "Trong",    "Phong Standard view san vuon, thiet ke hien dai.",        "room_standard"},
                {1, "P102", 550_000.0,  2, 22.0, 1, "Trong",    "Phong Standard view duong pho, day du tien nghi co ban.", "room_standard"},
                {1, "P103", 550_000.0,  2, 24.0, 1, "DangThue", "Phong Standard goc, rong hon, co ban cong nho.",          "room_standard"},

                // --- Tầng 2 (Deluxe) ---
                {2, "P201", 950_000.0,  3, 30.0, 2, "DangThue", "Phong Deluxe view ho boi, noi that sang trong.",          "room_deluxe"},
                {2, "P202", 950_000.0,  3, 30.0, 2, "DaDat",    "Phong Deluxe cuoi tuan, uu tien gia dinh.",               "room_deluxe"},
                {2, "P203", 950_000.0,  3, 32.0, 2, "DangThue", "Phong Deluxe goc 2 mat thong, nhieu anh sang tu nhien.",  "room_deluxe"},
                {2, "P204", 950_000.0,  3, 30.0, 2, "Trong",    "Phong Deluxe phong cach toi gian, thich hop cong tac.",   "room_deluxe"},

                // --- Tầng 3 (Suite) ---
                {3, "P301", 1_900_000.0, 4, 55.0, 3, "Trong",    "Suite hang sang, phong khach rieng, bon tam jacuzzi.",   "room_suite"},
                {3, "P302", 1_900_000.0, 4, 55.0, 3, "DangThue", "Suite gia dinh, 2 phong ngu, bep nho tich hop.",         "room_suite"},
                {3, "P304", 1_850_000.0, 3, 50.0, 3, "Trong",    "Suite studio mo, view toan canh thanh pho.",             "room_suite"},

                // --- Tầng 4 (Deluxe cao cap) ---
                {2, "P401", 1_000_000.0, 3, 35.0, 4, "DaDat",    "Phong Deluxe tang thuong, view song Sai Gon.",           "room_deluxe_top"},
                {2, "P402", 1_000_000.0, 3, 35.0, 4, "Trong",    "Phong Deluxe tang thuong, ban cong lon.",                "room_deluxe_top"},
        };
        for (Object[] p : phongs) {
            ContentValues cv = new ContentValues();
            cv.put("MaLoaiPhong", (Integer) p[0]);
            cv.put("TenPhong",    (String)  p[1]);
            cv.put("GiaMoiDem",   (Double)  p[2]);
            cv.put("SucChua",     (Integer) p[3]);
            cv.put("DienTich",    (Double)  p[4]);
            cv.put("Tang",        (Integer) p[5]);
            cv.put("TrangThai",   (String)  p[6]);
            cv.put("MoTa",        (String)  p[7]);
            cv.put("HinhAnh",     (String)  p[8]);
            db.insert("Phong", null, cv);
        }
    }

    // =========================================================================
    // SEED 12: Phong_TienNghi (bảng nối N-N)
    // MaPhong 1..12, MaTienNghi: WiFi=1, TV=2, DieuHoa=3, TuLanh=4,
    //                             BonTam=5, BanLamViec=6, TuQuan=7
    // =========================================================================
    private void seedPhongTienNghi(SQLiteDatabase db) {
        // {MaPhong, MaTienNghi[]}
        int[][] rows = {
                // P101 Standard: WiFi, TV, DieuHoa, TuQuan
                {1, 1}, {1, 2}, {1, 3}, {1, 7},
                // P102 Standard: WiFi, TV, DieuHoa, TuQuan
                {2, 1}, {2, 2}, {2, 3}, {2, 7},
                // P103 Standard: WiFi, TV, DieuHoa, TuLanh, TuQuan
                {3, 1}, {3, 2}, {3, 3}, {3, 4}, {3, 7},
                // P201 Deluxe: WiFi, TV, DieuHoa, TuLanh, BanLamViec, TuQuan
                {4, 1}, {4, 2}, {4, 3}, {4, 4}, {4, 6}, {4, 7},
                // P202 Deluxe: WiFi, TV, DieuHoa, TuLanh, BanLamViec, TuQuan
                {5, 1}, {5, 2}, {5, 3}, {5, 4}, {5, 6}, {5, 7},
                // P203 Deluxe: WiFi, TV, DieuHoa, TuLanh, BanLamViec, TuQuan
                {6, 1}, {6, 2}, {6, 3}, {6, 4}, {6, 6}, {6, 7},
                // P204 Deluxe: WiFi, TV, DieuHoa, TuLanh, BanLamViec, TuQuan
                {7, 1}, {7, 2}, {7, 3}, {7, 4}, {7, 6}, {7, 7},
                // P301 Suite: tất cả 7 tiện nghi
                {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 6}, {8, 7},
                // P302 Suite: tất cả 7 tiện nghi
                {9, 1}, {9, 2}, {9, 3}, {9, 4}, {9, 5}, {9, 6}, {9, 7},
                // P304 Suite: tất cả 7 tiện nghi
                {10, 1}, {10, 2}, {10, 3}, {10, 4}, {10, 5}, {10, 6}, {10, 7},
                // P401 Deluxe+: WiFi, TV, DieuHoa, TuLanh, BonTam, BanLamViec, TuQuan
                {11, 1}, {11, 2}, {11, 3}, {11, 4}, {11, 5}, {11, 6}, {11, 7},
                // P402 Deluxe+: WiFi, TV, DieuHoa, TuLanh, BanLamViec, TuQuan
                {12, 1}, {12, 2}, {12, 3}, {12, 4}, {12, 6}, {12, 7},
        };
        for (int[] r : rows) {
            ContentValues cv = new ContentValues();
            cv.put("MaPhong",     r[0]);
            cv.put("MaTienNghi",  r[1]);
            db.insert("Phong_TienNghi", null, cv);
        }
    }

    // =========================================================================
    // SEED 13: DatPhong (14 đặt phòng)
    //
    // Mốc "hôm nay" = 17/06/2026
    //
    // Trạng thái phân bố (phải khớp seedPhong):
    //   DangO      → MaPhong 3 (P103), 4 (P201), 6 (P203), 9 (P302)
    //   DaDat      → MaPhong 5 (P202), 11 (P401)  [check-in tương lai]
    //   DaTraPhong → các đặt phòng đã trả về phòng Trong
    //   DaHuy      → 1-2 đặt phòng đã hủy
    //   SapDen     → 2-3 đặt phòng sắp tới (phòng Trong sẽ chuyển DaDat khi
    //                 nhân viên bấm "Đặt" – trong seed ta để phòng = DaDat cho
    //                 nhất quán vì SapDen = đã đặt nhưng chưa check-in)
    //
    // Lưu ý:
    //   - MaPhong 5 & 11 có TrangThai = 'DaDat' trong Phong → bao gồm cả
    //     DatPhong trạng thái 'SapDen' (chưa check-in) và 'DaDat' (đã đặt
    //     online, chưa check-in, phòng đang giữ chỗ).
    //   - SoDem được tính chính xác = (CheckOut - CheckIn) tính theo ngày.
    //   - MaNV = 2 (lê tân Bich) là người xử lý chính.
    // =========================================================================
    private void seedDatPhong(SQLiteDatabase db) {
        // {MaKH, MaPhong, MaNV, NgayCI, NgayCO, SoKhach, SoDem, TrangThai, PhuongThuc, GhiChu, NgayTao}
        Object[][] dps = {
                // ---- ĐÃ TRẢ PHÒNG (lịch sử) ----
                {1,  1, 2, "2026-06-01", "2026-06-04", 2, 3, "DaTraPhong", "CK",    "Khach quen, uu tien phong san vuon.", "2026-05-28"},
                {2,  2, 2, "2026-06-05", "2026-06-08", 2, 3, "DaTraPhong", "TM",    null,                                  "2026-06-04"},
                {4,  7, 3, "2026-06-08", "2026-06-12", 2, 4, "DaTraPhong", "CK",    "Khach cong tac dai han.",             "2026-06-07"},
                {8, 10, 2, "2026-06-10", "2026-06-14", 2, 4, "DaTraPhong", "TM",    null,                                  "2026-06-09"},
                {11, 8, 3, "2026-06-12", "2026-06-15", 3, 3, "DaTraPhong", "VNPAY", "Doan gia dinh, yeu cau jacuzzi.",     "2026-06-11"},

                // ---- ĐÃ HỦY ----
                {5,  1, 2, "2026-06-20", "2026-06-22", 1, 2, "DaHuy",      "TM",    "Khach doi lich dot xuat.",            "2026-06-15"},
                {10, 2, 2, "2026-06-25", "2026-06-27", 2, 2, "DaHuy",      "CK",    null,                                  "2026-06-16"},

                // ---- ĐANG Ở (check-in trước 17/06, check-out sau 17/06) ----
                {3,  3, 2, "2026-06-15", "2026-06-19", 2, 4, "DangO",      "TM",    null,                                  "2026-06-14"},
                {6,  4, 3, "2026-06-16", "2026-06-20", 2, 4, "DangO",      "CK",    "Khach doanh nhan, can hoa don VAT.",  "2026-06-15"},
                {9,  6, 2, "2026-06-14", "2026-06-18", 3, 4, "DangO",      "VNPAY", null,                                  "2026-06-13"},
                {12, 9, 3, "2026-06-13", "2026-06-18", 4, 5, "DangO",      "TM",    "Gia dinh 4 nguoi, 2 tre em.",         "2026-06-12"},

                // ---- SẮP ĐẾN (check-in sau 17/06) – Phòng đang ở trạng thái 'DaDat' ----
                {7,  5, 2, "2026-06-19", "2026-06-22", 2, 3, "SapDen",     "CK",    null,                                  "2026-06-17"},
                {2, 11, 3, "2026-06-20", "2026-06-24", 3, 4, "SapDen",     "TM",    "Tang bao nuoc chao mung.",            "2026-06-17"},
                {4, 12, 2, "2026-06-22", "2026-06-25", 2, 3, "SapDen",     "CK",    null,                                  "2026-06-17"},
        };
        for (Object[] dp : dps) {
            ContentValues cv = getContentValues(dp);
            db.insert("DatPhong", null, cv);
        }
    }

    @NonNull
    private static ContentValues getContentValues(Object[] dp) {
        ContentValues cv = new ContentValues();
        cv.put("MaKH",                      (Integer) dp[0]);
        cv.put("MaPhong",                   (Integer) dp[1]);
        cv.put("MaNV",                      (Integer) dp[2]);
        cv.put("NgayCheckIn",               (String)  dp[3]);
        cv.put("NgayCheckOut",              (String)  dp[4]);
        cv.put("SoLuongKhach",              (Integer) dp[5]);
        cv.put("SoDem",                     (Integer) dp[6]);
        cv.put("TrangThai",                 (String)  dp[7]);
        cv.put("PhuongThucThanhToan",       (String)  dp[8]);
        if (dp[9] != null) cv.put("GhiChu", (String)  dp[9]);
        cv.put("NgayTao",                   (String)  dp[10]);
        return cv;
    }

    // =========================================================================
    // SEED 14: CheckInOut
    // Chỉ ghi log cho các DatPhong đã check-in (DangO + DaTraPhong).
    // Thứ tự MaDatPhong theo seedDatPhong:
    //   1..5  = DaTraPhong  → có cả CheckIn + CheckOut
    //   6..7  = DaHuy       → không có log
    //   8..11 = DangO       → chỉ có CheckIn
    //   12..14= SapDen      → không có log
    // =========================================================================
    private void seedCheckInOut(SQLiteDatabase db) {
        // {MaDatPhong, MaNV, Loai, ThoiGian}
        Object[][] logs = {
                // DaTraPhong (MaDatPhong 1..5): CheckIn + CheckOut
                {1, 2, "CheckIn",  "2026-06-01 13:45:00"},
                {1, 2, "CheckOut", "2026-06-04 11:20:00"},
                {2, 3, "CheckIn",  "2026-06-05 14:10:00"},
                {2, 3, "CheckOut", "2026-06-08 10:55:00"},
                {3, 2, "CheckIn",  "2026-06-08 15:00:00"},
                {3, 3, "CheckOut", "2026-06-12 11:30:00"},
                {4, 3, "CheckIn",  "2026-06-10 13:20:00"},
                {4, 2, "CheckOut", "2026-06-14 12:00:00"},
                {5, 2, "CheckIn",  "2026-06-12 14:50:00"},
                {5, 3, "CheckOut", "2026-06-15 10:40:00"},
                // DangO (MaDatPhong 8..11): chỉ CheckIn
                {8, 2, "CheckIn",  "2026-06-15 14:30:00"},
                {9, 3, "CheckIn",  "2026-06-16 15:15:00"},
                {10, 2, "CheckIn", "2026-06-14 13:00:00"},
                {11, 3, "CheckIn", "2026-06-13 14:00:00"},
        };
        for (Object[] log : logs) {
            ContentValues cv = new ContentValues();
            cv.put("MaDatPhong",     (Integer) log[0]);
            cv.put("MaNV",           (Integer) log[1]);
            cv.put("Loai",           (String)  log[2]);
            cv.put("ThoiGian",       (String)  log[3]);
            // GhiChuDacBiet = null (theo spec RM.md)
            db.insert("CheckInOut", null, cv);
        }
    }

    // =========================================================================
    // SEED 15: HoaDon
    //
    // Chỉ tạo hóa đơn cho DatPhong đã có dịch vụ thực tế:
    //   DaTraPhong (MaDatPhong 1..5) → DaThanhToan
    //   DangO (MaDatPhong 8..11)     → ChuaThanhToan (thanh toán khi trả phòng)
    //   DaHuy / SapDen               → không có hóa đơn
    //
    // Công thức: TongCong = TienPhong + PhuThuDichVu - GiamGia
    //
    // Giá phòng/đêm:
    //   MaPhong 3 (P103 Standard) = 550.000 x SoDem
    //   MaPhong 4 (P201 Deluxe)   = 950.000 x SoDem
    //   MaPhong 6 (P203 Deluxe)   = 950.000 x SoDem
    //   MaPhong 9 (P302 Suite)    = 1.900.000 x SoDem
    //   ...
    // =========================================================================
    private void seedHoaDon(SQLiteDatabase db) {
        // {MaDatPhong, NgayLap, TienPhong, PhuThu, GiamGia, TongCong, TrangThai, PhuongThucTT, NgayTT, MaNV}
        // TienPhong = GiaMoiDem x SoDem (theo seedPhong + seedDatPhong)
        Object[][] hds = {
                {1,  "2026-06-04", 1_650_000.0, 150_000.0, 0.0,       1_800_000.0, "DaThanhToan",   "CK",    "2026-06-04", 2},
                {2,  "2026-06-08", 1_650_000.0, 50_000.0,  0.0,       1_700_000.0, "DaThanhToan",   "TM",    "2026-06-08", 3},
                {3,  "2026-06-12", 3_800_000.0, 300_000.0, 200_000.0, 3_900_000.0, "DaThanhToan",   "CK",    "2026-06-12", 2},
                {4,  "2026-06-14", 7_400_000.0, 500_000.0, 0.0,       7_900_000.0, "DaThanhToan",   "TM",    "2026-06-14", 3},
                {5,  "2026-06-15", 5_700_000.0, 700_000.0, 500_000.0, 5_900_000.0, "DaThanhToan",   "VNPAY", "2026-06-15", 2},
                {8,  "2026-06-17", 2_200_000.0, 100_000.0, 0.0,       2_300_000.0, "ChuaThanhToan", null,    null,         2},
                {9,  "2026-06-17", 3_800_000.0, 200_000.0, 0.0,       4_000_000.0, "ChuaThanhToan", null,    null,         3},
                {10, "2026-06-17", 3_800_000.0, 380_000.0, 0.0,       4_180_000.0, "ChuaThanhToan", null,    null,         2},
                {11, "2026-06-17", 9_500_000.0, 500_000.0, 200_000.0, 9_800_000.0, "ChuaThanhToan", null,    null,         3},
        };
        for (Object[] hd : hds) {
            ContentValues cv = new ContentValues();
            cv.put("MaDatPhong",                      (Integer) hd[0]);
            cv.put("NgayLap",                         (String)  hd[1]);
            cv.put("TienPhong",                       (Double)  hd[2]);
            cv.put("PhuThuDichVu",                    (Double)  hd[3]);
            cv.put("GiamGia",                         (Double)  hd[4]);
            cv.put("TongCong",                        (Double)  hd[5]);
            cv.put("TrangThai",                       (String)  hd[6]);
            if (hd[7] != null) cv.put("PhuongThucTT", (String) hd[7]);
            if (hd[8] != null) cv.put("NgayTT",       (String) hd[8]);
            cv.put("MaNV",                            (Integer) hd[9]);
            db.insert("HoaDon", null, cv);
        }
    }

    // =========================================================================
    // SEED 16: ChiTietPhuThu
    // Tổng các dòng phụ thu của mỗi HoaDon phải khớp PhuThuDichVu ở seedHoaDon.
    //
    // MaHD AUTOINCREMENT theo thứ tự insert ở seedHoaDon:
    //   HĐ 1 = MaDatPhong 1  → PhuThu tổng 150.000
    //   HĐ 2 = MaDatPhong 2  → PhuThu tổng  50.000
    //   HĐ 3 = MaDatPhong 3  → PhuThu tổng 300.000
    //   HĐ 4 = MaDatPhong 4  → PhuThu tổng 500.000
    //   HĐ 5 = MaDatPhong 5  → PhuThu tổng 700.000
    //   HĐ 6 = MaDatPhong 8  → PhuThu tổng 100.000
    //   HĐ 7 = MaDatPhong 9  → PhuThu tổng 200.000
    //   HĐ 8 = MaDatPhong 10 → PhuThu tổng 380.000
    //   HĐ 9 = MaDatPhong 11 → PhuThu tổng 500.000
    // =========================================================================
    private void seedChiTietPhuThu(SQLiteDatabase db) {
        // {MaHD, TenPhuThu, SoTien}
        Object[][] items = {
                // HĐ 1: 150.000
                {1, "Dich vu don phong them",  100_000.0},
                {1, "Nuoc uong mini-bar",      50_000.0},
                // HĐ 2: 50.000
                {2, "Phi giu xe may",          50_000.0},
                // HĐ 3: 300.000
                {3, "Bua sang buffet x2",      200_000.0},
                {3, "Thue xe dua don san bay", 100_000.0},
                // HĐ 4: 500.000
                {4, "Bua sang buffet x2",      200_000.0},
                {4, "Dich vu spa",             200_000.0},
                {4, "Nuoc uong mini-bar",      100_000.0},
                // HĐ 5: 700.000
                {5, "Bua sang buffet x3",      300_000.0},
                {5, "Thue xe dua don san bay", 200_000.0},
                {5, "Dich vu spa",             200_000.0},
                // HĐ 6: 100.000
                {6, "Phi giu xe may",          50_000.0},
                {6, "Nuoc uong mini-bar",      50_000.0},
                // HĐ 7: 200.000
                {7, "Bua sang buffet x2",      200_000.0},
                // HĐ 8: 380.000 (phụ thu VAT 10% trên 3.800.000)
                {8, "Thue GTGT (VAT 10%)",     380_000.0},
                // HĐ 9: 500.000
                {9, "Bua sang buffet x4",      200_000.0},
                {9, "Dich vu spa",             200_000.0},
                {9, "Nuoc uong mini-bar",      100_000.0},
        };
        for (Object[] item : items) {
            ContentValues cv = new ContentValues();
            cv.put("MaHD",      (Integer) item[0]);
            cv.put("TenPhuThu", (String)  item[1]);
            cv.put("SoTien",    (Double)  item[2]);
            db.insert("ChiTietPhuThu", null, cv);
        }
    }

    // =========================================================================
    // SEED 17: ThongBao (15 thông báo cho các TaiKhoan 1..4)
    // MaTK: admin=1, letan01=2, letan02=3, ketoan01=4
    // DaDoc: 0=chưa đọc, 1=đã đọc
    // =========================================================================
    private void seedThongBao(SQLiteDatabase db) {
        // {MaTK, NoiDung, DaDoc, ThoiGian}
        Object[][] tbs = {
                // Thông báo cho Admin (MaTK=1)
                {1, "He thong da khoi dong thanh cong. Chao mung tro lai, Admin!",           1, "2026-06-17 07:00:00"},
                {1, "Phong P302 check-in thanh cong luc 14:00 ngay 13/06.",                  1, "2026-06-13 14:05:00"},
                {1, "Khach hang Nguyen Thi Mai da huy dat phong ngay 17/06.",                0, "2026-06-17 09:30:00"},
                {1, "Co 3 dat phong sap den trong 3 ngay toi.",                              0, "2026-06-17 08:00:00"},
                // Thông báo cho Lễ tân 1 (MaTK=2)
                {2, "Khach Tran Van Hung check-in Phong P102 luc 14:10 ngay 05/06.",         1, "2026-06-05 14:12:00"},
                {2, "Nhan viec: Huong dan khach Phong P103 lam thu tuc check-in luc 14:30.", 1, "2026-06-15 14:25:00"},
                {2, "Nhac nho: Khach Phong P201 check-out ngay 20/06/2026.",                 0, "2026-06-17 07:30:00"},
                {2, "Don dat phong moi tu khach Ly Hoang Nam – Phong P402 tu 22/06.",        0, "2026-06-17 10:00:00"},
                {2, "Phong P103 hien dang su dung, khong xep them khach.",                   0, "2026-06-15 15:00:00"},
                // Thông báo cho Lễ tân 2 (MaTK=3)
                {3, "Ban giao ca: Khach P302 (Gia dinh Ly Hoang Nam) can ho tro them chan.", 1, "2026-06-13 22:05:00"},
                {3, "Nhac nho check-out: Phong P203 ngay 18/06/2026 luc 12:00.",             0, "2026-06-17 07:00:00"},
                {3, "Dat phong moi: Tran Van Hung – Phong P401 tu 20/06.",                   0, "2026-06-17 10:05:00"},
                // Thông báo cho Kế toán (MaTK=4)
                {4, "Hoa don #9 (MaDatPhong 11 – P302) chua thanh toan. Tong: 9.800.000 d.", 0, "2026-06-17 08:00:00"},
                {4, "Bao cao doanh thu thang 6/2026 san sang de xuat.",                      0, "2026-06-17 09:00:00"},
                {4, "Da xac nhan thanh toan hoa don #5 (P301 – Suite): 5.900.000 d.",        1, "2026-06-15 10:45:00"},
        };
        for (Object[] tb : tbs) {
            ContentValues cv = new ContentValues();
            cv.put("MaTK",     (Integer) tb[0]);
            cv.put("NoiDung",  (String)  tb[1]);
            cv.put("DaDoc",    (Integer) tb[2]);
            cv.put("ThoiGian", (String)  tb[3]);
            db.insert("ThongBao", null, cv);
        }
    }
}