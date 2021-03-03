import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

class RightArrowBubble extends JPanel {
   private static final long serialVersionUID = -5389178141802153305L;
   private int strokeThickness = 3;
   private int radius = 10;
   private int arrowSize = 12;
   private int padding = strokeThickness / 2;
   @Override
   protected void paintComponent(final Graphics g) {
      final Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(java.awt.Color.GREEN);
      int bottomLineY = getHeight() - strokeThickness;
      int width = getWidth() - arrowSize - (strokeThickness * 2);
      g2d.fillRect(padding, padding, width, bottomLineY);
      RoundRectangle2D.Double rect = new RoundRectangle2D.Double(padding, padding, width, bottomLineY,  radius, radius);
      Polygon arrow = new Polygon();
      arrow.addPoint(width, 8);
      arrow.addPoint(width + arrowSize, 10);
      arrow.addPoint(width, 12);
      Area area = new Area(rect);
      area.add(new Area(arrow));
      g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
      g2d.setStroke(new BasicStroke(strokeThickness));
      g2d.draw(area);
   }
}

class LeftArrowBubble extends JPanel {
    private static final long serialVersionUID = -5389178141802153305L;
    private int radius = 10;
    private int arrowSize = 12;
    private int strokeThickness = 3;
    private int padding = strokeThickness / 2;
    @Override
    protected void paintComponent(final Graphics g) {
       final Graphics2D g2d = (Graphics2D) g;
       g2d.setColor(java.awt.Color.GRAY);
       int x = padding + strokeThickness + arrowSize;
       int width = getWidth() - arrowSize - (strokeThickness * 2);
       int bottomLineY = getHeight() - strokeThickness;
       g2d.fillRect(x, padding, width, bottomLineY);
       g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON));
       g2d.setStroke(new BasicStroke(strokeThickness));
       RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x, padding, width, bottomLineY, radius, radius);
       Polygon arrow = new Polygon();
       arrow.addPoint(20, 8);
       arrow.addPoint(0, 10);
       arrow.addPoint(20, 12);
       Area area = new Area(rect);
       area.add(new Area(arrow));
       g2d.draw(area);
    }
 }