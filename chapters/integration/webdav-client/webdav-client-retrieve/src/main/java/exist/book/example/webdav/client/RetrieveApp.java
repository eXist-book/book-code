package exist.book.example.webdav.client;

import io.milton.httpclient.File;
import io.milton.httpclient.Folder;
import io.milton.httpclient.Host;
import io.milton.httpclient.HostBuilder;
import io.milton.httpclient.Resource;
import static exist.book.example.webdav.client.ConsoleProgressListener.Operation.Download;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveApp {
    
    private final static Logger logger = LoggerFactory.getLogger(RetrieveApp.class);
    
    public static void main(final String[] args) throws Exception {
        
        //get the command line arguments
        if(args.length < 4) {
            printUseage();
            System.exit(1);
            return;
        }
        final String server = args[0];
        final int port = Integer.parseInt(args[1]);
        final String path = args[2];
        final String username = args[3];
        final String password;
        if(args.length == 5) {
            password = args[4];
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
        final Folder folder = host.getFolder(folderFromPath(path));
        
        //get a file from the WebDAV folder
        final Resource resource = folder.child(filenameFromPath(path));
        
        if(resource instanceof File) {
            
            logger.info("Starting download of {}...", path);
            
            //download the file and print the file out on the console
            final File f = (File)resource;
            f.download(System.out, new ConsoleProgressListener(Download));
            
            logger.info("Finished download OK.");
            
        } else if(resource instanceof File) {
            System.err.println("Path " + path + " is a Collection.");
            System.exit(2);
        }
    }
    
    private static String folderFromPath(String path) {
        path = path.replace("/db", "");
        path = path.substring(0, path.lastIndexOf('/'));
        if(path.isEmpty()) {
            path = "/";
        }
        return path;
    }
    
    private static String filenameFromPath(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: RetrieveApp <server> <port> <path> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tpath: The path to a document in the database e.g. /db/some-document.xml.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
        System.err.println("All messages and errors from the application are written to Standard error (stderr), whilst the content of the retrieved file is written to Standard output (stdout). This enables you to pipe the output of this application to a file or other destination if you wish.");
        System.err.println();
    }
}
