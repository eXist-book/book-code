package exist.book.example.xmldb.client;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Queue;
import org.exist.xmldb.EXistResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;

public class StoreApp {
    
    private final static Logger logger = LoggerFactory.getLogger(StoreApp.class);
    
    public static void main(final String[] args) throws Exception {
        
        //get the command line arguments
        if(args.length < 6) {
            printUseage();
            System.exit(1);
            return;
        }
        final String server = args[0];
        final int port = Integer.parseInt(args[1]);
        final File file = new File(args[2]);
        if(!file.exists()) {
           System.err.println("File '" + file.getAbsolutePath() + "' does not exist!");
           System.exit(2);
        }
        final String resourceType = args[3].equals("true") ? "XMLResource" : "BinaryResource";
        final String collection = args[4];
        final String username = args[5];
        final String password;
        if(args.length == 7) {
            password = args[6];
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
            coll = getOrCreateCollection(uri, collection, username, password);

            //store the resource
            logger.info("Starting upload of {} to {}{}...", file.getAbsolutePath(), uri, collection);
            res = coll.createResource(file.getName(), resourceType);
            res.setContent(file);
            coll.storeResource(res);
            logger.info("Finished upload OK.");
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
    
    private static Collection getOrCreateCollection(final String uri, final String collectionUri, final String username, final String password) throws XMLDBException {
        final Queue<String> segments = new ArrayDeque<String>();
        for(final String pathSegment : collectionUri.split("/")) {
            if(!pathSegment.isEmpty()) {
                segments.add(pathSegment);
            }
        }
        return getOrCreateCollection(DatabaseManager.getCollection(uri + "/" + segments.poll(), username, password), segments);
    }
    
    private static Collection getOrCreateCollection(final Collection current, final Queue<String> descendants) throws XMLDBException {
        if(descendants.isEmpty()) {
            return current;
        } else {
            final String childName = descendants.poll();
            Collection child = current.getChildCollection(childName);
            if(child == null) {
                final CollectionManagementService mgmt = (CollectionManagementService) current.getService("CollectionManagementService", "1.0");
                child = mgmt.createCollection(childName);
                current.close(); //close the current collection, child will remain open
            }
            return getOrCreateCollection(child, descendants);
        }
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: StoreApp <server> <port> <file> <is-xml-resource> <collection> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tfile: The path to a file you wish to store in the database.");
        System.err.println("\tis-xml-resource: true if the resource you wish to store is an XML document, false otherwise.");
        System.err.println("\tcollection: The path to a collection in the database where the file should be stored e.g. /db/mycollection.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
