import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class LoadingPanel extends JPanel {
	
	private static final long serialVersionUID = 1551571546L;
	
	private Timer timer;
	private int delay;
	private int startAngle;
	private int arcAngle = 0;
	private int orientation;
	
	public static final int CLOCKWISE = 0;
	public static final int ANTICLOCKWISE = 1;

	public LoadingPanel() {
		this.delay = 50;
		this.orientation = CLOCKWISE;
		init();
	}
	
	public LoadingPanel(int delay) {
		this.delay = delay;
		this.orientation = CLOCKWISE;
		init();
	}
	
	public LoadingPanel(int delay, int orientation) {
		this.delay = delay;
		this.orientation = orientation;
		init();
	}
	
	@Override
	public void show() {
		this.timer.start();
    }
    
    public void close() {
        this.timer.stop();
    }
	
	/**
	 * @param orientation	set the direction of rotation
	 * 
	 * @beaninfo
	 *        enum: CLOCKWISE LodingPanel.CLOCKWISE
	 *        		ANTICLOCKWISE LodingPanel.ANTICLOCKWISE
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
	private void init() {
		this.timer = new Timer(delay, new ReboundListener());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawArc(g);
	}
	
	private void drawArc(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		//抗锯齿 
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int width = getWidth();
		int height = getHeight();
		//设置画笔颜色
		g2d.setColor(Color.WHITE);
		g2d.drawArc(width / 2 - 110, height / 2 - 110, 20 + 200, 20 + 200, 0, 360);
		g2d.setColor(Color.RED);
		g2d.fillArc(width / 2 - 110, height / 2 - 110, 20 + 200, 20 + 200, startAngle, arcAngle);
		g2d.setColor(Color.WHITE);
		g2d.fillArc(width / 2 - 105, height / 2 - 105, 20 + 190, 20 + 190, 0, 360);
		g2d.dispose();
	}
	
	private class ReboundListener implements ActionListener {

		private int o = 0;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (startAngle < 360) {
				//控制每个DELAY周期旋转的角度，+ 为逆时针  - 为顺时针
				switch (orientation) {
				case CLOCKWISE:
					startAngle = startAngle + 5;
					break;
				case ANTICLOCKWISE:
					startAngle = startAngle - 5;
					break;
				default:
					startAngle = startAngle + 5;
					break;
				}
			} else {
				startAngle = 0;
			}
			
			if (o == 0) {
				if (arcAngle >= 355) {
					o = 1;
					orientation = CLOCKWISE;
				} else {
					if (orientation == CLOCKWISE) {
						arcAngle += 5;
					}
				}
			} else {
				if (arcAngle <= 5) {
					o = 0;
					orientation = ANTICLOCKWISE;
				}else {
					if (orientation == ANTICLOCKWISE) {
						arcAngle -= 5;
					}
				}
			}
			
			repaint();
		}
    }
}