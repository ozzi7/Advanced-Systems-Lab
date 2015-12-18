package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;

import settings.Settings;

public class EventLogger{
	
	private File file;
	private boolean logging = false;
	private BufferedWriter out;
	private StringBuilder sb;
	private long currentTimeMs;
	
	public EventLogger(String subDirectory, String fileName) throws IOException {
		this.logging = Settings.EVENT_LOGGING;
		
		String path = System.getProperty("user.home") + File.separator + "logs" +
				File.separator + subDirectory+ File.separator + fileName;

		file = new File(path);

		file.getParentFile().mkdirs(); 
		file.createNewFile();
		
		out = new BufferedWriter(new FileWriter(file));
		sb = new StringBuilder(Settings.EVENTLOG_CAPACITY);
		currentTimeMs = System.currentTimeMillis();
	}
	public void log(String message) throws IOException {
		if(logging) {
			sb.append(System.currentTimeMillis() - currentTimeMs + " ");
			sb.append(message);
			sb.append("\n");
		}
		currentTimeMs = System.currentTimeMillis();
	}
	public void saveToDisk() {
		try {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
