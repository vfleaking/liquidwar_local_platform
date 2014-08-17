package liquidwar.logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import liquidwar.logic.situation.Owner;

/**
 * 液滴
 * @author vfleaking
 *
 */
public class Droplet extends Life {
	/**
	 * 液滴的满血量
	 */
	static public final int FULL_BLOOD = 20;
	
	private int level;
	
	public Droplet(int id, Owner owner, int blood, int level, Vector pos, Vector vel) {
		super(id, owner, blood, pos, vel);
		setLevel(level);
	}
	
	/**
	 * 获取等级
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 设置等级
	 * @param level
	 */
	public void setLevel(int level) {
		this.level = level;
	}
	
	/**
	 * 升级
	 * @param level
	 */
	public void incLevel() {
		this.level++;
	}
	
	public double getShootingDistance() {
		return 300 + level * 3; 
	}
	
	public boolean canShoot(Droplet aim) {
		return getPos().getDist2(aim.getPos()) <= getShootingDistance() * getShootingDistance() + 1e-5;
	}
	
	@Override
	public void draw(Graphics2D g) {
		int r = 5;
		int x = (int)Math.round(getPos().getX() / 10.0);
		int y = (int)Math.round(getPos().getY() / 10.0);
		
		g.setStroke(new BasicStroke(2.0f));
		g.setColor(getOwner().getColor());
		g.fillArc(x - r, y - r, 2 * r, 2 * r, 90, (int)(360.0 * getBlood() / FULL_BLOOD));
		g.drawOval(x - r, y - r, 2 * r, 2 * r);
		
		int d = r * level / 100;
		if (d > r)
			d = r;
		
		g.setColor(Color.YELLOW);
		g.drawLine(x - d, y, x + d, y);
	}
}
