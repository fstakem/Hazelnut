package main.research.fstakem.mocap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility 
{
	// Logger
	static final Logger logger = LoggerFactory.getLogger(Utility.class);
	
	static final String STACK_TRACE_HEADER = "*******************************************************************************";
		
	public static ArrayList<String> readFile(InputStream input_stream) throws IOException
	{
		logger.debug("Utility.readFile(): Entering method.");
		
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		
		BufferedReader buffer_reader = new BufferedReader(new InputStreamReader(input_stream));
		while ( (line = buffer_reader.readLine()) != null ) 
			lines.add(line);
		
		logger.debug("Utility.readFile(): Exiting method.");
		return lines;
	}
	
	public static void printStackTraceToLog(Logger logger, Exception e)
	{
		StringWriter string_writer = new StringWriter();
		PrintWriter print_writer = new PrintWriter(string_writer);
		e.printStackTrace(print_writer);
		
		logger.error(Utility.STACK_TRACE_HEADER);
		logger.error(string_writer.toString());
		logger.error(Utility.STACK_TRACE_HEADER);
	}
}
