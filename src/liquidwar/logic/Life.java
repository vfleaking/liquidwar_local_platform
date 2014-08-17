package liquidwar.logic;

import liquidwar.logic.situation.Owner;

public class Life extends Object {
	private int id;
	private Owner owner;
	private int blood;

	public Life(int id, Owner owner, int blood, Vector pos, Vector vel) {
		super(pos, vel);
		setId(id);
		setOwner(owner);
		setBlood(blood);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Owner getOwner() {
		return owner;
	}
	public void setOwner(Owner owner) {
		this.owner = owner;
	}
	
	public int getBlood() {
		return blood;
	}
	public void setBlood(int blood) {
		this.blood = blood;
	}
	public void decBlood() {
		this.blood--;
	}
}
