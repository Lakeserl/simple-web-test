package com.app.desktop;

import com.app.desktop.api.ApiClient;
import com.app.desktop.ui.AdminOrderPanel;
import com.app.desktop.ui.AdminProductPanel;
import com.app.desktop.ui.ShopPanel;
import com.app.desktop.util.SwingUtils;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with tabbed navigation.
 */
public class DesktopApp extends JFrame {

    private final ApiClient apiClient;

    public DesktopApp() {
        this.apiClient = new ApiClient();

        setTitle("🛍️ Quản Lí Sản Phẩm & Đơn Hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(SwingUtils.BG_DARK);

        initUI();
    }

    private void initUI() {
        // Sidebar navigation
        JPanel sidebar = createSidebar();

        // Content area with CardLayout
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(SwingUtils.BG_DARK);

        AdminProductPanel productPanel = new AdminProductPanel(apiClient);
        ShopPanel shopPanel = new ShopPanel(apiClient);
        AdminOrderPanel orderPanel = new AdminOrderPanel(apiClient);

        contentPanel.add(productPanel, "products");
        contentPanel.add(shopPanel, "shop");
        contentPanel.add(orderPanel, "orders");

        // Sidebar buttons
        JPanel btnPanel = (JPanel) sidebar.getComponent(1); // buttons container
        Component[] buttons = btnPanel.getComponents();

        ((JButton) buttons[0]).addActionListener(e -> {
            showCard(contentPanel, "products");
            highlightButton(buttons, 0);
            productPanel.refreshData();
        });
        ((JButton) buttons[1]).addActionListener(e -> {
            showCard(contentPanel, "shop");
            highlightButton(buttons, 1);
            shopPanel.refreshData();
        });
        ((JButton) buttons[2]).addActionListener(e -> {
            showCard(contentPanel, "orders");
            highlightButton(buttons, 2);
            orderPanel.refreshData();
        });

        // Default selection
        highlightButton(buttons, 0);

        // Layout
        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Load initial data
        productPanel.refreshData();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(SwingUtils.BG_PANEL);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(55, 65, 81)));

        // Logo / title
        JLabel logo = new JLabel("  🛍️ ShopManager");
        logo.setFont(SwingUtils.FONT_SUBTITLE);
        logo.setForeground(SwingUtils.ACCENT);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.add(logo, BorderLayout.NORTH);

        // Navigation buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setBackground(SwingUtils.BG_PANEL);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        btnPanel.add(createNavButton("📦  Quản Lí Sản Phẩm"));
        btnPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        btnPanel.add(createNavButton("🛒  Cửa Hàng"));
        btnPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        btnPanel.add(createNavButton("📋  Đơn Hàng"));

        sidebar.add(btnPanel, BorderLayout.CENTER);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(SwingUtils.FONT_BODY);
        btn.setForeground(SwingUtils.TEXT_SECONDARY);
        btn.setBackground(SwingUtils.BG_PANEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return btn;
    }

    private void showCard(JPanel container, String name) {
        ((CardLayout) container.getLayout()).show(container, name);
    }

    private void highlightButton(Component[] buttons, int activeIndex) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] instanceof JButton btn) {
                if (i == activeIndex) {
                    btn.setBackground(SwingUtils.ACCENT);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(SwingUtils.BG_PANEL);
                    btn.setForeground(SwingUtils.TEXT_SECONDARY);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Set FlatLaf dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DesktopApp app = new DesktopApp();
            app.setVisible(true);
        });
    }
}
