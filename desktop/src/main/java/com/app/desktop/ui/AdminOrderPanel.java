package com.app.desktop.ui;

import com.app.desktop.api.ApiClient;
import com.app.desktop.util.SwingUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Admin panel for viewing and managing orders.
 */
public class AdminOrderPanel extends JPanel {

    private final ApiClient api;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public AdminOrderPanel(ApiClient api) {
        this.api = api;
        setLayout(new BorderLayout(0, 10));
        setBackground(SwingUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingUtils.BG_DARK);

        JLabel title = SwingUtils.createTitleLabel("📋 Quản Lí Đơn Hàng");
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = SwingUtils.createButton("⟳ Làm mới", SwingUtils.BG_CARD);
        refreshBtn.addActionListener(e -> refreshData());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã ĐH", "Khách hàng", "SĐT", "Tổng (₫)", "Trạng thái", "Ngày tạo", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 6; // status combo and detail
            }
        };

        table = new JTable(tableModel);
        table.setBackground(SwingUtils.BG_PANEL);
        table.setForeground(SwingUtils.TEXT_PRIMARY);
        table.setGridColor(new Color(55, 65, 81));
        table.setRowHeight(44);
        table.setFont(SwingUtils.FONT_BODY);
        table.setSelectionBackground(SwingUtils.ACCENT.darker());
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(SwingUtils.FONT_BUTTON);
        table.getTableHeader().setBackground(SwingUtils.BG_CARD);
        table.getTableHeader().setForeground(SwingUtils.TEXT_SECONDARY);
        table.setShowVerticalLines(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);

        // Price right-aligned
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // Status column with colored badges
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new StatusEditor());

        // Detail button column
        table.getColumnModel().getColumn(6).setCellRenderer(new DetailBtnRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new DetailBtnEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(SwingUtils.BG_DARK);
        scrollPane.getViewport().setBackground(SwingUtils.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return api.getOrders();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> orders = get();
                    tableModel.setRowCount(0);
                    for (var o : orders) {
                        String createdAt = o.get("createdAt") != null ?
                                o.get("createdAt").toString().replace("T", " ").substring(0, Math.min(o.get("createdAt").toString().length(), 16)) : "";
                        tableModel.addRow(new Object[]{
                                "#" + ((Number) o.get("id")).intValue(),
                                o.get("customerName"),
                                o.get("customerPhone") != null ? o.get("customerPhone") : "",
                                SwingUtils.formatPrice(((Number) o.get("total")).doubleValue()),
                                o.get("status"),
                                createdAt,
                                "detail"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminOrderPanel.this,
                            "Lỗi tải đơn hàng: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showOrderDetail(int row) {
        String idStr = ((String) tableModel.getValueAt(row, 0)).replace("#", "");
        long id = Long.parseLong(idStr);

        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return api.getOrder(id);
            }

            @Override
            protected void done() {
                try {
                    var order = get();
                    new OrderDetailDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(AdminOrderPanel.this),
                            order
                    ).setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminOrderPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateStatus(int row, String newStatus) {
        String idStr = ((String) tableModel.getValueAt(row, 0)).replace("#", "");
        long id = Long.parseLong(idStr);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.updateOrderStatus(id, newStatus);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminOrderPanel.this,
                            "Lỗi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ─── Status renderer ───────────────────────────

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(SwingUtils.FONT_BUTTON);

            if (!isSelected) {
                label.setForeground(SwingUtils.getStatusColor(status));
            }

            // Vietnamese labels
            label.setText(switch (status) {
                case "PENDING" -> "⏳ Chờ xử lý";
                case "CONFIRMED" -> "✓ Đã xác nhận";
                case "SHIPPED" -> "🚚 Đang giao";
                case "DELIVERED" -> "✅ Đã giao";
                case "CANCELLED" -> "✗ Đã hủy";
                default -> status;
            });

            return label;
        }
    }

    private class StatusEditor extends DefaultCellEditor {
        private int currentRow;

        public StatusEditor() {
            super(new JComboBox<>(new String[]{
                    "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
            }));
            JComboBox<?> combo = (JComboBox<?>) getComponent();
            combo.setFont(SwingUtils.FONT_BODY);
            combo.addActionListener(e -> {
                if (currentRow >= 0) {
                    String selected = (String) combo.getSelectedItem();
                    fireEditingStopped();
                    updateStatus(currentRow, selected);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    // ─── Detail button renderer/editor ─────────────

    private class DetailBtnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JButton btn = new JButton("👁");
            btn.setBackground(isSelected ? SwingUtils.ACCENT.darker() : SwingUtils.INFO);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        }
    }

    private class DetailBtnEditor extends DefaultCellEditor {
        private int currentRow;

        public DetailBtnEditor() {
            super(new JCheckBox());
            JButton btn = new JButton("👁");
            btn.setBackground(SwingUtils.INFO);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                fireEditingStopped();
                showOrderDetail(currentRow);
            });
            editorComponent = btn;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return editorComponent;
        }

        @Override
        public Object getCellEditorValue() {
            return "detail";
        }
    }
}
