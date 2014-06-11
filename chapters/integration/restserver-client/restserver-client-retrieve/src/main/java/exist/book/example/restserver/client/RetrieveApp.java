package exist.book.example.restserver.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveApp {
    
    private final static Logger logger = LoggerFactory.getLogger(RetrieveApp.class);
    
    public static void main(final String[] args) {
        
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
        
        //setup authentication
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        final DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, credentialsProvider);
        
        //setup the Resource for the REST Server API Call
        final Client client = Client.create();
        final String uri = "http://" + server + ":" + port + "/exist/rest" + path;
        final WebResource resource = client.resource(uri);
        
        logger.info("Starting download of {}...", uri);
        
        //GET the Resource
        final ClientResponse response = resource.get(ClientResponse.class);
        final Status responseStatus = response.getClientResponseStatus();
        if(responseStatus == Status.OK) {
            logger.info("Received: {}", response.getType().toString());
            
            //download the Resource and print the content out on the console
            ConsoleWriter.writeResponseBody(response, "Finished download OK.");
        } else {
            logger.error("Received HTTP Response: {} {}", responseStatus.getStatusCode(), responseStatus.toString());
            System.exit(2);
        }
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
