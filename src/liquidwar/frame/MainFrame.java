package liquidwar.frame;
import java.awt.BorderLayout;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import liquidwar.logic.GameController;

/**
 * 游戏主窗口
 * @author vfleaking
 *
 */
public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GameController gameController = new GameController();
	
	public MainFrame() {
		BorderLayout mainLayout = new BorderLayout();
		this.setLayout(mainLayout);
		
		JPanel eastPanel = new JPanel();
		GroupLayout eastLayout = new GroupLayout(eastPanel);
		eastPanel.setLayout(eastLayout);
		eastLayout.setAutoCreateContainerGaps(true);
		eastLayout.setAutoCreateGaps(true);
		eastLayout.setHorizontalGroup(eastLayout.createSequentialGroup()
				.addGroup(eastLayout.createParallelGroup()
						.addComponent(gameController.messagePane)
						.addComponent(gameController.speedSlider)
						.addGroup(eastLayout.createSequentialGroup()
								.addGroup(eastLayout.createParallelGroup()
										.addComponent(gameController.replayLabel)
										.addComponent(gameController.redCmdLabel)
										.addComponent(gameController.blueCmdLabel)
								)
								.addGroup(eastLayout.createParallelGroup()
										.addComponent(gameController.replayTextField)
										.addComponent(gameController.replayButton)
										.addComponent(gameController.redCmdTextField)
										.addComponent(gameController.blueCmdTextField)
										.addComponent(gameController.startAndStopButton)
								)
						)
				)
		);
		eastLayout.setVerticalGroup(
				eastLayout.createSequentialGroup()
						.addComponent(gameController.messagePane)
						.addComponent(gameController.speedSlider)
						.addGroup(eastLayout.createParallelGroup()
								.addComponent(gameController.replayLabel)
								.addComponent(gameController.replayTextField)
						)
						.addGroup(eastLayout.createParallelGroup()
								.addComponent(gameController.replayButton)
						)
						.addGroup(eastLayout.createParallelGroup()
								.addComponent(gameController.redCmdLabel)
								.addComponent(gameController.redCmdTextField)
						)
						.addGroup(eastLayout.createParallelGroup()
								.addComponent(gameController.blueCmdLabel)
								.addComponent(gameController.blueCmdTextField)
						)
						.addComponent(gameController.startAndStopButton)
		);
		
		this.add("Center", gameController.gameLabel);
		this.add("East", eastPanel);
		
		this.addWindowListener(gameController.userInputHandler);
		
		this.setTitle("Liquid War");
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
