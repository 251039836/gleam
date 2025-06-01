package gleam.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 配置工具类<br>
 * store时不会修改内容顺序<br>
 * 
 * @author hdh
 *
 */
public class SafeProperties extends Properties {

	class PropertiesContext {
		class PropertyEntry {
			private String key;

			private String value;

			private String line;

			public PropertyEntry(String key, String value) {
				this.key = key;
				this.value = value;
			}

			/**
			 * @param key
			 * @param value
			 * @param line
			 */
			public PropertyEntry(String key, String value, String line) {
				this(key, value);
				this.line = line;
			}

			public String getKey() {
				return key;
			}

			public String getLine() {
				return line;
			}

			public String getValue() {
				return value;
			}

			public void setKey(String key) {
				this.key = key;
			}

			public void setLine(String line) {
				this.line = line;
			}

			public void setValue(String value) {
				this.value = value;
			}

			@Override
			public String toString() {
				if (line != null) {
					return line;
				}
				if (key != null && value != null) {
					String k = saveConvert(key, true);
					String v = saveConvert(value, false);
					return k + "=" + v;
				}
				return null;
			}
		}

		private List<Object> commentOrEntrys = new ArrayList<>();

		public void addCommentLine(String line) {
			commentOrEntrys.add(line);
		}

		public List<Object> getCommentOrEntrys() {
			return commentOrEntrys;
		}

		public void putOrUpdate(PropertyEntry pe) {
			remove(pe.getKey());
			commentOrEntrys.add(pe);
		}

		public void putOrUpdate(String key, String value) {
			PropertyEntry pe = new PropertyEntry(key, value);
			int index = remove(key);
			commentOrEntrys.add(index, pe);
		}

		public void putOrUpdate(String key, String value, String line) {
			PropertyEntry pe = new PropertyEntry(key, value, line);
			remove(key);
			commentOrEntrys.add(pe);
		}

		public int remove(String key) {
			for (int index = 0; index < commentOrEntrys.size(); index++) {
				Object obj = commentOrEntrys.get(index);
				if (obj instanceof PropertyEntry) {
					if (obj != null) {
						if (key.equals(((PropertyEntry) obj).getKey())) {
							commentOrEntrys.remove(obj);
							return index;
						}
					}
				}
			}
			return commentOrEntrys.size();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2554146149351098591L;

	private static final String KeyValueSeparators = "=: \t\r\n\f";

	private static final String StrictKeyValueSeparators = "=:";

	private static final String SpecialSaveChars = "=: \t\r\n\f#!";
	private static final String WhiteSpaceChars = " \t\r\n\f";

	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	private static final Charset Default_Charset = StandardCharsets.UTF_8;

	/**
	 * Convert a nibble to a hex character
	 *
	 * @param nibble the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	private static void writeln(BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	private PropertiesContext context = new PropertiesContext();

	public void addComment(String comment) {
		if (comment != null) {
			context.addCommentLine("#" + comment);
		}
	}

	private boolean continueLine(String line) {
		int slashCount = 0;
		int index = line.length() - 1;
		while ((index >= 0) && (line.charAt(index--) == '\\'))
			slashCount++;
		return (slashCount % 2 == 1);
	}

	protected void doLoad(Reader reader) throws IOException {
		BufferedReader in = new BufferedReader(reader);
		while (true) {
			String line = in.readLine();
			String intactLine = line;
			if (line == null)
				return;

			if (line.length() > 0) {

				int len = line.length();
				int keyStart;
				for (keyStart = 0; keyStart < len; keyStart++)
					if (WhiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
						break;

				if (keyStart == len)
					continue;

				char firstChar = line.charAt(keyStart);

				if ((firstChar != '#') && (firstChar != '!')) {
					while (continueLine(line)) {
						String nextLine = in.readLine();
						intactLine = intactLine + "\n" + nextLine;
						if (nextLine == null)
							nextLine = "";
						String loppedLine = line.substring(0, len - 1);
						int startIndex;
						for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
							if (WhiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
								break;
						nextLine = nextLine.substring(startIndex, nextLine.length());
						line = new String(loppedLine + nextLine);
						len = line.length();
					}

					int separatorIndex;
					for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
						char currentChar = line.charAt(separatorIndex);
						if (currentChar == '\\')
							separatorIndex++;
						else if (KeyValueSeparators.indexOf(currentChar) != -1)
							break;
					}

					int valueIndex;
					for (valueIndex = separatorIndex; valueIndex < len; valueIndex++)
						if (WhiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
							break;

					if (valueIndex < len)
						if (StrictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
							valueIndex++;

					while (valueIndex < len) {
						if (WhiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
							break;
						valueIndex++;
					}
					String key = line.substring(keyStart, separatorIndex);
					String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

					key = loadConvert(key);
					value = loadConvert(value);
					put(key, value, intactLine);
				} else {
					context.addCommentLine(intactLine);
				}
			} else {
				context.addCommentLine(intactLine);
			}
		}
	}

	public PropertiesContext getContext() {
		return context;
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inStream, Default_Charset);
		doLoad(reader);
	}

	@Override
	public synchronized void load(Reader reader) throws IOException {
		doLoad(reader);
	}

	private String loadConvert(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);

		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						outBuffer.append('\t'); /* ibm@7211 */

					else if (aChar == 'r')
						outBuffer.append('\r'); /* ibm@7211 */
					else if (aChar == 'n') {
						/*
						 * ibm@8897 do not convert a \n to a line.separator because on some platforms
						 * line.separator is a String of "\r\n". When a Properties class is saved as a
						 * file (store()) and then restored (load()) the restored input MUST be the same
						 * as the output (so that Properties.equals() works).
						 *
						 */
						outBuffer.append('\n'); /* ibm@8897 ibm@7211 */
					} else if (aChar == 'f')
						outBuffer.append('\f'); /* ibm@7211 */
					else
						/* ibm@7211 */
						outBuffer.append(aChar); /* ibm@7211 */
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}

	@Override
	public synchronized Object put(Object key, Object value) {
		context.putOrUpdate(key.toString(), value.toString());
		return super.put(key, value);
	}

	public synchronized Object put(Object key, Object value, String line) {
		context.putOrUpdate(key.toString(), value.toString(), line);
		return super.put(key, value);
	}

	@Override
	public synchronized Object remove(Object key) {
		context.remove(key.toString());
		return super.remove(key);
	}

	private String saveConvert(String theString, boolean escapeSpace) {
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len * 2);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');

				outBuffer.append(' ');
				break;
			case '\\':
				outBuffer.append('\\');
				outBuffer.append('\\');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					if (SpecialSaveChars.indexOf(aChar) != -1)
						outBuffer.append('\\');
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	@Override
	public synchronized void store(OutputStream out, String header) throws IOException {
		BufferedWriter awriter;
		awriter = new BufferedWriter(new OutputStreamWriter(out, Default_Charset));
		if (header != null)
			writeln(awriter, "#" + header);
		List<Object> entrys = context.getCommentOrEntrys();
		for (Iterator<Object> iter = entrys.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj.toString() != null) {
				writeln(awriter, obj.toString());
			}
		}
		awriter.flush();
	}
}