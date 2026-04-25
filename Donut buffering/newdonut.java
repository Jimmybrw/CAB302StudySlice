import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewDonut extends JPanel implements ActionListener {
    private float rotationAngle = 0.0f;
    private float pulseAngle = 0.0f;
    private final Timer timer;

    public NewDonut() {
        // Make the panel itself transparent
        this.setOpaque(false);
        this.timer = new Timer(16, this);
        this.timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // HIGH QUALITY SETTINGS
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // CLEAR the canvas for this frame (Transparent)
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // DRAW the donut (Normal Composite)
        g2d.setComposite(AlphaComposite.SrcOver);

        int size = 250;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Color[] colors = {
            new Color(0, 255, 200), 
            new Color(255, 100, 100), 
            new Color(100, 255, 150), 
            new Color(150, 100, 255)
        };

        double[] arcWidths = new double[4];
        double totalAssigned = 0;

        for (int i = 0; i < 3; i++) {
            arcWidths[i] = 90.0 + Math.sin(pulseAngle + i * 1.5) * 40.0;
            totalAssigned += arcWidths[i];
        }
        arcWidths[3] = 360.0 - totalAssigned;

        double currentPos = rotationAngle;
        for (int i = 0; i < 4; i++) {
            g2d.setColor(colors[i]);
            g2d.fillArc(x, y, size, size, (int) currentPos, (int) arcWidths[i] + 1);
            currentPos += arcWidths[i];
        }

        // PUNCH THE HOLE (Transparency)
        g2d.setComposite(AlphaComposite.Clear);
        int holeSize = 130;
        int holeOffset = (size - holeSize) / 2;
        g2d.fillOval(x + holeOffset, y + holeOffset, holeSize, holeSize);

        // Animation updates
        rotationAngle -= 3.0f;
        pulseAngle += 0.05f;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public static void main(String[] args) {
        // We use JWindow instead of JFrame for no borders/title bar
        JWindow window = new JWindow();
        
        // This is the magic line for transparency
        window.setBackground(new Color(0, 0, 0, 0));
        
        window.setContentPane(new NewDonut());
        window.setSize(500, 500);
        window.setLocationRelativeTo(null);
        window.setAlwaysOnTop(true); // Keeps it floating above other windows
        window.setVisible(true);

        // Since there is no "X" button, you can stop the app from your IDE/Terminal
        // Or add a way to close it:
        window.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.exit(0); // Clicking the donut closes the app
            }
        });
    }
}