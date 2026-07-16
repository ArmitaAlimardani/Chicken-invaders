package view;

import model.database.DatabaseManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class HighScorePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private MainMenu mainMenu;
    private JPanel menuPanel;

    public HighScorePanel(MainMenu mainMenu, JPanel menuPanel) {
        this.mainMenu = mainMenu;
        this.menuPanel = menuPanel;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel titleLabel = new JLabel("* جدول بالاترین امتیازها *", JLabel.CENTER);
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"نام کاربر", "امتیاز نهایی", "سطح رسیده", "تاریخ و زمان بازی"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.DARK_GRAY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.GRAY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        JButton btnBack = new JButton("بازگشت به منوی اصلی");
        btnBack.setFont(new Font("Arial", Font.BOLD, 16));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(Color.DARK_GRAY);
        btnBack.setFocusPainted(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        buttonPanel.add(btnBack);
        add(buttonPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            if (this.mainMenu != null && this.menuPanel != null) {
                this.mainMenu.getContentPane().removeAll();
                this.mainMenu.add(this.menuPanel);
                this.mainMenu.revalidate();
                this.mainMenu.repaint();
            }
        });

        refreshScores();
    }

    public void refreshScores() {
        tableModel.setRowCount(0);
        ArrayList<String[]> scores = DatabaseManager.getHighScores();
        for (String[] row : scores) {
            tableModel.addRow(row);
        }
    }
}