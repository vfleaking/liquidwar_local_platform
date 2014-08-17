package liquidwar.logic;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 物体
 * @author vfleaking
 *
 */
public class Object implements Cloneable {
	private Vector pos;
	private Vector vel;
	
	/**
	 * @param pos 位置
	 * @param vel 瞬时速度
	 */
	public Object(Vector pos, Vector vel) {
		this.pos = pos;
		this.vel = vel;
	}
	
	/**
	 * 获取物体的位置
	 * @return 物体的位置
	 */
	public Vector getPos() {
		return this.pos;
	}
	/**
	 * 设置物体的位置
	 * @param pos 新的物体的位置
	 */
	public void setPos(Vector pos) {
		this.pos = pos;
	}
	
	/**
	 * 获取物体的速度
	 * @return 物体的速度
	 */
	public Vector getVel() {
		return this.vel;
	}
	/**
	 * 设置物体的速度
	 * @param vel 新的物体的速度
	 */
	public void setVel(Vector vel) {
		this.vel = vel;
	}
	
	/**
	 * 增加一个位移
	 * @param pos 增加的位移
	 */
	public void addPos(Vector pos) {
		this.pos = Vector.add(this.pos, pos);
	}
	/**
	 * 增加一个速度
	 * @param vel 增加的速度
	 */
	public void addVel(Vector vel) {
		this.vel = Vector.add(this.vel, vel);
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillOval(pos.getX() - 5, pos.getY() - 5, 10, 10);
	}
	
	@Override
	public java.lang.Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
