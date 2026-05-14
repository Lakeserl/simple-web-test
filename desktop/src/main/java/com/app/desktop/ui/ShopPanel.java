package com.app.desktop.ui;

import com.app.desktop.api.ApiClient;
import com.app.desktop.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shop panel for users to browse products and create orders.
 */
public class ShopPanel extends JPanel {

    private final ApiClient api;
    private final JPanel productsGrid;
    private final JPanel cartPanel;
    private final JLabel cartTotalLabel;
    private final JTextField searchField;
    private final List<Map<String, Object>> cartItems = new ArrayList<>();

    public ShopPanel(ApiClient api) {
        this.api = api;
        setLayout(new BorderLayout(0, 0));
        setBackground(SwingUtils.BG_DARK);

        // ─── Left: Products ─────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(SwingUtils.BG_DARK);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingUtils.BG_DARK);
        header.add(SwingUtils.createTitleLabel("🛒 Cửa Hàng"), BorderLayout.WEST);

        searchField = SwingUtils.createTextField();
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Tìm sản phẩm...");
        searchField.addActionListener(e -> refreshData());
        header.add(searchField, BorderLayout.EAST);
        leftPanel.add(header, BorderLayout.NORTH);

        // Products grid
        productsGrid = new JPanel();
        productsGrid.setLayout(new BoxLayout(productsGrid, BoxLayout.Y_AXIS));
        productsGrid.setBackground(SwingUtils.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(productsGrid);
        scrollPane.setBackground(SwingUtils.BG_DARK);
        scrollPane.getViewport().setBackground(SwingUtils.BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // ─── Right: Cart ────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(SwingUtils.BG_PANEL);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(55, 65, 81)),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JLabel cartTitle = SwingUtils.createTitleLabel("🧺 Giỏ hàng");
        rightPanel.add(cartTitle, BorderLayout.NORTH);

        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(SwingUtils.BG_PANEL);

        JScrollPane cartScroll = new JScrollPane(cartPanel);
        cartScroll.setBackground(SwingUtils.BG_PANEL);
        cartScroll.getViewport().setBackground(SwingUtils.BG_PANEL);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(cartScroll, BorderLayout.CENTER);

        // Cart footer
        JPanel cartFooter = new JPanel(new BorderLayout(0, 8));
        cartFooter.setBackground(SwingUtils.BG_PANEL);

        cartTotalLabel = new JLabel("Tổng: 0₫");
        cartTotalLabel.setFont(SwingUtils.FONT_SUBTITLE);
        cartTotalLabel.setForeground(SwingUtils.TEXT_PRIMARY);
        cartFooter.add(cartTotalLabel, BorderLayout.NORTH);

        JButton checkoutBtn = SwingUtils.createPrimaryButton("📝 Đặt hàng");
        checkoutBtn.addActionListener(e -> checkout());
        cartFooter.add(checkoutBtn, BorderLayout.SOUTH);

        rightPanel.add(cartFooter, BorderLayout.SOUTH);

        // ─── Main split ─────────────────────────────
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void refreshData() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                String q = searchField.getText().trim();
                return q.isEmpty() ? api.getProducts() : api.searchProducts(q);
            }

            @Override
            protected void done() {
                try {
                    renderProducts(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ShopPanel.this,
                            "Lỗi tải sản phẩm: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void renderProducts(List<Map<String, Object>> products) {
        productsGrid.removeAll();

        // Two-column grid using rows
        for (int i = 0; i < products.size(); i += 2) {
            JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
            row.setBackground(SwingUtils.BG_DARK);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            row.add(createProductCard(products.get(i)));
            if (i + 1 < products.size()) {
                row.add(createProductCard(products.get(i + 1)));
            } else {
                JPanel empty = new JPanel();
                empty.setBackground(SwingUtils.BG_DARK);
                row.add(empty);
            }

            productsGrid.add(row);
            productsGrid.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        productsGrid.revalidate();
        productsGrid.repaint();
    }

    private JPanel createProductCard(Map<String, Object> product) {
        JPanel card = SwingUtils.createCardPanel();
        card.setLayout(new BorderLayout(10, 6));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(SwingUtils.BG_CARD);

        JLabel nameLabel = new JLabel((String) product.get("name"));
        nameLabel.setFont(SwingUtils.FONT_SUBTITLE);
        nameLabel.setForeground(SwingUtils.TEXT_PRIMARY);
        info.add(nameLabel);

        String cat = product.get("category") != null ? (String) product.get("category") : "";
        if (!cat.isEmpty()) {
            JLabel catLabel = new JLabel(cat);
            catLabel.setFont(SwingUtils.FONT_SMALL);
            catLabel.setForeground(SwingUtils.TEXT_SECONDARY);
            info.add(catLabel);
        }

        JLabel priceLabel = new JLabel(
                SwingUtils.formatPrice(((Number) product.get("price")).doubleValue()));
        priceLabel.setFont(SwingUtils.FONT_SUBTITLE);
        priceLabel.setForeground(SwingUtils.ACCENT);
        info.add(Box.createRigidArea(new Dimension(0, 4)));
        info.add(priceLabel);

        int stock = ((Number) product.get("stock")).intValue();
        JLabel stockLabel = new JLabel("Kho: " + stock);
        stockLabel.setFont(SwingUtils.FONT_SMALL);
        stockLabel.setForeground(stock > 0 ? SwingUtils.SUCCESS : SwingUtils.DANGER);
        info.add(stockLabel);

        card.add(info, BorderLayout.CENTER);

        // Add to cart button
        JButton addBtn = SwingUtils.createPrimaryButton("+");
        addBtn.setPreferredSize(new Dimension(45, 45));
        addBtn.setEnabled(stock > 0);
        addBtn.addActionListener(e -> addToCart(product));
        card.add(addBtn, BorderLayout.EAST);

        return card;
    }

    private void addToCart(Map<String, Object> product) {
        int productId = ((Number) product.get("id")).intValue();

        // Check if already in cart
        for (var item : cartItems) {
            if (((Number) item.get("productId")).intValue() == productId) {
                int qty = ((Number) item.get("quantity")).intValue() + 1;
                item.put("quantity", qty);
                refreshCart();
                return;
            }
        }

        // New item
        Map<String, Object> cartItem = new LinkedHashMap<>();
        cartItem.put("productId", productId);
        cartItem.put("name", product.get("name"));
        cartItem.put("price", ((Number) product.get("price")).doubleValue());
        cartItem.put("quantity", 1);
        cartItems.add(cartItem);
        refreshCart();
    }

    private void refreshCart() {
        cartPanel.removeAll();
        double total = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            var item = cartItems.get(i);
            double price = ((Number) item.get("price")).doubleValue();
            int qty = ((Number) item.get("quantity")).intValue();
            total += price * qty;

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(SwingUtils.BG_CARD);
            row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Name + price
            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setBackground(SwingUtils.BG_CARD);
            JLabel nameLabel = new JLabel((String) item.get("name"));
            nameLabel.setFont(SwingUtils.FONT_SMALL);
            nameLabel.setForeground(SwingUtils.TEXT_PRIMARY);
            info.add(nameLabel);
            JLabel priceLabel = new JLabel(SwingUtils.formatPrice(price) + " × " + qty);
            priceLabel.setFont(SwingUtils.FONT_SMALL);
            priceLabel.setForeground(SwingUtils.TEXT_SECONDARY);
            info.add(priceLabel);
            row.add(info, BorderLayout.CENTER);

            // Remove button
            final int index = i;
            JButton removeBtn = new JButton("✕");
            removeBtn.setFont(SwingUtils.FONT_SMALL);
            removeBtn.setForeground(SwingUtils.DANGER);
            removeBtn.setBackground(SwingUtils.BG_CARD);
            removeBtn.setBorderPainted(false);
            removeBtn.setFocusPainted(false);
            removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            removeBtn.setPreferredSize(new Dimension(30, 30));
            removeBtn.addActionListener(e -> {
                cartItems.remove(index);
                refreshCart();
            });
            row.add(removeBtn, BorderLayout.EAST);

            cartPanel.add(row);
            cartPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        cartTotalLabel.setText("Tổng: " + SwingUtils.formatPrice(total));
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Giỏ hàng trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Checkout dialog
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBackground(SwingUtils.BG_PANEL);
        JTextField nameField = SwingUtils.createTextField();
        JTextField phoneField = SwingUtils.createTextField();
        form.add(SwingUtils.createLabel("Tên khách hàng *"));
        form.add(nameField);
        form.add(SwingUtils.createLabel("Số điện thoại"));
        form.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Thông tin đặt hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String customerName = nameField.getText().trim();
            if (customerName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập tên khách hàng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Build order request
            List<Map<String, Object>> items = new ArrayList<>();
            for (var ci : cartItems) {
                Map<String, Object> orderItem = new LinkedHashMap<>();
                orderItem.put("productId", ((Number) ci.get("productId")).intValue());
                orderItem.put("quantity", ((Number) ci.get("quantity")).intValue());
                items.add(orderItem);
            }

            Map<String, Object> orderData = new LinkedHashMap<>();
            orderData.put("customerName", customerName);
            orderData.put("customerPhone", phoneField.getText().trim());
            orderData.put("items", items);

            SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
                @Override
                protected Map<String, Object> doInBackground() throws Exception {
                    return api.createOrder(orderData);
                }

                @Override
                protected void done() {
                    try {
                        var order = get();
                        cartItems.clear();
                        refreshCart();
                        refreshData();
                        JOptionPane.showMessageDialog(ShopPanel.this,
                                "✅ Đặt hàng thành công!\nMã đơn: #" +
                                        ((Number) order.get("id")).intValue(),
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ShopPanel.this,
                                "Lỗi đặt hàng: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }
}
