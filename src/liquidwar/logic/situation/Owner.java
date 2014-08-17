package liquidwar.logic.situation;

import java.awt.Color;
import java.security.InvalidParameterException;

public enum Owner {
	RED, BLUE, NONE;
	
	public Owner getEnemy() {
		switch (this) {
		case RED:
			return Owner.BLUE;
		case BLUE:
			return Owner.RED;
		case NONE:
		default:
			throw new InvalidParameterException();
		}
	}
	public Color getColor() {
		switch (this) {
		case RED:
			return Color.RED;
		case BLUE:
			return Color.BLUE;
		case NONE:
		default:
			throw new InvalidParameterException();
		}
	}
	public String getMessageHeader() {
		switch (this) {
		case RED:
			return "Red > ";
		case BLUE:
			return "Blue> ";
		case NONE:
		default:
			throw new InvalidParameterException();
		}
	}
	public int getOwnerID() {
		switch (this) {
		case RED:
			return 1;
		case BLUE:
			return 2;
		case NONE:
		default:
			throw new InvalidParameterException();
		}
	}
}
