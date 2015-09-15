package exist.book.example.trigger.collection;

import org.apache.log4j.Logger;
import org.exist.collections.Collection;
import org.exist.collections.triggers.CollectionTrigger;
import org.exist.collections.triggers.TriggerException;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example CollectionTrigger which when given
 * a blacklist of collection URIs, prevents
 * those collections from being deleted (and
 * optionally from being moved).
 *
 * The NoDeleteCollectionTrigger takes two parameter:
 *
 *  blacklist - A list of collection URIs to protect
 *
 *  treatMoveAsDelete (optional) - The value true/false indicating whether to
 *      also protect the collections from being moved.
 *
 * The following example Trigger configuration for
 * '/db' placed in '/db/system/config/db/collection.xconf' would correctly
 * setup this trigger to protect the collections '/db/super-secret'
 * and '/db/private' being deleted or moved.
 *
 *  <trigger class="exist.book.example.trigger.collection.NoDeleteCollectionTrigger">
 *      <parameter name="treatMoveAsDelete" value="true"/>
 *
 *      <parameter name="blacklist" value="/db/super-secret"/>
 *      <parameter name="blacklist" value="/db/private"/>
 *  </trigger>
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class NoDeleteCollectionTrigger implements CollectionTrigger {

    private final static Logger LOG = Logger.getLogger(NoDeleteCollectionTrigger.class);
    private final static String BLACKLIST_PARAM = "blacklist";
    private final static String PROTECT_MOVE_PARAM = "treatMoveAsDelete";

    /**
     * Holds a blacklist of Collection URI
     * which should not be deleted
     */
    private final Set<XmldbURI> blacklist = new HashSet<XmldbURI>();

    /**
     * Indicates whether we should also
     * protect collections from Move operations
     * in the same way as we do Delete operations
     */
    private boolean protectMove = false;


    @Override
    public void configure(final DBBroker broker, final Collection collection, final Map<String, List<? extends Object>> parameters) throws TriggerException {

        //extract the black list parameter value from the parameters
        final List<String> lst = (List<String>)parameters.get(BLACKLIST_PARAM);
        if(lst == null) {
            throw new TriggerException("The parameter '" + blacklist + "' has not been provided in the collection configuration of '" + collection.getURI() + "' for '" + getClass().getName() + "'.");
        }

        for(final String blacklisted : lst) {
            try {
                this.blacklist.add(XmldbURI.create(blacklisted));
            } catch(final IllegalArgumentException iae) {
                LOG.warn("Skipping... collection uri '" + blacklisted + "' provided in the black list is invalid: " + iae.getMessage());
            }
        }

        //extract the optional protectMode parameter value from the parameters
        final List<String> protect = (List<String>)parameters.get(PROTECT_MOVE_PARAM);
        if(protect != null && protect.size() == 1) {
            this.protectMove = Boolean.parseBoolean(protect.get(0));
        }
    }

    //<editor-fold desc="Delete Collection">
    @Override
    public void beforeDeleteCollection(final DBBroker broker, final Txn txn, final Collection collection) throws TriggerException {
        /**
         * If the collection is on the blacklist,
         * we throw a TriggerException to abort
         * the operation which is attempting to delete
         * it
         */
        if(blacklist.contains(collection.getURI())) {
            LOG.info("Preventing deletion of blacklisted collection '" + collection.getURI() + "'.");
            throw new TriggerException("The collection '" + collection.getURI().lastSegment() + "' is black-listed by the NoDeleteCollectionTrigger and may not be deleted!");
        }
    }

    @Override
    public void afterDeleteCollection(final DBBroker broker, final Txn txn, final XmldbURI uri) throws TriggerException {
        /**
         * Do nothing! The beforeDeleteCollection
         * protects what we need
         */
    }
    //</editor-fold>

    //<editor-fold desc="Move Collection">
    @Override
    public void beforeMoveCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI newUri) throws TriggerException {
        /**
         * If we are protecting moves (as a form
         * of delete) and the collection is on
         * the blacklist, we throw a TriggerException
         * to abort the operation which is attempting
         * to move the collection.
         *
         * After all a Move would remove the
         * original location of the collection!
         */
        if(protectMove && blacklist.contains(collection.getURI())) {
            LOG.info("Preventing move of blacklisted collection '" + collection.getURI() + "'.");
            throw new TriggerException("The collection '" + collection.getURI().lastSegment() + "' is black-listed by the NoDeleteCollectionTrigger and may not be moved, consider a copy instead!");
        }
    }

    @Override
    public void afterMoveCollection(final DBBroker broker, final Txn txn, final Collection collection, XmldbURI oldUri) throws TriggerException {
        /**
         * Do nothing! The beforeMoveCollection
         * protects what we need
         */
    }
    //</editor-fold>

    //<editor-fold desc="Create Collection">
    @Override
    public void beforeCreateCollection(final DBBroker broker, final Txn txn, final XmldbURI uri) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterCreateCollection(final DBBroker broker, final Txn txn, final Collection collection) throws TriggerException {
        //do nothing
    }
    //</editor-fold>

    //<editor-fold desc="Copy Collection">
    @Override
    public void beforeCopyCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI newUri) throws TriggerException {
        //do nothing
    }

    @Override
    public void afterCopyCollection(final DBBroker broker, final Txn txn, final Collection collection, final XmldbURI oldUri) throws TriggerException {
        //do nothing
    }
    //</editor-fold>
}
