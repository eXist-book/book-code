package exist.book.example.xmldb.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.exist.xmldb.EXistResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XPathQueryService;

public class QueryApp {
    
    private final static Logger logger = LoggerFactory.getLogger(QueryApp.class);
    
    public static void main(final String[] args) throws Exception {
        
        //get the command line arguments
        if(args.length < 5) {
            printUseage();
            System.exit(1);
            return;
        }
        final String server = args[0];
        final int port = Integer.parseInt(args[1]);
        
        final String query = args[2];
        final File queryFile;
        if(query.charAt(0) == '@') {
           queryFile = new File(query.substring(1));
           if(!queryFile.exists()) {
               System.err.println("Query file '" + queryFile.getAbsolutePath() + "' does not exist!");
               System.exit(2);
            } 
        } else {
            queryFile = null;
        }
        
        final String path = args[3];
        final String username = args[4];
        final String password;
        if(args.length == 6) {
            password = args[5];
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
            final String uri = "xmldb:exist://" + server + ":" + port + "/exist/xmlrpc" + path;
            coll = DatabaseManager.getCollection(uri, username, password);
            
            if(coll == null) {
                logger.error("Collection {} does not exist!", path);
                System.exit(3);
            } else {
        
                final XPathQueryService queryService = (XPathQueryService)coll.getService("XPathQueryService", "1.0");

                logger.info("Starting Query of {}...", uri);

                final ResourceSet results;
                if(queryFile == null) {
                    //execute the XQuery
                    results = queryService.query(query);
                } else {
                    //execute the XQuery from a file
                    final StringBuilder queryBuilder = new StringBuilder();
                    fileContents(queryFile, queryBuilder);

                    results = queryService.query(queryBuilder.toString());
                }

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
                    logger.info("Finished Query OK.");
                } else {
                    logger.error("Query returned null results");
                    System.exit(4);
                }
            }
        } finally {
            //close the collection
            if(coll != null) {
                coll.close();
            }
        }
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
        System.err.println("useage: QueryApp <server> <port> <query> <path> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tquery: The query itself or a file path containing the query prefixed by an @, e.g. @/tmp/my.xquery.");
        System.err.println("\tpath: The path to a collection or resource in the database which should be set as the context of the query e.g. /db/mycollection.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
