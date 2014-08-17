package liquidwar.logic.situation;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import liquidwar.logic.LogInputStream;
import liquidwar.logic.LogOutputStream;
import liquidwar.logic.Vector;
import liquidwar.logic.situation.Movement.Shooting;

public class ExternalPlayer implements Player {
	Owner owner;
	
	Process process;
	String cmd;
	
	boolean hasInitError = false;
	
	PrintStream messageStream;
	PrintStream processInputPrintStream;
	Reader processOutputReader;
	InputStream processErrorStream;
	
	int getNextInt(char ed) throws IOException
	{
		int c = processOutputReader.read();
		int res;
		if (c == '0')
		{
			res = 0;
			c = processOutputReader.read();
		}
		else if ('1' <= c && c <= '9')
		{
			res = c - '0';
			
			c = processOutputReader.read();
			while ('0' <= c && c <= '9')
			{
				int d = c - '0';
				if (res > (Integer.MAX_VALUE - d) / 10)
					throw new IOException();
				res = res * 10 + d;
				
				c = processOutputReader.read();
			}
		}
		else if (c == '-')
		{
			res = 0;
		    c = processOutputReader.read();
		    if (!('1' <= c && c <= '9'))
		    	throw new IOException();
		    res = c - '0';
		    
		    c = processOutputReader.read();
		    while ('0' <= c && c <= '9')
		    {
		    	int d = c - '0';
		    	if (-((long)res * 10 + d) < Integer.MIN_VALUE)
		    		throw new IOException();
	            res = res * 10 + d;
	            c = processOutputReader.read();
	        }
		    res = -res;
        }
	    else
	        throw new IOException();
		if (c != ed) {
			if (ed == '\n') {
				if (c != '\r') {
					throw new IOException();
				} else if (processOutputReader.read() != '\n') {
					throw new IOException();
				}
			} else {
				throw new IOException();
			}
		}
		return res;
	}
	
	Movement readMovement(GameSituation situ) throws IOException {
		Movement movement = new Movement();
		
		int nShootings = getNextInt('\n');
		for (int i = 0; i < nShootings; i++) {
			int shooter = getNextInt(' ');
			int target = getNextInt('\n');
			movement.shootings.add(new Shooting(shooter, target));
		}
		
		int nDropletsNewPos = getNextInt('\n');
		for (int i = 0; i < nDropletsNewPos; i++) {
			int id = getNextInt(' ');
			int newPosX = getNextInt(' ');
			int newPosY = getNextInt('\n');
			if (owner == Owner.BLUE) {
				newPosX = situ.mapWidth - newPosX;
			}
			if (movement.dropletsNewPos.containsKey(id)) {
				throw new IOException();
			}
			movement.dropletsNewPos.put(id, new Vector(newPosX, newPosY));
		}
		
		int nNewDropletsPos = getNextInt('\n');
		for (int i = 0; i < nNewDropletsPos; i++) {
			int posY = getNextInt('\n');
			movement.newDropletsPosY.add(posY);
		}
		
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
		}
		if (processErrorStream.available() > 0) {
			byte errMsg[] = new byte[processErrorStream.available()];
			processErrorStream.read(errMsg);
			messageStream.print(owner.getMessageHeader());
			messageStream.write(errMsg);
			messageStream.flush();
		}
		
		return movement;
	}
	
	public ExternalPlayer(String cmd, PrintStream messageStream) {
		this.cmd = cmd;
		this.messageStream = messageStream;
	}

	@Override
	public void init(Owner owner) {
		this.owner = owner;
		
		try {
			process = Runtime.getRuntime().exec(cmd);
			
			BufferedOutputStream processInputFileOutputStream = new BufferedOutputStream(new FileOutputStream(owner.toString() + ".in"));
			BufferedOutputStream processOutputFileOutputStream = new BufferedOutputStream(new FileOutputStream(owner.toString() + ".out"));
			
			processOutputReader = new InputStreamReader(new LogInputStream(process.getInputStream(), processOutputFileOutputStream));
			
			processErrorStream = process.getErrorStream();
			
			processInputPrintStream = new PrintStream(new LogOutputStream(process.getOutputStream(), processInputFileOutputStream));
		} catch (Exception e) {
			hasInitError = true;
		}
	}

	@Override
	public Movement move(GameSituation situ) {
		if (hasInitError) {
			return new Movement(Owner.BLUE.getMessageHeader() + "failed to initization.");
		}
		
		try {
			situ.printSitu(owner, processInputPrintStream);
		} catch (Exception e) {
			return new Movement(Owner.BLUE.getMessageHeader() + "failed to print the situation information.");
		}
		
		try {
			return readMovement(situ);
		} catch (Exception e) {
			return new Movement(Owner.BLUE.getMessageHeader() + "failed to read the movement.");
		}
	}

	@Override
	public void destroy() {
		if (process != null) {
			processInputPrintStream.close();
			try {
				processOutputReader.close();
			} catch (IOException e) {
			}
			process.destroy();
		}
	}

}
