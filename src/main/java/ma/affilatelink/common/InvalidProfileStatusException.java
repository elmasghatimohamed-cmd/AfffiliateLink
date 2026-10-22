
package ma.affilatelink.common;

public class InvalidProfileStatusException
        extends RuntimeException {

    public InvalidProfileStatusException(
            String message) {
        super(message);
    }
}
