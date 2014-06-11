package exist.book.example.restserver.client;

import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleWriter {
   
    private final static Logger logger = LoggerFactory.getLogger(ConsoleWriter.class);
    
    public static void writeResponseBody(final ClientResponse response, final String successMessage) {
        InputStream is = null;
        try {
            is = response.getEntityInputStream();
            final byte buf[] = new byte[1024];
            int read = -1;

            while((read = is.read(buf)) > -1) {
                System.out.print(new String(buf, 0, read));
            }
            System.out.println();

            logger.info(successMessage);
        } catch(final IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        } finally {
            if(is != null) {
                try { is.close(); } catch(final IOException ioe) { logger.warn(ioe.getMessage(), ioe); }
            }
        } 
   }
}
