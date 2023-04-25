package server;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Logger class used to log error/info message on stdout. Every log message logs current timestamp
 * with every log message.
 */
public class ServerLogger {

  /**
   * returns timestamp as string in 'yyyy-mm-dd:mm:ss.SSS' format.
   * @return string timestamp
   */
  private static String getCurrentFormattedTime() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
  }

  /**
   * Logs an info message on the stdout.
   * @param msg message to be logged.
   */
  public static void info(String msg) {
    System.out.println(String.format("%s Level: INFO, message: %s", getCurrentFormattedTime(), msg));
  }


  /**
   * Logs an error message on the stdout.
   * @param msg message to be logged.
   */
  public static void error(String msg) {
    System.out.println(String.format("%s Level: ERROR, message: %s", getCurrentFormattedTime(), msg));
  }
}
