package liquidwar.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LogOutputStream extends OutputStream {
	private OutputStream out;
	private OutputStream logOut;
	private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	
	public LogOutputStream(OutputStream out, OutputStream logOut) {
		this.out = out;
		this.logOut = logOut;
	}
	
	@Override
	public void close() throws IOException {
		out.close();
		byteOut.writeTo(logOut);
		logOut.close();
	}
	
	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		byteOut.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
		byteOut.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		byteOut.write(b, off, len);
	}
}