package exist.book.example.webdav.client;

import io.milton.httpclient.ProgressListener;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleProgressListener implements ProgressListener {
    
    private final static Logger logger = LoggerFactory.getLogger(ConsoleProgressListener.class);
    
    public enum Operation {
        Upload,
        Download
    }
    
    private final Operation operation;

    public ConsoleProgressListener(final Operation operation) {
        this.operation = operation;
    }
    
    @Override
    public void onRead(final int bytes) {
        //logger.info("{}ed {} bytes.", operation.name, bytes);
    }

    @Override
    public void onProgress(final long bytesRead, final Long totalBytes, final String fileName) {
        if(totalBytes == null) {
            logger.info("{}ed {} bytes of {}", new Object[]{operation.name(), bytesRead, fileName});
        } else {
            final double percentage = ((double)bytesRead / totalBytes);
            logger.info("{} of {}: {} complete.", new Object[]{operation.name(), fileName, new DecimalFormat("##0.00#%").format(percentage)});
        }
    }

    @Override
    public void onComplete(final String fileName) {
        logger.info("Completed {} of {}", operation.name(), fileName);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
