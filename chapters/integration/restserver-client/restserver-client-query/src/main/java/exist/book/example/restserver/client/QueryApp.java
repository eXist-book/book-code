package exist.book.example.restserver.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import javax.ws.rs.core.MediaType;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        //setup authentication
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        final DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, credentialsProvider);
        
        //setup the Resource for the REST Server API Call
        final Client client = ApacheHttpClient4.create(config);
        final String uri = "http://" + server + ":" + port + "/exist/rest" + path;
        final WebResource resource;
        final ClientResponse response;
        
        logger.info("Starting Query of {}...", uri);
        
        if(queryFile == null) {
            resource = client.resource(uri + "?_query=" + URLEncoder.encode(query, "UTF-8"));
            logger.info("Using HTTP GET to perform the Query...");
            
            //GET the Resource
            response = resource.get(ClientResponse.class);
        } else {
            resource = client.resource(uri);
            logger.info("Using HTTP POST to perform the Query...");
            
            //POST the Resource
            response = resource.type(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class, createQueryDocument(queryFile));
        }
        
        
        final Status responseStatus = response.getClientResponseStatus();
        if(responseStatus == Status.OK) {
            ConsoleWriter.writeResponseBody(response, "Finished Query OK.");
        } else {
            logger.error("Received HTTP Response: {} {}", responseStatus.getStatusCode(), responseStatus.toString());
            System.exit(3);
        }
    }
    
    /**
     * @param queryContent The file containing the content of the XML Query
     * document that this function is constructing
     * 
     * @return An XML document suitable for POST'ing the REST Server API
     * to execute a Query
     */
    private static String createQueryDocument(final File queryContent) {
        
        final String EOL = System.getProperty("line.separator");
        
        final StringBuilder builder = new StringBuilder();
        
        builder.append("<query xmlns=\"http://exist.sourceforge.net/NS/exist\">");
        builder.append(EOL);
        builder.append("<text><![CDATA[");
        builder.append(EOL);
        
        fileContents(queryContent, builder);
        
        builder.append(EOL);
        builder.append("]]></text>");
        builder.append(EOL);
        builder.append("</query>");
        
        return builder.toString();
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
