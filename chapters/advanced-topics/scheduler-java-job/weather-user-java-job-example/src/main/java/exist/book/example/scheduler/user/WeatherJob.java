package exist.book.example.scheduler.user;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static exist.book.example.scheduler.CommonUtils.storeDocument;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import exist.book.example.scheduler.StoreException;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.Namespaces;
import org.exist.dom.QName;
import org.exist.dom.memtree.DocumentImpl;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.dom.memtree.SAXAdapter;
import org.exist.scheduler.JobException;
import org.exist.scheduler.UserJavaJob;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Example UserJavaJob which retrieves 
 * the current Weather from a public Web Service
 * located at http://www.webservicex.net/globalweather.asmx/GetWeather
 * parse the result and stores it into the database.
 *
 * You will need to make sure that the collection indicated
 * by the parameter weather-collection exists and is writable.
 *
 *
 * This job was written with the idea that it would be scheduled
 * for recurring execution, which would effectively build up a
 * collection of historic weather data. The job takes three
 * parameters:
 *
 *   city
 *   Which determine for which city the weather is retrieved.
 *
 *   country
 *   Which identifies the country of the city.
 *
 *   weather-collection
 *   Which is the database collection that the weather data
 *   should be stored into.
 *
 * The following example Scheduler Configuration for $EXIST_HOME/conf.xml
 * would execute the job once every hour:
 *
 *  <job type="user" class="exist.book.example.scheduler.user.WeatherJob" name="hourly-weather" cron-trigger="0 0 0/1 * * ?">
 *      <parameter name="city" value="Exeter"/>
 *      <parameter name="country" value="United Kingdom"/>
 *      <parameter name="weather-collection" value="/db/weather"/>
 *  </job>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class WeatherJob extends UserJavaJob {

    private final static Logger LOG = Logger.getLogger(WeatherJob.class);
    private final static String WEBSERVICE = "http://www.webservicex.net/globalweather.asmx/GetWeather";

    private String name = null;

    @Override
    public String getName() {
        if(this.name == null) {
            this.name = "Weather Job";
        }

        return this.name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void execute(final BrokerPool brokerPool, final Map<String, ?> parameters) throws JobException {

        //get the parameters for the task
        final String city = (String)parameters.get("city");
        final String country = (String)parameters.get("country");
        final String weatherCollection = (String)parameters.get("weather-collection");
        if(city == null || country == null || weatherCollection == null) {
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, "Aborting job and de-scheduling, all required parameters (city, country, weather-collection) were not provided!");
        }


        //the task itself
        DBBroker broker = null;
        try {

            final String uri = WEBSERVICE + "?CityName=" + URLEncoder.encode(city, "UTF-8") + "&CountryName=" + URLEncoder.encode(country, "UTF-8");

            LOG.debug("Calling Weather web-service: " + uri);

            //setup a webservice client
            final Client client = Client.create();
            final WebResource resource = client.resource(new URI(uri));

            //make the webservice call
            final ClientResponse response = resource.get(ClientResponse.class);

            //process the result of the webservice call
            final ClientResponse.Status status = response.getClientResponseStatus();

            //get a broker for accessing the database
            broker = brokerPool.getBroker();

            if(status == ClientResponse.Status.OK) {
                //OK
                final DocumentImpl weatherDoc = parseWeatherResponse(response);
                storeDocument(broker, weatherCollection, weatherDoc, true);
                LOG.debug("Successfully retrieved weather from web-service.");
            } else {
                //FAIL
                final org.exist.dom.memtree.DocumentImpl failureDoc = createFailureDoc(response);
                storeDocument(broker, weatherCollection, failureDoc, true);

                final String msg = "Weather web-service request failed: " + status.toString();
                LOG.error(msg);
                throw new JobException(JobException.JobExceptionAction.JOB_ABORT, "Weather web-service request failed: " + msg);
            }
        } catch(final URISyntaxException use) {
            final String msg = "Invalid URI: " + use.getMessage();
            LOG.error(msg, use);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, msg);
        } catch(final UnsupportedEncodingException uee) {
            final String msg = "Unsupported Encoding: " + uee.getMessage();
            LOG.error(msg, uee);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, msg);
        } catch(final EXistException ee) {
            LOG.error(ee.getMessage(), ee);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT, ee.getMessage());
        } catch(final StoreException se) {
            LOG.error(se.getMessage(), se);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT, se.getMessage());
        } finally {
            //return the broker for accessing the database
            if(broker != null) {
                brokerPool.release(broker);
            }
        }
    }

    /**
     * Parses the response from the weather web-service
     *
     * @param response The response from the weather web-service call
     *
     * @return A document representing the result of the weather web-service
     */
    private final DocumentImpl parseWeatherResponse(final ClientResponse response) throws JobException {
        try {

            //this is horrible, it tidies up the nasty output from the webservice, but for a simple example it is fine
            final String body = response.getEntity(String.class)
                    .replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<string xmlns=\"http://www.webserviceX.NET\">", "")
                    .replace("</string>", "")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");

            //parse the cleaned up XML from the webservice response
            final Reader reader = new StringReader(body);
            final SAXAdapter adapter = new SAXAdapter();
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final InputSource src = new InputSource(reader);

            final SAXParser parser = factory.newSAXParser();
            final XMLReader xr = parser.getXMLReader();
            xr.setContentHandler(adapter);
            xr.setProperty(Namespaces.SAX_LEXICAL_HANDLER, adapter);
            xr.parse(src);

            return (DocumentImpl)adapter.getDocument();

        } catch (final ParserConfigurationException pce) {
            final String msg = "Error while constructing XML parser: " + pce.getMessage();
            LOG.error(msg, pce);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, msg);
        } catch (final SAXException saxe) {
            final String msg = "Error while parsing XML: " + saxe.getMessage();
            LOG.error(msg, saxe);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT, msg);
        } catch(final IOException ioe) {
            final String msg = ioe.getMessage();
            LOG.error(msg, ioe);
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT, msg);
        }
    }

    /**
     * Creates a failure document based on the failure result from the weather web-service
     *
     * @param response The response from the weather web-service call
     *
     * @return An in-memory document representing the failure
     */
    private final org.exist.dom.memtree.DocumentImpl createFailureDoc(final ClientResponse response) throws JobException {
        try {
            final MemTreeBuilder builder = new MemTreeBuilder();
            builder.startDocument();

            final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            final XMLGregorianCalendar xmlCal = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());

            final AttributesImpl attribs = new AttributesImpl();
            final ClientResponse.Status status = response.getClientResponseStatus();
            attribs.addAttribute(null, "at", "at", "string", xmlCal.toXMLFormat());
            attribs.addAttribute(null, "http-status", "http-status", "string", String.valueOf(status.getStatusCode()));
            attribs.addAttribute(null, "http-reason", "http-status", "string", String.valueOf(status.getReasonPhrase()));

            builder.startElement(new QName("failed"), attribs);
            builder.characters(response.getEntity(String.class));
            builder.endElement();
            builder.endDocument();

            return builder.getDocument();
        } catch(final DatatypeConfigurationException dce) {
            final String msg = "Unable to configure XML DatatypeFactory: " + dce.getMessage();
            throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, msg);
        }
    }
}
