import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A modern, "flat 3D" styled button: rounded corners, a soft vertical
 * gradient fill, a subtle drop shadow that lifts it off the page, and
 * a hover/press animation. Drop this file into your project and use
 * it anywhere you currently use `new JButton(...)`.
 *
 * Usage:
 *   RoundedButton btn = new RoundedButton("Start Quiz", new Color(99, 91, 255), Color.WHITE);
 *   btn.addActionListener(e -> ...);
 */
public class RoundedButton extends JButton {

    private final Color baseColor;
    private final Color hoverColor;
    private final Color pressColor;
    private final int arc;
    private boolean hovering = false;
    private boolean pressing = false;

    public RoundedButton(String text, Color baseColor, Color textColor) {
        this(text, baseColor, textColor, 16);
    }

    public RoundedButton(String text, Color baseColor, Color textColor, int cornerArc) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = brighten(baseColor, 0.12f);
        this.pressColor = darken(baseColor, 0.12f);
        this.arc = cornerArc;

        setForeground(textColor);
        setFont(getFont() != null ? getFont().deriveFont(Font.BOLD, 15f) : new Font("SansSerif", Font.BOLD, 15));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(13, 26, 13, 26));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressing = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressing = false; repaint(); }
        });
    }

    private static Color brighten(Color c, float amount) {
        int r = Math.min(255, (int) (c.getRed() + 255 * amount));
        int g = Math.min(255, (int) (c.getGreen() + 255 * amount));
        int b = Math.min(255, (int) (c.getBlue() + 255 * amount));
        return new Color(r, g, b);
    }

    private static Color darken(Color c, float amount) {
        int r = Math.max(0, (int) (c.getRed() * (1 - amount)));
        int g = Math.max(0, (int) (c.getGreen() * (1 - amount)));
        int b = Math.max(0, (int) (c.getBlue() * (1 - amount)));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowGap = 5; // space reserved at the bottom for the drop shadow
        int w = getWidth();
        int h = getHeight() - shadowGap;
        int liftOffset = pressing ? 1 : shadowGap; // button "presses down" when clicked

        // --- soft layered drop shadow (fake blur) ---
        if (!pressing) {
            for (int i = 4; i >= 1; i--) {
                int alpha = 10 - i * 2;
                g2.setColor(new Color(30, 20, 90, Math.max(alpha, 2)));
                g2.fill(new RoundRectangle2D.Float(i, liftOffset - shadowGap + i + 2, w - i * 2f, h - i * 2f, arc, arc));
            }
        }

        // --- gradient fill ---
        Color fill = pressing ? pressColor : (hovering ? hoverColor : baseColor);
        int fillH = pressing ? h + shadowGap - 1 : h;
        GradientPaint gp = new GradientPaint(0, 0, brighten(fill, 0.10f), 0, fillH, darken(fill, 0.06f));
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, pressing ? shadowGap - 1 : 0, w, fillH, arc, arc));

        // --- subtle top highlight for a glossy 3D feel ---
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fill(new RoundRectangle2D.Float(2, pressing ? shadowGap : 2, w - 4, (fillH / 2f), arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, d.height + 5);
    }
}
