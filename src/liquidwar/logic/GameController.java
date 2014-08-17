package liquidwar.logic;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import liquidwar.logic.situation.AIBattleSituation;
import liquidwar.logic.situation.GameSituation;
import liquidwar.logic.situation.ReplaySituation;

/**
 * 游戏控制器
 * @author vfleaking
 *
 */
public class GameController {
	private boolean isRuning = false;
	
	private GameSituation gameSituation;
	
	private Set<String> pressingCmds = new HashSet<String>();
	private Set<String> pressedCmds = new HashSet<String>(); 
	
	public UserInputHandler userInputHandler = new UserInputHandler();
	public JTextArea messageTextArea = new JTextArea(); 
	public JScrollPane messagePane = new JScrollPane(messageTextArea);
	public PrintStream messageTextAreaPrintStream = new PrintStream(new MessageTextAreaOutputStream()); 
	
	public JSlider speedSlider = new JSlider(0, 200, 100);
	
	public JLabel replayLabel = new JLabel("Log Id");
	public JTextField replayTextField = new JTextField("");
	public JButton replayButton = new JButton("Replay");
	
	public JLabel redCmdLabel = new JLabel("Player Red");
	public JTextField redCmdTextField = new JTextField("", 10);
	public JLabel blueCmdLabel = new JLabel("Player Blue");
	public JTextField blueCmdTextField = new JTextField("", 10);
	public JButton startAndStopButton = new JButton("Start");
	
	public GameLabel gameLabel = new GameLabel();
	
	public int sleepTime = 100;
	
	/**
	 * 绘制游戏的标签
	 * @author vfleaking
	 *
	 */
	public class GameLabel extends JLabel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public GameLabel() {
			this.setOpaque(true);
			this.setBackground(Color.WHITE);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			draw(g, getWidth(), getHeight());
		}
	}
	
	public class MessageTextAreaOutputStream extends OutputStream {
		public static final int MAX_LEN = 4000;
		
		public void write(String string) {
			messageTextArea.append(string);
			
			if (messageTextArea.getDocument().getLength() > MAX_LEN) {
				messageTextArea.setText(messageTextArea.getText().substring(messageTextArea.getDocument().getLength() - MAX_LEN));
			}
			messageTextArea.setCaretPosition(messageTextArea.getDocument().getLength());
		}

		@Override
		public void write(byte b[], int off, int len) {
			write(new String(b, off, len));
		}
		@Override
		public void write(byte b[]) {
			write(new String(b));
		}
		@Override
		public void write(int b) throws IOException {
			write(String.valueOf((char)b));
		}
	}
	
	/**
	 * 用户输入处理器
	 * @author vfleaking
	 *
	 */
	public class UserInputHandler implements KeyListener, MouseListener, MouseMotionListener, ActionListener, FocusListener, WindowListener { 
		private void cmdPressed(String cmd) {
			pressingCmds.add(cmd);
			pressedCmds.add(cmd);
		}

		private void cmdReleased(String cmd) {
			pressingCmds.remove(cmd);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			
			synchronized (GameController.this) {
				if (cmd.startsWith("pressed ")) {
					cmdPressed(cmd.substring("pressed ".length()));
				} else if (cmd.startsWith("released ")) {
					cmdReleased(cmd.substring("released ".length()));
				} else {
					pressedCmds.add(cmd);
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (getIsRunning()) {
				gameSituation.userInputHandler.keyPressed(e);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (getIsRunning()) {
				gameSituation.userInputHandler.keyReleased(e);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if (getIsRunning()) {
				gameSituation.userInputHandler.keyTyped(e);
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			pressingCmds.clear();
			if (getIsRunning()) {
				gameSituation.userInputHandler.allKeyReleased();
			}
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			if (getIsRunning()) {
				gameSituation.gameOver();
			}
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
		private void normalizeMouseEvent(MouseEvent e) {
			int l = Math.min(gameLabel.getWidth(), gameLabel.getHeight());
			e.translatePoint(-(gameLabel.getWidth() - l) / 2, -(gameLabel.getHeight() - l) / 2);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e); 
				gameSituation.userInputHandler.mouseClicked(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mousePressed(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mouseReleased(e);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mouseEntered(e);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mouseExited(e);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mouseDragged(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (getIsRunning()) {
				normalizeMouseEvent(e);
				gameSituation.userInputHandler.mouseMoved(e);
			}
		}
	}
	
	synchronized boolean getIsRunning() {
		return isRuning;
	}
	synchronized void setIsRunning(boolean isRunning) {
		if (isRunning) {
			replayButton.setEnabled(false);
			replayTextField.setEditable(false);
			redCmdTextField.setEditable(false);
			blueCmdTextField.setEditable(false);
			startAndStopButton.setText("Stop");
		} else {
			replayButton.setEnabled(true);
			replayTextField.setEditable(true);
			redCmdTextField.setEditable(true);
			blueCmdTextField.setEditable(true);
			startAndStopButton.setText("Start");
		}
		this.isRuning = isRunning;
	}
	
	synchronized int getSleepTime() {
		return this.sleepTime;
	}
	synchronized void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	/**
	 * 游戏运行器，一直在后台运行。
	 * @author vfleaking
	 *
	 */
	public class GameRunner implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				long last = System.currentTimeMillis();
				synchronized (GameController.this) {
					if (pressedCmds.contains("startAndStop")) {
						if (getIsRunning()) {
							gameSituation.gameOver();
							setIsRunning(false);
						} else {
							try {
								PrintStream configPrintStream = new PrintStream("config.txt");
								configPrintStream.println(redCmdTextField.getText());
								configPrintStream.println(blueCmdTextField.getText());
								configPrintStream.close();
							} catch (FileNotFoundException e) {
							}
							
							gameSituation = new AIBattleSituation(messageTextAreaPrintStream, redCmdTextField.getText(), blueCmdTextField.getText());
							setIsRunning(true);
						}
						
						pressedCmds = new HashSet<String>(pressingCmds);
						
						gameLabel.repaint();
					} else if (pressedCmds.contains("replay")) {
						if (!getIsRunning()) {
							gameSituation = new ReplaySituation(messageTextAreaPrintStream, replayTextField.getText());
							setIsRunning(true);
						
							pressedCmds = new HashSet<String>(pressingCmds);
						
							gameLabel.repaint();
						}
					} else if (getIsRunning()) {
						if (!gameLabel.hasFocus())
							gameLabel.requestFocusInWindow();
						
						gameSituation.goNext();
						pressedCmds = new HashSet<String>(pressingCmds);
						gameLabel.repaint();
						if (gameSituation.isOver()) {
							setIsRunning(false);
						}
					}
				}
				
				try {
					long sleepTime = last + getSleepTime() - System.currentTimeMillis();
					if (sleepTime < 2) {
						sleepTime = 2;
					}
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					setIsRunning(false);
					return;
				}
			}
		}
	}
	
	
	public GameController() {
		try {
			BufferedReader configReader = new BufferedReader(new InputStreamReader(new FileInputStream("config.txt")));
			try {
				redCmdTextField.setText(configReader.readLine());
				blueCmdTextField.setText(configReader.readLine());
			} catch (IOException e) {
			} finally {
				try {
					configReader.close();
				} catch (IOException e) {
				}
			}
		} catch (FileNotFoundException e) {
		}
		
		
		speedSlider.setMajorTickSpacing(50);
		speedSlider.setPaintTicks(true);
		speedSlider.setLabelTable(new Hashtable<Integer, JLabel>(){
			private static final long serialVersionUID = 1L;

			{
				put(new Integer(0), new JLabel("fast"));
				put(new Integer(100), new JLabel("normal"));
				put(new Integer(200), new JLabel("slow"));
			}
		});
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int x = speedSlider.getValue();
				double a = 900.0 / 85;
				double lam = 85 / (a - 1);
				double c = 15 - lam;
				double y = lam * Math.pow(a, x / 100.0) + c;
				setSleepTime((int)Math.round(y));
			}
		});

		speedSlider.setPaintLabels(true);
		
		replayButton.setActionCommand("replay");
		replayButton.addActionListener(userInputHandler);
		startAndStopButton.setActionCommand("startAndStop");
		startAndStopButton.addActionListener(userInputHandler);
		gameLabel.addKeyListener(userInputHandler);
		gameLabel.addMouseListener(userInputHandler);
		gameLabel.addMouseMotionListener(userInputHandler);
		gameLabel.addFocusListener(userInputHandler);
		
		messageTextArea.setEditable(false);
		
		replayTextField.setMaximumSize(new Dimension(114, 19));
		redCmdTextField.setMaximumSize(new Dimension(114, 19));
		blueCmdTextField.setMaximumSize(new Dimension(114, 19));
		
		messageTextArea.setLineWrap(true);
		messageTextArea.setWrapStyleWord(true); 
		
		Thread thread = new Thread(new GameRunner(), "Game Runner");
		thread.start();
	}
	
	public void draw(Graphics g, int width, int height) {
		if (gameSituation != null) {
			Graphics2D g2d = (Graphics2D)g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int l = Math.min(width, height);
			g2d.translate((width - l) / 2.0, (height - l) / 2.0);

			gameSituation.draw(g2d, l);
		}
	}
}
