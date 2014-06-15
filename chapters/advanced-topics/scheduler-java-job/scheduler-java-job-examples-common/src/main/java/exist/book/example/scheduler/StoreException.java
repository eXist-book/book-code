package exist.book.example.scheduler;

/**
 * Exception may be raised whilst storing a document
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class StoreException extends Exception {
    public StoreException(final Throwable cause) {
        super(cause);
    }
}
