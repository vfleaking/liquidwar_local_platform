package liquidwar.logic.situation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import liquidwar.logic.situation.GameSituation;

public class AIBattleSituation extends GameSituation {
	Player player1, player2;
	
	Player getPlayerFromString(String name) {
		return new ExternalPlayer(name, messageStream);
	}
	
	public AIBattleSituation(PrintStream messageStream, String redPlayerName, String bluePlayerName) {
		super(messageStream);
		
		player1 = getPlayerFromString(redPlayerName);
		player2 = getPlayerFromString(bluePlayerName);
		
		player1.init(Owner.RED);
		player2.init(Owner.BLUE);
	}
	
	@Override
	void simulate() {
		if (restTime == totalGameTime) {
			this.redMovement = new Movement();
			this.blueMovement = new Movement();
		} else {
			Movement redMovement, blueMovement;
			redMovement = player1.move(this);
			blueMovement = player2.move(this);
			
			this.redMovement = redMovement;
			this.blueMovement = blueMovement;
		}
		
		super.simulate();
	}
	
	@Override
	public void gameOver() {
		super.gameOver();
		
		player1.destroy();
		player2.destroy();
		
		try {
			logOutputStream.flush();
		} catch (IOException e) {
		}
		
		GZIPOutputStream logFileOutputStream = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String logFileIdString = dateFormat.format(new Date());
			String logFileName = "log/" + logFileIdString + ".gz";
			logFileOutputStream = new GZIPOutputStream(new FileOutputStream(logFileName));
			logFileOutputStream.write(logByteOutputStream.toByteArray());
			messageStream.println("log file id:" + logFileIdString);
		} catch (IOException e) {
			messageStream.println("failed to save the log file");
		} finally {
			if (logFileOutputStream != null) {
				try {
					logFileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
