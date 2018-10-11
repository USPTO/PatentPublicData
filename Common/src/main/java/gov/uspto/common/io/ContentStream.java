package gov.uspto.common.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.input.BoundedInputStream;

/**
 * <h3>ContentStream</h3>
 * 
 * <p>Uses the same {@link Appendable} interface as {@link StringBuilder}. As content is appended the char count is
 * captured and once the provided char threshold is reached the trailing content can be written to a temp file, 
 * truncated or ignored. Then an {@link InputStream} is provided to read the data from memory, memory and file, or file;
 * depending on the selected LargeAction. Also, sections within the stream can be marked for quick retrieval of
 * sections from within the stream.
 * </p>
 *
 * <p>
 * Large Actions once byte threshold is exceeded:
 *  *<li>{@link LargeAction.NONE} disabled ; keeps everything in-memory.</li>
 *   <li>{@link LargeAction.DEFAULT} maintains in-memory content and writes all trailing append request to temp file</li>
 *   <li>{@link LargeAction.ALL_TO_TEMP} writes in-memory content and all trailing append request to temp file</li>
 *   <li>{@link LargeAction.TRUNCATE_TAIL} ignores all trailing request to append</li>
 *   <li>{@link LargeAction.SKIP_ALL} clears in-memory content and ignores all request to append; returns empty InputStream</li/>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ContentStream implements Appendable {

	private static final long DEFAULT_THESHOLD = 2000000; // about 2mb.

	private final long threshold;
	private LargeAction largeAction = LargeAction.DEFAULT;
	private long charLength;
	//private OutputStream output;
	private boolean largeFlag = false;
	private File tempFile;
	private OutputStream writer;
	private OutputStream output;
	private ByteArrayInOutStream buffer;
	private Map<String, Long[]> marks = new LinkedHashMap<String, Long[]>();
	private String header = "";
	private String footer = "";


	public enum LargeAction {
		NONE, DEFAULT, ALL_TO_TEMP, TRUNCATE_TAIL, SKIP_FULLY 
	}
	
	/**
	 * Constructor
	 * 
	 * Uses the default threshold of {@value #DEFAULT_THESHOLD} chars
	 */
	public ContentStream() {
		this.threshold = DEFAULT_THESHOLD;
		this.buffer = new ByteArrayInOutStream();
		this.output = (OutputStream) buffer;
	}

	/**
	 * Constructor
	 * 
	 * @param threshold - of bytes
	 */
	public ContentStream(long threshold) {
		this.threshold = threshold;
		this.buffer = new ByteArrayInOutStream();
		this.output = (OutputStream) buffer;
	}

	/**
	 * Constructor
	 * 
	 * @param threshold - of chars
	 * @param capacity - exposed setting the internal StringBuilder capacity for possible optimization.
	 */
	public ContentStream(long threshold, int initialCapacity) {
		this.threshold = threshold;
		this.buffer = new ByteArrayInOutStream(initialCapacity);
		this.output = (OutputStream) buffer;
	}

	/**
	 * Set Action to Perform  when Large (above threshold has been reached)
	 * 
	 * @param LargeAction
	 * @return
	 */
	public ContentStream setLargeAction(LargeAction largeAction) {
		this.largeAction = largeAction;
		return this;
	}

	/**
	 * isLarge
	 * 
	 * @return true when content length is above threshold
	 */
	public boolean isLarge() {
		return largeFlag;
	}

	/**
	 * Character Length
	 *
	 * @return length of 16-bit characters
	 */
	public long getLength() {
		return charLength;
	}

	/**
	 * Temp File
	 * 
	 * @return temp file when above threshold else returns null
	 */
	public File getTempFile() {
		return tempFile;
	}

	/**
	 * File Size
	 * 
	 * @return temp file size on disk when above threshold else returns 0
	 */
	public long getFileSize() throws IOException {
		if (tempFile != null) {
			if (writer != null) {
				writer.flush();
			}
			return Files.size(tempFile.toPath());
		}
		return 0;
	}

	/**
	 * Capture the Start Position of a section of the stream.
	 * And giving it a name to later fetch the section of the stream by name.
	 * 
	 * <p>
	 * Note: repeated calls to markStart for same markName has no effect, 
	 * thus the very first location is kept.
	 * </p>
	 *  
	 * @param markName
	 * @return
	 */
	public ContentStream markStart(String markName) {
		if (!this.marks.containsKey(markName)) {
			Long[] mark = new Long[2];
			mark[0] = charLength;
			this.marks.put(markName, mark);
		}
		return this;
	}

	/**
	 * Capture the End Position of a section of the stream
	 * And giving it a name to later fetch the section of the stream by name.
	 * 
	 * <p>
	 * Note: repeated calls to markEnd for same markName overrides the previous end location,
	 * thus last location is kept.
	 * </p>
	 * 
	 * <p>
	 * Repeated calls when within a section like: <code>contentStream.markStart("section1").markEnd("section1");</code>
	 * Should correctly mark the start and end boundaries for the section.
	 * </p>
	 *
	 * @param markName
	 * @return
	 */
	public ContentStream markEnd(String markName) {
		if (this.marks.containsKey(markName)) {
			Long[] mark = this.marks.get(markName);
			mark[1] = charLength;
		}
		return this;
	}

	/**
	 * Get Names of all the marked sections within the Stream.
	 * 
	 * @return
	 */
	public Set<String> getMarkedNames(){
		return this.marks.keySet();
	}

	public long getMarkedLength(String markName) {
		Long[] mark = this.marks.get(markName);
		return mark[1] - mark[0];
	}
	
	public void setHeader(String header) {
		this.header = header;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public Appendable append(CharSequence csq) throws IOException {
		charLength += csq.length();

		if (largeAction != LargeAction.NONE && !largeFlag && charLength > threshold) {
			setLargeSwitchAppender();
		}

		output.write(csq.toString().getBytes("UTF-8"));

		return this;
	}

	@Override
	public Appendable append(char c) throws IOException {
		charLength += Character.charCount(c);

		if (largeAction != LargeAction.NONE && !largeFlag && charLength > threshold) {
			setLargeSwitchAppender();
		}

		output.write(c);

		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int offset, int len) throws IOException {
		this.append(csq.subSequence(offset, len));
		return this;
	}

	private void setLargeSwitchAppender() throws IOException {
		if (largeAction == LargeAction.TRUNCATE_TAIL || largeAction == LargeAction.SKIP_FULLY) {
			largeFlag = true;
			if (largeAction == LargeAction.SKIP_FULLY) {
				buffer = new ByteArrayInOutStream(0); // don't empty if truncateLarge.
			}
			output = (OutputStream) new DummyOutputStream();
		} else {
			// use temp file.
			largeFlag = true;
			tempFile = File.createTempFile("contentStream-", ".tmp");
			tempFile.deleteOnExit();
			writer = new BufferedOutputStream(new FileOutputStream(tempFile));
			if (largeAction == LargeAction.ALL_TO_TEMP) {
				writer.write(buffer.toByteArray());
				buffer = new ByteArrayInOutStream(0);
			}
			output = (OutputStream) writer;
		}
	}

	/**
	 * Get Stream
	 * 
	 * <p>
	 * Note: if the content is large enough and a temp file is used (LargeAction.DEFAULT or LargeAction.ALL_TO_TEMP)
	 * calling this function closes and sets the temp file as read only. Further appends after calling this function
	 * will fail with IOException "Stream Closed".
	 * </p>
	 * 
	 * @return InputStream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		Vector<InputStream> streams = new Vector<InputStream>();
		streams.add(new ByteArrayInputStream(header.getBytes("UTF-8")));

		if (!largeFlag || largeAction == LargeAction.TRUNCATE_TAIL || largeAction == LargeAction.SKIP_FULLY) {
			// Content is only streamed from memory
			streams.add(buffer.getInputStream());
			streams.add(new ByteArrayInputStream(footer.getBytes("UTF-8")));
		}
		else {
			// Content streamed from memory, and temp file.
			if (writer != null) {
				writer.flush();
				writer.close();
				writer = null;
			}
			tempFile.setReadOnly();
			
			streams.add(buffer.getInputStream());
			streams.add(new FileInputStream(tempFile));
			streams.add(new ByteArrayInputStream(footer.getBytes("UTF-8")));
		}
		
		streams.add(new ByteArrayInputStream(footer.getBytes("UTF-8")));
		return new SequenceInputStream(streams.elements());
	}
	
	@SuppressWarnings("resource")
	public InputStream getInputStream(String markName) throws IOException {
		Long[] mark = this.marks.get(markName);

		Vector<InputStream> streams = new Vector<InputStream>();
		streams.add(new ByteArrayInputStream(header.getBytes("UTF-8")));

		if (!largeFlag || largeAction == LargeAction.TRUNCATE_TAIL || largeAction == LargeAction.SKIP_FULLY) {
			// Content is only streamed from memory
			InputStream in = buffer.getInputStream();
			in.skip(mark[0]);
			BoundedInputStream bout = new BoundedInputStream(in, mark[1]-mark[0]);
			streams.add(bout);
		}
		else {
			// Content streamed from memory, and temp file.
			if (writer != null) {
				writer.flush();
				writer.close();
				writer = null;
			}
			tempFile.setReadOnly();
	
			InputStream in = new SequenceInputStream(buffer.getInputStream(), new FileInputStream(tempFile));
			in.skip(mark[0]);
			BoundedInputStream bout = new BoundedInputStream(in, mark[1]-mark[0]);
			streams.add(bout);
		}

		streams.add(new ByteArrayInputStream(footer.getBytes("UTF-8")));
		return new SequenceInputStream(streams.elements());
	}

	public InputStream getMarkedInputStream(String markName) throws IOException {
		return getInputStream(markName);
	}
	
	@Override
	public String toString() {
		throw new UnsupportedOperationException("Method Not Supported; use getStream() instead.");
	}

	public static String inputStream2String(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}
}
