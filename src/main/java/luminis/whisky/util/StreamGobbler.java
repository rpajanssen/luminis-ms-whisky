package luminis.whisky.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Writes an input stream to system.out.
 */
public class StreamGobbler extends Thread {
    private final InputStream inputStream;
    private final String type;

    public StreamGobbler(InputStream inputStream, String type) {
        this.inputStream = inputStream;
        this.type = type;
    }

    public void run() {
        try
        {
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}