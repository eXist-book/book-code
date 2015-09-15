package exist.book.example.startuptrigger;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.dom.QName;
import org.exist.dom.memtree.DocumentImpl;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.storage.DBBroker;
import org.exist.storage.StartupTrigger;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.Configuration;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.InternalModule;
import org.exist.xquery.XQueryContext;
import org.xml.sax.helpers.AttributesImpl;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Example StartupTrigger which summarises the
 * available XQuery Extension Modules (written in Java)
 * into a document in the database.
 *
 * The StartupTask takes one parameter:
 *
 *  target - the URI path to store a document in the database containing the modules summary
 *
 * You must ensure that any collections in the target URI have already been created!
 *
 * The following example Startup Triggers configuration for $EXIST_HOME/conf.xml
 * would correctly execute this trigger during database startup:
 *
 *  <trigger class="exist.book.example.startuptrigger.ConfiguredModulesStartupTrigger">
 *      <parameter name="target" value="/db/modules-summary.xml"/>
 *  </trigger>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class ConfiguredModulesStartupTrigger implements StartupTrigger {

    private final static Logger LOG = Logger.getLogger(ConfiguredModulesStartupTrigger.class);

    @Override
    public void execute(final DBBroker sysBroker, final Map<String, List<? extends Object>> params) {

        //get the target parameter for where to store the modules summary
        final List<? extends Object> maybeTarget = params.get("target");
        if(maybeTarget == null || maybeTarget.size() != 1) {
            LOG.error("Missing 'target' parameter which provides the database uri for storing the modules summary!");
            return;

        } else {
            final String target = (String)maybeTarget.get(0);

            //start a document
            final MemTreeBuilder builder = new MemTreeBuilder();
            builder.startDocument();
            builder.startElement(new QName("modules-summary"), null);

            //get java modules info into document
            final Configuration existConf = sysBroker.getConfiguration();
            buildModulesSummary(existConf, builder);

            //finish document
            builder.endElement();
            builder.endDocument();

            //store document
            storeDocument(sysBroker, XmldbURI.create(target), builder.getDocument());
        }
    }

    /**
     * Finds the available java modules and writes a summary of each to the builder
     *
     * @param conf The eXist Configuration
     * @param builder The builder for writing out the module summaries
     */
    private void buildModulesSummary(final Configuration conf, final MemTreeBuilder builder) {
        /**
         * The configuredModules map has the format:
         *  key: module uri (i.e. namespace)
         *  value: implementing Java Class
         */
        final Map<String, Class<?>> configuredModules = (Map<String, Class<?>>)conf.getProperty(XQueryContext.PROPERTY_BUILT_IN_MODULES);

        for(final Map.Entry<String, Class<?>> configuredModule : configuredModules.entrySet()) {
            buildModuleSummary(configuredModule, builder);
        }
    }

    /**
     * Writes a summary of a module to the builder
     *
     * @param configuredModule The configured module. key is the URI, value is the implementing class.
     * @param builder The builder for writing out the summary
     */
    private void buildModuleSummary(final  Map.Entry<String, Class<?>> configuredModule, final MemTreeBuilder builder) {

        //instantiate an instance of the module
        final Class<InternalModule> moduleClass = (Class<InternalModule>)configuredModule.getValue();
        final Constructor ctr = moduleClass.getConstructors()[0];
        final Object[] ctrParams = new Object[ctr.getParameterTypes().length];

        AttributesImpl attribs;
        String description = null;
        try {
            final InternalModule module = (InternalModule)ctr.newInstance(ctrParams);
            attribs = new AttributesImpl();
            attribs.addAttribute(null, "prefix", "prefix", "string", module.getDefaultPrefix());
            attribs.addAttribute(null, "uri", "uri", "string", module.getNamespaceURI());
            attribs.addAttribute(null, "class", "class", "string", moduleClass.getName());
            attribs.addAttribute(null, "released-in", "released-in", "string", module.getReleaseVersion());

            description = module.getDescription();

        } catch(final Exception e) {
            LOG.error("Unable to build module summary for: " + moduleClass.getName(), e);

            attribs = new AttributesImpl();
            attribs.addAttribute(null, "uri", "uri", "string", configuredModule.getKey());
            attribs.addAttribute(null, "class", "class", "string", moduleClass.getName());
        }

        builder.startElement(new QName("module"), attribs);
        if(description != null) {
            builder.characters(description);
        }
        builder.endElement();
    }

    /**
     * Stores a document into the database
     *
     * Does not need to use locking, as we have exclusive access
     * inside a Startup Task!
     *
     * @param broker The broker for accessing the database
     * @param target The uri of where the document is to be stored in the database
     * @param document The document to store
     */
    public void storeDocument(final DBBroker broker, final XmldbURI target, final DocumentImpl document) {

        final TransactionManager transact = broker.getBrokerPool().getTransactionManager();
        final Txn txn = transact.beginTransaction();
        final IndexInfo indexInfo;
        try {
            //open the collection
            final XmldbURI targetCollection = target.removeLastSegment();
            final Collection collection = broker.openCollection(targetCollection, Lock.NO_LOCK);

            if(collection == null) {
                //no such collection
                transact.abort(txn);
                LOG.error("Collection: " + targetCollection.toString() + " not found!");
                return;

            } else {

                //get the name for the document
                final XmldbURI documentName = target.lastSegment();

                //validate the document
                indexInfo = collection.validateXMLResource(txn, broker, documentName, document);

                //store
                collection.store(txn, broker, indexInfo, document, false);
                transact.commit(txn);
            }
        } catch(final Exception e) {
            transact.abort(txn);
            LOG.error(e);
        }
    }
}
