import javax.swing.*;
import java.awt.*;

/**
 * A panel with a smooth diagonal gradient background, used for modern
 * "hero banner" style headers. Drop this file into your project and
 * use it anywhere you currently use `new JPanel()` for a colored header.
 *
 * Usage:
 *   GradientPanel header = new GradientPanel(new Color(99,91,255), new Color(70,61,214));
 *   header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
 *   header.add(titleLabel);
 */
public class GradientPanel extends JPanel {

    private final Color colorStart;
    private final Color colorEnd;

    public GradientPanel(Color colorStart, Color colorEnd) {
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, colorStart, getWidth(), getHeight(), colorEnd);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // faint decorative circle for visual interest, like a modern web hero section
        g2.setColor(new Color(255, 255, 255, 18));
        int d = Math.min(getWidth(), getHeight()) + 80;
        g2.fillOval(getWidth() - d / 2, -d / 3, d, d);

        g2.dispose();
        super.paintComponent(g);
    }
}
