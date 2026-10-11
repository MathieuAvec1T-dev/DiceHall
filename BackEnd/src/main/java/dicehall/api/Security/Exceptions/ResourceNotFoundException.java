package dicehall.api.Security.Exceptions;

public class ResourceNotFoundException extends RuntimeException {
    /**
     *
     * @param resource Name of the Resource that was not found.
     * @param searchValue Value that was used to search the resource.
     * @param message Message you can add after the initial error.
     */
    public ResourceNotFoundException(String resource, String searchValue,  String message) {
        super(resource + " was not found with value: " + searchValue + "." + "\n " + message);
    }

    /**
     *
     * @param resource Name of the Resource that was not found.
     * @param searchValue Value that was used to search the resource.
     */
    public ResourceNotFoundException(String resource, String searchValue) {
        super(resource + " was not found with value: " + searchValue + ".");
    }
}
