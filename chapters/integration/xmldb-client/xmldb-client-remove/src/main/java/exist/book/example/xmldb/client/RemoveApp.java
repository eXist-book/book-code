package exist.book.example.xmldb.client;

import org.exist.xmldb.CollectionManagementServiceImpl;
import org.exist.xmldb.EXistResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;

public class RemoveApp {
    
    private final static Logger logger = LoggerFactory.getLogger(RemoveApp.class);
    
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
        try {
            //get the collection
            final String uri = "xmldb:exist://" + server + ":" + port + "/exist/xmlrpc";
            final String subPath = subPathFromPath(path);
            coll = DatabaseManager.getCollection(uri + subPath, username, password);
            
            if(coll == null) {
                logger.error("Collection {} does not exist!", subPath);
                System.exit(3);
            } else {
                logger.info("About to remove {}...", path);
                
                final String name = nameFromPath(path);
                if(hasSubCollection(coll, name)) {
                    //remove sub-collection
                    final CollectionManagementService mgmtService = (CollectionManagementServiceImpl)coll.getService("CollectionManagementService", "1.0");
                    mgmtService.removeCollection(name);
                } else {
                    //remove resource
                    Resource res = null;
                    try {
                        res = coll.getResource(name);
                        if(res == null) {
                            logger.error("Resource {} does not exist!", path);
                            System.exit(4);
                        } else {
                            coll.removeResource(res);
                        }       
                    } finally {
                        //cleanup resource
                        if(res != null) {
                            ((EXistResource)res).freeResources();
                        }
                    }
                }
                logger.info("Removed OK.");
            }
        } finally {
            //cleanup collection
            if(coll != null) {
                coll.close();
            }
        }
    }
    
    private static boolean hasSubCollection(final Collection collection, final String subcollectionName) throws XMLDBException {
        Collection sub = null;
        try {
            sub = collection.getChildCollection(subcollectionName);
            return sub != null;
        } finally {
            //cleanup sub-collection
            if(sub != null) {
                sub.close();
            }
        }
    }
    
    private static String subPathFromPath(String path) {
        path = path.substring(0, path.lastIndexOf('/'));
        if(path.isEmpty()) {
            path = "/db";
        }
        return path;
    }

    private static String nameFromPath(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: RemoveApp <server> <port> <path> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tpath: The path to a collection or resource in the database that should be removed.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
