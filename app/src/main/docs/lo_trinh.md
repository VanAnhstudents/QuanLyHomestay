# LỘ TRÌNH CODE – ỨNG DỤNG QUẢN LÝ HOMESTAY "LALA HOUSE"

> **Ngày tham chiếu:** 18/06/2026  
> **Stack:** Android Studio · Java · SQLite (SQLiteOpenHelper) · Material Components  
> **Kiến trúc:** Model → DAO → Repository → UI (Activity / Fragment)  
> **Phân chia nhóm:** 4 phần — 1 phần chung + 3 thành viên

---

## TỔNG QUAN DEMO MỤC TIÊU

Khi hoàn thành, ứng dụng phải demo được luồng sau **không lỗi**:

1. Mở app → màn hình Login → đăng nhập bằng `admin / Admin@123`
2. Vào Trang chủ → thấy 4 KPI card (Tổng phòng / Phòng trống / Đang thuê / Doanh thu hôm nay)
3. Tab Phòng → danh sách 12 phòng, lọc theo trạng thái
4. Tab Đặt phòng → danh sách 14 đặt phòng, nhấn vào xem chi tiết, bấm Check-in / Check-out
5. Tab Khách hàng → danh sách 12 khách, xem chi tiết, thêm mới
6. Menu "Hơn nữa" → Thanh toán → danh sách 9 hóa đơn; Nhân viên → 7 nhân viên; Tài khoản; Thống kê
7. Phân quyền: đăng xuất → đăng nhập `letan01 / Letan@123` → các nút bị ẩn đúng theo role

---

## CÁC BƯỚC NỀN TẢNG CHUNG (Làm trước khi phân chia)

> Những bước này **cả nhóm cần đồng thuận và merge sớm nhất** vì mọi module đều phụ thuộc.

| # | Việc cần làm | File liên quan | Ghi chú |
|---|---|---|---|
| 0 | ✅ **DatabaseHelper.java** đã xong | `data/DatabaseHelper.java` | Không cần sửa thêm |
| 1 | Tạo 16 **Model (POJO)** — 1 class/bảng | `model/*.java` | Chỉ field + constructor + getter/setter |
| 2 | Khai báo **build.gradle** — thêm Material, Navigation, MPAndroidChart | `build.gradle.kts` | `implementation 'com.google.android.material:...'` |
| 3 | Tạo `colors.xml`, `dimens.xml`, `styles.xml`, `themes.xml` | `res/values/` | Sao chép đúng giá trị từ `ux_ui.md` Phần 1 |
| 4 | Tạo các drawable shape: `bg_button_primary`, `bg_input_normal`, `bg_card_white`, `bg_badge_pill` v.v. | `res/drawable/` | Làm theo bảng 1.4.2 trong `ux_ui.md` |
| 5 | Tạo `SessionManager.java` | `util/SessionManager.java` | Lưu MaTK + VaiTro vào SharedPreferences, cung cấp `login()`, `logout()`, `getVaiTro()`, `isLoggedIn()` |
| 6 | Tạo `PermissionHelper.java` | `util/PermissionHelper.java` | Đọc bảng `PhanQuyen_VaiTro` → kiểm tra `hasAccess(module, minQuyen)` — **toàn bộ RBAC gọi vào đây** |
| 7 | Tạo `layout_appbar.xml`, `layout_bottom_nav.xml`, `layout_breadcrumb.xml` | `res/layout/` | Include vào mọi Fragment |
| 8 | Cấu hình `activity_main.xml` + `MainActivity.java` | — | FrameLayout chứa Fragment + BottomNavigationView; dùng Fragment transaction thay NavController nếu chưa quen Navigation Component |

---

## LỘ TRÌNH 0 – PHẦN CHUNG (Đăng nhập · Đăng ký · Trang chủ)

> **Người làm:** Cả nhóm thống nhất, 1 người đại diện commit, hoặc phân cho người rảnh nhất sau khi xong bước nền tảng.

### Giai đoạn A – Tầng Data

**A1. TaiKhoanDAO.java**
- `TaiKhoan findByUsername(String username)` — dùng cho Login
- `TaiKhoan findById(int maTK)`
- `long insert(TaiKhoan tk)` — dùng cho Register
- `boolean isUsernameTaken(String username)` — kiểm tra trùng khi đăng ký

**A2. AuthRepository.java** (đã có file, cần implement)
- `TaiKhoan login(String username, String password)` — trả null nếu sai
- `boolean register(String tenDangNhap, String email, String matKhau)` — gọi `TaiKhoanDAO.insert()`

---

### Giai đoạn B – Tầng UI

**B1. LoginActivity.java + activity_login.xml**
- Layout theo spec A1 trong `ux_ui.md`
- Nhấn "Đăng nhập" → gọi `AuthRepository.login()` → nếu thành công: `SessionManager.login()` → chuyển sang `MainActivity`
- Nếu sai: hiện SnackBar "Tên đăng nhập hoặc mật khẩu không đúng"
- CheckBox "Ghi nhớ" → lưu username vào SharedPreferences để điền sẵn lần sau

**B2. RegisterActivity.java + activity_register.xml**
- Layout theo spec A2 trong `ux_ui.md`
- Validate: không bỏ trống, email đúng định dạng, mật khẩu xác nhận khớp
- Gọi `AuthRepository.register()` → SnackBar thành công → finish() quay về Login

**B3. HomeFragment.java + fragment_home.xml**
- Layout theo spec A6 trong `ux_ui.md`
- Tính 4 KPI: đọc từ `PhongDAO.countByTrangThai()` và `HoaDonDAO.getTotalTodayRevenue()`
- RecyclerView "Đặt phòng hôm nay": lấy DatPhong có `NgayCheckIn = today` hoặc `TrangThai = DangO`
- RecyclerView "Hoạt động gần đây": lấy 5 CheckInOut gần nhất

**B4. Thông báo (badge số đỏ trên App Bar)**
- `ThongBaoDAO.countUnread(maTK)` → set số lên icon chuông

---

### Kiểm tra hoàn thành
- [ ] Đăng nhập thành công với 4 vai trò trong tài khoản (admin, lễ tân, kế toán, nhân viên)
- [ ] Đăng nhập sai hiện thông báo lỗi
- [ ] Trang chủ hiển thị đúng số KPI (Tổng: 12, Trống: 6, Đang thuê: 4, Doanh thu: tính từ HoaDon ngày 17/06)
- [ ] BottomNav chuyển tab không crash

---

## LỘ TRÌNH 1 – NGUYENVANANH (Khách hàng · Nhân viên · Tài khoản)

### Giai đoạn A – Tầng Data

**A1. KhachHangDAO.java**
- `List<KhachHang> getAll()`
- `List<KhachHang> search(String keyword)` — tìm theo HoTen / SDT / CCCD
- `KhachHang findById(int maKH)`
- `long insert(KhachHang kh)`
- `int update(KhachHang kh)` — trả số dòng bị ảnh hưởng
- `int delete(int maKH)`
- `void incrementSoLanThue(int maKH)` — gọi khi DatPhong → DaTraPhong

**A2. NhanVienDAO.java**
- `List<NhanVien> getAll()`
- `List<NhanVien> search(String keyword)`
- `NhanVien findById(int maNV)`
- `NhanVien findByMaTK(int maTK)`
- `long insert(NhanVien nv)`
- `int update(NhanVien nv)`
- `int delete(int maNV)`

**A3. TaiKhoanDAO.java** (mở rộng từ bước Chung)
- `List<TaiKhoan> getAll()`
- `List<TaiKhoan> filterByVaiTro(String vaiTro)`
- `int updateTrangThai(int maTK, String trangThai)` — khoá / mở tài khoản
- `int delete(int maTK)`

**A4. PhanCongCaDAO.java**
- `List<PhanCongCa> getByNhanVien(int maNV)`
- `void replaceAll(int maNV, List<PhanCongCa> danhSach)` — xóa hết rồi insert lại

**A5. CustomerRepository.java, StaffRepository.java, PermissionRepository.java** (đã có file, implement gọi DAO tương ứng)

---

### Giai đoạn B – Tầng UI

**B1. Module Khách hàng (D1 → D2 → D3)**

`fragment_customer_list.xml` + `CustomerListFragment.java`
- RecyclerView dùng `item_customer_row.xml` (avatar initials + tên + SĐT)
- SearchBar lọc realtime qua `TextWatcher`
- FAB "+ Thêm" → ẩn nếu `PermissionHelper.hasAccess("QuanLyKhachHang", "ToanQuyen")` = false

`fragment_customer_detail.xml` + `CustomerDetailFragment.java`
- Hiện 5 dòng thông tin, lịch sử 3 đặt phòng gần nhất (JOIN KhachHang + DatPhong)
- Row nút Sửa/Xóa → ẩn nếu ChiXem

`fragment_customer_add_edit.xml` + `CustomerAddEditFragment.java`
- Dùng chung cho thêm mới và chỉnh sửa (truyền `maKH = -1` để phân biệt)
- Validate không bỏ trống HoTen, SDT

**B2. Module Nhân viên (F1 → F2 → F3 → F4)**

`fragment_staff_list.xml` + `StaffListFragment.java`
- item = `item_staff_row.xml` (avatar + tên + chức vụ + SĐT)
- FAB ẩn nếu không phải Admin

`fragment_staff_detail.xml` + `StaffDetailFragment.java`
- Hiện thông tin + bảng ca làm việc (7 ô CheckBox dạng lưới đọc-only)
- Button "Phân công" → mở F4; Row Sửa/Xóa → chỉ Admin

`fragment_staff_add_edit.xml` + `StaffAddEditFragment.java`
- Tạo NhanVien + TaiKhoan liên kết trong 1 transaction

`fragment_shift_assignment.xml` + `ShiftAssignmentFragment.java`
- Bảng 3 hàng (Sáng/Chiều/Tối) × 7 cột (T2–CN) với CheckBox
- Load hiện trạng từ `PhanCongCaDAO.getByNhanVien()`
- Nút "Lưu" → gọi `replaceAll()`

**B3. Module Tài khoản (E1 → E2 → E3)** — chỉ Admin truy cập

`fragment_account_list.xml` + `AccountListFragment.java`
- Filter chips: Tất cả / Admin / Nhân viên / Đã khóa
- item = `item_account_row.xml` (avatar + tên + email + badge role + icon 3 chấm)
- PopupMenu từ icon 3 chấm: Sửa / Khóa (toggle) / Xóa

`fragment_account_detail.xml` + `AccountDetailFragment.java`
- 4 tab thủ công chọn vai trò
- RecyclerView 8 dòng module, mỗi dòng có Dropdown quyền (MaterialAutoCompleteTextView)
- Khi chọn tab Admin: disable tất cả dropdown, set "Toàn quyền"
- Nút "Lưu" → cập nhật `PhanQuyen_VaiTro` cho tất cả 8 module

`fragment_account_add_edit.xml` + `AccountAddEditFragment.java`
- EditText ẩn/hiện trường mật khẩu tùy mode thêm/sửa

---

### Kiểm tra hoàn thành
- [ ] Tìm kiếm khách hàng theo tên/SĐT hoạt động
- [ ] Thêm/sửa/xóa khách hàng → danh sách cập nhật ngay
- [ ] Phân công ca → lưu đúng vào DB, màn F2 hiển thị lại đúng
- [ ] Khoá tài khoản → login với tài khoản đó bị từ chối
- [ ] Role Kế toán đăng nhập: FAB thêm khách hàng ẩn

---

## LỘ TRÌNH 2 – DINHTHIHA (Quản lý phòng · Đặt phòng)

### Giai đoạn A – Tầng Data

**A1. PhongDAO.java**
- `List<Phong> getAll()`
- `List<Phong> filterByTrangThai(String trangThai)` — Trong / DangThue / DaDat
- `List<Phong> search(String keyword)` — tìm theo TenPhong
- `List<Phong> getAvailable()` — chỉ lấy TrangThai = 'Trong' (cho form đặt phòng)
- `Phong findById(int maPhong)`
- `long insert(Phong phong)`
- `int update(Phong phong)`
- `int delete(int maPhong)`
- `int updateTrangThai(int maPhong, String trangThai)` — gọi khi Check-in/Check-out

**A2. LoaiPhongDAO.java**
- `List<LoaiPhong> getAll()` — populate Spinner trong form thêm phòng

**A3. PhongTienNghiDAO.java**
- `List<TienNghi> getByPhong(int maPhong)`
- `void replaceAll(int maPhong, List<Integer> maTienNghiList)`

**A4. TienNghiDAO.java**
- `List<TienNghi> getAll()` — populate CheckBox tiện nghi

**A5. DatPhongDAO.java**
- `List<DatPhong> getAll()`
- `List<DatPhong> filterByTrangThai(String trangThai)`
- `List<DatPhong> search(String keyword)` — tìm theo tên KH / mã đặt phòng
- `DatPhong findById(int maDatPhong)`
- `long insert(DatPhong dp)`
- `int update(DatPhong dp)`
- `int updateTrangThai(int maDatPhong, String trangThai)`
- `int delete(int maDatPhong)`
- `List<DatPhong> getByPhong(int maPhong)` — lịch sử đặt của 1 phòng
- `List<DatPhong> getByKhachHang(int maKH)` — lịch sử của 1 khách

**A6. CheckInOutDAO.java**
- `long insertLog(int maDatPhong, int maNV, String loai)` — tự lấy `now()` cho ThoiGian
- `List<CheckInOut> getByDatPhong(int maDatPhong)`
- `List<CheckInOut> getRecent(int limit)` — cho widget "Hoạt động gần đây" ở Home

**A7. RoomRepository.java, BookingRepository.java** (đã có file, implement)

---

### Giai đoạn B – Tầng UI

**B1. Module Phòng (B1 → B2 → B3)**

`fragment_room_list.xml` + `RoomListFragment.java`
- RecyclerView dùng `item_room_card.xml` (ảnh + tên + loại + giá + badge trạng thái)
- Filter chips: Tất cả / Trống / Đang thuê / Đã đặt
- SearchBar tìm theo TenPhong
- FAB + nút "Thêm phòng" → chỉ Admin thấy

`fragment_room_detail.xml` + `RoomDetailFragment.java`
- Header ảnh 220dp + overlay gradient + back button + badge trạng thái
- Card thông tin: tên, loại, giá, sức chứa, diện tích, tầng
- HorizontalScrollView tiện nghi (chip)
- Row nút cố định dưới: "Chỉnh sửa" (ẩn nếu ChiXem) + "Đặt phòng" (ẩn nếu phòng không Trong)

`fragment_room_add_edit.xml` + `RoomAddEditFragment.java`
- Spinner LoaiPhong + CheckBox tiện nghi (2–3 item/hàng)
- Khi Lưu: insert/update Phong + gọi `PhongTienNghiDAO.replaceAll()`

**B2. Module Đặt phòng (C1 → C2 → C3)**

`fragment_booking_list.xml` + `BookingListFragment.java`
- item = `item_booking_card.xml` (3 hàng: mã+ngày / tên KH+phòng / CI→CO + badge)
- Filter chips: Tất cả / Sắp đến / Đang ở / Đã trả phòng / Đã hủy
- FAB ẩn nếu không phải Admin / Lễ tân

`fragment_booking_detail.xml` + `BookingDetailFragment.java`  
→ **Màn hình phức tạp nhất, cần chú ý:**
- ScrollView nội dung + Row nút cố định dưới (dùng RelativeLayout root)
- Row 6A: Sửa / Xóa; Row 6B: Check-in / Check-out
- Logic ẩn/hiện 4 nút theo bảng trạng thái trong `ux_ui.md` C2
- Khi nhấn **Check-in:**
  1. `DatPhongDAO.updateTrangThai(maDatPhong, "DangO")`
  2. `PhongDAO.updateTrangThai(maPhong, "DangThue")`
  3. `CheckInOutDAO.insertLog(maDatPhong, maNVhienTai, "CheckIn")`
  4. Refresh badge + Row nút + hiện Toast thành công
- Khi nhấn **Check-out:**
  1. `DatPhongDAO.updateTrangThai(maDatPhong, "DaTraPhong")`
  2. `PhongDAO.updateTrangThai(maPhong, "Trong")`
  3. `CheckInOutDAO.insertLog(maDatPhong, maNVhienTai, "CheckOut")`
  4. `KhachHangDAO.incrementSoLanThue(maKH)`
  5. Refresh + Toast
- Role KeToan / NhanVien: ẩn toàn bộ Row 6, thay bằng TextView read-only

`fragment_booking_add_edit.xml` + `BookingAddEditFragment.java`
- AutoComplete tìm KhachHang (realtime)
- Spinner chỉ hiện phòng TrangThai = 'Trong'
- DatePicker CI/CO → tự tính SoDem + TienPhong realtime
- Khi lưu: insert DatPhong + cập nhật TrangThai Phong → 'DaDat'

---

### Kiểm tra hoàn thành
- [ ] Filter phòng theo trạng thái đúng số lượng (Trống: 6, Đang thuê: 4, Đã đặt: 2)
- [ ] Thêm phòng mới → xuất hiện trong danh sách
- [ ] Check-in thành công → badge phòng + badge đặt phòng cập nhật ngay
- [ ] Check-out → phòng trở về "Trống", SoLanThue của khách tăng 1
- [ ] Role LeTan: không thấy nút Thêm phòng (FAB B1 ẩn), nhưng thấy FAB Thêm đặt phòng (C1)

---

## LỘ TRÌNH 3 – DINHTHI​TRUC (Thống kê & Báo cáo · Quản lý Thanh toán)

### Giai đoạn A – Tầng Data

**A1. HoaDonDAO.java**
- `List<HoaDon> getAll()`
- `List<HoaDon> filterByTrangThai(String trangThai)`
- `HoaDon findById(int maHD)`
- `HoaDon findByDatPhong(int maDatPhong)`
- `long insert(HoaDon hd)`
- `int updateTrangThai(int maHD, String trangThai)` — xác nhận đã thanh toán
- `double getTotalRevenueByDate(String date)` — dùng cho KPI Home
- `double getTotalRevenueByDateRange(String from, String to)` — dùng cho báo cáo
- `List<Object[]> getRevenueByDay(String from, String to)` — trả [{ngay, tong}] cho BarChart

**A2. ChiTietPhuThuDAO.java**
- `List<ChiTietPhuThu> getByHoaDon(int maHD)`
- `long insert(ChiTietPhuThu ct)`
- `int delete(int maChiTiet)`

**A3. StatisticsRepository.java** (đã có file, implement)
- `int getTotalRooms()` — đếm tất cả Phong
- `int getRoomsByTrangThai(String trangThai)`
- `double getRevenueToday()`
- `double getRevenueByRange(String from, String to)`
- `int getTotalBookingsByRange(String from, String to)`
- `double getOccupancyRateByRange(String from, String to)` — (tổng đêm phòng đã thuê) / (tổng phòng × số ngày)
- `List<Object[]> getTopRoomsByRevenue(int limit)` — [{tenPhong, doanhThu}]

**A4. InvoiceRepository.java** (đã có file, implement)

---

### Giai đoạn B – Tầng UI

**B1. Module Thanh toán (G1 → G2 → G3)**

`fragment_invoice_list.xml` + `InvoiceListFragment.java`
- Row tóm tắt nhanh ở đầu: "Tổng hôm nay: X" + "Số hóa đơn: N"
- item = `item_invoice_row.xml` (icon + mã HĐ + ngày / tên KH + phòng / tổng tiền + badge)
- Filter chips: Tất cả / Đã thanh toán / Chưa thanh toán / Hoàn tiền

`fragment_invoice_detail.xml` + `InvoiceDetailFragment.java`
- Card tổng quan HĐ (mã, ngày, badge)
- Card đặt phòng (tên phòng, CI, CO, số đêm)
- Card chi tiết tiền: TienPhong / từng ChiTietPhuThu / GiamGia / **TongCong (18sp, primary_main)**
- Card thanh toán (phương thức, ngày TT, người thu)
- Nút "Xác nhận đã TT" → chỉ KeToan mới thấy; Lễ tân chỉ thấy "In hóa đơn"
- Khi xác nhận: `HoaDonDAO.updateTrangThai(maHD, "DaThanhToan")` + set `NgayTT = today`

`fragment_invoice_create.xml` + `InvoiceCreateFragment.java`
- AutoComplete tìm DatPhong (theo mã / tên khách), chỉ hiện đặt phòng chưa có HĐ hoặc chưa thanh toán
- Card tự điền: phòng, ngày, khách hàng
- Card chi tiết phí: các dòng phụ thu động ("+Thêm dòng phí" → thêm row)
- Tự tính TienPhong = GiaMoiDem × SoDem, TongCong cập nhật realtime
- Nút "Tạo hóa đơn" → `HoaDonDAO.insert()` + lặp `ChiTietPhuThuDAO.insert()` cho từng dòng

**B2. Module Thống kê (H1 → H2 → H3)**

> **Lưu ý thư viện:** Thêm vào `build.gradle.kts`:  
> `implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'`  
> và thêm maven `{ url 'https://jitpack.io' }` vào `settings.gradle.kts`

`fragment_statistics_dashboard.xml` + `StatisticsDashboardFragment.java`
- Row chọn kỳ: Hôm nay / Tuần / Tháng / Quý / Năm → tính `fromDate` / `toDate` tương ứng
- 4 KPI card (2×2): Doanh thu / Số lượt khách / Công suất % / Số đặt phòng
- BarChart doanh thu theo ngày (dùng `StatisticsRepository.getRevenueByDay()`)
- PieChart tỷ lệ phòng (Trống / Đang thuê / Đã đặt)
- Top 5 phòng doanh thu cao

`fragment_revenue_report.xml` + `RevenueReportFragment.java`
- 2 DatePicker (từ ngày → đến ngày) + Button "Xem báo cáo"
- Card tóm tắt: Tổng DT + % so kỳ trước (tính từ cùng khoảng thời gian liền trước)
- BarChart DT theo ngày trong kỳ
- RecyclerView danh sách hóa đơn trong kỳ (compact: tên KH + phòng + tổng tiền)
- Button "Xuất báo cáo" → tạm thời: copy text vào clipboard hoặc share Intent

`fragment_occupancy_report.xml` + `OccupancyReportFragment.java`
- DateRange Picker tương tự
- Card 4 chỉ số: Tổng phòng / Đêm phòng khả dụng / Đêm đã bán / Công suất %
- LineChart công suất theo ngày
- RecyclerView bảng chi tiết từng phòng (tên phòng / số đêm / doanh thu / công suất %)

---

### Kiểm tra hoàn thành
- [ ] Danh sách hóa đơn hiện đúng 9 hóa đơn từ seed
- [ ] Lập hóa đơn mới → xuất hiện trong danh sách
- [ ] Xác nhận đã thanh toán → badge HĐ đổi sang "Đã thanh toán", nút biến mất
- [ ] Dashboard thống kê: chọn "Tháng" → doanh thu và số đặt phòng đúng với dữ liệu seed
- [ ] Role Lễ tân đăng nhập: không thấy module Thống kê trong menu "Hơn nữa"
- [ ] BarChart doanh thu render không crash, hiện đúng dữ liệu


---

## LƯU Ý KỸ THUẬT QUAN TRỌNG

1. **PRAGMA foreign_keys:** Mỗi DAO khi lấy `getWritableDatabase()` phải gọi `db.execSQL("PRAGMA foreign_keys = ON;")` hoặc đặt trong `onOpen()` của DatabaseHelper (đã có).

2. **Chỉ 3 loại layout:** Theo `ux_ui.md` — chỉ dùng `LinearLayout`, `RelativeLayout`, `FrameLayout`. Không dùng `ConstraintLayout`.

3. **RBAC runtime:** Tất cả `visibility = GONE/VISIBLE` set trong Java, không hardcode XML. Mẫu chuẩn:
   ```java
   if (!PermissionHelper.hasAccess(db, vaiTro, "QuanLyPhong", "ToanQuyen")) {
       btnChinhSua.setVisibility(View.GONE);
   }
   ```

4. **Singleton DatabaseHelper:** Dùng 1 instance duy nhất toàn app để tránh lock DB:
   ```java
   // Trong Application class hoặc truyền qua constructor của mỗi DAO
   private static DatabaseHelper instance;
   public static DatabaseHelper getInstance(Context ctx) {
       if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
       return instance;
   }
   ```

5. **Không xử lý DB trên Main Thread:** Với các thao tác nặng (load danh sách), dùng `AsyncTask` (deprecated nhưng vẫn chạy với API target hiện tại) hoặc `Thread` + `Handler` đơn giản để không block UI.


6. **Reset DB khi thay đổi schema:** Tăng `DB_VERSION` từ 1 lên 2 trong `DatabaseHelper` → `onUpgrade()` sẽ drop và tạo lại toàn bộ. Không cần gỡ app thủ công.