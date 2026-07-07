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
}
