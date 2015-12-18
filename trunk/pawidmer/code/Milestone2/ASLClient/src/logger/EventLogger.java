package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import clientSettings.Settings;

public class EventLogger{
	
	private File file;
	public boolean logging = false;
	private BufferedWriter out;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public EventLogger(String subDirectory, String fileName) throws IOException {
		this.logging = Settings.EVENT_LOGGING;
		
		String path = System.getProperty("user.home") + File.separator + "logs" +
				File.separator + subDirectory+ File.separator + fileName + ".txt";

		file = new File(path);

		file.getParentFile().mkdirs(); 
		file.createNewFile();
		
		out = new BufferedWriter(new FileWriter(file));
	}
	public void log(String message) throws IOException {
		if(logging) {
			try {
				out.write(sdf.format(new Date())+": ");
				out.write(message);
				out.write("\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void logNoTimestamp(String message) throws IOException {
		if(logging) {
			try {
				out.write(message);
				out.write("\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
