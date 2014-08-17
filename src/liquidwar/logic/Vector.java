package liquidwar.logic;

/**
 * 向量
 * @author vfleaking
 * 
 */
public class Vector {
	private int x, y;
	
	public static final Vector ZERO_VECTOR = new Vector(0, 0);
	
	/**
	 * @param x 向量的横坐标
	 * @param y 向量的纵坐标
	 */
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * 获取向量的横坐标
	 * @return 向量的横坐标
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * 获取向量的纵坐标
	 * @return 向量的纵坐标
	 */
	public int getY() {
		return this.y;
	}
	
	/**
	 * 向量加法
	 * @param lhs
	 * @param rhs
	 * @return 返回lhs加rhs
	 */
	public static Vector add(Vector lhs, Vector rhs) {
		return new Vector(lhs.x + rhs.x, lhs.y + rhs.y);
	}
	/**
	 * 向量减法
	 * @param lhs
	 * @param rhs
	 * @return 返回lhs减rhs
	 */
	public static Vector sub(Vector lhs, Vector rhs) {
		return new Vector(lhs.x - rhs.x, lhs.y - rhs.y);
	}
	/**
	 * 向量点积
	 * @param lhs
	 * @param rhs
	 * @return 返回lhs点乘rhs
	 */
	public static int dot(Vector lhs, Vector rhs) {
		return lhs.x * rhs.x + lhs.y * rhs.y;
	}
	/**
	 * 向量叉积
	 * @param lhs
	 * @param rhs
	 * @return 返回lhs叉乘rhs
	 */
	public static int cross(Vector lhs, Vector rhs) {
		return lhs.x * rhs.y - lhs.y * rhs.x;
	}
	
	/**
	 * 获取向量的模长的平方
	 * @param v
	 * @return 向量的模长的平方
	 */
	public int getLen2() {
		return x * x + y * y;
	}
	/**
	 * 获取两点间的距离的平方
	 * @param lhs
	 * @param rhs
	 * @return 两点间的距离的平方
	 */
	public double getDist2(Vector rhs) {
		return Vector.sub(this, rhs).getLen2();
	}
	
	public Vector getOpposite() {
		return new Vector(-x, -y);
	}
	
	@Override
	public boolean equals(java.lang.Object obj) {
		try {
			Vector rhs = (Vector)obj;
			return this.x == rhs.x && this.y == rhs.y;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
}
