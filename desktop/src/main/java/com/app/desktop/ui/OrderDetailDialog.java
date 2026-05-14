package com.app.desktop.ui;

import com.app.desktop.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog showing order details with items list.
 */
public class OrderDetailDialog extends JDialog {

    @SuppressWarnings("unchecked")
    public OrderDetailDialog(JFrame parent, Map<String, Object> order) {
        super(parent, "Chi tiết đơn hàng", true);
        setSize(500, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(SwingUtils.BG_PANEL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SwingUtils.BG_PANEL);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JLabel title = SwingUtils.createTitleLabel(
                "📋 Đơn hàng #" + ((Number) order.get("id")).intValue());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Status badge
        String status = (String) order.get("status");
        JLabel statusLabel = new JLabel("  " + getVietnameseStatus(status) + "  ");
        statusLabel.setFont(SwingUtils.FONT_BUTTON);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(SwingUtils.getStatusColor(status));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Customer info
        addInfoRow(content, "Khách hàng", (String) order.get("customerName"));
        String phone = order.get("customerPhone") != null ? (String) order.get("customerPhone") : "N/A";
        addInfoRow(content, "SĐT", phone);
        addInfoRow(content, "Tổng tiền",
                SwingUtils.formatPrice(((Number) order.get("total")).doubleValue()));

        String createdAt = order.get("createdAt") != null ?
                order.get("createdAt").toString().replace("T", " ") : "";
        addInfoRow(content, "Ngày tạo", createdAt);

        content.add(Box.createRigidArea(new Dimension(0, 16)));

        // Items
        JLabel itemsTitle = SwingUtils.createLabel("Sản phẩm trong đơn");
        itemsTitle.setFont(SwingUtils.FONT_SUBTITLE);
        itemsTitle.setForeground(SwingUtils.TEXT_PRIMARY);
        itemsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(itemsTitle);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null) {
            for (var item : items) {
                JPanel itemRow = SwingUtils.createCardPanel();
                itemRow.setLayout(new BorderLayout(8, 0));
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

                Map<String, Object> product = (Map<String, Object>) item.get("product");
                String productName = product != null ? (String) product.get("name") : "Unknown";

                JLabel nameLabel = new JLabel(productName);
                nameLabel.setFont(SwingUtils.FONT_BODY);
                nameLabel.setForeground(SwingUtils.TEXT_PRIMARY);
                itemRow.add(nameLabel, BorderLayout.CENTER);

                int qty = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("price")).doubleValue();

                JLabel detailLabel = new JLabel(
                        SwingUtils.formatPrice(price) + " × " + qty + " = " +
                                SwingUtils.formatPrice(price * qty));
                detailLabel.setFont(SwingUtils.FONT_SMALL);
                detailLabel.setForeground(SwingUtils.ACCENT);
                itemRow.add(detailLabel, BorderLayout.EAST);

                content.add(itemRow);
                content.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }

        content.add(Box.createVerticalGlue());
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton closeBtn = SwingUtils.createPrimaryButton("Đóng");
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dispose());
        content.add(closeBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);
    }

    private void addInfoRow(JPanel container, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(SwingUtils.BG_PANEL);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = SwingUtils.createLabel(label);
        JLabel val = new JLabel(value);
        val.setFont(SwingUtils.FONT_BODY);
        val.setForeground(SwingUtils.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private String getVietnameseStatus(String status) {
        return switch (status) {
            case "PENDING" -> "⏳ Chờ xử lý";
            case "CONFIRMED" -> "✓ Đã xác nhận";
            case "SHIPPED" -> "🚚 Đang giao";
            case "DELIVERED" -> "✅ Đã giao";
            case "CANCELLED" -> "✗ Đã hủy";
            default -> status;
        };
    }
}
