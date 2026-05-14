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
 * Admin panel for product CRUD operations.
 */
public class AdminProductPanel extends JPanel {

    private final ApiClient api;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    public AdminProductPanel(ApiClient api) {
        this.api = api;
        setLayout(new BorderLayout(0, 10));
        setBackground(SwingUtils.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingUtils.BG_DARK);

        JLabel title = SwingUtils.createTitleLabel("📦 Quản Lí Sản Phẩm");
        header.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(SwingUtils.BG_DARK);

        searchField = SwingUtils.createTextField();
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Tìm kiếm...");
        searchField.addActionListener(e -> refreshData());
        toolbar.add(searchField);

        JButton refreshBtn = SwingUtils.createButton("⟳ Làm mới", SwingUtils.BG_CARD);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });
        toolbar.add(refreshBtn);

        JButton addBtn = SwingUtils.createPrimaryButton("+ Thêm sản phẩm");
        addBtn.addActionListener(e -> showProductForm(null));
        toolbar.add(addBtn);

        header.add(toolbar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên sản phẩm", "Giá (₫)", "Danh mục", "Tồn kho", ""};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action column
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
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        // Price column right-aligned
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        // Center align stock
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Action column with buttons
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());

        // Double-click to view detail
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedColumn() != 5) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showProductDetail(row);
                }
            }
        });

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
                String query = searchField.getText().trim();
                return query.isEmpty() ? api.getProducts() : api.searchProducts(query);
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> products = get();
                    tableModel.setRowCount(0);
                    for (var p : products) {
                        tableModel.addRow(new Object[]{
                                ((Number) p.get("id")).intValue(),
                                p.get("name"),
                                SwingUtils.formatPrice(((Number) p.get("price")).doubleValue()),
                                p.get("category"),
                                ((Number) p.get("stock")).intValue(),
                                "actions"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminProductPanel.this,
                            "Lỗi tải dữ liệu: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showProductDetail(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return api.getProduct(id);
            }

            @Override
            protected void done() {
                try {
                    var product = get();
                    new ProductDetailDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(AdminProductPanel.this),
                            product
                    ).setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminProductPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showProductForm(Map<String, Object> existingProduct) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProductFormDialog dialog = new ProductFormDialog(parent, api, existingProduct);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void editProduct(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return api.getProduct(id);
            }

            @Override
            protected void done() {
                try {
                    showProductForm(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminProductPanel.this,
                            "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void deleteProduct(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sản phẩm \"" + name + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    api.deleteProduct(id);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        refreshData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminProductPanel.this,
                                "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    // ─── Custom cell renderer/editor for action buttons ────────

    private class ActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setBackground(isSelected ? SwingUtils.ACCENT.darker() : SwingUtils.BG_PANEL);

            JButton viewBtn = createSmallButton("👁", SwingUtils.INFO);
            JButton editBtn = createSmallButton("✏", SwingUtils.ACCENT);
            JButton deleteBtn = createSmallButton("🗑", SwingUtils.DANGER);

            panel.add(viewBtn);
            panel.add(editBtn);
            panel.add(deleteBtn);
            return panel;
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private final JPanel panel;
        private int currentRow;

        public ActionEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setBackground(SwingUtils.BG_PANEL);

            JButton viewBtn = createSmallButton("👁", SwingUtils.INFO);
            JButton editBtn = createSmallButton("✏", SwingUtils.ACCENT);
            JButton deleteBtn = createSmallButton("🗑", SwingUtils.DANGER);

            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                showProductDetail(currentRow);
            });
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                editProduct(currentRow);
            });
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deleteProduct(currentRow);
            });

            panel.add(viewBtn);
            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }

    private static JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 30));
        return btn;
    }
}
