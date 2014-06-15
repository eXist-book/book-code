package exist.book.example.scheduler;

import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.scheduler.JobException;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;
import org.w3c.dom.Node;

import java.util.UUID;

/**
 * Common utilities used by the scheduled java
 * examples
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class CommonUtils {

    /**
     * Stores a document into the weather collection
     *
     * @param broker The broker for accessing the database
     * @param targetCollection The collection to store the document into
     * @param document The document to store
     * @param lockCollection Should the collection be locked whilst storing the document?
     */
    public static void storeDocument(final DBBroker broker, final String targetCollection, final Node document, final boolean lockCollection) throws StoreException {

        Collection collection = null;
        final TransactionManager transact = broker.getBrokerPool().getTransactionManager();
        final Txn txn = transact.beginTransaction();
        final IndexInfo indexInfo;
        try {

            //prepare to store
            try {

                //open the weather collection
                collection = broker.openCollection(XmldbURI.create(targetCollection), lockCollection ? Lock.WRITE_LOCK : Lock.NO_LOCK);
                if(collection == null) {
                    transact.abort(txn);
                    throw new JobException(JobException.JobExceptionAction.JOB_ABORT_THIS, "Collection: " + targetCollection + " not found!");
                }

                //generate a unique name for the document
                final String documentName = UUID.randomUUID().toString() + ".xml";

                //validate the document
                indexInfo = collection.validateXMLResource(txn, broker, XmldbURI.create(documentName), document);
            } finally {
                //release the lock on the weather collection
                if(collection != null && lockCollection) {
                    collection.release(Lock.WRITE_LOCK);
                }
            }

            //store
            collection.store(txn, broker, indexInfo, document, false);
            transact.commit(txn);

        } catch(final Exception e) {
            transact.abort(txn);
            throw new StoreException(e);
        }
    }
}
