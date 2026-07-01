import javax.swing.*;
import view.GamePanel;

public class GameMain extends JFrame {
    public GameMain() {
        setTitle("Chicken Invaders - AP Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameMain().setVisible(true);
        });
    }
}