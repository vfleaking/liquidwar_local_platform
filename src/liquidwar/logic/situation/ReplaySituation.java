package liquidwar.logic.situation;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import liquidwar.logic.Droplet;
import liquidwar.logic.Vector;
import liquidwar.logic.situation.Movement.Shooting;

public class ReplaySituation extends GameSituation {
	DataInputStream logInputStream;
	
	ByteArrayOutputStream redByteArrayOutputStream = new ByteArrayOutputStream();
	ByteArrayOutputStream blueByteArrayOutputStream = new ByteArrayOutputStream();
	PrintStream redSituPrintStream = new PrintStream(redByteArrayOutputStream);
	PrintStream blueSituPrintStream = new PrintStream(blueByteArrayOutputStream);
	
	List<Vector> dispList = new ArrayList<Vector>();
	
	Movement readMovementFromLog(Owner owner) throws IOException {
		int errMsgLen = logInputStream.readInt();
		if (errMsgLen > 0) {
			int off = 0;
			byte[] b = new byte[errMsgLen];
			while (off != errMsgLen) {
				off += logInputStream.read(b, off, errMsgLen - off);
			}
			return new Movement(new String(b, Charset.forName("UTF-8")));
		}
		Movement movement = new Movement();
		
		int nShootings = logInputStream.readShort();
		for (int i = 0; i < nShootings; i++) {
			int shooter = logInputStream.readShort();
			int target = logInputStream.readShort();
			movement.shootings.add(new Shooting(shooter, target));
		}
		
		for (Droplet droplet : getDroplets(owner)) {
			if (droplet == null) {
				continue;
			}
			int fst = logInputStream.readUnsignedByte();
			
			if (fst == 255) {
				continue;
			}
			int p;
			if (fst >= 15) {
				p = fst - 15;
			} else {
				p = fst * 256 + logInputStream.readUnsignedByte() + 200;
			}
			Vector d = dispList.get(p);
			movement.dropletsNewPos.put(droplet.getId(), Vector.add(droplet.getPos(), d));
			
			for (int i = p; i > 0; i--) {
				dispList.set(i, dispList.get(i - 1));
			}
			dispList.set(0, d);
		}
		
		int nNewDropletsPos = logInputStream.readShort();
		for (int i = 0; i < nNewDropletsPos; i++) {
			int posY = logInputStream.readShort();
			movement.newDropletsPosY.add(posY);
		}
		
		return movement;
	}

	public ReplaySituation(PrintStream messageStream, String logId) {
		super(messageStream);
		
		enableHumanHelp = false;
		
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
		
		File logFile = new File("log/"+ logId + ".gz");
		if (!logFile.isFile()) {
			InputStream inputStream = null;
			try {
				URLConnection connection = new URL("http://hzhwcmhf.duapp.com/log?id=" + logId).openConnection();
				inputStream = connection.getInputStream();
			} catch (IOException e) {
				messageStream.println("log file not found");
				gameOver();
				return;
			}
			
			FileOutputStream logFileOutputStream = null;
			try {
				logFileOutputStream = new FileOutputStream("log/" + logId + ".gz");
				
				byte[] buffer = new byte[1024];
				int nRead;
				int fileSize = 0;
				while ((nRead = inputStream.read(buffer)) != -1) {
					fileSize += nRead;
					logFileOutputStream.write(buffer, 0, nRead);
				}
				messageStream.printf("successfully downloaded %d bytes.\n", fileSize);
			} catch (IOException e){
				messageStream.println("download log file failed.");
				gameOver();
				return;
			} finally {
				if (logFileOutputStream != null) {
					try {
						logFileOutputStream.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		try {
			logInputStream = new DataInputStream(new GZIPInputStream(new FileInputStream(logFile)));
		} catch (FileNotFoundException e) {
			messageStream.println("downloaded but log file not found...");
			gameOver();
		} catch (IOException e) {
			messageStream.println("log file is damaged.");
			gameOver();
		}
	}
	
	@Override
	void simulate() {
		try {
			Movement redMovement, blueMovement;
			
			if (restTime != totalGameTime) {
				printSitu(Owner.RED, redSituPrintStream);
				printSitu(Owner.BLUE, blueSituPrintStream);
			}
			
			redMovement = readMovementFromLog(Owner.RED);
			blueMovement = readMovementFromLog(Owner.BLUE);
			
			this.redMovement = redMovement;
			this.blueMovement = blueMovement;
			
			super.simulate();
		} catch (IOException e) {
			messageStream.println("log file is damaged.");
			gameOver();
		}
	}
	
	@Override
	public void gameOver() {
		super.gameOver();
		
		if (logInputStream != null) {
			try {
				logInputStream.close();
			} catch (IOException e) {
			}
		}
		if (redSituPrintStream != null) {
			try {
				redSituPrintStream.flush();
				FileOutputStream redInFileOutputStream = new FileOutputStream("RED.in");
				redByteArrayOutputStream.writeTo(redInFileOutputStream);
				redInFileOutputStream.close();
			} catch (IOException e) {
			}
		}
		if (blueSituPrintStream != null) {
			try {
				blueSituPrintStream.flush();
				FileOutputStream blueInFileOutputStream = new FileOutputStream("BLUE.in");
				blueByteArrayOutputStream.writeTo(blueInFileOutputStream);
				blueInFileOutputStream.close();
			} catch (IOException e) {
			}
		}
	}
}
