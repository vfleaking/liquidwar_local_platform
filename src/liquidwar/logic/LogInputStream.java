package liquidwar.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogInputStream extends InputStream {
	private InputStream in;
	private OutputStream logOut;
	private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	
	public LogInputStream(InputStream in, OutputStream logOut) {
		this.in = in;
		this.logOut = logOut;
	}
	
	@Override
	public int available() throws IOException {
		return in.available();
	}
	
	@Override
	public void close() throws IOException {
		in.close();
		byteOut.writeTo(logOut);
		logOut.close();
	}
	
	@Override
	public void mark(int readlimit) {
		in.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read() throws IOException {
		int b = in.read();
		if (b != -1) {
			byteOut.write(b);
		}
		return b;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int len = in.read(b);
		if (len != -1) {
			byteOut.write(b);
		}
		return len;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		len = in.read(b, off, len);
		if (len != -1) {
			byteOut.write(b, off, len);
		}
		return len;
	}
	
	@Override
	public void reset() throws IOException {
		in.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}