package exist.book.example.trigger.document;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.triggers.FilteringTrigger;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.DocumentImpl;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;


/**
 * Example FilteringTrigger which does two things:
 *
 * 1) when given a list of paths to drop removes
 * elements at those paths during the store phase
 * but does not effect the document during the
 * validation phase.
 *
 * 2) when given a list of elements to rename
 * renames those elements in both the validation
 * and store phases.
 *
 * The ExampleFilteringTrigger takes two parameter:
 *
 *  drop - A list of element paths within the document
 *      to drop during storage
 *
 *  elements - A map of element names to rename during
 *      validation and storage
 *
 * The following example Trigger configuration for
 * '/db/test-data' placed in '/db/system/config/db/test-data/collection.xconf'
 * would correctly setup this trigger to drop any elements
 * with the path '/a/b/c' and rename elements named 'd' to 'e'
 * and elements named 'e' to 'd'.
 *
 * <trigger class="exist.book.example.trigger.document.ExampleFilteringTrigger">
 *      <parameter name="drop" value="/a/b/c"/>
 *      <parameter name="elements">
 *          <rename from="d" to="e"/>
 *          <rename from="e" to="d"/>
 *      </parameter>
 * </trigger>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class ExampleFilteringTrigger extends FilteringTrigger {

    private final static Logger LOG = Logger.getLogger(ExampleFilteringTrigger.class);
    private final static String DROP_PARAM = "drop";
    private final static String ELEMENTS_PARAM = "elements";
    private final static String RENAME_PARAM = "rename";
    private final static String FROM_PARAM = "from";
    private final static String TO_PARAM = "to";

    final Set<Path> dropPaths = new HashSet<Path>();
    final Map<String, String> renameElements = new HashMap<String, String>();

    //the current element path in the document stream
    final Path current = new Path();

    @Override
    public void configure(final DBBroker broker, final Collection collection, final Map<String, List<? extends Object>> parameters) throws TriggerException {

        //make sure to call configure on the super class
        super.configure(broker, collection, parameters);

        //extract the drop parameter values from the parameters
        final List<String> drops = (List<String>)parameters.get(DROP_PARAM);
        if(drops != null) {
            for(final String drop : drops) {
                dropPaths.add(new Path(drop));
            }
        }

        //extract the rename elements parameter values from the parameters
        //this is a good example of more complicated parameter passing and extraction
        final List<Map<String, List<Properties>>> renames = (List<Map<String, List<Properties>>>)parameters.get(ELEMENTS_PARAM);
        if(renames != null && !renames.isEmpty()) {
            for(final Map<String, List<Properties>> rename : renames) {
                final List<Properties> lstProps = (List<Properties>)rename.get(RENAME_PARAM);
                if(lstProps != null) {
                    for(final Properties props : lstProps) {
                        renameElements.put(
                                props.getProperty(FROM_PARAM),
                                props.getProperty(TO_PARAM)
                        );
                    }
                }
            }
        }
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, final String qname, final Attributes attributes) throws SAXException {
        current.push(localName);

        if(!isValidating() && dropPaths.contains(current)) {
            //drop the startElement event
            LOG.info("Dropping startElement for path: " + current.toString());

        } else if(renameElements.containsKey(localName)) {
            //rename the element
            final String newName = renameElements.get(localName);
            LOG.info("Renaming startElement from '" + localName + "' to '" + newName + "'");

            //forward the startElement event but for a renamed element
            super.startElement(namespaceURI, newName, qname.replace(localName, newName), attributes);

        } else {
            //forward the startElement event
            super.startElement(namespaceURI, localName, qname, attributes);
        }
    }

    @Override
    public void endElement(final String namespaceURI, final String localName, final String qname) throws SAXException {
        try {
            if(!isValidating() && dropPaths.contains(current)) {
                //drop the endElement event
                LOG.info("Dropping endElement for path: " + current.toString());

            } else if(renameElements.containsKey(localName)) {
                //rename the element
                final String newName = renameElements.get(localName);
                LOG.info("Renaming endElement from '" + localName + "' to '" + newName + "'");

                //forward the endElement event, but for a renamed element
                super.endElement(namespaceURI, newName, qname.replace(localName, newName));

            } else {
                //forward the endElement event
                super.endElement(namespaceURI, localName, qname);
            }
        } finally {
            current.pop();
        }
    }


    /**
     * Simple stack based implementation
     * of a Path.
     *
     * Does not cope with namespaces!
     */
    private class Path {

        private Stack<String> segments = new Stack<String>();

        public Path() {
        }

        public Path(final String path) {
            for(final String segment : path.substring(1).split("/")) {
                segments.push(segment);
            }
        }

        public void push(final String segment) {
            segments.push(segment);
        }

        public String pop() {
            return segments.pop();
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            final Iterator<String> itSegments = segments.iterator();
            while(itSegments.hasNext()) {
                final String segment = itSegments.next();

                builder.append("/");
                builder.append(segment);
            }
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return segments.hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            if(other != null && other instanceof Path) {
                return ((Path)other).segments.equals(segments);
            }
            return false;
        }
    }


    //<editor-fold desc="ignored before/after events, only interested in SAX events">

    //<editor-fold desc="Delete Document">
    @Override
    public void beforeDeleteDocument(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterDeleteDocument(final DBBroker broker, final Txn txn, final XmldbURI documentUri) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Move Document">
    @Override
    public void beforeMoveDocument(final DBBroker broker, final Txn txn, final DocumentImpl document, final XmldbURI newUri) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterMoveDocument(final DBBroker broker, final Txn txn, final DocumentImpl document, XmldbURI oldUri) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Update Document">
    @Override
    public void beforeUpdateDocument(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterUpdateDocument(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Update Document Metadata">
    @Override
    public void beforeUpdateDocumentMetadata(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterUpdateDocumentMetadata(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Create Document">
    @Override
    public void beforeCreateDocument(final DBBroker broker, final Txn txn, final XmldbURI uri) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterCreateDocument(final DBBroker broker, final Txn txn, final DocumentImpl document) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Copy Document">
    @Override
    public void beforeCopyDocument(final DBBroker broker, final Txn txn, final DocumentImpl document, final XmldbURI newUri) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterCopyDocument(final DBBroker broker, final Txn txn, final DocumentImpl document, final XmldbURI oldUri) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="deprecated, so we can ignore">
    @Override
    public void prepare(final int event, final DBBroker broker, final Txn txn, final XmldbURI documentPath, final DocumentImpl existingDocument) throws TriggerException {
        //ignore
    }

    @Override
    public void finish(final int event, final DBBroker broker, final Txn txn, final XmldbURI documentPath, final DocumentImpl document) {
        //ignore
    }
    //</editor-fold>

    //</editor-fold>
}
