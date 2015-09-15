package exist.book.example.fluent.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.exist.fluent.Database;
import org.exist.fluent.DatabaseException;
import org.exist.fluent.Document;
import org.exist.fluent.Folder;
import org.exist.fluent.Item;
import org.exist.fluent.ItemList;
import org.exist.fluent.Name;
import org.exist.fluent.Source.Blob;
import org.exist.fluent.Source.XML;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        
        final Path source = Paths.get(args[1]);
        if(!Files.exists(source)) {
            System.err.println("Source '" + source.toAbsolutePath() + "' does not exist!");
            System.exit(2);
         } 
        
        final String query = args[2];
        final Path queryFile;
        if(query.charAt(0) == '@') {
           queryFile = Paths.get(query.substring(1));
           if(!Files.exists(queryFile)) {
               System.err.println("Query file '" + queryFile.toAbsolutePath() + "' does not exist!");
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
        
        try {
            //initialise the database driver and login
            final Path config = new ExampleApp().getConfig();
            if(config == null) {
                logger.error("Could not retrieve conf.xml from the classpath");
                System.exit(4);
            }
            
            Database.startup(config);
            final Database db = Database.login(username, password);
            
            //1) get the collection
            Folder folder = null;
            try {
                folder = db.getFolder(path);
            } catch(final DatabaseException de) {
                //1.1) if the collection does not exist, create it!
                logger.warn("Collection {} does not exist! Creating collection...", path);
                folder = db.createFolder(path);
                logger.info("Created Collection {}", path);
                
            }
        
            //2) store the document(s) into the collection
            storeDocuments(folder, source);

            //3) query the collection
            final ItemList results;
            if(queryFile == null) {
                //execute the XQuery
                 results = queryCollection(folder, query);
            } else {
                //execute the XQuery from a file
                final StringBuilder queryBuilder = new StringBuilder();
                fileContents(queryFile, queryBuilder);
                results = queryCollection(folder, queryBuilder.toString());
            }

            //4) print the results
            printResults(results);

            //5) remove the document(s) we stored earlier
            removeDocuments(folder, source);
            
        } finally {
            //6) shutdown the database
            Database.shutdown();
        }
    }
    
    /**
     * Stores a File or Directory of Files into a Collection in eXist.
     * Given a Directory, all files and sub-directories are stored recursively.
     */
    private static void storeDocuments(final Folder folder, final Path source) {
        if(Files.isDirectory(source)) {
            //create a sub-collection
            final Folder subFolder = folder.children().create(source.getFileName().toString());
            try (final Stream<Path> children = Files.list(source)) {
                for(final Path f : children.collect(Collectors.toList())) {
                    storeDocuments(subFolder, f);
                }
            } catch (final IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            }
        } else {
            //determine the files type
            final MimeTable mimeTable = MimeTable.getInstance();
            final MimeType mimeType = mimeTable.getContentTypeFor(source.getFileName().toString());
            //final String resourceType = mimeType.isXMLType() ? "XMLResource" : "BinaryResource";
            
            //store the file
            logger.info("Starting store of {} to {}/{}...", source.toAbsolutePath(), folder.name(), source.getFileName());
            final Name name = Name.create(folder.database(), source.getFileName().toString());
            final Document doc;
            if(mimeType != null && mimeType.isXMLType()) {
                doc = folder.documents().load(name, XML.xml(source.toFile()));
            } else {
                doc = folder.documents().load(name, Blob.blob(source.toFile()));
            }
        }
    }
    
    /**
     * Queries a Collection in eXist
     */
    private static ItemList queryCollection(final Folder folder, final String query) {
        logger.info("Starting Query of {}...", folder.name());
        try {
            return folder.query().all(query);
        } finally {
            logger.info("Finished Query OK.");
        }
    }
    
    /**
     * Prints the results of a Query to stdout
     */
    private static void printResults(final ItemList results) {
        
        logger.info("Printing Query results...");
        
        //iterate through the results
        if(results != null) {
            for(final Item result : results.asList()) {
                //print the result on the console
                System.out.println(result.value());
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
    private static void removeDocuments(final Folder folder, final Path source) {
        
        logger.info("Removing previously stored file(s)...");
        if(Files.isDirectory(source)) {
            //removing a collection removes all of its sub-collections and files recursively
            folder.children().get(source.getFileName().toString()).delete();
        } else {
            folder.documents().get(source.getFileName().toString()).delete();
        }
        logger.info("Removed previously stored file(s) OK");
    }
    
    /**
     * Appends the content of a text file to the String Builder
     * 
     * @param f The file to read the contents of
     * @param builder The String Builder to append the file contents to
     */
    private static void fileContents(final Path f, final StringBuilder builder) {
        try {
            builder.append(new String(Files.readAllBytes(f), UTF_8));
        } catch(final IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
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
    
    public Path getConfig() {
//        try {
            
            InputStream is = null;
            try {
                
                final URL url = getClass().getClassLoader().getResource("conf.xml");
                logger.info("Found conf.xml on classpath at {}", url);
                
                is = url.openStream();
                final Path tmpConfig = Files.createTempFile("conf.xml", "tmp");
                Files.copy(is, tmpConfig);
                
                logger.info("Unpacked conf.xml to {}", tmpConfig.toAbsolutePath());
                
                return tmpConfig;
            } catch(final IOException ioe) {
                logger.error("Could not get conf.xml: {}", ioe.getMessage());
                return null;
            } finally {
                try { is.close(); } catch(final IOException ioe) { logger.warn(ioe.getMessage()); }
            }
    }
}
