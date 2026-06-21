# TÀI LIỆU UX/UI – ỨNG DỤNG QUẢN LÝ HOMESTAY "LALA HOUSE"
### Tài liệu kỹ thuật phục vụ thiết kế giao diện XML (Android Studio – Java – API target: Pixel 8 / Redmi Note 11)

> Đơn vị đo: tất cả kích thước dùng **dp** (density-independent pixel) cho layout và **sp** (scale-independent pixel) cho chữ, theo chuẩn Android. Màu được khai báo dạng HEX để đưa vào `colors.xml`.

---

# PHẦN 1: HỆ THỐNG THIẾT KẾ (DESIGN SYSTEM)

## 1.1 BẢNG MÀU (colors.xml)

### 1.1.1 Màu chủ đạo (Primary)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `primary_main` | `#4B9DA8` | Màu logo, nút chính (Button mặc định), App Bar nếu cần nền màu, icon active |
| `primary_dark` | `#3A7D87` | Trạng thái pressed/hover của nút chính, ripple effect |
| `primary_light` | `#D0E8EC` | Nền card form (login/register), nền ô search, nền filter chip khi active |

### 1.1.2 Màu nền (Background)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `background_screen` | `#F5F5F5` | `android:background` cho root layout của mọi Activity/Fragment |
| `background_card` | `#FFFFFF` | Nền `CardView`, nền `Dialog`, nền `BottomSheet` |
| `background_form_card` | `#D0E8EC` | Nền khối form đăng nhập/đăng ký, card tổng quan booking |
| `background_input` | `#FFFFFF` | Nền `EditText`, `Spinner`, `AutoCompleteTextView` |

### 1.1.3 Màu văn bản (Text)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `text_primary` | `#1A1A1A` | `TextView` tiêu đề, label, nội dung chính |
| `text_secondary` | `#666666` | Mô tả phụ, placeholder tĩnh, caption |
| `text_on_primary` | `#FFFFFF` | Chữ trên nền teal (button, header) |
| `text_link` | `#2B6CB0` | Link "Đăng ký ngay", "Quên mật khẩu?", "Xem tất cả" |
| `text_placeholder` | `#A0A0A0` | `android:textColorHint` của EditText |

### 1.1.4 Màu trạng thái (Status)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `status_success` | `#38A169` | Trạng thái "Phòng trống", icon thành công |
| `status_warning` | `#D97706` | Trạng thái "Đang sử dụng/Đang thuê", icon cảnh báo nhẹ |
| `status_error` | `#E53E3E` | Trạng thái "Đã đặt/Đã hủy", icon lỗi, nút xóa |
| `status_info` | `#3182CE` | Trạng thái thông tin chung |

### 1.1.5 Màu viền và phân cách (Border / Divider)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `border_input` | `#C5DDE2` | `app:strokeColor` của EditText/TextInputLayout |
| `border_card` | `#E0EEEF` | `app:strokeColor` của CardView nhẹ |
| `divider` | `#EBEBEB` | `View` divider 1dp giữa các item RecyclerView |

### 1.1.6 Màu biểu tượng (Icon)
| Tên resource | HEX | Mô tả sử dụng |
|---|---|---|
| `icon_active` | `#4B9DA8` | Icon tab đang chọn ở BottomNavigationView |
| `icon_inactive` | `#9E9E9E` | Icon tab chưa chọn, chevron ">" |
| `icon_action` | `#1A1A1A` | Icon hành động (sửa, xóa, more) |

### 1.1.7 Màu StatusBadge bổ sung (dùng riêng cho `bg_status_*` trong drawable)
| Tên resource | HEX nền | HEX chữ | Trạng thái |
|---|---|---|---|
| `badge_trong_bg` / `badge_trong_text` | `#DCFCE7` | `#166534` | Trống |
| `badge_dangthue_bg` / `badge_dangthue_text` | `#FEF3C7` | `#92400E` | Đang thuê |
| `badge_dadat_bg` / `badge_dadat_text` | `#FEE2E2` | `#991B1B` | Đã đặt |
| `badge_dathanhtoan_bg` / `badge_dathanhtoan_text` | `#D1FAE5` | `#065F46` | Đã thanh toán |
| `badge_chuathanhtoan_bg` / `badge_chuathanhtoan_text` | `#FEF3C7` | `#92400E` | Chưa thanh toán |
| `badge_dahuy_bg` / `badge_dahuy_text` | `#F3F4F6` | `#6B7280` | Đã hủy |
| `shift_sang` | `#FEF3C7` | — | Ca Sáng |
| `shift_chieu` | `#DCFCE7` | — | Ca Chiều |
| `shift_toi` | `#E0E7FF` | — | Ca Tối |

---

## 1.2 CHỮ (TYPOGRAPHY) – `styles.xml` / `themes.xml`

Font chữ: **Roboto** (font hệ thống Android, không cần khai báo file font riêng — chỉ định `android:fontFamily="sans-serif"` hoặc các biến thể `sans-serif-medium`, `sans-serif-light` khi cần).

| Style name (`TextAppearance.LalaHouse.*`) | sp | Weight / fontFamily | `lineSpacingMultiplier` | Dùng cho |
|---|---|---|---|---|
| `Display` | 28sp | Bold (700) → `sans-serif-bold` | 1.4 | Tiêu đề màn hình lớn (Đăng nhập, Đăng ký) |
| `Heading1` | 22sp | Bold (700) → `sans-serif-bold` | 1.4 | Tiêu đề section quan trọng |
| `Heading2` | 18sp | SemiBold (600) → `sans-serif-medium` | 1.4 | Tiêu đề card, dialog |
| `Heading3` | 16sp | SemiBold (600) → `sans-serif-medium` | 1.4 | Nhãn nhóm, tiêu đề item |
| `Body1` | 16sp | Regular (400) → `sans-serif` | 1.5 | Nội dung chính, text input |
| `Body2` | 14sp | Regular (400) → `sans-serif` | 1.5 | Nội dung phụ, nhãn form, caption |
| `Label` | 14sp | Medium (500) → `sans-serif-medium` | 1.4 | Nhãn trường dữ liệu (Label "Tên phòng:", v.v.) |
| `ButtonText` | 16sp | Bold (700) → `sans-serif-bold` | 1.4 | Chữ trên nút |
| `Caption` | 12sp | Regular (400) → `sans-serif` | 1.4 | Thông tin nhỏ, badge, gợi ý |
| `Overline` | 11sp | Medium (500) → `sans-serif-medium` | 1.4 | Breadcrumb, tag phân loại, label icon BottomNav |

> **Quy ước đặt tên trong `styles.xml`:** `TextAppearance.LalaHouse.Display`, `TextAppearance.LalaHouse.Heading1`, … `TextAppearance.LalaHouse.Overline`. Mỗi style kế thừa từ `TextAppearance.MaterialComponents.*` tương ứng và override `textSize`, `fontFamily`, `textColor` (mặc định `text_primary`), `lineSpacingMultiplier`.

---

## 1.3 KÍCH THƯỚC & HÌNH DẠNG (dimens.xml)

### 1.3.1 Margin / Padding chuẩn
| Tên resource | Giá trị | Mô tả |
|---|---|---|
| `margin_screen_horizontal` | 16dp | Margin trái/phải toàn màn hình (root layout) |
| `padding_card_default` | 16dp | Padding nội bộ tất cả 4 phía của CardView |
| `padding_section_vertical` | 12dp | Padding trên/dưới của section |
| `spacing_xs` | 8dp | Khoảng cách nhỏ giữa các element |
| `spacing_sm` | 12dp | Khoảng cách giữa các field trong form |
| `spacing_md` | 16dp | Khoảng cách chuẩn (margin section) |

### 1.3.2 Border Radius (sử dụng `app:cornerRadius` hoặc `shapeAppearance` trong drawable)
| Tên resource | Giá trị | Áp dụng cho |
|---|---|---|
| `radius_button` | 8dp | `MaterialButton` |
| `radius_input` | 8dp | `TextInputLayout` / `EditText` background |
| `radius_card_small` | 10dp | Card item danh sách (Room Card, KPI Card) |
| `radius_card_large` | 12dp | Card form, Dialog |
| `radius_badge` | 20dp | Badge/Chip trạng thái (pill, dùng `radius = height/2`) |
| `radius_circle` | 50% | Avatar/Icon tròn (dùng `CircleCrop` hoặc `shapeAppearanceOverlay` circular) |

### 1.3.3 Chiều cao component chuẩn (height)
| Tên resource | Giá trị | Component |
|---|---|---|
| `height_appbar` | 64dp | App Bar (Toolbar/Header), bao gồm icon + logo + title |
| `height_bottomnav` | 56dp | BottomNavigationView |
| `height_input` | 48dp | `TextInputLayout`/`EditText` (form chính) |
| `height_input_compact` | 38dp | EditText trong form Thêm/Sửa phòng (B3) |
| `height_button_main` | 48dp | Button chính full width |
| `height_button_small` | 36dp | Button nhỏ (action, "+ Thêm phòng") |
| `height_searchbar` | 44dp | SearchBar |
| `height_listitem_with_subtitle` | 72dp | List item có subtitle (Customer Row, Account Row) |
| `height_listitem_title_only` | 56dp | List item chỉ có title |
| `height_bottomsheet_min` | 300dp | Bottom Sheet tối thiểu |
| `width_dialog` | 330dp | Dialog (80% chiều rộng màn hình giả định 412dp) |

### 1.3.4 Độ rộng cố định (Width)
| Tên resource | Giá trị | Áp dụng cho |
|---|---|---|
| `width_full_content` | `match_parent` với margin 16dp mỗi bên (≈ 380dp trên màn hình 412dp) | Button full width, Input trong form, Card trong danh sách |

> **Lưu ý kỹ thuật:** Vì màn hình thực tế (Pixel 8: 412dp width logical; Redmi Note 11: ~393–412dp tùy density) khác nhau, **không hardcode width = 380dp**. Thay vào đó dùng `match_parent` + `layout_marginStart/End = margin_screen_horizontal (16dp)`. Giá trị 380dp trong tài liệu gốc chỉ là tham chiếu thiết kế trên màn hình 412dp.

### 1.3.5 Viền (Stroke)
| Tên resource | Giá trị | Áp dụng |
|---|---|---|
| `stroke_input_normal` | 1dp | Viền EditText khi không focus |
| `stroke_input_focus` | 2dp | Viền EditText khi focus (dùng `TextInputLayout` box stroke) |
| `stroke_card` | 1dp | Viền CardView nhẹ |
| `stroke_divider` | 1dp | Divider |

### 1.3.6 Elevation (Shadow)
| Tên resource | Giá trị | Áp dụng |
|---|---|---|
| `elevation_card` | 2dp | Card nổi nhẹ (Room Card, list item dạng card) |
| `elevation_appbar` | 4dp | App Bar |
| `elevation_fab` | 6dp | FAB |
| `elevation_dialog` | 8dp | Dialog |
| `elevation_bottomsheet` | 8dp | Bottom Sheet, Bottom Navigation Bar |

### 1.3.7 Icon size
| Tên resource | Giá trị | Áp dụng |
|---|---|---|
| `icon_size_appbar` | 24dp x 24dp | Icon trên App Bar (chuông thông báo) |
| `icon_size_bottomnav` | 24dp x 24dp | Icon BottomNavigationView |
| `icon_size_listitem` | 20dp x 20dp | Icon trong list item |
| `icon_size_small_action` | 18dp x 18dp | Icon hành động nhỏ |
| `icon_size_logo_header` | 40dp x 40dp | Logo tròn trên App Bar |

### 1.3.8 Avatar / Logo
| Tên resource | Giá trị | Áp dụng |
|---|---|---|
| `logo_login_size` | 100dp x 100dp (circle) | Logo trang đăng nhập |
| `avatar_listitem_size` | 48dp x 48dp | Avatar nhân viên/khách hàng trong list |
| `avatar_header_size` | 36dp x 36dp | Avatar header |
| `avatar_detail_large` | 80dp x 80dp | Avatar lớn ở Detail screen (Customer/Staff/Account) |
| `avatar_detail_medium` | 64dp x 64dp | Avatar Account Detail |

---

## 1.4 ĐẶT TÊN FILE & TÀI NGUYÊN GỢI Ý (Android Studio)

### 1.4.1 Quy ước file resource
- `colors.xml` → chứa toàn bộ mục 1.1
- `dimens.xml` → chứa toàn bộ mục 1.3
- `styles.xml` (hoặc `themes.xml` + `type_*.xml`) → chứa toàn bộ mục 1.2
- `drawable/` → các file `bg_button_primary.xml`, `bg_input_normal.xml`, `bg_input_focus.xml`, `bg_card_rounded_12.xml`, `bg_card_rounded_10.xml`, `bg_badge_*.xml`, `bg_chip_filter.xml`, `bg_fab.xml`

### 1.4.2 Quy ước drawable shape (gợi ý nội dung)
| File | cornerRadius | solidColor | strokeColor / width |
|---|---|---|---|
| `bg_button_primary.xml` | `radius_button` (8dp) | `primary_main` | — |
| `bg_button_outline.xml` | `radius_button` (8dp) | `background_card` | `primary_main` / 1dp |
| `bg_input_normal.xml` | `radius_input` (8dp) | `background_input` | `border_input` / `stroke_input_normal` |
| `bg_input_focus.xml` | `radius_input` (8dp) | `background_input` | `primary_main` / `stroke_input_focus` |
| `bg_card_form.xml` | `radius_card_large` (12dp) | `primary_light` | — |
| `bg_card_white.xml` | `radius_card_large` (12dp) | `background_card` | `stroke_card` / 1dp với `border_card` |
| `bg_card_listitem.xml` | `radius_card_small` (10dp) | `background_card` | — |
| `bg_badge_pill.xml` | `radius_badge` (20dp) | dynamic theo trạng thái | — |
| `bg_chip_filter_active.xml` | `radius_badge` (20dp) | `primary_light` | — |
| `bg_chip_filter_inactive.xml` | `radius_badge` (20dp) | `background_card` | `divider` / 1dp |
| `bg_fab.xml` | 50% (circle) | `primary_main` | — |

---

# PHẦN 2: CÁC MÀN HÌNH VÀ THÀNH PHẦN UI THEO CHỨC NĂNG

> Mỗi màn hình được mô tả theo cấu trúc: **Loại file Android** (Activity/Fragment), **Root layout**, **Cây thành phần (top-down)**, kèm resource style/color/dimen tham chiếu từ Phần 1. Tên file `.xml` gợi ý theo chuẩn `activity_*` hoặc `fragment_*` / `item_*` cho RecyclerView.

---

## A. MÀN HÌNH DÙNG CHUNG

### A1. Màn hình Đăng nhập (Login)
**File:** `activity_login.xml`
**Root:** `ScrollView > LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Logo tròn Lala House | `ImageView` (`shapeAppearanceOverlay` circular hoặc `CircleImageView`) | `width/height = logo_login_size (100dp)`, `layout_marginTop = 80dp`, căn giữa ngang |
| 2 | Tiêu đề "Đăng nhập" | `TextView` | `style = TextAppearance.LalaHouse.Display (28sp Bold)`, `textAlignment = center`, `layout_marginTop = spacing_md (16dp)` |
| 3 | Card form | `CardView` / `MaterialCardView` | `background = bg_card_form` (`primary_light`), `cornerRadius = radius_card_large (12dp)`, `padding = padding_card_default (16dp)`, `layout_margin = 16dp`, `layout_marginTop = 24dp` |
| 3.1 | Label "Tài khoản:" | `TextView` | `style = TextAppearance.LalaHouse.Label`, `textStyle = bold` |
| 3.2 | EditText tài khoản | `TextInputLayout > TextInputEditText` | `height = height_input (48dp)`, `background = bg_input_normal`, `boxCornerRadius = radius_input (8dp)`, `strokeColor = border_input`, `strokeWidth = stroke_input_normal (1dp)` |
| 3.3 | Label "Mật khẩu:" | `TextView` | `style = TextAppearance.LalaHouse.Label`, `textStyle = bold`, `layout_marginTop = 12dp` |
| 3.4 | EditText mật khẩu | `TextInputLayout > TextInputEditText` | `height = height_input (48dp)`, `inputType = textPassword`, `app:passwordToggleEnabled = true` (icon mắt) |
| 3.5 | Row ngang: Checkbox + Link | `LinearLayout (horizontal)`, `weightSum` chia 2 | Trái: `CheckBox` "Ghi nhớ đăng nhập"; Phải: `TextView` "Quên mật khẩu?" (`12sp`, `textColor = text_link`, `gravity = end`) |
| 4 | Button "Đăng nhập" | `MaterialButton` | `height = height_button_main (48dp)`, `layout_width = match_parent`, `backgroundTint = primary_main`, `textColor = text_on_primary`, `style = TextAppearance.LalaHouse.ButtonText (16sp Bold)`, `cornerRadius = radius_button (8dp)`, `layout_marginTop = 20dp` |
| 5 | Text "Chưa có tài khoản? Đăng ký ngay" | `TextView` (dùng `SpannableString` hoặc 2 `TextView` lồng) | `14sp`, `textAlignment = center`, `layout_marginTop = 16dp`; phần "Đăng ký ngay" → `textColor = text_link`, `underline` |

---

### A2. Màn hình Đăng ký (Register)
**File:** `activity_register.xml`
**Root:** `ScrollView > LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Tiêu đề "Đăng ký tài khoản" | `TextView` | `style = TextAppearance.LalaHouse.Display (28sp Bold)`, `layout_marginTop = 48dp`, `layout_marginStart = 16dp` |
| 2 | Card form | `MaterialCardView` | `background = primary_light`, `cornerRadius = 12dp`, `padding = 16dp`, `layout_margin = 16dp`, `layout_marginTop = 20dp` |
| 2.1 | Label "Họ tên:" + EditText | `TextView` + `TextInputLayout` | `height EditText = 48dp` |
| 2.2 | Label "Email:" + EditText | `TextView` + `TextInputLayout` | `inputType = textEmailAddress`, `height = 48dp` |
| 2.3 | Label "Mật khẩu:" + EditText | `TextView` + `TextInputLayout` | `inputType = textPassword`, `passwordToggleEnabled = true`, `height = 48dp` |
| 2.4 | Label "Xác nhận mật khẩu:" + EditText | `TextView` + `TextInputLayout` | `inputType = textPassword`, `height = 48dp` |
| 3 | Button "Đăng ký" | `MaterialButton` | `height = 48dp`, `width = match_parent`, `backgroundTint = primary_main`, `cornerRadius = 8dp` |
| 4 | Text "Đã có tài khoản? Đăng nhập ngay" | `TextView` | `textAlignment = center`, `layout_marginTop = 16dp`, phần "Đăng nhập ngay" → `text_link` underline |

---

### A3. App Bar (Header – dùng chung, đặt trong `include` layout)
**File:** `layout_appbar.xml` (dùng `<include layout="@layout/layout_appbar"/>` trong mọi Activity sau đăng nhập)
**Root:** `LinearLayout (horizontal)`, `height = height_appbar (64dp)`, `background = background_card (#FFFFFF)`, `elevation = elevation_appbar (4dp)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Logo tròn (40dp) | `ImageView` (circular) | `width/height = icon_size_logo_header (40dp)`, `layout_marginStart = 16dp`, căn dọc giữa |
| 2 | Text "Lala House" | `TextView` | `style = TextAppearance.LalaHouse.Heading3 (16sp Bold)`, `layout_marginStart = 8dp`, nằm cạnh logo (cùng group bên trái) |
| 3 | Icon chuông thông báo | `ImageView` (clickable) | `width/height = icon_size_appbar (24dp)`, `layout_marginEnd = spacing_xs (8dp)` trước Avatar; bọc trong `RelativeLayout` để đặt `TextView` badge số đỏ (`bg_badge_pill` đỏ, `8sp`) ở góc trên-phải icon bằng `android:layout_alignTop/alignRight` |
| 4 | Avatar tròn (36dp) + Text "Admin"/tên user | `ImageView` (circular, `avatar_header_size`) + `TextView (14sp)` | Nhóm trong `LinearLayout (horizontal)`, `layout_marginEnd = 16dp`, căn phải App Bar |

> **Ghi chú kỹ thuật:** App Bar nên implement như **fragment/include riêng** hoặc base `Toolbar` để tái sử dụng cho tất cả màn hình B–H, tránh lặp code XML.

---

### A4. Breadcrumb (dùng chung, đặt dưới App Bar)
**File:** `layout_breadcrumb.xml` (include)
**Root:** `LinearLayout (horizontal)`, `height = 36dp`, `background = background_screen (#F5F5F5)`, `paddingStart = 16dp`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Text breadcrumb | `TextView` (có thể dùng nhiều `TextView` con hoặc `SpannableString`) | `13sp`, `textColor = text_secondary (#666666)`; phần "Trang chủ" → `textColor = text_link`, clickable |
| 2 | Separator "→" | `TextView` (inline trong cùng SpannableString) | `13sp`, `textColor = icon_inactive (#9E9E9E)` |

> **Quy ước:** Mỗi Fragment truyền vào `breadcrumb` 1 chuỗi dạng `"Trang chủ → [Tên màn hình]"` thông qua hàm `setBreadcrumb(String path)` của layout include.

---

### A5. Bottom Navigation (Điều hướng chính)
**File:** `layout_bottom_nav.xml` (include trong `activity_main.xml`)
**Root:** `BottomNavigationView` (Material Components)

| Thuộc tính | Giá trị |
|---|---|
| `height` | `height_bottomnav (56dp)` |
| `background` | `background_card (#FFFFFF)` |
| `elevation` | `elevation_bottomsheet (8dp)` |
| Border top | 1dp `divider (#EBEBEB)` — dùng `app:itemHorizontalTranslationEnabled` + custom divider hoặc `View` 1dp phía trên |
| `app:itemIconTint` (selector) | active → `icon_active (#4B9DA8)`, inactive → `icon_inactive (#9E9E9E)` |
| `app:itemTextColor` (selector) | tương tự icon |
| Label style | `11sp`, active = Bold, inactive = Regular |

**Menu items (`menu/bottom_nav_menu.xml`):**
| # | Label | Icon gợi ý |
|---|---|---|
| 1 | Trang chủ | `ic_home` |
| 2 | Phòng | `ic_bed` |
| 3 | Đặt phòng | `ic_calendar` |
| 4 | Khách hàng | `ic_person` |
| 5 | Hơn nữa | `ic_menu` (mở `BottomSheetDialogFragment` hoặc `NavigationDrawer` chứa: Thanh toán, Nhân viên, Thống kê, Tài khoản) |

> **Áp dụng phân quyền (RBAC):** Tab "Hơn nữa" hiển thị submenu động dựa trên bảng `PhanQuyen_VaiTro` — ẩn các item module có `MaQuyen = KhongTruyCap` đối với role hiện tại (Lễ tân ẩn "Báo cáo & Thống kê"; Nhân viên ẩn "Hóa đơn & Thanh toán", "Quản lý nhân viên", "Báo cáo & Thống kê", "Cài đặt hệ thống").

---

### A6. Trang chủ / Dashboard (Home)
**File:** `fragment_home.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | `<include layout="@layout/layout_appbar"/>` | — | (A3) |
| 2 | Greeting text "Xin chào, [Tên]! 👋" | `TextView` | `style = TextAppearance.LalaHouse.Heading2 (18sp SemiBold)`, `layout_margin = 16dp` |
| 3 | `ScrollView` (bọc phần nội dung cuộn) | `ScrollView` | `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` — co giãn lấp đầy khoảng giữa App Bar và Bottom Nav |
| 3.1 | LinearLayout chứa nội dung cuộn | `LinearLayout (vertical)` | bên trong ScrollView |
| 3.2 | Row thống kê nhanh (4 KPI card) | `LinearLayout (horizontal)`, `flexWrap` qua 2 hàng thủ công (2 `LinearLayout (horizontal)` lồng vào 1 `LinearLayout (vertical)`) | mỗi card `weight = 1`, `height = 80dp`, `cornerRadius = radius_card_small (10dp)`, `layout_margin = 4dp`, `background = background_card` |
| 3.3 | Item KPI: Tổng phòng | `item_kpi_card.xml` | icon + số (Heading1) + label (Caption) |
| 3.4 | Item KPI: Phòng trống | (giống trên) | accent color = `status_success` |
| 3.5 | Item KPI: Đang thuê | (giống trên) | accent color = `status_warning` |
| 3.6 | Item KPI: Doanh thu hôm nay | (giống trên) | accent color = `primary_main` |
| 4 | Section "Đặt phòng hôm nay" | `LinearLayout (vertical)` | Tiêu đề `TextView (16sp SemiBold)` + `RecyclerView` (3–5 item, `item_booking_compact.xml`, `height = 56dp`) |
| 5 | Section "Hoạt động gần đây" | `LinearLayout (vertical)` | Tương tự, `RecyclerView` riêng |
| 6 | `<include layout="@layout/layout_bottom_nav"/>` | — | (A5), nằm cuối `LinearLayout` root, cố định dưới nhờ `layout_weight` của ScrollView đẩy xuống |

> **Layout tổng `activity_main.xml`:** dùng `LinearLayout (vertical)` với 3 tầng: App Bar (A3, `wrap_content`) → `FrameLayout` chứa Fragment (`layout_weight = 1`, `layout_height = 0dp`) → `BottomNavigationView` (A5, `wrap_content`). Fragment thay nhau hiển thị trong `FrameLayout` theo tab được chọn.

---

## B. QUẢN LÝ PHÒNG

### B1. Danh sách phòng (Room List)
**File:** `fragment_room_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb "Trang chủ → Phòng" | `<include>` A3 + A4 | — |
| 2 | Row công cụ | `LinearLayout (horizontal)`, `height = 52dp`, `paddingHorizontal = 16dp` | — |
| 2.1 | SearchBar | `TextInputLayout` (style box rounded) | `width = 0dp`, `weight = 1`, `height = height_searchbar (44dp)`, `cornerRadius = 22dp` (height/2), icon kính lúp `startIconDrawable` |
| 2.2 | Button "+ Thêm phòng" | `MaterialButton` | `width = 100dp`, `height = height_button_small (36dp)`, `backgroundTint = primary_main`, `textColor = text_on_primary (13sp)`, `cornerRadius = 8dp`, `layout_marginStart = 8dp` |
| 3 | Filter chips (cuộn ngang) | `HorizontalScrollView > LinearLayout (horizontal)` | mỗi chip (`TextView` tự style): `height = 32dp`, `cornerRadius = radius_badge (20dp)`; active → `background = primary_light`; chips: "Tất cả", "Trống", "Đang thuê", "Đã đặt" |
| 4 | `RelativeLayout` chứa List + FAB | `RelativeLayout`, `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` | Cho phép FAB nằm đè góc dưới phải lên RecyclerView |
| 4.1 | RecyclerView danh sách phòng | `RecyclerView` (`LinearLayoutManager vertical`) | `android:layout_alignParentTop`, `android:layout_alignParentBottom`, item = `item_room_card.xml` |
| 5 | FAB "+ Thêm" | `FloatingActionButton` | `56dp x 56dp`, `backgroundTint = primary_main`, icon "+", `elevation = elevation_fab (6dp)`, `android:layout_alignParentBottom = true`, `android:layout_alignParentEnd = true`, `layout_margin = 16dp` |

**`item_room_card.xml` (Room Card – cao 90dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| Root | Bao ngoài | `RelativeLayout` | `height = 90dp`, `width = match_parent`, `padding = 8dp` |
| a | Hình ảnh phòng | `ImageView` | `80dp x 80dp`, `cornerRadius = 8dp` (drawable), `android:layout_alignParentStart`, `layout_marginStart = 4dp`, căn dọc giữa (`layout_centerVertical`) |
| b | Cột text | `LinearLayout (vertical)` | `android:layout_toEndOf` ảnh, `layout_marginStart = 12dp`, `layout_centerVertical` |
| b.1 | Tên phòng | `TextView` | `16sp SemiBold` |
| b.2 | Loại phòng | `TextView` | `13sp`, `textColor = text_secondary` |
| b.3 | Giá/đêm | `TextView` | `14sp`, `textColor = primary_main` |
| c | Badge trạng thái | `TextView` (background = `bg_badge_pill` theo trạng thái) | `height = 24dp`, `cornerRadius = 20dp`, `text size = 11sp`, `android:layout_alignParentEnd = true`, `android:layout_alignParentTop = true`, `layout_margin = 8dp` |
| d | Divider | `View` | `height = 1dp`, `background = divider`, `android:layout_alignParentBottom = true`, `layout_width = match_parent` |

---

### B2. Chi tiết phòng (Room Detail)
**File:** `fragment_room_detail.xml`
**Root:** `RelativeLayout` (`match_parent` cả hai chiều)

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | `ScrollView` (toàn bộ nội dung trên, trừ Row nút cố định) | `ScrollView` | `android:layout_above = @id/row_buttons`, `layout_width = match_parent`, `layout_height = match_parent` |
| 1.1 | `LinearLayout (vertical)` bên trong ScrollView | `LinearLayout (vertical)` | chứa toàn bộ nội dung dưới đây |
| 2 | Header ảnh phòng | `RelativeLayout` | `width = match_parent`, `height = 220dp` |
| 2.1 | Ảnh phòng | `ImageView` | `match_parent`, `height = 220dp`, `scaleType = centerCrop` |
| 2.2 | Overlay gradient mờ | `View` | `match_parent` x `220dp`, `background = bg_gradient_overlay` (`GradientDrawable` trong suốt → đen mờ dưới cùng) |
| 2.3 | Back button | `ImageView` (clickable) | `40dp x 40dp`, `background = #80FFFFFF`, icon "←", `android:layout_alignParentTop`, `android:layout_alignParentStart`, `margin = 16dp` |
| 2.4 | Badge trạng thái to | `TextView` (`bg_badge_pill`) | `android:layout_alignParentTop`, `android:layout_alignParentEnd`, `margin = 16dp` |
| 3 | Card thông tin | `LinearLayout (vertical)` | `background = bg_card_white` (corner top 20dp qua drawable), `layout_marginTop = -20dp` (đè lên ảnh bằng `RelativeLayout` parent hoặc negative margin trong `LinearLayout` — nếu không dùng được negative margin thì đặt trong `RelativeLayout` riêng), `padding = 16dp` |
| 3.1 | Tên phòng | `TextView` | `22sp Bold` |
| 3.2 | Loại phòng | `TextView` | `14sp`, `text_secondary` |
| 3.3 | Giá | `TextView` | `20sp Bold`, `textColor = primary_main` |
| 3.4 | Divider | `View` | `1dp`, `divider` |
| 3.5 | Grid thông tin (Sức chứa / Diện tích / Tầng) | `LinearLayout (horizontal)`, `weightSum = 3` | 3 `LinearLayout (vertical)` con, mỗi cái `weight = 1`, `height = 64dp`, icon + value + label, căn giữa |
| 3.6 | Divider | `View` | `1dp` |
| 3.7 | Section "Tiện nghi" | `HorizontalScrollView > LinearLayout (horizontal)` | mỗi tiện nghi là `TextView` tự style chip: `height = 32dp`, `cornerRadius = 20dp`, `marginEnd = 8dp` |
| 3.8 | Section "Mô tả" | `TextView` | `14sp Regular` |
| 4 | Row 2 nút cố định dưới | `LinearLayout (horizontal)`, `id = row_buttons`, `height = 56dp`, `android:layout_alignParentBottom = true` (trong `RelativeLayout` root) | `background = background_card`, `padding = 8dp` |
| 4.1 | Button "Chỉnh sửa" | `MaterialButton` (outline) | `weight = 1`, `height = 44dp`, `background = #FFFFFF`, `strokeColor = primary_main`, `cornerRadius = 8dp` |
| 4.2 | Button "Đặt phòng" | `MaterialButton` (filled) | `weight = 1`, `height = 44dp`, `backgroundTint = primary_main`, `cornerRadius = 8dp`, `layout_marginStart = 8dp` |

> **Phân quyền B2:** Với role Lễ tân/Kế toán/Nhân viên (`ChiXem`), **ẩn nút "Chỉnh sửa"** (3.1), giữ hoặc ẩn "Đặt phòng" theo quyền module Đặt phòng tương ứng (Lễ tân: hiện; Kế toán/Nhân viên: ẩn vì `ChiXem` ở Đặt phòng).

---

### B3. Thêm / Sửa phòng (Add / Edit Room)
**File:** `fragment_room_add_edit.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + tiêu đề động ("Thêm phòng"/"Sửa phòng") | `<include>` A3, set title runtime | — |
| 2 | Section "Ảnh phòng" | `LinearLayout` chứa `FrameLayout` (clickable) | ô chọn ảnh: `width = match_parent` (margin 16dp), `height = 180dp`, `cornerRadius = 10dp`, icon upload + text "Thêm ảnh" giữa |
| 3 | Card form | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `padding = 16dp`, `margin = 16dp` |
| 3.1 | Label + EditText "Tên phòng" | `TextInputLayout` | `height = height_input_compact (38dp)` |
| 3.2 | Label + Spinner "Loại phòng" (Standard/Deluxe/Suite) | `TextInputLayout` (exposed dropdown / `MaterialAutoCompleteTextView`) | `height = 38dp` |
| 3.3 | Label + EditText "Giá/đêm" | `TextInputLayout` | `inputType = numberDecimal`, `height = 38dp` |
| 3.4 | Label + EditText "Sức chứa" | `TextInputLayout` | `inputType = number`, `height = 38dp` |
| 3.5 | Label + EditText "Diện tích (m²)" | `TextInputLayout` | `inputType = numberDecimal`, `height = 38dp` |
| 3.6 | Label + EditText "Tầng" | `TextInputLayout` | `inputType = number`, `height = 38dp` |
| 3.7 | Label + Spinner "Trạng thái" | `MaterialAutoCompleteTextView` | `height = 38dp`, options: Trống/Đang thuê/Đã đặt |
| 3.8 | Label "Tiện nghi" + checkbox tiện nghi | `LinearLayout (vertical)` bọc 2 `LinearLayout (horizontal)` (chia thành 2 hàng) | mỗi ô: `LinearLayout (horizontal)` chứa `CheckBox` + `TextView` label; WiFi, TV, Điều hòa, Tủ lạnh, Bồn tắm… chia đều 2-3 item/hàng |
| 3.9 | Label + EditText multiline "Mô tả" | `TextInputLayout` | `inputType = textMultiLine`, `height = 100dp`, `gravity = top` |
| 4 | Button "Lưu" | `MaterialButton` | `width = match_parent`, `height = 48dp`, `backgroundTint = primary_main`, `cornerRadius = 8dp`, `margin = 16dp`, `layout_marginBottom = 24dp` |

> **Phân quyền B3:** Toàn bộ màn hình chỉ render khi `MaQuyen(QuanLyPhong) = ToanQuyen` (chỉ Admin). Nếu user khác cố mở (deep-link), redirect về B2 hoặc hiện `EmptyState`/thông báo không có quyền.

---

## C. QUẢN LÝ ĐẶT PHÒNG

### C1. Danh sách đặt phòng (Booking List)
**File:** `fragment_booking_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb | `<include>` A3 + A4 | breadcrumb = "Trang chủ → Đặt phòng" |
| 2 | SearchBar full width | `TextInputLayout` | `height = 44dp`, `cornerRadius = 22dp`, `margin = 16dp` |
| 3 | Filter chips | `HorizontalScrollView > LinearLayout (horizontal)` | mỗi chip (`TextView` tự style): `height = 32dp`, `cornerRadius = 20dp`; active → `bg_chip_filter_active`; inactive → `bg_chip_filter_inactive`; chips: "Tất cả" \| "Sắp đến" \| "Đang ở" \| "Đã trả phòng" \| "Đã hủy" |
| 4 | `RelativeLayout` chứa List + FAB | `RelativeLayout`, `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` | — |
| 4.1 | RecyclerView Booking Card | `RecyclerView` | `android:layout_alignParentTop`, `android:layout_alignParentBottom`, item = `item_booking_card.xml`, `height = 100dp` |
| 5 | FAB "+ Thêm đặt phòng" | `FloatingActionButton` | `backgroundTint = primary_main`, icon "+", `android:layout_alignParentBottom = true`, `android:layout_alignParentEnd = true`, `layout_margin = 16dp` |

**`item_booking_card.xml` (cao 100dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Hàng trên | `LinearLayout (horizontal)` | Icon lịch (`24dp`, `tint = primary_main`) + Mã đặt phòng (`14sp SemiBold`) + Ngày (`12sp`, `text_secondary`, `gravity = end`) |
| b | Hàng giữa | `LinearLayout (vertical)` | Tên khách hàng (`16sp`) + Tên phòng (`13sp`, `text_secondary`) |
| c | Hàng dưới | `LinearLayout (horizontal)` | Check-in → Check-out (`13sp`) + Badge trạng thái (`bg_badge_pill`, `gravity = end`) |

> **Phân quyền C1 (Nhân viên dọn phòng):**
> - Filter chips: khi role = `NhanVien`, **mặc định active filter = "Đang ở"** (hoặc thêm filter ảo "Cần dọn" = trạng thái "Đang ở" + "Đã trả phòng") khi `onCreate`.
> - FAB "+ Thêm đặt phòng" → **ẩn** (`visibility = GONE`) nếu `MaQuyen(QuanLyDatPhong) != ToanQuyen` (áp dụng cho Kế toán, Nhân viên).

---

### C2. Chi tiết đặt phòng (Booking Detail)
**File:** `fragment_booking_detail.xml`
**Root:** `RelativeLayout` (`match_parent` cả hai chiều)

> **Tổng quan thay đổi:** Màn hình C4 (Check-in / Check-out) đã được **xóa bỏ**. Toàn bộ chức năng ghi nhận Check-in / Check-out được **gộp trực tiếp vào C2** thông qua 2 nút ở Row hành động phía dưới. Khi nhấn, hệ thống tự động cập nhật trạng thái và thông báo kết quả bằng **Toast 3 giây** — không điều hướng sang màn hình mới.

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar, tiêu đề "Chi tiết đặt phòng" | `<include>` A3 | `id = appbar` |
| 2 | `ScrollView` (nội dung chính) | `ScrollView` | `android:layout_below = @id/appbar`, `android:layout_above = @id/row_action` — đảm bảo nội dung scroll không bị che bởi Row 6 |
| 2.1 | `LinearLayout (vertical)` bên trong ScrollView | `LinearLayout (vertical)` | chứa toàn bộ card |
| 3 | Card tổng quan | `MaterialCardView` | `background = primary_light`, `cornerRadius = 12dp`, `padding = 16dp`, `margin = 16dp` |
| 3.1 | Mã đặt phòng | `TextView` | `18sp Bold` |
| 3.2 | Badge trạng thái | `TextView` (`bg_badge_pill`) | to, `gravity = end` (căn phải trên cùng dòng với 3.1, dùng `LinearLayout (horizontal)` + `layout_weight`) |
| 3.3 | Tên phòng | `TextView` | `16sp` |
| 3.4 | Ngày check-in/check-out (icon lịch) | `LinearLayout (horizontal)` | icon `16dp` + `TextView 14sp` |
| 3.5 | Số đêm | `TextView` | `14sp` |
| 4 | Card khách hàng | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 4.1 | Avatar 48dp + Tên khách + SĐT + CCCD | `LinearLayout (horizontal)` | Avatar (`48dp`, circle, `layout_marginEnd = 12dp`); `LinearLayout (vertical)` bên phải chứa Tên (`16sp Bold`), SĐT, CCCD (`13sp`) |
| 5 | Card thanh toán | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 5.1 | Giá phòng / Phụ thu / Giảm giá / Tổng cộng | Nhiều `LinearLayout (horizontal)` xếp chồng dọc | mỗi dòng: Label (`weight = 1`) + Giá trị (`wrap_content`, `gravity = end`); dòng "Tổng cộng" → `18sp Bold`, `textColor = primary_main` |
| 6 | **Row nút hành động (cố định dưới, không scroll)** | `LinearLayout (vertical)`, `id = row_action`, `android:layout_alignParentBottom = true`, `background = background_card`, `elevation = 8dp`, `paddingHorizontal = 16dp`, `paddingVertical = 10dp` | Chứa 2 hàng nút (Row 6A và 6B) phân tầng theo chức năng |
| **6A** | **Hàng 1 – Nút Check-in / Check-out** | `LinearLayout (horizontal)`, `height = 44dp` | — |
| 6A.1 | Nút **"Sửa đặt phòng"** | `MaterialButton` (outline) | `width = 0dp`, `weight = 1`, `height = 44dp`, `strokeColor = primary_main`, `textColor = primary_main`, `cornerRadius = 8dp` |
| 6A.2 | Nút **"Xóa đặt phòng"** | `MaterialButton` (outline) | `width = 0dp`, `weight = 1`, `height = 44dp`, `strokeColor = status_error`, `textColor = status_error`, `cornerRadius = 8dp`, `layout_marginStart = 8dp` |
| **6B** | **Hàng 2 – Nút quản lý đặt phòng** | `LinearLayout (horizontal)`, `height = 44dp`, `layout_marginTop = 8dp` | — |
| 6B.1 | Nút **"Check-in"** | `MaterialButton` (filled) | `width = 0dp`, `weight = 1`, `height = 44dp`, `backgroundTint = status_success`, `textColor = text_on_primary`, `cornerRadius = 8dp` |
| 6B.2 | Nút **"Check-out"** | `MaterialButton` (filled) | `width = 0dp`, `weight = 1`, `height = 44dp`, `backgroundTint = status_warning`, `textColor = text_on_primary`, `cornerRadius = 8dp`, `layout_marginStart = 8dp` |

**Logic hiển thị động của Row 6A và 6B (set trong Java tại `onViewCreated`):**

| Trạng thái đặt phòng (`TrangThai`) | 6A.1 Sửa | 6A.2 Xóa | 6B.1 Check-in | 6B.2 Check-out |
|---|---|---|---|---|
| `SắpĐến` | VISIBLE | VISIBLE | VISIBLE | GONE |
| `ĐangỞ` | GONE | GONE | GONE | VISIBLE |
| `ĐãTrảPhòng` | GONE | GONE | GONE | GONE |
| `ĐãHủy` | GONE | GONE | GONE | GONE |

> Khi tất cả 4 nút đều GONE (`ĐãTrảPhòng`/`ĐãHủy`), ẩn luôn toàn bộ Row 6 (`visibility = GONE`) để không chiếm khoảng trắng phía dưới màn hình.

**Logic cập nhật trạng thái khi nhấn Check-in / Check-out:**

| Hành động | Trạng thái `DatPhong` sau | Trạng thái `Phong` sau | Toast thông báo (3 giây) |
|---|---|---|---|
| Nhấn **"Check-in"** | `ĐangỞ` | `ĐangThuê` | ✅ `"Check-in thành công! Phòng [TênPhòng] đang được sử dụng."` |
| Nhấn **"Check-out"** | `ĐãTrảPhòng` | `Trống` | ✅ `"Check-out thành công! Phòng [TênPhòng] đã được trả."` |

> **Cơ chế Toast:** Dùng `Toast.makeText(context, message, Toast.LENGTH_LONG).show()` (`LENGTH_LONG` ≈ 3,5 giây, đủ gần 3 giây theo yêu cầu). Toast hiện ở vị trí mặc định (dưới màn hình). Sau khi Toast hiện, **tự động refresh Badge trạng thái (3.2)** và **cập nhật lại Row 6** theo bảng logic trên mà **không cần điều hướng sang màn hình mới**.

> **Ghi nhận vào `CheckInOut`:** Đồng thời với cập nhật trạng thái, insert 1 bản ghi vào bảng `CheckInOut` (`Loai = CheckIn`/`CheckOut`, `ThoiGian = now()`, `MaNV` = nhân viên đang đăng nhập, `GhiChuDacBiet = null` hoặc tùy mở rộng sau).

> **Phân quyền C2:**
> - Role `KeToan`, `NhanVien` (`ChiXem` ở `QuanLyDatPhong`): **ẩn toàn bộ Row 6** (cả 6A và 6B). Thay bằng `TextView` read-only hiển thị trạng thái hiện tại: `"Trạng thái: [badge]"`.
> - Role `Admin`, `LeTan` (`ToanQuyen`): hiển thị đầy đủ Row 6A và 6B theo bảng logic động trạng thái ở trên.

---

### C3. Thêm / Sửa đặt phòng (Add / Edit Booking)
**File:** `fragment_booking_add_edit.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar | `<include>` A3 | — |
| 2 | Card form | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 2.1 | AutoComplete "Khách hàng" | `AutoCompleteTextView` (trong `TextInputLayout`) | `height = 48dp`, gợi ý realtime từ `KhachHang` |
| 2.2 | Button "Thêm khách mới" | `MaterialButton` (outline, text) | `height = 36dp`, hiện khi không tìm thấy kết quả |
| 2.3 | Spinner "Chọn phòng" | `MaterialAutoCompleteTextView` | `height = 48dp`, lọc theo `TrangThai = Trống` |
| 2.4 | DatePicker "Ngày check-in" | `TextInputEditText` (read-only, `OnClickListener` mở `MaterialDatePicker`) | `height = 48dp`, icon lịch `endIconDrawable` |
| 2.5 | DatePicker "Ngày check-out" | tương tự 2.4 | `height = 48dp` |
| 2.6 | EditText "Số lượng khách" | `TextInputLayout` | `inputType = number`, `height = 48dp` |
| 2.7 | Spinner "Phương thức thanh toán" | `MaterialAutoCompleteTextView` | `height = 48dp` |
| 2.8 | EditText "Ghi chú" | `TextInputLayout` | `inputType = textMultiLine`, `height = 80dp` |
| 3 | Card tính tiền tự động | `MaterialCardView` | `background = primary_light`, `cornerRadius = 12dp`, `margin = 16dp` |
| 3.1 | Số đêm, Đơn giá, Thành tiền (realtime) | `TextView` x3 | cập nhật qua `TextWatcher`/listener khi đổi DatePicker |
| 4 | Button "Xác nhận đặt phòng" | `MaterialButton` | `width = match_parent`, `height = 48dp`, `backgroundTint = primary_main`, `margin = 16dp` |

> **Phân quyền C3:** Chỉ accessible khi `MaQuyen(QuanLyDatPhong) = ToanQuyen` (Admin, Lễ tân).

---

## D. QUẢN LÝ KHÁCH HÀNG

### D1. Danh sách khách hàng (Customer List)
**File:** `fragment_customer_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb | `<include>` A3 + A4 | "Trang chủ → Khách hàng" |
| 2 | SearchBar | `TextInputLayout` | `height = 44dp`, `cornerRadius = 22dp`, `margin = 16dp` |
| 3 | `RelativeLayout` chứa List + FAB | `RelativeLayout`, `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` | — |
| 3.1 | RecyclerView Customer Row | `RecyclerView` | `android:layout_alignParentTop`, `android:layout_alignParentBottom`, item = `item_customer_row.xml`, `height = 72dp`, divider `1dp` |
| 4 | FAB "+ Thêm khách hàng" | `FloatingActionButton` | `backgroundTint = primary_main`, icon "+", `android:layout_alignParentBottom = true`, `android:layout_alignParentEnd = true`, `layout_margin = 16dp` |

**`item_customer_row.xml` (72dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Avatar tròn 48dp | `ImageView`/`TextView` (initials nếu không có ảnh) | `background = primary_main` (khi initials), `layout_marginStart = 16dp` |
| b | Cột phải: Tên + SĐT + Email | `LinearLayout (vertical)` | Tên (`16sp SemiBold`), SĐT (`13sp` gray), Email (`12sp` gray) |
| c | Icon ">" | `ImageView` | `16dp`, `tint = icon_inactive (#9E9E9E)`, `gravity = end` |

> **Phân quyền D1:** FAB ẩn nếu `MaQuyen(QuanLyKhachHang) != ToanQuyen` (Kế toán, Nhân viên → `ChiXem`).

---

### D2. Chi tiết khách hàng (Customer Detail)
**File:** `fragment_customer_detail.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Header: Avatar 80dp + Tên + badge số lần thuê | `LinearLayout (vertical)`, căn giữa | Avatar `80dp` circle; Tên `20sp Bold`; badge (`bg_badge_pill`) |
| 2 | Card thông tin cá nhân | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 2.x | Rows: SĐT, Email, CCCD/CMND, Địa chỉ, Ngày sinh | `LinearLayout (horizontal)` x5 | mỗi row `height = 44dp`: Label (trái) + Value (phải) |
| 3 | Card lịch sử đặt phòng | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 3.1 | Tiêu đề "Lịch sử lưu trú" + link "Xem tất cả" | `LinearLayout (horizontal)` | tiêu đề trái (`16sp SemiBold`), link phải (`text_link`) |
| 3.2 | 3 item gần nhất | `RecyclerView` (hoặc 3 `View` tĩnh) | mỗi item `64dp`: Tên phòng + Ngày + Tổng tiền |
| 4 | Row 2 nút "Chỉnh sửa"/"Xóa" | `LinearLayout (horizontal)`, `height = 44dp` | `cornerRadius = 8dp` mỗi nút |

> **Phân quyền D2:** Row 4 (Chỉnh sửa/Xóa) chỉ hiện khi `MaQuyen(QuanLyKhachHang) = ToanQuyen` (Admin, Lễ tân).

---

### D3. Thêm / Sửa khách hàng (Add / Edit Customer)
**File:** `fragment_customer_add_edit.xml`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar | `<include>` A3 | — |
| 2 | Ô chọn ảnh avatar | `FrameLayout` (clickable, circular) | `80dp`, căn giữa, icon camera overlay |
| 3 | Card form | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp`, `padding = 16dp` |
| 3.1 | EditText "Họ và tên" | `TextInputLayout` | `48dp` |
| 3.2 | EditText "Số điện thoại" | `TextInputLayout` | `inputType = phone`, `48dp` |
| 3.3 | EditText "Email" | `TextInputLayout` | `inputType = textEmailAddress`, `48dp` |
| 3.4 | EditText "CCCD/CMND" | `TextInputLayout` | `inputType = number`, `48dp` |
| 3.5 | EditText "Địa chỉ" | `TextInputLayout` | `48dp` |
| 3.6 | DatePicker "Ngày sinh" | `TextInputEditText` (read-only + `MaterialDatePicker`) | `48dp` |
| 3.7 | Spinner "Giới tính" | `MaterialAutoCompleteTextView` | `48dp` |
| 4 | Button "Lưu" | `MaterialButton` | `width = match_parent`, `height = 48dp`, `margin = 16dp` |

---

## E. QUẢN LÝ TÀI KHOẢN

### E1. Danh sách tài khoản (Account List)
**File:** `fragment_account_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb | `<include>` A3 + A4 | "Trang chủ → Tài khoản" |
| 2 | SearchBar | `TextInputLayout` | `height = 44dp` |
| 3 | Filter chips | `HorizontalScrollView > LinearLayout (horizontal)` | chip (`TextView` tự style): "Tất cả" \| "Admin" \| "Nhân viên" \| "Đã khóa", `height = 32dp`, `cornerRadius = 20dp` |
| 4 | `RelativeLayout` chứa List + FAB | `RelativeLayout`, `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` | — |
| 4.1 | RecyclerView Account Row | `RecyclerView` | `android:layout_alignParentTop`, `android:layout_alignParentBottom`, item = `item_account_row.xml`, `height = 72dp` |
| 5 | FAB "+ Thêm tài khoản" | `FloatingActionButton` | `backgroundTint = primary_main`, `android:layout_alignParentBottom = true`, `android:layout_alignParentEnd = true`, `layout_margin = 16dp` |

**`item_account_row.xml` (72dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Avatar 48dp | `ImageView` (circle) | — |
| b | Tên tài khoản (16sp) + Email (13sp gray) | `LinearLayout (vertical)` | — |
| c | Badge role | `TextView` (`bg_chip_filter`, `cornerRadius = 20dp`) | nhỏ, hiển thị VaiTro |
| d | Icon ba chấm (more) | `ImageButton` | mở `PopupMenu`/`BottomSheet`: Sửa / Khóa / Xóa |

> **Phân quyền E1:** Toàn màn hình chỉ Admin truy cập (`CaiDatHeThong = ToanQuyen` chỉ Admin).

---

### E2. Chi tiết / Phân quyền tài khoản (Account Detail & Permissions)
**File:** `fragment_account_detail.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + tiêu đề "Chi tiết tài khoản" | `<include>` A3 | — |
| 2 | Card thông tin | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 2.1 | Avatar 64dp + Tên (18sp Bold) + Email (14sp) | `LinearLayout (horizontal)` | — |
| 2.2 | Row "Trạng thái" (Badge Active/Locked) | `LinearLayout (horizontal)` | label + `bg_badge_pill` |
| 2.3 | Row "Ngày tạo" | `LinearLayout (horizontal)` | label + value |
| 3 | **Row chọn vai trò (4 nút tab thủ công)** | `LinearLayout (horizontal)`, `height = 44dp`, `margin = 16dp`, `background = bg_card_white`, `cornerRadius = 8dp` | 4 `TextView` con, `weight = 1` mỗi cái; active → `background = primary_main`, `textColor = text_on_primary`; inactive → `background = background_card`, `textColor = text_primary`; text: "Admin" \| "Lễ tân" \| "Kế toán" \| "Nhân viên" |
| 4 | Card phân quyền | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 4.1 | Tiêu đề "Phân quyền" | `TextView` | `16sp Bold` |
| 4.2 | **RecyclerView danh sách module** | `RecyclerView` | item = `item_permission_row.xml` (8 dòng cố định theo bảng `Module`) |
| 5 | Button "Lưu thay đổi" | `MaterialButton` | `width = match_parent`, `height = 48dp`, `backgroundTint = primary_main`, `margin = 16dp` |

**`item_permission_row.xml` — THIẾT KẾ MỚI (thay thế "Được phép" tĩnh):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Icon module | `ImageView` | `24dp`, `tint = icon_action` |
| b | Cột text | `LinearLayout (vertical)` | Tên module (`16sp SemiBold`) + mô tả ngắn (`13sp`, `text_secondary`) |
| c | **Dropdown quyền** | `MaterialAutoCompleteTextView` (exposed dropdown, không editable) | `width ≈ 140dp`, `height = 36dp`, `cornerRadius = 8dp`; options: "Toàn quyền" (`primary_main`), "Chỉ xem" (`status_warning`), "Không truy cập" (`status_error`) |
| — | Divider | `View` | `1dp`, `divider` giữa các row |

**Logic UI cho 8 module (theo `Module` table):**
1. Trang chủ / Dashboard
2. Quản lý phòng
3. Quản lý đặt phòng
4. Quản lý khách hàng
5. Hóa đơn & Thanh toán
6. Quản lý nhân viên
7. Báo cáo & Thống kê
8. Cài đặt hệ thống

**Hành vi tương tác (mô tả cho `Activity`/`ViewModel`, không code):**
- Khi tab "Admin" được chọn: tất cả `item_permission_row` set dropdown = "Toàn quyền" và `isEnabled = false` (màu chữ nhạt đi, không cho tương tác) — phản ánh đúng nguyên tắc Admin luôn full quyền.
- Khi chọn tab Lễ tân/Kế toán/Nhân viên: load giá trị từ bảng `PhanQuyen_VaiTro` tương ứng (seed data đã có ở phần phân tích RM), dropdown `isEnabled = true` để Admin tùy chỉnh.
- Khi dropdown 1 row đổi sang "Không truy cập": có thể thêm icon cảnh báo nhỏ (status_error) bên phải dropdown để nhấn mạnh module sẽ bị ẩn khỏi điều hướng của vai trò đó.

---

### E3. Thêm / Sửa tài khoản (Add / Edit Account)
**File:** `fragment_account_add_edit.xml`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar | `<include>` A3 | — |
| 2 | Card form | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 2.1 | EditText "Tên người dùng" | `TextInputLayout` | `48dp` |
| 2.2 | EditText "Email" | `TextInputLayout` | `inputType = textEmailAddress`, `48dp` |
| 2.3 | EditText "Mật khẩu" (chỉ khi thêm mới) | `TextInputLayout` | `48dp`, `passwordToggleEnabled = true`, `visibility = GONE` khi mode = Sửa |
| 2.4 | EditText "Xác nhận mật khẩu" | `TextInputLayout` | `48dp`, ẩn cùng điều kiện với 2.3 |
| 2.5 | Spinner "Vai trò" | `MaterialAutoCompleteTextView` | `48dp`, options = Admin/Lễ tân/Kế toán/Nhân viên |
| 2.6 | Switch "Trạng thái hoạt động" | `SwitchMaterial` | `height = 48dp`, `thumbTint`/`trackTint` theo `primary_main` khi ON |
| 3 | Button "Lưu" | `MaterialButton` | `width = match_parent`, `height = 48dp` |

---

## F. QUẢN LÝ NHÂN VIÊN

### F1. Danh sách nhân viên (Staff List)
**File:** `fragment_staff_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb | `<include>` A3 + A4 | "Trang chủ → Nhân viên" |
| 2 | SearchBar | `TextInputLayout` | `height = 44dp` |
| 3 | `RelativeLayout` chứa List + FAB | `RelativeLayout`, `layout_width = match_parent`, `layout_height = 0dp`, `layout_weight = 1` | — |
| 3.1 | RecyclerView Staff Row | `RecyclerView` | `android:layout_alignParentTop`, `android:layout_alignParentBottom`, item = `item_staff_row.xml`, `height = 80dp` |
| 4 | FAB "+ Thêm nhân viên" | `FloatingActionButton` | `backgroundTint = primary_main`, `android:layout_alignParentBottom = true`, `android:layout_alignParentEnd = true`, `layout_margin = 16dp` |

**`item_staff_row.xml` (80dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Avatar 48dp | `ImageView` (circle) | — |
| b | Tên (16sp SemiBold) + Chức vụ (13sp gray) + SĐT (12sp gray) | `LinearLayout (vertical)` | — |
| c | Icon ">" | `ImageView` | `16dp`, `icon_inactive` |

> **Phân quyền F1:** FAB ẩn cho Kế toán (`ChiXem`). Toàn màn hình ẩn khỏi menu "Hơn nữa" cho Lễ tân/Nhân viên (`KhongTruyCap`).

---

### F2. Chi tiết nhân viên (Staff Detail)
**File:** `fragment_staff_detail.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Header: Avatar 80dp + Tên (20sp Bold) + Chức vụ | `LinearLayout (vertical)`, căn giữa | — |
| 2 | Card thông tin cá nhân | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp` |
| 2.x | Rows: SĐT, Email, CCCD, Địa chỉ, Ngày vào làm | `LinearLayout (horizontal)` x5 | mỗi row `44dp` |
| 3 | Card ca làm việc | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp` |
| 3.1 | Tiêu đề "Ca làm việc" + Button "Phân công" | `LinearLayout (horizontal)` | Button (`outline`, `36dp`) → mở F4 |
| 3.2 | List ca trong tuần | `RecyclerView` hoặc 7x `LinearLayout` | mỗi row `48dp`: Thứ + Ca sáng/chiều/tối (badge màu theo `shift_sang/chieu/toi`) |
| 4 | Row 2 nút "Chỉnh sửa"/"Xóa" | `LinearLayout (horizontal)`, `44dp` | — |

> **Phân quyền F2:** Row 4 chỉ hiện cho Admin (`ToanQuyen`). Kế toán xem F1/F2 ở dạng read-only (ẩn Row 4 và Button "Phân công" ở 3.1).

---

### F3. Thêm / Sửa nhân viên (Add / Edit Staff)
**File:** `fragment_staff_add_edit.xml`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Card form | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 1.1 | EditText "Họ tên" | `TextInputLayout` | `48dp` |
| 1.2 | Spinner "Chức vụ" (Lễ tân/Kế toán/Dọn phòng/Bảo vệ) | `MaterialAutoCompleteTextView` | `48dp` |
| 1.3 | EditText "SĐT" | `TextInputLayout` | `inputType = phone`, `48dp` |
| 1.4 | EditText "Email" | `TextInputLayout` | `inputType = textEmailAddress`, `48dp` |
| 1.5 | EditText "CCCD" | `TextInputLayout` | `inputType = number`, `48dp` |
| 1.6 | DatePicker "Ngày vào làm" | `TextInputEditText` + `MaterialDatePicker` | `48dp` |
| 1.7 | EditText "Địa chỉ" | `TextInputLayout` | `48dp` |
| 2 | Button "Lưu" | `MaterialButton` | `width = match_parent`, `height = 48dp` |

---

### F4. Phân công ca làm việc (Shift Assignment)
**File:** `fragment_shift_assignment.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar "Phân công ca – [Tên NV]" | `<include>` A3 | title động |
| 2 | Header hàng tiêu đề 7 ngày | `LinearLayout (horizontal)`, `height = 36dp`, `paddingHorizontal = 8dp` | 7 `TextView` con, `weight = 1`, text: Th2 → CN, `11sp Medium`, `textColor = text_secondary`, `gravity = center` |
| 3 | `ScrollView` (nội dung bảng phân công) | `ScrollView` | `layout_height = 0dp`, `layout_weight = 1` |
| 3.1 | `LinearLayout (vertical)` bên trong ScrollView | `LinearLayout (vertical)` | chứa 3 hàng ca |
| 3.2 | Hàng ca Sáng | `LinearLayout (horizontal)`, `height = 52dp` | label "Sáng" (`40dp` fixed, `14sp`) + 7 ô `LinearLayout (vertical)` con `weight = 1`, nền `shift_sang (#FEF3C7)`, chứa `CheckBox` căn giữa |
| 3.3 | Hàng ca Chiều | `LinearLayout (horizontal)`, `height = 52dp` | tương tự, nền `shift_chieu (#DCFCE7)` |
| 3.4 | Hàng ca Tối | `LinearLayout (horizontal)`, `height = 52dp` | tương tự, nền `shift_toi (#E0E7FF)` |
| 4 | Button "Lưu phân công" | `MaterialButton` | `width = match_parent`, `height = 48dp`, `margin = 16dp` |

---

## G. QUẢN LÝ THANH TOÁN

### G1. Danh sách hóa đơn (Invoice List)
**File:** `fragment_invoice_list.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar + Breadcrumb | `<include>` A3 + A4 | "Trang chủ → Thanh toán" |
| 2 | Row tóm tắt nhanh | `LinearLayout (horizontal)` | `background = primary_light`, `height = 60dp`, `padding = 16dp`: "Tổng hôm nay: [số tiền]" + "Số hóa đơn: [N]" |
| 3 | SearchBar | `TextInputLayout` | `height = 44dp` |
| 4 | Filter chips | `HorizontalScrollView > LinearLayout (horizontal)` | chip (`TextView` tự style): "Tất cả" \| "Đã thanh toán" \| "Chưa thanh toán" \| "Hoàn tiền", `height = 32dp`, `cornerRadius = 20dp` |
| 5 | RecyclerView Invoice Row | `RecyclerView` | `layout_height = 0dp`, `layout_weight = 1`, item = `item_invoice_row.xml`, `height = 88dp` |

**`item_invoice_row.xml` (88dp):**
| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| a | Hàng trên | `LinearLayout (horizontal)` | Icon hóa đơn (`24dp`, `primary_main`) + Mã HĐ (`14sp SemiBold`) + Ngày (`12sp gray`, `end`) |
| b | Hàng giữa | `LinearLayout (vertical)` | Tên khách hàng + Tên phòng |
| c | Hàng dưới | `LinearLayout (horizontal)` | Tổng tiền (`16sp Bold`, `primary_main`) + Badge trạng thái (`end`) |

> **Phân quyền G1:** Lễ tân (`XemVaTao`) — ẩn filter "Hoàn tiền" nếu chỉ Kế toán xử lý hoàn tiền (tùy nghiệp vụ, có thể giữ ẩn hoặc disable). Nhân viên (`KhongTruyCap`) — toàn module ẩn khỏi "Hơn nữa".

---

### G2. Chi tiết hóa đơn (Invoice Detail)
**File:** `fragment_invoice_detail.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar "Hóa đơn #[Mã]" | `<include>` A3 | title động |
| 2 | Card thông tin | `MaterialCardView` | `background = primary_light`, `cornerRadius = 12dp`, `margin = 16dp` |
| 2.1 | Mã HĐ (18sp Bold), Ngày lập (14sp), Badge trạng thái | — | — |
| 3 | Card đặt phòng | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 3.1 | Tên phòng, Check-in, Check-out, Số đêm | `LinearLayout (vertical)` | text rows |
| 4 | Card chi tiết tiền | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 4.1 | Row 2 cột: Tiền phòng + giá trị | `LinearLayout (horizontal)` | — |
| 4.2 | Row: Phụ thu dịch vụ + giá trị | — | — |
| 4.3 | Row: Giảm giá + giá trị | — | — |
| 4.4 | Divider | `View 1dp` | — |
| 4.5 | Row: TỔNG CỘNG + giá trị | `18sp Bold`, `primary_main` | — |
| 5 | Card thanh toán | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp` |
| 5.1 | Phương thức TT, Ngày TT, Người thu | `LinearLayout (vertical)` | — |
| 6 | Row nút | `LinearLayout (horizontal)` | "In hóa đơn" (outline) + "Xác nhận đã TT" (filled `primary_main`, chỉ hiện nếu chưa TT) |

> **Phân quyền G2:** Lễ tân (`XemVaTao`) — chỉ thấy nút "In hóa đơn"; ẩn "Xác nhận đã TT" (quyền Kế toán). Kế toán (`ToanQuyen`) — đầy đủ 2 nút.

---

### G3. Lập hóa đơn mới (Create Invoice)
**File:** `fragment_invoice_create.xml`
**Root:** `ScrollView > LinearLayout (vertical)`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar "Lập hóa đơn" | `<include>` A3 | — |
| 2 | AutoComplete chọn đặt phòng | `AutoCompleteTextView` | `height = 48dp`, tìm theo mã/tên khách |
| 3 | Card tự điền thông tin | `MaterialCardView` | `background = primary_light`, `cornerRadius = 12dp`, `margin = 16dp`: hiển thị phòng, ngày, khách |
| 4 | Card chi tiết phí | `MaterialCardView` | `background = background_card`, `cornerRadius = 12dp`, `margin = 16dp` |
| 4.1 | Mỗi dòng phí | `LinearLayout (horizontal)`, `height = 44dp` | Label (`EditText 200dp`) + Số tiền (`EditText 100dp`) |
| 4.2 | Button "+ Thêm dòng phí" | `MaterialButton` (outline) | `height = 36dp`, `cornerRadius = 8dp` |
| 4.3 | Divider + Tổng tính tự động | `View` + `TextView` | `18sp Bold`, `primary_main` |
| 5 | Spinner "Phương thức TT" | `MaterialAutoCompleteTextView` | `48dp` |
| 6 | EditText "Ghi chú" | `TextInputLayout` | `80dp` |
| 7 | Button "Tạo hóa đơn" | `MaterialButton` | `width = match_parent`, `height = 56dp`, `backgroundTint = primary_main` |

> **Phân quyền G3:** Truy cập được bởi Lễ tân (`XemVaTao`) và Kế toán (`ToanQuyen`).

---

## H. THỐNG KÊ & BÁO CÁO

### H1. Dashboard Thống kê (Statistics Dashboard)
**File:** `fragment_statistics_dashboard.xml`
**Root:** `LinearLayout (vertical)`, `background = background_screen`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | App Bar "Thống kê & Báo cáo" | `<include>` A3 | — |
| 2 | Row chọn kỳ | `HorizontalScrollView > LinearLayout (horizontal)`, `height = 40dp`, `background = primary_light` | 5 `TextView` tự style: Hôm nay / Tuần / Tháng / Quý / Năm; active → `backgroundTint = primary_main`, `textColor = text_on_primary`; inactive → trong suốt |
| 3 | Grid 4 KPI card (2 hàng × 2 cột) | `LinearLayout (vertical)`, `margin = 8dp` | — |
| 3.1 | Hàng trên | `LinearLayout (horizontal)` | 2 `LinearLayout (vertical)` con, `weight = 1`, `height = 80dp`, `cornerRadius = 10dp`, `background = background_card`, `margin = 4dp`: Doanh thu (`primary_main`), Số lượt khách (`status_success`) |
| 3.2 | Hàng dưới | `LinearLayout (horizontal)` | 2 `LinearLayout (vertical)` con, `weight = 1`, `height = 80dp`, `cornerRadius = 10dp`, `background = background_card`, `margin = 4dp`: Công suất phòng % (`status_warning`), Số đặt phòng (tím nhạt `#C7B6F5`) |
| 4 | `ScrollView` (phần biểu đồ cuộn) | `ScrollView` | `layout_height = 0dp`, `layout_weight = 1` |
| 4.1 | `LinearLayout (vertical)` bên trong ScrollView | `LinearLayout (vertical)` | — |
| 5 | Card biểu đồ Doanh thu | `LinearLayout (vertical)` tự style card | `background = bg_card_white`, `cornerRadius = 12dp`, `margin = 16dp`, `padding = 12dp`; chứa `BarChart`/`LineChart` (MPAndroidChart), `height = 220dp`; tiêu đề `16sp SemiBold` |
| 6 | Card biểu đồ Công suất phòng | `LinearLayout (vertical)` tự style card | `height = 200dp`; chứa `PieChart`; Legend dạng 3 `TextView` nhỏ kèm màu inline |
| 7 | Card top phòng doanh thu cao | `LinearLayout (vertical)` tự style card | `background = bg_card_white`, `cornerRadius = 12dp`, `margin = 16dp`; tiêu đề `16sp SemiBold`; 5 item `LinearLayout (horizontal)` 44dp: số thứ tự + Tên phòng + Doanh thu |

> **Phân quyền H1:** Chỉ Admin và Kế toán (`ToanQuyen` ở `BaoCaoThongKe`). Lễ tân/Nhân viên — module ẩn hoàn toàn khỏi "Hơn nữa".

---

### H2. Báo cáo Doanh thu (Revenue Report)
**File:** `fragment_revenue_report.xml`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | DateRange Picker (2 DatePicker từ-đến) | 2x `TextInputEditText` + `MaterialDatePicker` | `height = 48dp` mỗi cái |
| 2 | Button "Xem báo cáo" | `MaterialButton` | `backgroundTint = primary_main`, `height = 48dp` |
| 3 | Card tóm tắt | `MaterialCardView` | Tổng DT + % tăng/giảm (icon trend up/down, `status_success`/`status_error`) |
| 4 | BarChart DT theo ngày/tuần/tháng | `BarChart` (trong `MaterialCardView`) | `height = 240dp` |
| 5 | Danh sách chi tiết hóa đơn trong kỳ | `RecyclerView` (compact list) | `56dp`/item |
| 6 | Button "Xuất báo cáo" | `MaterialButton` (outline) | icon file, `height = 48dp` |

---

### H3. Báo cáo Công suất phòng (Room Occupancy Report)
**File:** `fragment_occupancy_report.xml`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | DateRange Picker + Button "Xem báo cáo" | tương tự H2 | — |
| 2 | Card: Tổng số phòng / Đêm phòng khả dụng / Đêm phòng đã bán / Công suất % | `MaterialCardView` | 4 dòng/giá trị |
| 3 | LineChart công suất theo ngày | `LineChart` | `height = 220dp` |
| 4 | Bảng chi tiết theo từng phòng | `RecyclerView` | header `44dp` (`background = primary_light`): Tên phòng \| Số đêm cho thuê \| Doanh thu \| Công suất %; mỗi row `52dp` |

---

# PHẦN 3: COMPONENT TÁI SỬ DỤNG (REUSABLE COMPONENTS)

> Các component này nên được tách thành file `layout/component_*.xml` hoặc custom `View` Java class (kế thừa `LinearLayout`/`RelativeLayout`) để `<include>` hoặc khởi tạo programmatically trong nhiều màn hình.

---

## 3.1 StatusBadge
**File gợi ý:** `component_status_badge.xml` hoặc custom view `StatusBadgeView.java`
**Root:** `TextView`

| Thuộc tính | Giá trị |
|---|---|
| `height` | 24dp |
| `paddingHorizontal` | 10dp |
| `cornerRadius` (background drawable) | 20dp (pill) |
| `textSize` | 11sp |

**Bảng trạng thái (drawable + textColor theo `state`):**
| Trạng thái | `background` (resource) | `textColor` |
|---|---|---|
| "Trống" | `#DCFCE7` → `badge_trong_bg` | `#166534` → `badge_trong_text` |
| "Đang thuê" | `#FEF3C7` → `badge_dangthue_bg` | `#92400E` → `badge_dangthue_text` |
| "Đã đặt" | `#FEE2E2` → `badge_dadat_bg` | `#991B1B` → `badge_dadat_text` |
| "Đã thanh toán" | `#D1FAE5` → `badge_dathanhtoan_bg` | `#065F46` → `badge_dathanhtoan_text` |
| "Chưa thanh toán" | `#FEF3C7` → `badge_chuathanhtoan_bg` | `#92400E` → `badge_chuathanhtoan_text` |
| "Đã hủy" | `#F3F4F6` → `badge_dahuy_bg` | `#6B7280` → `badge_dahuy_text` |

**Cách dùng (Java, mô tả logic không code):** custom view nhận tham số `setState(String trangThai)`, ánh xạ tới bảng trên để set `background` (qua `setBackgroundResource` với drawable shape pill tương ứng màu) và `setTextColor`.

---

## 3.2 EmptyState
**File:** `component_empty_state.xml`
**Root:** `LinearLayout (vertical)`, `gravity = center`, `layout_width = match_parent`, `layout_height = match_parent` (hoặc `wrap_content` khi nhúng giữa list)

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Icon lớn | `ImageView` | `64dp x 64dp`, `tint = #C5DDE2`, căn giữa |
| 2 | Text "Không có dữ liệu" | `TextView` | `16sp`, `textColor = #9E9E9E (icon_inactive)`, `textAlignment = center`, `layout_marginTop = 8dp` |
| 3 | Button "Thêm mới" | `MaterialButton` (outline) | `height = 44dp`, `background = #FFFFFF`, `strokeColor = primary_main`, `layout_marginTop = 16dp` |

**Sử dụng:** hiển thị thay cho `RecyclerView` khi `adapter.getItemCount() == 0` ở các màn B1, C1, D1, E1, F1, G1.

---

## 3.3 ConfirmDialog
**File:** `dialog_confirm.xml` (dùng với `MaterialAlertDialogBuilder` hoặc custom `DialogFragment`)
**Root:** `LinearLayout (vertical)`, `width = 330dp`, `cornerRadius = 12dp`, `background = #FFFFFF`, `padding = 16dp`

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | Icon cảnh báo | `ImageView` | `40dp`, `tint = #E53E3E (status_error)`, căn giữa |
| 2 | Tiêu đề | `TextView` | `18sp Bold`, căn giữa, `layout_marginTop = 8dp` |
| 3 | Nội dung | `TextView` | `14sp`, `textColor = text_secondary (#666666)`, căn giữa |
| 4 | Row 2 nút | `LinearLayout (horizontal)`, `layout_marginTop = 16dp` | — |
| 4.1 | "Hủy" | `MaterialButton` (outline) | `50% width`, `height = 44dp` |
| 4.2 | "Xóa" | `MaterialButton` (filled) | `50% width`, `height = 44dp`, `backgroundTint = #E53E3E (status_error)` |

**Sử dụng:** xác nhận xóa Phòng (B), Khách hàng (D2), Nhân viên (F2), Tài khoản (E1), Đặt phòng (C2 — "Hủy đặt phòng").

---

## 3.4 LoadingIndicator
**File:** `component_loading.xml`
**Root:** `FrameLayout`, `layout_width = match_parent`, `layout_height = match_parent`, `background = #80000000` (overlay mờ, tùy chọn) hoặc trong suốt

| # | Thành phần | View Android | Thuộc tính chính |
|---|---|---|---|
| 1 | CircularProgressIndicator | `CircularProgressIndicator` (Material Components) | `width/height = 40dp`, `indicatorColor = #4B9DA8 (primary_main)`, căn giữa màn hình (`layout_gravity = center`) |

**Sử dụng:** overlay khi gọi API/database (load danh sách, lưu form…), `visibility = VISIBLE/GONE` toggle qua `ViewModel` state (loading/success/error).

---

## 3.5 Thông báo: SnackBar và Toast

Dự án dùng **2 loại thông báo khác nhau** tùy ngữ cảnh:

### 3.5.1 SnackBar (form lưu / xóa)
**Không cần file XML riêng** — dùng `com.google.android.material.snackbar.Snackbar` chuẩn, custom style qua code hoặc `style="Widget.MaterialComponents.Snackbar"`.

| Thuộc tính | Giá trị |
|---|---|
| `height` | 48dp |
| `padding` | 16dp |
| `background` | `#1A1A1A (text_primary)` |
| `textColor` | `#FFFFFF` |
| `textSize` | 14sp |
| `actionTextColor` | `#4B9DA8 (primary_main)` (cho action "Hoàn tác", "OK") |
| Vị trí | hiện dưới màn hình (`anchorView` = BottomNavigationView nếu cần tránh che) |
| Duration | `Snackbar.LENGTH_LONG` (~3 giây) hoặc custom 3000ms |

**Sử dụng:** thông báo kết quả Lưu/Xóa/Cập nhật thành công hoặc lỗi ở tất cả màn hình form (B3, C3, D3, E3, F3, G3) và sau action xóa (kết hợp ConfirmDialog).

### 3.5.2 Toast (Check-in / Check-out tại C2)
**Không cần file XML riêng** — dùng `Toast.makeText(context, message, Toast.LENGTH_LONG).show()`.

| Thuộc tính | Giá trị |
|---|---|
| Duration | `Toast.LENGTH_LONG` (≈ 3,5 giây, đáp ứng yêu cầu ~3 giây) |
| Vị trí | Mặc định hệ thống Android (dưới màn hình) |
| Nội dung Check-in | `"✅ Check-in thành công! Phòng [TênPhòng] đang được sử dụng."` |
| Nội dung Check-out | `"✅ Check-out thành công! Phòng [TênPhòng] đã được trả."` |
| Nội dung lỗi | `"❌ Không thể thực hiện. Vui lòng thử lại."` |

> **Lý do dùng Toast thay SnackBar ở C2:** Check-in/Check-out là hành động tức thì, không có option "Hoàn tác" — Toast phù hợp hơn vì nhẹ, không chiếm layout, và tự tắt mà không cần tương tác thêm từ người dùng.

**Sử dụng:** Chỉ dùng Toast tại **C2** cho 2 action Check-in và Check-out. Mọi thông báo khác trong ứng dụng dùng SnackBar (3.5.1).

---

## 3.6 Component bổ sung phục vụ phân quyền (RBAC UI Helper)

### 3.6.1 PermissionRow (item_permission_row)
Đã mô tả chi tiết tại **E2**. Tái sử dụng nếu sau này cần thêm màn hình "Xem nhanh phân quyền" ở Profile cá nhân (read-only mode, ẩn dropdown, chỉ hiện badge "Toàn quyền/Chỉ xem/Không truy cập" dạng `StatusBadge` tái dùng từ 3.1 với 3 màu mới: `primary_main` (Toàn quyền), `status_warning` (Chỉ xem), `status_error` (Không truy cập)).

### 3.6.2 FilterChipGroup chuẩn
**File:** `component_filter_chips.xml`
**Root:** `HorizontalScrollView > LinearLayout (horizontal)`

| Thuộc tính | Giá trị |
|---|---|
| `LinearLayout.orientation` | horizontal |
| `HorizontalScrollView.scrollbars` | none |
| mỗi chip (`TextView`) `.height` | 32dp |
| chip `background` drawable (`cornerRadius`) | 20dp |
| Chip active | `background = bg_chip_filter_active` (`primary_light`), `textColor = text_primary` |
| Chip inactive | `background = bg_chip_filter_inactive` (`background_card`, stroke `divider` 1dp), `textColor = text_secondary` |
| Khoảng cách giữa các chip | `layout_marginEnd = 8dp` trên mỗi `TextView` chip |

> **Logic toggle (Java):** Click vào chip → set `background` chip được chọn = `bg_chip_filter_active`, các chip còn lại = `bg_chip_filter_inactive`. Không dùng `ChipGroup` (Material) vì phạm vi bài học giới hạn.

**Sử dụng:** B1, C1, E1, G1, H1.

---

# GHI CHÚ TRIỂN KHAI CHUNG (Android Studio – Java)

0. **Quy tắc layout bắt buộc:** Toàn bộ file XML trong dự án **chỉ được dùng 3 loại layout**: `LinearLayout`, `RelativeLayout`, `FrameLayout`. Không dùng `ConstraintLayout`, `CoordinatorLayout`, `GridLayout`, `NestedScrollView`, `AppBarLayout`, `CollapsingToolbarLayout`, hay bất kỳ layout nào khác ngoài danh sách này.
   - **Thay thế phổ biến:** nút cố định dưới màn hình → `RelativeLayout` root + `android:layout_alignParentBottom`; FAB đè góc dưới phải → `RelativeLayout` bọc RecyclerView + FAB; bố cục lưới 2 cột → 2 `LinearLayout (horizontal)` lồng vào 1 `LinearLayout (vertical)`; tab thủ công → `LinearLayout (horizontal)` + `weight`.

1. **Cấu trúc module gợi ý:**
   - `res/layout/activity_*.xml` — màn hình cấp Activity (Login, Register, Main)
   - `res/layout/fragment_*.xml` — màn hình con trong `BottomNavigationView` + `NavController` (Navigation Component)
   - `res/layout/item_*.xml` — item cho `RecyclerView.Adapter`
   - `res/layout/component_*.xml` / `dialog_*.xml` — component tái sử dụng (Phần 3)
   - `res/layout/layout_appbar.xml`, `layout_breadcrumb.xml`, `layout_bottom_nav.xml` — các phần `<include>`

2. **Tương thích thiết bị:**
   - Pixel 8 (~412dp width, density 2.625) và Redmi Note 11 (~393–412dp width tùy bản, density ~2.75) đều thuộc nhóm **sw400dp** trở lên → các giá trị dp trong tài liệu áp dụng trực tiếp không cần file `dimens` riêng theo `values-sw*dp`, **trừ khi** kiểm thử thực tế phát hiện overflow trên Redmi Note 11 → khi đó tạo `values-sw360dp/dimens.xml` giảm nhẹ `margin_screen_horizontal` hoặc `width` các card cố định.

3. **Thư viện đề xuất (Java, Gradle):**
   - `com.google.android.material:material` (MaterialButton, TextInputLayout, Chip, BottomNavigationView, CircularProgressIndicator, Snackbar, MaterialDatePicker)
   - `androidx.navigation:navigation-fragment` + `navigation-ui` (điều hướng giữa Fragment qua BottomNavigationView)
   - `com.github.PhilJay:MPAndroidChart` (BarChart/LineChart/PieChart cho H1–H3)
   - `de.hdodenhof:circleimageview` (nếu không dùng `shapeAppearanceOverlay` circular của Material 1.x cho Avatar/Logo)

4. **Áp dụng RBAC (phân quyền) trong XML/Java:**
   - Mọi `visibility = GONE/VISIBLE` của nút hành động (Thêm/Sửa/Xóa/FAB/Check-in/Check-out…) được set **runtime trong Java** dựa trên `MaQuyen` của `VaiTro` hiện tại đọc từ bảng `PhanQuyen_VaiTro` (đã seed ở phần phân tích RM), **không hardcode trong XML**.
   - BottomNavigationView (A5) và submenu "Hơn nữa" filter item dựa trên cùng cơ chế — implement bằng cách build `Menu` programmatically hoặc `menu.findItem(id).setVisible(false)` trong `onCreateOptionsMenu`/`onNavigationItemSelected`.

5. **Đặt tên ID (`android:id`) gợi ý:** `@+id/tv_*` (TextView), `@+id/et_*`/`@+id/til_*` (EditText/TextInputLayout), `@+id/btn_*` (Button), `@+id/iv_*` (ImageView), `@+id/cv_*` (CardView), `@+id/rv_*` (RecyclerView), `@+id/chip_*`, `@+id/fab_*`, `@+id/badge_*`, `@+id/dropdown_*` — đảm bảo tính nhất quán khi `findViewById`/`ViewBinding` trong code Java.