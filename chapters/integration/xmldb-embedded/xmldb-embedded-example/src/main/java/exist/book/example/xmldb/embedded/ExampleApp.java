package exist.book.example.xmldb.embedded;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.xmldb.DatabaseInstanceManager;
import org.exist.xmldb.EXistResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XPathQueryService;

public class ExampleApp {
    private final static Logger logger = LoggerFactory.getLogger(ExampleApp.class);
    
    public static void main(final String[] args) throws Exception {
        
        //get the command line arguments
        if(args.length < 4) {
            printUseage();
            System.exit(1);
            return;
        }
        
        final String path = args[0];
        
        final File source = new File(args[1]);
        if(!source.exists()) {
            System.err.println("Source '" + source.getAbsolutePath() + "' does not exist!");
            System.exit(2);
         } 
        
        final String query = args[2];
        final File queryFile;
        if(query.charAt(0) == '@') {
           queryFile = new File(query.substring(1));
           if(!queryFile.exists()) {
               System.err.println("Query file '" + queryFile.getAbsolutePath() + "' does not exist!");
               System.exit(3);
            } 
        } else {
            queryFile = null;
        }
        
        final String username = args[3];
        final String password;
        if(args.length == 5) {
            password = args[4];
        } else {
            password = "";
        }
        
        //set EXIST_HOME
        final String EXIST_HOME = System.getProperty("user.dir");
        logger.info("Setting EXIST_HOME=" + EXIST_HOME);
        System.setProperty("exist.home", EXIST_HOME);
        
        //initialise the database driver
        final Class<Database> dbClass = (Class<Database>) Class.forName("org.exist.xmldb.DatabaseImpl");
        final Database database = dbClass.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);
        
        Collection coll = null;
        try {
            //1) get the collection
            final String uri = "xmldb:exist://" + path;
            coll = DatabaseManager.getCollection(uri, username, password);
            
            if(coll == null) {
                //1.1) if the collection does not exist, create it!
                logger.warn("Collection {} does not exist! Creating collection...", path);
                coll = createCollection(uri, username, password);
                logger.info("Created Collection {}", path);
            }
        
            //2) store the document(s) into the collection
            storeDocuments(coll, source);

            //3) query the collection
            final ResourceSet results;
            if(queryFile == null) {
                //execute the XQuery
                results = queryCollection(coll, query);
            } else {
                //execute the XQuery from a file
                final StringBuilder queryBuilder = new StringBuilder();
                fileContents(queryFile, queryBuilder);

                results = queryCollection(coll, queryBuilder.toString());
            }

            //4) print the results
            printResults(results);

            //5) remove the document(s) we stored earlier
            removeDocuments(coll, source);
            
        } finally {
            //close the collection
            if(coll != null) {
                final DatabaseInstanceManager manager = (DatabaseInstanceManager)coll.getService("DatabaseInstanceManager", "1.0");
                try {
                    coll.close();
                } finally {
                    //6) shutdown the database
                    manager.shutdown();
                }
            }
        }
    }
    
    /**
     * Creates a Collection from an XMLDB URI
     * 
     * @param uri The XMLDB URI to a Collection to create in the database
     * @param username
     * @param password 
     * 
     * @return The created Collection
     */
    private static Collection createCollection(final String uri, final String username, final String password) throws XMLDBException {
        if(!uri.startsWith("xmldb:exist:///db")) {
            throw new IllegalArgumentException("Invalid Local Database URI: " + uri);
        }
        
        final Collection dbColl = DatabaseManager.getCollection("xmldb:exist:///db", username, password);
        final Queue<String> subNames = new LinkedList<String>();
        for(String subName : uri.replaceFirst("xmldb:exist:///db", "").split("\\/")) {
            subNames.add(subName);
        }
        
        return createCollectionPath(dbColl, subNames);
    }
    
    /**
     * Creates a collection at a path from the parent collection
     * 
     * i.e. if the parent collection is "/db" and the pathSegments are ['a', 'b', c']
     * we will create and return the collection /db/a/b/c
     * 
     * @param parent the parent collection in which to create the collection path
     * @param pathSegments Each segment of the path to create, i.e. each segment is a sub-collection
     * 
     * @return The created collection indicated by the path
     */
    private static Collection createCollectionPath(final Collection parent, final Queue<String> pathSegments) throws XMLDBException {
        if(pathSegments.isEmpty()) {
            return parent;
        } else {
            final String subCollectionName = pathSegments.remove();
            final CollectionManagementService mgmtService = (CollectionManagementService)parent.getService("CollectionManagementService", "1.0");
            try {
                final Collection subColl = mgmtService.createCollection(subCollectionName);
                return createCollectionPath(subColl, pathSegments);
            } finally {
                parent.close();
            }
        }
    }
    
    /**
     * Stores a File or Directory of Files into a Collection in eXist.
     * Given a Directory, all files and sub-directories are stored recursively.
     */
    private static void storeDocuments(final Collection coll, final File source) throws XMLDBException {
        if(source.isDirectory()) {
            final CollectionManagementService mgmtService = (CollectionManagementService)coll.getService("CollectionManagementService", "1.0");
            final Collection subColl = mgmtService.createCollection(source.getName());
            try {
                for(final File f: source.listFiles()) {
                    storeDocuments(subColl, f);
                }
            } finally {
                //close the collection
                subColl.close();
            }
        } else {
            //determine the files type
            final MimeTable mimeTable = MimeTable.getInstance();
            final MimeType mimeType = mimeTable.getContentTypeFor(source.getName());
            final String resourceType = mimeType != null && mimeType.isXMLType() ? "XMLResource" : "BinaryResource";
            
            //store the file
            logger.info("Starting store of {} to {}/{}...", source.getAbsolutePath(), coll.getName(), source.getName());
            Resource res = null; 
            try {
                res = coll.createResource(source.getName(), resourceType);
                res.setContent(source);
                coll.storeResource(res);
            } finally {
                //cleanup resource
                if(res != null) {
                    ((EXistResource)res).freeResources();
                } 
            }
        }
    }
    
    /**
     * Queries a Collection in eXist
     */
    private static ResourceSet queryCollection(final Collection coll, final String query) throws XMLDBException {
        final XPathQueryService queryService = (XPathQueryService)coll.getService("XPathQueryService", "1.0");
        logger.info("Starting Query of {}...", coll.getName());
        try {
            return queryService.query(query);
        } finally {
            logger.info("Finished Query OK.");
        }
    }
    
    /**
     * Prints the results of a Query to stdout
     */
    private static void printResults(final ResourceSet results) throws XMLDBException {
        
        logger.info("Printing Query results...");
        
        //iterate through the results
        if(results != null) {
            final ResourceIterator iterator = results.getIterator();

            while(iterator.hasMoreResources()) {
                Resource res = null;
                try {
                    res = iterator.nextResource();

                    //print the result on the console
                    System.out.println(res.getContent());
                } finally {
                    //cleanup resource
                    if(res != null) {
                        ((EXistResource)res).freeResources();
                    }
                }
            }
            logger.info("Printed Query results OK");
        } else {
            logger.error("Query returned null results");
            System.exit(5);
        }
    }
    
    /**
     * Removes document(s) and/or collection(s) from a collection based on a Source file or directory
     */
    private static void removeDocuments(final Collection coll, final File source) throws XMLDBException {
        
        logger.info("Removing previously stored file(s)...");
        if(source.isDirectory()) {
            //removing a collection removes all of its sub-collections and files recursively
            final CollectionManagementService mgmtService = (CollectionManagementService)coll.getService("CollectionManagementService", "1.0");
            mgmtService.removeCollection(source.getName());
        } else {
            Resource res = null;
            try {
                res = coll.getResource(source.getName());
                coll.removeResource(res);
            } finally {
              //cleanup resource
                if(res != null) {
                    ((EXistResource)res).freeResources();
                }  
            }           
        }
        logger.info("Removed previously stored file(s) OK");
    }
    
    /**
     * Appends the content of a text file to the String Builder
     * 
     * @param f The file to read the contents of
     * @param builder The String Builder to append the file contents to
     */
    private static void fileContents(final File f, final StringBuilder builder) {
        Reader reader = null;
        try {
           reader = new FileReader(f); 
           final char buf[] = new char[1024];
           int read = -1;
           while((read = reader.read(buf)) != -1) {
               builder.append(buf, 0, read);
           }
        } catch(final IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        } finally {
            if(reader != null) {
                try { reader.close(); } catch(final IOException ioe) { logger.warn(ioe.getMessage(), ioe); }
            }
        }
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: ExampleApp <path> <source> <query> <username> [password]");
        System.err.println("\tpath: The path to a collection in the database, e.g. /db.");
        System.err.println("\tsource: The file or directory of files to store into the collection indicated by <path>.");
        System.err.println("\tquery: The query itself or a file path containing the query prefixed by an @, e.g. @/tmp/my.xquery.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
