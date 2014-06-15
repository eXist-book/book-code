package exist.book.example.trigger.collection;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.triggers.CollectionTrigger;
import org.exist.collections.triggers.TriggerException;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;

import java.util.List;
import java.util.Map;

/**
 * Example CollectionTrigger which
 * demonstrates the implementation
 * of a very simple trigger
 *
 * The following example Trigger configuration for
 * '/db' placed in '/db/system/config/db/collection.xconf' would correctly
 * setup this trigger to produce log events when Collections
 * are created.
 *
 *  <trigger class="exist.book.example.trigger.collection.SimplestTrigger"/>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class SimplestTrigger implements CollectionTrigger {

    private final static Logger LOG = Logger.getLogger(SimplestTrigger.class);

    @Override
    public void beforeCreateCollection(final DBBroker broker, final Txn txn, final XmldbURI uri) throws TriggerException {
        LOG.info("User '" + broker.getSubject().getName() + "' is creating the Collection '" + uri + "'...");
    }

    //<editor-fold desc="other empty function implementations here!">
    @Override
    public void afterCreateCollection(final DBBroker broker, final Txn txn, final Collection collection) throws TriggerException {
    }

    @Override
    public void beforeCopyCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI uri) throws TriggerException {
    }

    @Override
    public void afterCopyCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI uri) throws TriggerException {
    }

    @Override
    public void beforeMoveCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI uri) throws TriggerException {
    }

    @Override
    public void afterMoveCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI uri) throws TriggerException {
    }

    @Override
    public void beforeDeleteCollection(final DBBroker broker, final Txn txn, final Collection collection) throws TriggerException {
    }

    @Override
    public void afterDeleteCollection(final DBBroker broker, final Txn txn, final XmldbURI uri) throws TriggerException {
    }

    @Override
    public void configure(final DBBroker broker, final Collection collection, final Map<String, List<? extends Object>> parameters) throws TriggerException {
    }

    //<editor-fold desc="deprecated, so we can ignore">
    @Override
    public void prepare(final int event, final DBBroker broker, final Txn txn, final Collection collection, final Collection newCollection) throws TriggerException {
        //ignore
    }

    @Override
    public void finish(final int event, final DBBroker broker, final Txn txn, final Collection collection, final Collection newCollection) {
        //ignore
    }
    //</editor-fold>

    //</editor-fold>
}