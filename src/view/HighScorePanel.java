package view;

import model.database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

public class HighScorePanel extends JPanel {
    private static final String[] COLUMN_NAMES = {"نام کاربر", "امتیاز نهایی", "سطح رسیده", "تاریخ و زمان بازی"};

    private static final Color BACKGROUND_COLOR = new Color(12, 15, 30);
    private static final Color TABLE_BACKGROUND_COLOR = new Color(22, 27, 48);
    private static final Color ALTERNATE_ROW_COLOR = new Color(28, 34, 58);
    private static final Color HEADER_COLOR = new Color(45, 55, 95);
    private static final Color ACCENT_COLOR = new Color(255, 204, 51);
    private static final Color TEXT_COLOR = new Color(235, 238, 245);
    private static final Color SELECTION_COLOR = new Color(66, 90, 150);
    private static final Color GRID_COLOR = new Color(55, 62, 90);
    private static final Color BUTTON_COLOR = new Color(58, 73, 120);
    private static final Color BUTTON_BORDER_COLOR = new Color(100, 125, 190);

    private static final Font TITLE_FONT = new Font("Tahoma", Font.BOLD, 28);
    private static final Font TABLE_FONT = new Font("Tahoma", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 13);
    private static final Font BUTTON_FONT = new Font("Tahoma", Font.BOLD, 15);

    private final DefaultTableModel tableModel;
    private final MainMenu mainMenu;
    private final JPanel menuPanel;

    public HighScorePanel(MainMenu mainMenu, JPanel menuPanel) {
        this.mainMenu = mainMenu;
        this.menuPanel = menuPanel;

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        tableModel = createTableModel();

        add(createTitleLabel(), BorderLayout.NORTH);
        add(createTableScrollPane(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        refreshScores();
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("جدول برترین بازیکنان", SwingConstants.CENTER);
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        return titleLabel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JScrollPane createTableScrollPane() {
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);

                if (isRowSelected(row)) {
                    component.setBackground(SELECTION_COLOR);
                    component.setForeground(Color.WHITE);
                } else {
                    component.setBackground(
                            row % 2 == 0 ? TABLE_BACKGROUND_COLOR : ALTERNATE_ROW_COLOR
                    );
                    component.setForeground(TEXT_COLOR);
                }

                return component;
            }
        };

        configureTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(TABLE_BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 2),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));

        return scrollPane;
    }

    private void configureTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(34);
        table.setBackground(TABLE_BACKGROUND_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(GRID_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        configureTableHeader(table.getTableHeader());
        centerTableContent(table);
        configureColumnWidths(table);
    }

    private void configureTableHeader(JTableHeader tableHeader) {
        tableHeader.setFont(HEADER_FONT);
        tableHeader.setBackground(HEADER_COLOR);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 42));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);
    }

    private void centerTableContent(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }
    }

    private void configureColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(135);
        table.getColumnModel().getColumn(3).setPreferredWidth(240);
    }

    private JPanel createButtonPanel() {
        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.setFont(BUTTON_FONT);
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(BUTTON_COLOR);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(240, 42));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        backButton.addActionListener(e -> returnToMainMenu());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    private void returnToMainMenu() {
        if (mainMenu == null || menuPanel == null) {
            return;
        }

        mainMenu.setContentPane(menuPanel);
        mainMenu.revalidate();
        mainMenu.repaint();
    }

    public void refreshScores() {
        tableModel.setRowCount(0);

        ArrayList<String[]> scores = DatabaseManager.getHighScores();

        for (String[] row : scores) {
            tableModel.addRow(row);
        }
    }
}