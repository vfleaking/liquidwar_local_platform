package liquidwar.logic.situation;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import liquidwar.logic.Vector;

public class Movement {
	String errMsg = null;
	List<Shooting> shootings = new ArrayList<Shooting>();
	Map<Integer, Vector> dropletsNewPos = new TreeMap<Integer, Vector>();
	List<Integer> newDropletsPosY = new ArrayList<Integer>();
	
	public Movement() {
	}
	public Movement(String errMsg) {
		this.errMsg = errMsg;
	}
	
	public static class Shooting {
		int shooter, target;
		
		public Shooting(int shooter, int target) {
			this.shooter = shooter;
			this.target = target;
		}
	}
}
