package com.app.desktop.ui;

import com.app.desktop.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Dialog showing product details (read-only).
 */
public class ProductDetailDialog extends JDialog {

    public ProductDetailDialog(JFrame parent, Map<String, Object> product) {
        super(parent, "Chi tiết sản phẩm", true);
        setSize(450, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(SwingUtils.BG_PANEL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JLabel title = new JLabel((String) product.get("name"));
        title.setFont(SwingUtils.FONT_TITLE);
        title.setForeground(SwingUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Category badge
        String category = product.get("category") != null ? (String) product.get("category") : "N/A";
        JLabel categoryLabel = new JLabel("  " + category + "  ");
        categoryLabel.setFont(SwingUtils.FONT_SMALL);
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setOpaque(true);
        categoryLabel.setBackground(SwingUtils.ACCENT);
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(categoryLabel);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Info rows
        addInfoRow(content, "Mã sản phẩm", "#" + ((Number) product.get("id")).intValue());
        addInfoRow(content, "Giá", SwingUtils.formatPrice(((Number) product.get("price")).doubleValue()));
        addInfoRow(content, "Tồn kho", String.valueOf(((Number) product.get("stock")).intValue()));

        // Description
        String desc = product.get("description") != null ? (String) product.get("description") : "";
        if (!desc.isEmpty()) {
            content.add(Box.createRigidArea(new Dimension(0, 12)));
            JLabel descTitle = SwingUtils.createLabel("Mô tả");
            descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(descTitle);
            content.add(Box.createRigidArea(new Dimension(0, 4)));

            JTextArea descArea = new JTextArea(desc);
            descArea.setFont(SwingUtils.FONT_BODY);
            descArea.setForeground(SwingUtils.TEXT_PRIMARY);
            descArea.setBackground(SwingUtils.BG_CARD);
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            descArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            content.add(descArea);
        }

        content.add(Box.createVerticalGlue());

        // Close button
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        JButton closeBtn = SwingUtils.createPrimaryButton("Đóng");
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());
        content.add(closeBtn);

        add(content);
    }

    private void addInfoRow(JPanel container, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(SwingUtils.BG_PANEL);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = SwingUtils.createLabel(label);
        JLabel val = new JLabel(value);
        val.setFont(SwingUtils.FONT_BODY);
        val.setForeground(SwingUtils.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 6)));
    }
}
