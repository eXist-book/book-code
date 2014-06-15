package exist.book.example.webdav.client;

import io.milton.common.Path;
import io.milton.httpclient.Folder;
import io.milton.httpclient.Host;
import io.milton.httpclient.HostBuilder;
import static exist.book.example.webdav.client.ConsoleProgressListener.Operation.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreApp {
    
    private final static Logger logger = LoggerFactory.getLogger(StoreApp.class);
    
    public static void main(final String[] args) throws Exception {
        
        //get the command line arguments
        if(args.length < 5) {
            printUseage();
            System.exit(1);
            return;
        }
        final String server = args[0];
        final int port = Integer.parseInt(args[1]);
        final java.io.File file = new java.io.File(args[2]);
        if(!file.exists()) {
           System.err.println("File '" + file.getAbsolutePath() + "' does not exist!");
           System.exit(2);
        }
        final String collection = args[3];
        final String username = args[4];
        final String password;
        if(args.length == 6) {
            password = args[5];
        } else {
            password = "";
        }
        
        //connect to the eXist WebDAV Server
        final HostBuilder builder = new HostBuilder();
        builder.setServer(server);
        builder.setPort(port);
        builder.setRootPath("exist/webdav/db");
        builder.setUser(username);
        builder.setPassword(password);
        final Host host = builder.buildHost();
        
        //access the WebDAV folder
        final Path collectionPath = Path.path(host.path(), folderFromCollectionPath(collection));
        final Folder folder = host.getOrCreateFolder(collectionPath, true);
        
        logger.info("Starting upload of {} to {}...", file.getAbsolutePath(), collection);
        
        //upload the file
        folder.upload(file, new ConsoleProgressListener(Upload));
        
        logger.info("Finished upload OK.");
    }
    
    private static String folderFromCollectionPath(String path) {
        path = path.replace("/db", "");
        if(path.isEmpty()) {
            path = "/";
        }
        return path;
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: StoreApp <server> <port> <file> <collection> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tfile: The path to a file you wish to store in the database.");
        System.err.println("\tcollection: The path to a collection in the database where the file should be stored e.g. /db/mycollection.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
