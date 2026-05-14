package com.app.desktop.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Styling constants and helpers for the desktop UI.
 */
public final class SwingUtils {

    // Color palette — dark theme with purple accent
    public static final Color BG_DARK = new Color(15, 17, 26);
    public static final Color BG_PANEL = new Color(22, 27, 42);
    public static final Color BG_CARD = new Color(30, 36, 56);
    public static final Color BG_INPUT = new Color(38, 45, 68);
    public static final Color ACCENT = new Color(99, 102, 241);
    public static final Color ACCENT_HOVER = new Color(129, 140, 248);
    public static final Color TEXT_PRIMARY = new Color(226, 232, 240);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(250, 204, 21);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color INFO = new Color(56, 189, 248);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    private SwingUtils() {}

    /** Create a styled button with accent color */
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /** Create primary accent button */
    public static JButton createPrimaryButton(String text) {
        return createButton(text, ACCENT);
    }

    /** Create danger button */
    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER);
    }

    /** Create a styled text field */
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    /** Create a styled text area */
    public static JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 0);
        area.setFont(FONT_BODY);
        area.setBackground(BG_INPUT);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(TEXT_PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return area;
    }

    /** Create a styled label */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /** Create a title label */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /** Status badge color */
    public static Color getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> WARNING;
            case "CONFIRMED" -> INFO;
            case "SHIPPED" -> ACCENT;
            case "DELIVERED" -> SUCCESS;
            case "CANCELLED" -> DANGER;
            default -> TEXT_SECONDARY;
        };
    }

    /** Format price in VND */
    public static String formatPrice(double price) {
        return String.format("%,.0f₫", price);
    }

    /** Create a panel with card background */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }
}
