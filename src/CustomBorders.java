import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
/**
 * 
 * This is a border class, which extends from {@link AbstractBorder} and renders a new bubbled border, 
 * thus making the GUI interface prettier. 
 * @author: Mark Chen
 * @version: 1.1
 * 
 */
class TextBubbleBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;

    private java.awt.Color color;

    private int thickness;
    private int radius;
    private int pointerSize = 0;
    private int strokePad;
    private double pointerPadPercent = 0.5;

    private Insets insets = null;
    private BasicStroke stroke = null;
    
    int pointerSide = SwingConstants.TOP;
    RenderingHints hints;

    /**
     * Constructor of the {@link TextBubbleBorder}.
     * @param color
     */
    TextBubbleBorder(java.awt.Color color) {
        this(color, 2, 4, 0);
    }

    /**
     * This is another Constructor of the {@link TextBubbleBorder}.
     * @param color
     * @param thickness
     * @param radius
     * @param pointerSize
     */
    TextBubbleBorder(java.awt.Color color, int thickness, int radius, int pointerSize) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
        this.pointerSize = pointerSize;

        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        insets = new Insets(0, 0, 0, 0);

        setThickness(thickness);
    }

    public java.awt.Color getColor() {
        return color;
    }

    public TextBubbleBorder setColor(java.awt.Color color) {
        this.color = color;
        return this;
    }

    public double getPointerPadPercent() {
        return pointerPadPercent;
    }

    public TextBubbleBorder setPointerPadPercent(double percent) {
        this.pointerPadPercent = percent > 1 ? 1 : percent;
        pointerPadPercent = pointerPadPercent < 0 ? 0 : pointerPadPercent;
        return this;
    }

    public int getThickness() {
        return thickness;
    }

    public int getRadius() {
        return radius;
    }

    public int getPointerSize() {
        return pointerSize;
    }

    public TextBubbleBorder setThickness(int n) {
        thickness = n < 0 ? 0 : n;

        stroke = new BasicStroke(thickness);
        strokePad = thickness / 2;

        setPointerSize(pointerSize);
        return this;
    }

    /**
     * This method would automatically generate a ideal size of the pointer.
     * @param size
     *        The pointer's size.
     */
    public TextBubbleBorder setPointerSize(int size) {
        pointerSize = size < 0 ? 0 : size;

        int pad = radius / 2 + strokePad;

        int pointerSidePad = pad + pointerSize + strokePad;
        int left, right, bottom, top;
        left = right = bottom = top = pad;
        switch (pointerSide) {
            case SwingConstants.TOP:
                top = pointerSidePad;
                break;
            case SwingConstants.LEFT:
                left = pointerSidePad;
                break;
            case SwingConstants.RIGHT:
                right = pointerSidePad;
                break;
            default:
            case SwingConstants.BOTTOM:
                bottom = pointerSidePad;
                break;
        }
        insets.set(top, left, bottom, right);
        return this;
    }

    public int getPointerSide() {
        return pointerSide;
    }

    public TextBubbleBorder setPointerSide(int pointerSide) {
        this.pointerSide = pointerSide;
        setPointerSize(pointerSize);
        return this;
    }

    public TextBubbleBorder setRadius(int radius) {
        this.radius = radius;
        setPointerSize(pointerSize);
        return this;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(c.getBackground());

        int bottomLineY = height - thickness - pointerSize;

        RoundRectangle2D.Double bubble;
        Polygon pointer = new Polygon();


        {
            int rx, ry, rw, rh;
            rx = ry = strokePad;
            rw = width - thickness;
            rh = height - thickness;

            switch (pointerSide) {
                case SwingConstants.LEFT:
                    rx += pointerSize;
                case SwingConstants.RIGHT:
                    rw -= pointerSize;
                    break;

                case SwingConstants.TOP:
                    ry += pointerSize;
                case SwingConstants.BOTTOM:
                default:
                    rh -= pointerSize;
                    break;
            }

            bubble = new RoundRectangle2D.Double(rx, ry, rw, rh, radius, radius);

            // 计算偏移
            int pointerPad;

            if (pointerSide == SwingConstants.LEFT || pointerSide == SwingConstants.RIGHT) {
                pointerPad = (int) (pointerPadPercent * (height - radius * 2 - pointerSize));
            } else {
                pointerPad = (int) (pointerPadPercent * (width - radius * 2 - pointerSize));
            }

            // 设置三角
            int basePad = strokePad + radius + pointerPad;

            switch (pointerSide) {

                case SwingConstants.LEFT:
                    pointer.addPoint(rx, basePad);// top
                    pointer.addPoint(rx, basePad + pointerSize);// bottom
                    pointer.addPoint(strokePad, basePad + pointerSize / 2);
                    break;
                case SwingConstants.RIGHT:
                    pointer.addPoint(rw, basePad);// top
                    pointer.addPoint(rw, basePad + pointerSize);// bottom
                    pointer.addPoint(width - strokePad, basePad + pointerSize / 2);
                    break;

                case SwingConstants.TOP:
                    pointer.addPoint(basePad, ry);// left
                    pointer.addPoint(basePad + pointerSize, ry);// right
                    pointer.addPoint(basePad + (pointerSize / 2), strokePad);
                    break;
                default:
                case SwingConstants.BOTTOM:
                    pointer.addPoint(basePad, rh);// left
                    pointer.addPoint(basePad + pointerSize, rh);// right
                    pointer.addPoint(basePad + (pointerSize / 2), height - strokePad);
                    break;
            }
        }

        Area area = new Area(bubble);
        area.add(new Area(pointer));

        g2.setRenderingHints(hints);

        Area spareSpace = new Area(new Rectangle(0, 0, width, height));
        spareSpace.subtract(area);
        g2.setClip(spareSpace);
        g2.clearRect(0, 0, width, height);

        g2.setClip(null);

        g2.setColor(color);
        g2.setStroke(stroke);
        g2.draw(area);
    }
}

class CustomMarginBorder {
    private JPanel contentPane;
    private CustomBorder customBorder;

    private void displayGUI() {
        JFrame frame = new JFrame("Custom Arrow Border Example");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        customBorder = new CustomBorder(java.awt.Color.BLUE, 5);
        contentPane = new JPanel();
        contentPane.setBorder(customBorder);

        frame.setContentPane(contentPane);
        frame.setSize(300, 300);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}

class CustomBorder extends AbstractBorder {
    private java.awt.Color borderColor;
    private int gap;

    public CustomBorder(java.awt.Color color, int g) {
        borderColor = color;
        gap = g;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        Graphics2D g2d = null;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            g2d.setColor(borderColor);
            // Left Border
            g2d.draw(new Line2D.Double((double) x + 10, (double) y + 10, (double) x + 10, (double) y + 20));
            g2d.draw(new Line2D.Double((double) x + 10, (double) y + 10, (double) x + 20, (double) y + 10));
            // Right Border
            g2d.draw(new Line2D.Double((double) width - 10, (double) y + 10, (double) width - 10, (double) y + 20));
            g2d.draw(new Line2D.Double((double) width - 10, (double) y + 10, (double) width - 20, (double) y + 10));
            // Lower Left Border
            g2d.draw(new Line2D.Double((double) x + 10, (double) height - 10, (double) x + 20, (double) height - 10));
            g2d.draw(new Line2D.Double((double) x + 10, (double) height - 10, (double) x + 10, (double) height - 20));
            // Lower Right Border
            g2d.draw(new Line2D.Double((double) width - 10, (double) height - 10, (double) width - 20,
                    (double) height - 10));
            g2d.draw(new Line2D.Double((double) width - 10, (double) height - 10, (double) width - 10,
                    (double) height - 20));
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(gap, gap, gap, gap)));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = gap;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}