package exist.book.example.xqj.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import net.xqj.exist.ExistXQDataSource;
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
        final String port = args[1];
        
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
        
        //setup the Data Source
        final XQDataSource dataSource = new ExistXQDataSource();
        dataSource.setProperty("serverName", server);
        dataSource.setProperty("port", port);
        
        //get connection with authenticated credentials
        XQConnection connection = null;
        try {
            connection = dataSource.getConnection(username, password);
        
            final String uri = "http://" + server + ":" + port + "/exist/rest" + path;
            logger.info("Starting Query of {}...", uri);

            //execute the query expression
            final XQExpression expression = connection.createExpression();
            final XQResultSequence result;
            if(queryFile == null) {
                result = expression.executeQuery(query);
            } else {
                InputStream is = null;
                try {
                    is = new FileInputStream(queryFile);
                    result = expression.executeQuery(is);
                } finally {
                    if(is != null) {
                        is.close();
                    }
                }
            }
            
            //output the results
            boolean producedResults = false;
            while(result.next()) {
                result.writeItem(System.out, null);    
                producedResults = true;
            }
            if(producedResults) {
                System.out.println();
            } else {
                logger.warn("Your XQuery produced no results!");
            }
            logger.info("Finished Query OK.");
            
        } finally {
            if(connection != null) {
                connection.close();
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
