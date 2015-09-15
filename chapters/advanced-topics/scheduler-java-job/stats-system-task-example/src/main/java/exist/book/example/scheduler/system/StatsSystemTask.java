package exist.book.example.scheduler.system;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;

import static exist.book.example.scheduler.CommonUtils.storeDocument;

import exist.book.example.scheduler.StoreException;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.QName;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.DBBroker;
import org.exist.storage.SystemTask;
import org.exist.util.Configuration;
import org.exist.xmldb.XmldbURI;
import org.w3c.dom.*;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Example SystemTask which gathers statistics about
 * documents in the database and stores
 * them into the collection defined by the parameter
 * stats-collection.
 *
 * You will need to make sure that the collection indicated
 * by the parameter stats-collection exists and is writable.
 *
 * This job was written with the idea that it would be scheduled
 * for recurring execution, which would effectively build up a
 * collection of database stats over time. The job takes one
 * parameter:
 *
 *   stats-collection
 *   Which is the database collection that the stats data
 *   should be stored into.
 *
 * The following example Scheduler Configuration for $EXIST_HOME/conf.xml
 * would execute the job once every hour:
 *
 *  <job type="system" class="exist.book.example.scheduler.system.StatsSystemTask" name="hourly-stats" cron-trigger="0 0 0/1 * * ?">
 *      <parameter name="stats-collection" value="/db/stats"/>
 *  </job>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class StatsSystemTask implements SystemTask {

    private final static Logger LOG = Logger.getLogger(StatsSystemTask.class);

    private String statsCollection = null;

    @Override
    public String getName() {
        return "StatsSystemTask";
    }

    @Override
    public void configure(final Configuration configuration, final Properties properties) throws EXistException {
        this.statsCollection = properties.getProperty("stats-collection", "/db/stats");
    }

    @Override
    public boolean afterCheckpoint() {
        return false;
    }

    @Override
    public void execute(final DBBroker broker) throws EXistException {

        try {

            //setup an output document
            final MemTreeBuilder builder = new MemTreeBuilder();
            builder.startDocument();

            final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            final XMLGregorianCalendar xmlCal = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());

            final AttributesImpl attribs = new AttributesImpl();
            attribs.addAttribute(null, "timestamp", "timestamp", "string", xmlCal.toXMLFormat());
            builder.startElement(new QName("stats"), attribs);

            //collect stats
            collectStats(broker, XmldbURI.DB, builder);

            //finalise output document
            builder.endElement();
            builder.endDocument();

            //store stats document
            storeDocument(broker, statsCollection, builder.getDocument(), false);

        } catch(final DatatypeConfigurationException dce) {
            final String msg = "Unable to configure XML DatatypeFactory: " + dce.getMessage();
            LOG.error(msg, dce);
            throw new EXistException(msg, dce);
        } catch(final PermissionDeniedException pde) {
            LOG.error(pde);
            throw new EXistException(pde);
        } catch(final StoreException se) {
            LOG.error(se);
            throw new EXistException(se);
        }
    }

    /**
     * Collects statistics about a Collection and its documents and sub-collections recursively
     * writes the results to the builder
     *
     * @param broker The database broker to use to access the database
     * @param collUri The absolute database URI of the collection to generate statistics for
     * @param builder The builder to write the statistics output to
     */
    private void collectStats(final DBBroker broker, final XmldbURI collUri, final MemTreeBuilder builder) throws PermissionDeniedException {

        final Collection collection = broker.getCollection(collUri);

        final AttributesImpl attribs = new AttributesImpl();
        attribs.addAttribute(null, "name", "name", "string", collection.getURI().lastSegment().toString());
        attribs.addAttribute(null, "uri", "uri", "string", collection.getURI().toString());
        attribs.addAttribute(null, "sub-collections", "sub-collections", "string", Integer.toString(collection.getChildCollectionCount(broker)));
        attribs.addAttribute(null, "documents", "documents", "string", Integer.toString(collection.getDocumentCountNoLock(broker)));

        builder.startElement(new QName("collection"), attribs);

        final Iterator<DocumentImpl> itDocs = collection.iteratorNoLock(broker);
        while(itDocs.hasNext()) {
            final DocumentImpl doc = itDocs.next();

            final AttributesImpl docAttribs = new AttributesImpl();
            docAttribs.addAttribute(null, "name", "name", "string", doc.getURI().lastSegment().toString());
            docAttribs.addAttribute(null, "uri", "uri", "string", doc.getURI().toString());
            docAttribs.addAttribute(null, "nodes", "nodes", "string", Long.toString(countNodes(doc)));

            builder.startElement(new QName("document"), docAttribs);
            builder.endElement();
        }

        final Iterator<XmldbURI> itColls = collection.collectionIteratorNoLock(broker);
        while(itColls.hasNext()) {
            final XmldbURI childCollUri = collUri.append(itColls.next());
            collectStats(broker, childCollUri, builder);
        }

        builder.endElement();
    }

    /**
     * Counts descendant nodes (includes self)
     *
     * @param node The node to count down from
     *
     * @return The number of nodes
     */
    private long countNodes(final Node node) {
        return countNodes(node, 0);
    }

    /**
     * Counts descendant nodes recursively (including self)
     *
     * @param node The node to count down from
     * @param count The accumulated count
     *
     * @return The number of nodes
     */
    private long countNodes(final Node node, long count) {
        //add self
        count++;

        final NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if(child instanceof Element) {  //descend if element!
                count += countNodes(child);
            } else {
                count++;
            }
        }
        return count;
    }
}
