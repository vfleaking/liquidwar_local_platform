package liquidwar.logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import liquidwar.logic.situation.Owner;


public class Brain extends Life {
	/**
	 * 大脑半径
	 */
	static public final double RADIUS = 500;
	/**
	 * 大脑满血量
	 */
	static public final int FULL_BLOOD = 5000;
	
	private Image image;

	public Brain(int id, Owner owner, int blood, Vector pos, Vector vel, Image image) {
		super(id, owner, blood, pos, vel);
		this.image = image;
	}
	
	@Override
	public void draw(Graphics2D g) {
		int x = (int)Math.round(getPos().getX() / 10.0);
		int y = (int)Math.round(getPos().getY() / 10.0);
		
		int r = (int)Math.round(RADIUS / 10.0);
		
		g.drawImage(image, x - r, y - r, 2 * r, 2 * r, null);
		
		g.setStroke(new BasicStroke(5.0f));
		g.setColor(Color.BLACK);
		g.drawOval(x - r, y - r, 2 * r, 2 * r);
		
		g.setStroke(new BasicStroke(6.0f));
		g.setColor(Color.GREEN);
		g.drawArc(x - r, y - r, 2 * r, 2 * r, 90, (int)(360.0 * getBlood() / FULL_BLOOD));
	}
}
