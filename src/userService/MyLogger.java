package userService;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyLogger {

	private Logger logger;
	private FileHandler logFile;

	public MyLogger(String filename) throws IOException {
		this.logger = Logger.getLogger("");
		// Remove terminal output from logger - log only to output file serverLog.txt
		for (Handler x : logger.getHandlers()) {
			logger.removeHandler(x);
		}
		logFile = new FileHandler(filename);
		logger.addHandler(logFile);
		VerySimpleFormatter formatter = new VerySimpleFormatter();
        logFile.setFormatter(formatter);
		
	}

	// Helper function to return readable timestamp string
	private String getTimestamp() {
		return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS").format(new Date());
	}

	public void log(boolean outputTerminal, Level level, String message) {
		if (level == Level.WARNING) {
			logger.warning(message + ";"); // Print to logFile
		} else {
			logger.info(message + ";");
		}
		
		if (outputTerminal) {			
			System.out.println(getTimestamp() + "  " + level.toString() + "  " + message + ";"); // Print to terminal
		}
		
	}
	
	// close the filehandler to ensure no file locks remain
	public void close() {
		this.logFile.close();
	}
	
}
