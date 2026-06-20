package com.example.ql_homestay.repository;

import android.content.Context;

import com.example.ql_homestay.data.DatabaseHelper;
import com.example.ql_homestay.data.dao.ModuleDAO;
import com.example.ql_homestay.data.dao.PhanQuyenVaiTroDAO;
import com.example.ql_homestay.data.dao.QuyenDAO;
import com.example.ql_homestay.model.Module;
import com.example.ql_homestay.model.PhanQuyenVaiTro;
import com.example.ql_homestay.model.Quyen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Phục vụ riêng màn hình Phân quyền tài khoản. Khác với
 * util.PermissionHelper (chỉ trả boolean hasAccess() để ẩn/hiện View runtime
 * cho người dùng cuối), lớp này đọc/ghi trực tiếp PhanQuyen_VaiTro để Admin
 * chỉnh sửa phân quyền cho từng vai trò.
 */
public class PermissionRepository {
    private final ModuleDAO moduleDAO;
    private final QuyenDAO quyenDAO;
    private final PhanQuyenVaiTroDAO phanQuyenVaiTroDAO;

    public PermissionRepository(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        this.moduleDAO = new ModuleDAO(dbHelper);
        this.quyenDAO = new QuyenDAO(dbHelper);
        this.phanQuyenVaiTroDAO = new PhanQuyenVaiTroDAO(dbHelper);
    }

    /** 1 dòng hiển thị trong RecyclerView phân quyền (item_permission_row). */
    public static class PermissionRow {
        public final Module module;
        public int maQuyen;
        public String tenQuyen;

        public PermissionRow(Module module, int maQuyen, String tenQuyen) {
            this.module = module;
            this.maQuyen = maQuyen;
            this.tenQuyen = tenQuyen;
        }
    }

    public List<Quyen> getAllQuyen() {
        return quyenDAO.getAll();
    }

    /** 8 dòng module kèm quyền hiện tại của 1 vai trò — dữ liệu cho RecyclerView phân quyền. */
    public List<PermissionRow> getPermissionMatrix(String maVaiTro) {
        List<Module> modules = moduleDAO.getAll();
        List<Quyen> quyens = quyenDAO.getAll();
        List<PermissionRow> rows = new ArrayList<>();

        for (Module m : modules) {
            PhanQuyenVaiTro pq = phanQuyenVaiTroDAO.findByVaiTroAndModule(maVaiTro, m.getMaModule());
            int maQuyen = pq != null ? pq.getMaQuyen() : -1;
            String tenQuyen = "KhongTruyCap";
            for (Quyen q : quyens) {
                if (q.getMaQuyen() == maQuyen) {
                    tenQuyen = q.getTenQuyen();
                    break;
                }
            }
            rows.add(new PermissionRow(m, maQuyen, tenQuyen));
        }
        return rows;
    }

    /** Lưu toàn bộ thay đổi phân quyền của 1 vai trò (nút "Lưu thay đổi" ở AccountDetailFragment). */
    public void savePermissions(String maVaiTro, Map<Integer, String> tenQuyenByModule) {
        List<Quyen> quyens = quyenDAO.getAll();
        Map<Integer, Integer> maQuyenByModule = new java.util.HashMap<>();
        for (Map.Entry<Integer, String> entry : tenQuyenByModule.entrySet()) {
            int maQuyen = -1;
            for (Quyen q : quyens) {
                if (q.getTenQuyen().equals(entry.getValue())) {
                    maQuyen = q.getMaQuyen();
                    break;
                }
            }
            if (maQuyen != -1) {
                maQuyenByModule.put(entry.getKey(), maQuyen);
            }
        }
        phanQuyenVaiTroDAO.updateAllForVaiTro(maVaiTro, maQuyenByModule);
    }
}