package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import clientSettings.Settings;

public class DeferedLogger{
	
	private File file;
	public boolean logging = false;
	private BufferedWriter out;
	private StringBuilder sb = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public DeferedLogger(String subDirectory, String fileName) throws IOException {
		this.logging = Settings.EVENT_LOGGING;
		
		String path = System.getProperty("user.home") + File.separator + "logs" +
				File.separator + subDirectory+ File.separator + fileName + ".txt";

		file = new File(path);

		file.getParentFile().mkdirs(); 
		file.createNewFile();
		sb = new StringBuilder();
		out = new BufferedWriter(new FileWriter(file));
	}
	public void log(String message) throws IOException {
		if(logging) {
			sb.append(sdf.format(new Date())+": ");
			sb.append(message);
			sb.append("\n");
		}
	}
	public void logNoTimestamp(String message) throws IOException {
		if(logging) {
			sb.append(message);
			sb.append("\n");
		}
	}
	public void saveToDisk()
	{
		try {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
