package systemtoconsole;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
	private OutputStream out = null;
	private OutputStream tee = null;

	public TeeOutputStream(OutputStream chainedStream, OutputStream teeStream) {
		out = chainedStream;
		tee = teeStream;
	}

	/**
	 * Implementation for parent's abstract write method. This writes out the
	 * passed in character to the both, the chained stream and "tee" stream.
	 */

	public void write(int c) throws IOException {
		out.write(c);
		out.flush();

		tee.write(c);
		tee.flush();
	}

	/**
	 * Closes both, chained and tee, streams.
	 */
	public void close() throws IOException {
		flush();

		out.close();
		tee.close();
	}

	/**
	 * Flushes chained stream; the tee stream is flushed each time a character
	 * is written to it.
	 */
	public void flush() throws IOException {
		out.flush();
		tee.flush();
	}
}
