package liquidwar.logic.situation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import liquidwar.logic.Brain;
import liquidwar.logic.Droplet;
import liquidwar.logic.Vector;

/**
 * 游戏局面
 * @author vfleaking
 *
 */
public class GameSituation {
	/**
	 * 游戏总时间
	 */
	final int totalGameTime = 1800;
	
	/**
	 * 地图长度
	 */
	final int mapHeight = 10000;
	/**
	 * 地图宽度
	 */
	final int mapWidth = 10000;
	
	/**
	 * 液滴的最大移动距离
	 */
	final int maxDropletDisplacement = 30; 
	
	/**
	 * 造一个液滴的时间
	 */
	final int makeDropletTime = 5;
	
	List<Vector> dispList = new ArrayList<Vector>();
	
	public UserInputHandler userInputHandler = new UserInputHandler();
	PrintStream messageStream;
	ByteArrayOutputStream logByteOutputStream = new ByteArrayOutputStream();
	DataOutputStream logOutputStream = new DataOutputStream(logByteOutputStream);
	Drawer drawer = new Drawer();
	
	Brain redBrain, blueBrain;
	List<Droplet> redDroplets = new ArrayList<Droplet>(), blueDroplets = new ArrayList<Droplet>();
	int nRedWaitingDroplets = 0, nBlueWaitingDroplets = 0;
	int redMakeDropletRestTime = 1, blueMakeDropletRestTime = 1;
	int redScore, blueScore;
	int restTime;
	
	boolean enableHumanHelp = true;
	Vector choosenRectStartPos = null, choosenRectEndPos = null;
	List<Droplet> choosenDroplets = Collections.synchronizedList(new ArrayList<Droplet>());
	Vector choosenTargetPos = null;
	
	Set<Character> pressingCmds = Collections.synchronizedSet(new HashSet<Character>());
	Set<Character> pressedCmds = Collections.synchronizedSet(new HashSet<Character>());
	
	Movement redMovement, blueMovement;
	
	class ShootingLine {
		Vector start, end;
		Owner shooter;
		
		public ShootingLine(Vector start, Vector end, Owner shooter) {
			this.start = start;
			this.end = end;
			this.shooter = shooter;
		}
		
		public void draw(Graphics2D g) {
			g.setStroke(new BasicStroke(2.0f));
			g.setColor(shooter.getColor());
			
			int x1 = (int)Math.round(start.getX() / 10.0);
			int y1 = (int)Math.round(start.getY() / 10.0);
			int x2 = (int)Math.round(end.getX() / 10.0);
			int y2 = (int)Math.round(end.getY() / 10.0);
			g.drawLine(x1, y1, x2, y2);
		}
	}
	List<ShootingLine> shootingLines = new ArrayList<ShootingLine>();
	
	boolean isOver;
	Owner winner;
	
	Random random = new Random();
	
	List<Droplet> getDroplets(Owner owner) {
		switch (owner) {
		case RED:
			return redDroplets;
		case BLUE:
			return blueDroplets;
		case NONE:
		default:
			return null;
		}
	}
	
	Droplet getDroplet(Owner owner, int id) {
		switch (owner) {
		case RED:
			if (!(0 <= id && id < redDroplets.size())) {
				return null;
			}
			return redDroplets.get(id);
		case BLUE:
			if (!(0 <= id && id < blueDroplets.size())) {
				return null;
			}
			return blueDroplets.get(id);
		case NONE:
		default:
			return null;
		}
	}
	
	int getNWaitingDroplets(Owner owner) {
		switch (owner) {
		case RED:
			return nRedWaitingDroplets;
		case BLUE:
			return nBlueWaitingDroplets;
		case NONE:
		default:
			return 0;
		}
	}
	
	Movement getMovement(Owner owner) {
		switch (owner) {
		case RED:
			return redMovement;
		case BLUE:
			return blueMovement;
		case NONE:
		default:
			return null;
		}
	}
	
	/**
	 * 用户输入处理器
	 * @author vfleaking
	 *
	 */
	public class UserInputHandler implements KeyListener, MouseListener, MouseMotionListener {
		
		public synchronized void keyPressed(KeyEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED && !Character.isWhitespace(e.getKeyChar())) {
				pressingCmds.add(e.getKeyChar());
				pressedCmds.add(e.getKeyChar());
			}
		}

		public synchronized void keyReleased(KeyEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED && !Character.isWhitespace(e.getKeyChar())) {
				pressingCmds.remove(e.getKeyChar());
			}
		}

		public synchronized void keyTyped(KeyEvent e) {
		}
		
		public synchronized void allKeyReleased() {
			if (!enableHumanHelp) {
				return;
			}
			pressingCmds.clear();
		}
		
		public boolean isOutOfRange(Vector pos) {
			return !(0 <= pos.getX() && pos.getX() <= 10000 && 0 <= pos.getY() && pos.getY() <= 10000);
		}

		@Override
		public synchronized void mouseClicked(MouseEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				Vector pos = drawer.transformVector(new Vector(e.getX(), e.getY()));
				if (isOutOfRange(pos)) {
					return;
				}
				
				Droplet target = null;
				for (Droplet droplet : redDroplets) {
					if (droplet == null) {
						continue;
					}
					if (droplet.getPos().getDist2(pos) <= 100 * 100) {
						target = droplet;
						break;
					}
				}
				if (target == null) {
					if (!choosenDroplets.isEmpty()) {
						choosenTargetPos = pos;
					}
				} else {
					choosenDroplets.clear();
					choosenDroplets.add(target);
				}
			} else if (e.getButton() == MouseEvent.BUTTON3) {
				choosenDroplets.clear();
			}
		}

		@Override
		public synchronized void mousePressed(MouseEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			choosenRectStartPos = drawer.transformVector(new Vector(e.getX(), e.getY()));
		}

		@Override
		public synchronized void mouseReleased(MouseEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			choosenRectStartPos = null;
			choosenRectEndPos = null;
		}

		@Override
		public synchronized void mouseEntered(MouseEvent e) {
		}

		@Override
		public synchronized void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!enableHumanHelp) {
				return;
			}
			if (choosenRectStartPos == null) {
				return;
			}
			choosenRectEndPos = drawer.transformVector(new Vector(e.getX(), e.getY()));
			
			int x1 = Math.min(choosenRectStartPos.getX(), choosenRectEndPos.getX());
			int y1 = Math.min(choosenRectStartPos.getY(), choosenRectEndPos.getY());
			int x2 = Math.max(choosenRectStartPos.getX(), choosenRectEndPos.getX());
			int y2 = Math.max(choosenRectStartPos.getY(), choosenRectEndPos.getY());
			
			choosenDroplets.clear();
			for (Droplet droplet : redDroplets) {
				if (droplet == null) {
					continue;
				}
				int x = droplet.getPos().getX();
				int y = droplet.getPos().getY();
				if (x1 <= x && x <= x2 && y1 <= y && y <= y2) {
					choosenDroplets.add(droplet);
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
	
	/**
	 * 游戏绘制器
	 * @author vfleaking
	 *
	 */
	class Drawer {
		private int len = 1000;
		private BufferedImage image;
		private Graphics2D g;
		
		public Vector transformVector(Vector v) {
			return new Vector((int)Math.round((double)v.getX() / len * 10000), (int)Math.round((double)v.getY() / len * 10000));
		}
		
		private void drawCenterString(String str, int x, int y) {
			int width = g.getFontMetrics().stringWidth(str);
			g.drawString(str, x - width / 2, y);
		}
		
		private void drawRestTime() {
			g.setFont(g.getFont().deriveFont(30.0f));
			g.setColor(Color.BLACK);
			drawCenterString(String.format("Rest Time: %d", restTime), 500, 1000);
		}
		
		private void drawBrain() {
			redBrain.draw(g);
			blueBrain.draw(g);
		}
		
		private void drawShootingLine() {
			Collections.shuffle(shootingLines, random);
			for (ShootingLine shootingLine : shootingLines) {
				shootingLine.draw(g);
			}
		}
		
		private void drawObjects() {
			for (Droplet droplet : redDroplets) {
				if (droplet == null) {
					continue;
				}
				droplet.draw(g);
			}
			for (Droplet droplet : blueDroplets) {
				if (droplet == null) {
					continue;
				}
				droplet.draw(g);
			}
		}
		
		private void drawChoosen() {
			for (Droplet droplet : choosenDroplets) {
				int r = 6;
				int x = (int)Math.round(droplet.getPos().getX() / 10.0);
				int y = (int)Math.round(droplet.getPos().getY() / 10.0);
				g.setStroke(new BasicStroke(2.0f));
				g.setColor(Color.YELLOW);
				g.drawOval(x - r, y - r, 2 * r, 2 * r);
			}
			
			if (choosenRectStartPos != null && choosenRectEndPos != null) {
				g.setColor(Color.BLACK);
				double rx = Math.min(choosenRectStartPos.getX(),choosenRectEndPos.getX());
				double ry = Math.min(choosenRectStartPos.getY(),choosenRectEndPos.getY());
				double width = Math.abs(choosenRectStartPos.getX() - choosenRectEndPos.getX());
				double height = Math.abs(choosenRectStartPos.getY() - choosenRectEndPos.getY());
				g.drawRect((int)Math.round(rx / 10), (int)Math.round(ry / 10), (int)Math.round(width / 10), (int)Math.round(height / 10));
			}
		}
		
		private void drawScore() {
			g.setFont(g.getFont().deriveFont(50.0f));
			
			g.setColor(Color.RED);
			g.drawString(String.format("Score: %d", redScore), 0, 50);
			
			g.setColor(Color.BLUE);
			g.drawString(String.format("Score: %d", blueScore), 600, 50);
		}
		
		private void drawWinner() {
			g.setFont(g.getFont().deriveFont(70.0f));
			
			switch (winner) {
			case RED:
				g.setColor(Color.RED);
				drawCenterString("Winner: Red", 500, 500);
				break;
			case BLUE:
				g.setColor(Color.BLUE);
				drawCenterString("Winner: Blue", 500, 500);
				break;
			case NONE:
				g.setColor(Color.GRAY);
				drawCenterString("Draw", 500, 500);
				break;
			}
		}
		
		private void redrawImage() {
			drawRestTime();
			drawBrain();
			drawShootingLine();
			drawObjects();
			drawChoosen();
			drawScore();
			
			if (isOver) {
				drawWinner();
			}
		}
		
		public synchronized void record() {
			image = new BufferedImage(len, len, BufferedImage.TYPE_4BYTE_ABGR_PRE);
			g = image.createGraphics();
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			g.scale(len / 1000.0, len / 1000.0);
			
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			g.setColor(new Color(0, 128, 0));
			g.fillRect(0, 0, 1000, 1000);
			
			redrawImage();
		}
		
		public synchronized void draw(Graphics2D g, int len) {
			if (this.len != len) {
				this.len = len;
			}
			g.drawImage(image, 0, 0, len, len, null);
		}
	}	
	
	/**
	 * 默认构造函数
	 */
	public GameSituation(PrintStream messageStream) {
		this.messageStream = messageStream;
		
		for (int x = -maxDropletDisplacement; x <= maxDropletDisplacement; x++) {
			for (int y = -maxDropletDisplacement; y <= maxDropletDisplacement; y++) {
				if (x * x + y * y <= maxDropletDisplacement * maxDropletDisplacement) {
					dispList.add(new Vector(x, y));
				}
			}
		}
		Collections.sort(dispList, new Comparator<Vector>() {
			@Override
			public int compare(Vector a, Vector b) {
				int c = Double.compare(a.getLen2(), b.getLen2());
				if (c == 0) {
					c = Integer.compare(a.getX(), b.getX());
				}
				if (c == 0) {
					c = Integer.compare(a.getY(), b.getY());
				}
				return c;
			}
		});
		Collections.reverse(dispList);
		
		File logFileDirectory = new File("log/");
		if (!logFileDirectory.isDirectory()) {
			if (!logFileDirectory.mkdir()) {
				messageStream.println("couldn't create directory " + logFileDirectory.getAbsolutePath());
				gameOver();
				return;
			}
		}
		
		try {
			Image redBrainImage = ImageIO.read(this.getClass().getResource("redBrain.png"));
			Image blueBrainImage = ImageIO.read(this.getClass().getResource("blueBrain.png"));
			redBrain = new Brain(0, Owner.RED, Brain.FULL_BLOOD, new Vector(1000, 5000), Vector.ZERO_VECTOR, redBrainImage);
			blueBrain = new Brain(1, Owner.BLUE, Brain.FULL_BLOOD, new Vector(9000, 5000), Vector.ZERO_VECTOR, blueBrainImage);
		} catch (IOException e) {
		}

		restTime = totalGameTime;

		isOver = false;
		winner = Owner.NONE;
		
		messageStream.println("started");
	}
	
	boolean canGoNext() {
		return !isOver;
	}
	
	public void goNext() {
		if (!canGoNext())
		{
			drawer.record();
			return;
		}
		simulate();
		drawer.record();
	}
	
	void calcWinnerAndGameOver(boolean isRedLost, boolean isBlueLost, boolean considerBloodAndScore) {
		if (isRedLost && isBlueLost) {
			if (considerBloodAndScore) {
				if (redBrain.getBlood() != blueBrain.getBlood()) {
					if (redBrain.getBlood() > blueBrain.getBlood()) {
						winner = Owner.RED;
					} else {
						winner = Owner.BLUE;
					}
				} else if (redScore != blueScore) {
					if (redScore > blueScore) {
						winner = Owner.RED;
					} else {
						winner = Owner.BLUE;
					}
				} else {
					winner = Owner.NONE;
				}
			} else {
				winner = Owner.NONE;
			}
		} else if (isRedLost) {
			winner = Owner.BLUE;
		} else if (isBlueLost) {
			winner = Owner.RED;
		}
		gameOver();
	}
	
	void printSitu(Owner owner, PrintStream printStream) {
		Movement enemyMovement = getMovement(owner.getEnemy());
		
		printStream.printf("%d\n", enemyMovement.shootings.size());
		for (Movement.Shooting shooting : enemyMovement.shootings) {
			printStream.printf("%d %d\n", shooting.shooter, shooting.target);
		}
		
		printStream.printf("%d\n", enemyMovement.dropletsNewPos.size());
		for (Map.Entry<Integer, Vector> entry : enemyMovement.dropletsNewPos.entrySet()) {
			int x = entry.getValue().getX();
			int y = entry.getValue().getY();
			if (owner == Owner.BLUE) {
				x = mapWidth - x;
			}
			printStream.printf("%d %d %d\n", entry.getKey(), x, y);
		}
		
		printStream.printf("%d\n", enemyMovement.newDropletsPosY.size());
		for (Integer newDropletPosY : enemyMovement.newDropletsPosY) {
			printStream.printf("%d\n", newDropletPosY);
		}
		
		if (owner == Owner.RED) {
			printStream.printf("%d\n", choosenDroplets.size());
			for (Droplet droplet : choosenDroplets) {
				printStream.printf("%d\n", droplet.getId());
			}
			
			if (choosenTargetPos == null) {
				printStream.printf("-1 -1\n");
			} else {
				int x = choosenTargetPos.getX();
				int y = choosenTargetPos.getY();
				printStream.printf("%d %d\n", x, y);
			}
			
			printStream.printf("%d\n", pressedCmds.size());
			for (Character cmd : pressedCmds) {
				printStream.printf("%c\n", cmd);
			}
			pressedCmds.clear();
		} else {
			printStream.printf("0\n");
			printStream.printf("-1 -1\n");
			printStream.printf("0\n");
		}
		
		printStream.flush();
	}
	
	boolean checkMovementValid(Owner owner) {
		Movement movement = owner == Owner.RED ? redMovement : blueMovement;
		if (movement.errMsg != null) {
			return false;
		}
		
		Set<Droplet> shooterSet = new HashSet<Droplet>();
		for (Movement.Shooting shooting : movement.shootings) {
			Droplet shooterDroplet = getDroplet(owner, shooting.shooter);
			Droplet targetDroplet = getDroplet(owner.getEnemy(), shooting.target);
			if (shooterDroplet == null) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "invalid shooter.";
				return false;
			}
			if (targetDroplet == null) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "invalid target.";
				return false;
			}
			if (shooterSet.contains(shooterDroplet)) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "try shooting multiple targets.";
				return false;
			}
			if (!shooterDroplet.canShoot(targetDroplet)) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "out of firing range.";
				return false;
			}
			shooterSet.add(shooterDroplet);
		}
		
		for (Map.Entry<Integer, Vector> entry : movement.dropletsNewPos.entrySet()) {
			Droplet droplet = getDroplet(owner, entry.getKey());
			Vector newPos = entry.getValue();
			if (droplet == null) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "invalid move.";
				return false;
			}
			if (droplet.getPos().getDist2(newPos) > maxDropletDisplacement * maxDropletDisplacement) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "move too fast.";
				return false;
			}
			if (0 > newPos.getX() || newPos.getX() > mapWidth) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "move out of range.";
				return false;
			}
			if (0 > newPos.getY() || newPos.getY() > mapHeight) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "move out of range.";
				return false;
			}
		}
		
		if (movement.newDropletsPosY.size() > getNWaitingDroplets(owner)) {
			movement.errMsg = Owner.BLUE.getMessageHeader() + "too many new droplets.";
			return false;
		}
		for (Integer newDropletPosY : movement.newDropletsPosY) {
			if (0 > newDropletPosY || newDropletPosY > mapHeight) {
				movement.errMsg = Owner.BLUE.getMessageHeader() + "the new droplet is out of range.";
				return false;
			}
		}
		
		return true;
	}
	
	void recordDisp(Vector d) throws IOException {
		int p = dispList.indexOf(d);
		if (p == -1) {
			logOutputStream.writeByte(255);
		} else {
			if (p < 240) {
				logOutputStream.writeByte(15 + p);
			} else {
				logOutputStream.writeByte((p - 200) >> 8);
				logOutputStream.writeByte((p - 200) & 255);
			}
			for (int i = p; i > 0; i--) {
				dispList.set(i, dispList.get(i - 1));
			}
			dispList.set(0, d);
		}
	}
	
	void recordMovement(Owner owner) throws IOException {
		Movement movement = owner == Owner.RED ? redMovement : blueMovement;
		if (movement.errMsg == null) {
			logOutputStream.writeInt(0);
		} else {
			messageStream.println(movement.errMsg);
			logOutputStream.writeInt(movement.errMsg.length());
			logOutputStream.writeBytes(movement.errMsg);
			return;
		}
		logOutputStream.writeShort(movement.shootings.size());
		for (Movement.Shooting shooting : movement.shootings) {
			logOutputStream.writeShort(shooting.shooter);
			logOutputStream.writeShort(shooting.target);
		}
		for (Droplet droplet : getDroplets(owner)) {
			if (droplet == null) {
				continue;
			}
			Vector pos = movement.dropletsNewPos.get(droplet.getId());
			if (pos == null) {
				recordDisp(null);
			} else {
				recordDisp(Vector.sub(pos, droplet.getPos()));
			}
		}
		logOutputStream.writeShort(movement.newDropletsPosY.size());
		for (int y : movement.newDropletsPosY) {
			logOutputStream.writeShort(y);
		}
	}
	
	void simulate() {
		boolean isRedLost = !checkMovementValid(Owner.RED);
		boolean isBlueLost = !checkMovementValid(Owner.BLUE);
		
		try {
			recordMovement(Owner.RED);
			recordMovement(Owner.BLUE);
		} catch (IOException e) {
			messageStream.println("failed to record the log");
		}
		
		if (isRedLost || isBlueLost) {
			calcWinnerAndGameOver(isRedLost, isBlueLost, false);
			return;
		}
		
		for (Droplet droplet : redDroplets) {
			if (droplet == null) {
				continue;
			}
			if (droplet.getPos().getDist2(blueBrain.getPos()) <= Brain.RADIUS * Brain.RADIUS && blueBrain.getBlood() > 0) {
				blueBrain.decBlood();
			}
		}
		for (Droplet droplet : blueDroplets) {
			if (droplet == null) {
				continue;
			}
			if (droplet.getPos().getDist2(redBrain.getPos()) <= Brain.RADIUS * Brain.RADIUS && redBrain.getBlood() > 0) {
				redBrain.decBlood();
			}
		}
		
		isRedLost = redBrain.getBlood() == 0;
		isBlueLost = blueBrain.getBlood() == 0;
		if (isRedLost || isBlueLost) {
			calcWinnerAndGameOver(isRedLost, isBlueLost, true);
			return;
		}
		
		shootingLines.clear();
		for (Movement.Shooting shooting : redMovement.shootings) {
			if (blueDroplets.get(shooting.target).getBlood() > 0) {
				blueDroplets.get(shooting.target).decBlood();
				redDroplets.get(shooting.shooter).incLevel();
				shootingLines.add(new ShootingLine(redDroplets.get(shooting.shooter).getPos(), blueDroplets.get(shooting.target).getPos(), Owner.RED));
			}
		}
		for (Movement.Shooting shooting : blueMovement.shootings) {
			if (redDroplets.get(shooting.target).getBlood() > 0) {
				redDroplets.get(shooting.target).decBlood();
				blueDroplets.get(shooting.shooter).incLevel();
				shootingLines.add(new ShootingLine(blueDroplets.get(shooting.shooter).getPos(), redDroplets.get(shooting.target).getPos(), Owner.BLUE));
			}
		}
		for (Droplet droplet : redDroplets) {
			if (droplet == null) {
				continue;
			}
			if (droplet.getBlood() <= 0) {
				redDroplets.set(droplet.getId(), null);
			}
		}
		for (Droplet droplet : blueDroplets) {
			if (droplet == null) {
				continue;
			}
			if (droplet.getBlood() <= 0) {
				blueDroplets.set(droplet.getId(), null);
			}
		}
		
		for (Droplet droplet : redDroplets) {
			if (droplet == null) {
				continue;
			}
			Vector pos = redMovement.dropletsNewPos.get(droplet.getId());
			if (pos == null) {
				pos = droplet.getPos();
			}
			droplet.setPos(pos);
		}
		for (Droplet droplet : blueDroplets) {
			if (droplet == null) {
				continue;
			}
			Vector pos = blueMovement.dropletsNewPos.get(droplet.getId());
			if (pos == null) {
				pos = droplet.getPos();
			}
			droplet.setPos(pos);
		}
		
		nRedWaitingDroplets -= redMovement.newDropletsPosY.size();
		for (Integer newDropletPosY : redMovement.newDropletsPosY) {
			redDroplets.add(new Droplet(redDroplets.size(), Owner.RED, Droplet.FULL_BLOOD, 0, new Vector(0, newDropletPosY), Vector.ZERO_VECTOR));
		}
		
		nBlueWaitingDroplets -= blueMovement.newDropletsPosY.size();
		for (Integer newDropletPosY : blueMovement.newDropletsPosY) {
			blueDroplets.add(new Droplet(blueDroplets.size(), Owner.BLUE, Droplet.FULL_BLOOD, 0, new Vector(mapWidth, newDropletPosY), Vector.ZERO_VECTOR));
		}
		
		timePassedBy();
	}
	
	public boolean isOver() {
		return isOver;
	}
	
	/**
	 * 游戏结束
	 */
	public void gameOver() {
		isOver = true;
		
		messageStream.println("end");
	}
	
	void timePassedBy() {
		for (Droplet droplet : redDroplets) {
			if (droplet == null) {
				continue;
			}
			int d = (int)Math.floor(Math.sqrt(droplet.getPos().getDist2(blueBrain.getPos())) / 500);
			redScore += Math.max(8 - d, 0);
		}
		for (Droplet droplet : blueDroplets) {
			if (droplet == null) {
				continue;
			}
			int d = (int)Math.floor(Math.sqrt(droplet.getPos().getDist2(redBrain.getPos())) / 500);
			blueScore += Math.max(8 - d, 0);
		}
		
		List<Droplet> newChoosenDroplets = Collections.synchronizedList(new ArrayList<Droplet>());
		for (Droplet droplet : choosenDroplets) {
			if (redDroplets.get(droplet.getId()) != null) {
				newChoosenDroplets.add(droplet);
			}
		}
		choosenDroplets = newChoosenDroplets;
		
		choosenTargetPos = null;
		
		redMakeDropletRestTime--;
		if (redMakeDropletRestTime == 0) {
			nRedWaitingDroplets++;
			redMakeDropletRestTime += makeDropletTime;
		}
		blueMakeDropletRestTime--;
		if (blueMakeDropletRestTime == 0) {
			nBlueWaitingDroplets++;
			blueMakeDropletRestTime += makeDropletTime;
		}
		
		restTime--;
		if (restTime == 0) {
			calcWinnerAndGameOver(true, true, true);
		}
	}
	
	public void draw(Graphics2D g, int len) {
		drawer.draw(g, len);
	}
}
