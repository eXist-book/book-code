package exist.book.example.xmlrpc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redstone.xmlrpc.XmlRpcClient;

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
        final String mediaType = args[3];
        final String collection = args[4];
        final String username = args[5];
        final String password;
        if(args.length == 7) {
            password = args[6];
        } else {
            password = "";
        }
        
        //get an XML-RPC connection to the eXist server
        final String uri = "http://" + server + ":" + port + "/exist/xmlrpc";
        final XmlRpcClient rpc = new XmlRpcClient(new URL(uri), true);
        
        //setup HTTP Basic Authentication
        final String auth = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
        rpc.setRequestProperty("Authorization", "Basic " + auth);
        
        logger.info("Starting upload of {} to {}{}...", file.getAbsolutePath(), uri, collection);
        
        //upload the resource
        String remoteIdentifier = null;
        final byte buf[] = new byte[1024];
        int read = -1;
        InputStream is = null;
        try {
            is = new FileInputStream(file);            
            while((read = is.read(buf)) > -1) {
                if(remoteIdentifier == null) {
                    //upload first chunk of file to server
                    remoteIdentifier = (String)rpc.invoke("upload", new Object[] { buf, read });
                } else {
                    //append further chunks of file to remote file on server
                    remoteIdentifier = (String)rpc.invoke("upload", new Object[]{ remoteIdentifier, buf, read });
                }
            }
        } finally {
            if(is != null) {
                is.close();
            }
        }
        
        //have the server parse and store the remote file into the database
        final Boolean result = (Boolean)rpc.invoke("parseLocal", new Object[] { remoteIdentifier, collection + "/" + file.getName(), true, mediaType });
        if(result.booleanValue()) {
            logger.info("Finished upload OK.");     
        } else {
            logger.error("Could not store file.");
            System.exit(3);
        }
    }
    
    private static void printUseage() {
        System.err.println();
        System.err.println("useage: StoreApp <server> <port> <file> <internet-media-type> <collection> <username> [password]");
        System.err.println("\tserver: The hostname or ip address of the server e.g. localhost.");
        System.err.println("\tport: The tcp port of the server e.g. 8080.");
        System.err.println("\tfile: The path to a file you wish to store in the database.");
        System.err.println("\tinternet-media-type: The Internet Media Type of the file you wish to store in the database.");
        System.err.println("\tcollection: The path to a collection in the database where the file should be stored e.g. /db/mycollection.");
        System.err.println("\tusername: The eXist account to use to connect to the database e.g. admin.");
        System.err.println("\tpassword: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.");
        System.err.println();
    }
}
