# Hướng dẫn triển khai Navigation cho Dashboard Thống kê

## Tóm tắt thay đổi

Đã cập nhật giao diện **fragment_statistics_dashboard.xml** với các thay đổi:

### 1. Card Doanh thu (BarChart)
- **Tiêu đề mới**: `📊 Doanh thu` (trước đây: "Doanh thu theo ngày")
- **Nút mới**: `Xem chi tiết →` với ID: `@+id/tv_revenue_detail`
- **Chức năng**: Điều hướng đến màn hình báo cáo doanh thu chi tiết

### 2. Card Công suất phòng (PieChart)
- **Tiêu đề mới**: `🏠 Công suất các phòng` (trước đây: "Tỷ lệ trạng thái phòng")
- **Nút mới**: `→` với ID: `@+id/tv_occupancy_detail`
- **Legend cập nhật**: 
  - `⬤ Phòng trống: 0` (trước đây: "Trống")
  - `⬤ Đang thuê: 0`
  - `⬤ Đã đặt: 0`
- **Chức năng**: Điều hướng đến màn hình báo cáo công suất phòng chi tiết

---

## Cách triển khai sự kiện onClick

### Java Implementation

```java
// StatisticsDashboardFragment.java

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_statistics_dashboard, container, false);
    
    // Nút "Xem chi tiết" cho Doanh thu
    TextView tvRevenueDetail = view.findViewById(R.id.tv_revenue_detail);
    tvRevenueDetail.setOnClickListener(v -> {
        // Điều hướng đến RevenueReportFragment
        navigateToRevenueReport();
    });
    
    // Nút "→" cho Công suất phòng
    TextView tvOccupancyDetail = view.findViewById(R.id.tv_occupancy_detail);
    tvOccupancyDetail.setOnClickListener(v -> {
        // Điều hướng đến OccupancyReportFragment
        navigateToOccupancyReport();
    });
    
    return view;
}

// Phương thức điều hướng sử dụng Navigation Component
private void navigateToRevenueReport() {
    NavController navController = NavHostFragment.findNavController(this);
    navController.navigate(R.id.action_statistics_to_revenue_report);
}

private void navigateToOccupancyReport() {
    NavController navController = NavHostFragment.findNavController(this);
    navController.navigate(R.id.action_statistics_to_occupancy_report);
}
```

### Kotlin Implementation

```kotlin
// StatisticsDashboardFragment.kt

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    val view = inflater.inflate(R.layout.fragment_statistics_dashboard, container, false)
    
    // Nút "Xem chi tiết" cho Doanh thu
    view.findViewById<TextView>(R.id.tv_revenue_detail).setOnClickListener {
        navigateToRevenueReport()
    }
    
    // Nút "→" cho Công suất phòng
    view.findViewById<TextView>(R.id.tv_occupancy_detail).setOnClickListener {
        navigateToOccupancyReport()
    }
    
    return view
}

// Phương thức điều hướng sử dụng Navigation Component
private fun navigateToRevenueReport() {
    findNavController().navigate(R.id.action_statistics_to_revenue_report)
}

private fun navigateToOccupancyReport() {
    findNavController().navigate(R.id.action_statistics_to_occupancy_report)
}
```

---

## Cấu hình Navigation Graph

Thêm actions vào file `nav_graph.xml` (hoặc tên tương đương):

```xml
<fragment
    android:id="@+id/statisticsDashboardFragment"
    android:name="com.example.quanlyhomestay.ui.statistics.StatisticsDashboardFragment"
    android:label="Thống kê"
    tools:layout="@layout/fragment_statistics_dashboard">
    
    <!-- Action điều hướng đến Báo cáo Doanh thu -->
    <action
        android:id="@+id/action_statistics_to_revenue_report"
        app:destination="@id/revenueReportFragment"
        app:enterAnim="@anim/slide_in_right"
        app:exitAnim="@anim/slide_out_left"
        app:popEnterAnim="@anim/slide_in_left"
        app:popExitAnim="@anim/slide_out_right" />
    
    <!-- Action điều hướng đến Báo cáo Công suất phòng -->
    <action
        android:id="@+id/action_statistics_to_occupancy_report"
        app:destination="@id/occupancyReportFragment"
        app:enterAnim="@anim/slide_in_right"
        app:exitAnim="@anim/slide_out_left"
        app:popEnterAnim="@anim/slide_in_left"
        app:popExitAnim="@anim/slide_out_right" />
</fragment>

<fragment
    android:id="@+id/revenueReportFragment"
    android:name="com.example.quanlyhomestay.ui.statistics.RevenueReportFragment"
    android:label="Báo cáo Doanh thu"
    tools:layout="@layout/fragment_revenue_report" />

<fragment
    android:id="@+id/occupancyReportFragment"
    android:name="com.example.quanlyhomestay.ui.statistics.OccupancyReportFragment"
    android:label="Báo cáo Công suất phòng"
    tools:layout="@layout/fragment_occupancy_report" />
```

---

## Alternative: Sử dụng FragmentTransaction (không dùng Navigation Component)

Nếu project không dùng Navigation Component, sử dụng FragmentTransaction:

```java
// Java
private void navigateToRevenueReport() {
    Fragment revenueFragment = new RevenueReportFragment();
    getParentFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, revenueFragment)
        .addToBackStack(null)
        .commit();
}
```

```kotlin
// Kotlin
private fun navigateToRevenueReport() {
    val revenueFragment = RevenueReportFragment()
    parentFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, revenueFragment)
        .addToBackStack(null)
        .commit()
}
```

---

## UI/UX Enhancement (Optional)

### Thêm hiệu ứng ripple cho các nút

Tạo file `res/drawable/ripple_primary.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/primary_light">
    <item android:drawable="@android:color/transparent" />
</ripple>
```

Áp dụng vào các TextView:

```xml
<TextView
    android:id="@+id/tv_revenue_detail"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Xem chi tiết →"
    android:background="@drawable/ripple_primary"
    ... />
```

---

## Testing Checklist

Sau khi triển khai, kiểm tra:

- ✅ Nhấn "Xem chi tiết →" → Điều hướng đến màn hình Báo cáo Doanh thu
- ✅ Nhấn "→" ở card Công suất → Điều hướng đến màn hình Báo cáo Công suất phòng
- ✅ Nhấn nút Back trên màn hình chi tiết → Quay lại Dashboard Thống kê
- ✅ Legend hiển thị đúng: "Phòng trống", "Đang thuê", "Đã đặt"
- ✅ Hiệu ứng click/ripple hoạt động mượt mà

---

## Notes

- **Fragment destination**: Đảm bảo `RevenueReportFragment` và `OccupancyReportFragment` đã được tạo và đăng ký trong Navigation Graph
- **ID consistency**: Các action ID phải khớp với ID trong code (`R.id.action_statistics_to_revenue_report`)
- **Back stack**: Fragment sẽ được thêm vào back stack để user có thể quay lại bằng nút Back

---

**Cập nhật:** 07/07/2026  
**Người thực hiện:** Kiro AI
