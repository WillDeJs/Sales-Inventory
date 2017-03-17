package Utilities;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to log debugging events
 * Created by WillDeJs on 1/14/2017.
 */
public class Trace {
    // single instance
    private static Trace instance = null;
    
    // severity levels
    public enum Levels{INFO, WARNING, ERROR, FATAL_ERROR}
    
    // log file
    File file;

    // log writer
    private static  PrintWriter out = null;
    /**
     * Constructor builds a Trace object
     * @param url file path to save log
     */
    private Trace (String url) {
        try {
            file = new File(url);
            if (!file.exists())
                file.createNewFile();
            out = new PrintWriter(new FileOutputStream(file, true),true);
        }catch (Exception ex ){

            ex.printStackTrace();

        }
    }

    /**
     * Default Constructor builds a Trace object. By default, log file is run.log
     *
     */
    private Trace() {
        this( "run.log");
    }

    /**
     * Logs a debugging event to file
     * @param cls   class on which the error ocurred
     * @param level severity level of the event
     * @param message event message
     */
    public void log(Class cls, Levels level, String message) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy");
        switch (level) {
            case INFO:
                out.format("%s\t %s\t %s \t%s.\n\r", format.format(date), 
                        "INFO\t", cls.getName(),  message);
                break;
            case WARNING:
                out.format("%s\t %s\t %s \t%s.\n\r", format.format(date),
                        "WARNING\t", cls.getName(), message);
                break;
            case ERROR:
                out.format("%s\t %s\t %s \t%s.\n\r", format.format(date),
                        "ERROR\t", cls.getName(), message);
                break;
            case FATAL_ERROR:
                out.format("%s\t %s\t %s \t%s.\n\r", format.format(date),
                        "FATAL_ERROR\t", cls.getName(), message);
                break;
            default:
                break;
        }
    }
     /**
     * Logs a debugging event to file
     * @param cls   class on which the error ocurred
     * @param level severity level of the event
     * @param ex exception thrown if any
     */
    public void log (Class cls, Levels level,  Exception ex) {
        log(cls, level, "Line "  +
                ex.getStackTrace()[0].getFileName() +
                " FILE " + ex.getStackTrace()[0].getFileName()
                + " " + ex.getMessage());
    }
     /**
     * Logs a debugging event to file
     * @param cls   class on which the error ocurred
     * @param level severity level of the event
     * @param message event message
     * @param ex exception thrown if any
     */
    public void log(Class cls, Levels level, String message, Exception ex) {
        log(cls, level, "Line "  +
                ex.getStackTrace()[0].getFileName() +
                " FILE " + ex.getStackTrace()[0].getFileName()
                + " " + ex.getMessage() + " " + message);
    }

    /**
     * Retrieves current Trace 
     * @param traceName
     * @return current Trace instance
     */
    public static Trace  getTrace(String traceName) {
        if (instance == null) {
            instance = new Trace(traceName);
        }
        return instance;
    }
    public static Trace getTrace() {
        if (instance == null)
            instance = new Trace();
        return instance;
    }
    /* Clean up*/
    @Override
    public void finalize(){
        out.close();
    }

}
