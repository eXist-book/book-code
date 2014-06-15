package exist.book.example.restserver.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.File;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
        final File file = new File(args[2]);
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
        
        //setup authentication
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        final DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, credentialsProvider);
        
        //setup the Resource for the REST Server API Call
        final Client client = ApacheHttpClient4.create(config);
        final String uri = "http://" + server + ":" + port + "/exist/rest" + collection + "/" + file.getName();
        final WebResource resource = client.resource(uri);
        
        logger.info("Starting upload of {} to {}...", file.getAbsolutePath(), uri);
        
        //PUT the Resource
        final ClientResponse response = resource.put(ClientResponse.class, file);
        final Status responseStatus = response.getClientResponseStatus();
        if(responseStatus == Status.CREATED) {
            ConsoleWriter.writeResponseBody(response, "Finished upload OK.");
        } else {
            logger.error("Received HTTP Response: {} {}", responseStatus.getStatusCode(), responseStatus.toString());
            System.exit(3);
        }
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
