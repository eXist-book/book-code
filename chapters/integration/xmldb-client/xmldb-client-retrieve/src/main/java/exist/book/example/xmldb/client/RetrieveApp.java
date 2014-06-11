package exist.book.example.xmldb.client;

import org.exist.xmldb.EXistResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;

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
        
        //initialise the database driver
        final Class<Database> dbClass = (Class<Database>) Class.forName("org.exist.xmldb.DatabaseImpl");
        final Database database = dbClass.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);
        
        Collection coll = null;
        Resource res = null;
        try {
            //get the collection
            final String uri = "xmldb:exist://" + server + ":" + port + "/exist/xmlrpc";
            coll = DatabaseManager.getCollection(uri + collectionPathFromPath(path), username, password);
            
            logger.info("Starting download of {}{}...", uri, path);
            res = coll.getResource(resourceNameFromPath(path));
            if(res != null) {
                logger.info("Received: {}", res.getResourceType());
                
                //download the Resource and print the content out on the console
                System.out.println(res.getContent());
                
                logger.info("Finished download OK.");
            } else {
               logger.error("Resource: {}{} not found", uri, path);
                System.exit(2); 
            }
        } finally {
            //cleanup resource
            if(res != null) {
                ((EXistResource)res).freeResources();
            }
            
            //close the collection
            if(coll != null) {
                coll.close();
            }
        }
    }
    
    private static String collectionPathFromPath(String path) {
        path = path.substring(0, path.lastIndexOf('/'));
        if(path.isEmpty()) {
            path = "/db";
        }
        return path;
    }
    
    private static String resourceNameFromPath(final String path) {
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
