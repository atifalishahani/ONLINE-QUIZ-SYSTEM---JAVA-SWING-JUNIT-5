import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A modern "floating card" panel: white rounded rectangle with a soft
 * drop shadow beneath it, like a Material Design / modern web UI card.
 * Use it exactly like a JPanel - add components to it, set a layout, etc.
 *
 * Usage:
 *   ShadowCard card = new ShadowCard();
 *   card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
 *   card.add(someLabel);
 */
public class ShadowCard extends JPanel {

    private int arc = 24;
    private int shadowSize = 14;
    private Color cardColor = Color.WHITE;

    public ShadowCard() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(26, 28, 26 + shadowSize, 28));
    }

    public void setCardColor(Color c) { this.cardColor = c; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight() - shadowSize;

        // layered translucent shadow (fake blur) beneath the card
        for (int i = shadowSize; i >= 1; i--) {
            int alpha = (int) (2 + 14 * (1 - (double) i / shadowSize));
            g2.setColor(new Color(40, 30, 90, alpha));
            float inset = i * 0.4f;
            g2.fill(new RoundRectangle2D.Float(inset, i, w - inset * 2, h - inset, arc, arc));
        }

        // the card itself
        g2.setColor(cardColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // faint top highlight edge for depth
        g2.setColor(new Color(255, 255, 255, 160));
        g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
