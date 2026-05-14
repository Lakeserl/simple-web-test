package com.app.desktop.ui;

import com.app.desktop.api.ApiClient;
import com.app.desktop.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dialog for adding or editing a product.
 */
public class ProductFormDialog extends JDialog {

    private final ApiClient api;
    private final Map<String, Object> existingProduct; // null for create
    private boolean saved = false;

    private JTextField nameField;
    private JTextArea descField;
    private JTextField priceField;
    private JTextField categoryField;
    private JTextField stockField;
    private JTextField imageField;

    public ProductFormDialog(JFrame parent, ApiClient api, Map<String, Object> existingProduct) {
        super(parent, existingProduct == null ? "Thêm sản phẩm mới" : "Sửa sản phẩm", true);
        this.api = api;
        this.existingProduct = existingProduct;

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(SwingUtils.BG_PANEL);

        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        String titleText = existingProduct == null ? "➕ Thêm sản phẩm mới" : "✏️ Sửa sản phẩm";
        JLabel title = SwingUtils.createTitleLabel(titleText);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // Form fields
        nameField = addFormField(content, "Tên sản phẩm *");
        descField = addFormTextArea(content, "Mô tả");
        priceField = addFormField(content, "Giá (₫) *");
        categoryField = addFormField(content, "Danh mục");
        stockField = addFormField(content, "Tồn kho *");
        imageField = addFormField(content, "URL hình ảnh");

        // Pre-fill for edit
        if (existingProduct != null) {
            nameField.setText((String) existingProduct.get("name"));
            descField.setText(existingProduct.get("description") != null ?
                    (String) existingProduct.get("description") : "");
            priceField.setText(String.valueOf(((Number) existingProduct.get("price")).longValue()));
            categoryField.setText(existingProduct.get("category") != null ?
                    (String) existingProduct.get("category") : "");
            stockField.setText(String.valueOf(((Number) existingProduct.get("stock")).intValue()));
            imageField.setText(existingProduct.get("imageUrl") != null ?
                    (String) existingProduct.get("imageUrl") : "");
        }

        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(SwingUtils.BG_PANEL);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton cancelBtn = SwingUtils.createButton("Hủy", SwingUtils.BG_CARD);
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = SwingUtils.createPrimaryButton(existingProduct == null ? "Thêm" : "Lưu");
        saveBtn.addActionListener(e -> save());
        btnPanel.add(saveBtn);

        content.add(btnPanel);

        add(new JScrollPane(content));
    }

    private JTextField addFormField(JPanel container, String label) {
        JLabel lbl = SwingUtils.createLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createRigidArea(new Dimension(0, 4)));

        JTextField field = SwingUtils.createTextField();
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        container.add(field);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
        return field;
    }

    private JTextArea addFormTextArea(JPanel container, String label) {
        JLabel lbl = SwingUtils.createLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
        container.add(Box.createRigidArea(new Dimension(0, 4)));

        JTextArea area = SwingUtils.createTextArea(3);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        container.add(scroll);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
        return area;
    }

    private void save() {
        // Validate
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();

        if (name.isEmpty()) {
            showError("Tên sản phẩm không được để trống");
            return;
        }
        if (priceStr.isEmpty()) {
            showError("Giá không được để trống");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Giá phải là số dương");
            return;
        }

        int stock;
        try {
            stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Tồn kho phải là số nguyên >= 0");
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("description", descField.getText().trim());
        data.put("price", price);
        data.put("category", categoryField.getText().trim());
        data.put("stock", stock);
        data.put("imageUrl", imageField.getText().trim());

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (existingProduct == null) {
                    api.createProduct(data);
                } else {
                    long id = ((Number) existingProduct.get("id")).longValue();
                    api.updateProduct(id, data);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception e) {
                    showError("Lỗi lưu: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isSaved() {
        return saved;
    }
}
