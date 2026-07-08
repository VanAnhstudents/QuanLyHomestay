# QL_Homestay

QL_Homestay là dự án học tập xây dựng ứng dụng Android quản lý homestay. Mục tiêu chính là thực hành phát triển ứng dụng Android native, tổ chức mã nguồn theo các lớp UI, Repository, DAO, Model và lưu trữ dữ liệu cục bộ bằng SQLite.

Ứng dụng dùng dữ liệu mẫu được seed sẵn khi chạy lần đầu, phù hợp để demo các nghiệp vụ quản lý phòng, đặt phòng, khách hàng, nhân viên, tài khoản, hóa đơn và thống kê.

## Công nghệ sử dụng

- Java 11
- Android Native
- Gradle Kotlin DSL
- Android Gradle Plugin 9.2.1
- SQLite qua `SQLiteOpenHelper`
- AndroidX AppCompat, Material Components
- Navigation Component
- RecyclerView, CardView, GridLayout, ConstraintLayout
- MPAndroidChart
- CircleImageView

## Hướng dẫn cài đặt

### Yêu cầu

- Android Studio phiên bản mới
- JDK 11 hoặc mới hơn
- Android SDK có `compileSdk 36`
- Máy ảo Android hoặc thiết bị thật chạy Android 7.0 trở lên (`minSdk 24`)

### Cài đặt và chạy bằng Android Studio

1. Clone hoặc tải project về máy.
2. Mở Android Studio.
3. Chọn `Open` và trỏ tới thư mục project này.
4. Đợi Gradle sync hoàn tất.
5. Chọn emulator hoặc thiết bị thật.
6. Nhấn `Run`.

### Build bằng dòng lệnh

Trên Windows:

```powershell
.\gradlew.bat assembleDebug
```

File APK debug sau khi build nằm tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Hướng dẫn sử dụng

1. Mở ứng dụng `QL_Homestay`.
2. Đăng nhập bằng tài khoản mẫu:

```text
Tên đăng nhập: admin
Mật khẩu: admin@1001
```

3. Sau khi đăng nhập, sử dụng thanh điều hướng để truy cập các chức năng chính:

- Trang chủ
- Quản lý phòng
- Quản lý đặt phòng
- Quản lý khách hàng
- Hóa đơn, thanh toán
- Quản lý nhân viên
- Báo cáo, thống kê
- Cài đặt, tài khoản

4. Dữ liệu demo được tạo tự động trong SQLite ở lần chạy đầu tiên. Nếu thay đổi schema hoặc version database, ứng dụng sẽ tạo lại dữ liệu mẫu theo logic trong `DatabaseHelper`.

## Ghi chú

Đây là dự án phục vụ học tập, chưa hướng tới môi trường production. Một số phần như bảo mật mật khẩu, đồng bộ dữ liệu server và phân quyền nâng cao được giữ ở mức đơn giản để tập trung vào luồng nghiệp vụ Android cơ bản.
