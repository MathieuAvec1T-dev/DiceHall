package dicehall.api.Security.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String stringUsedToSearch) {
        super("User not found: " + stringUsedToSearch);
    }
    public UserNotFoundException(int id) {
        super("User not found: " + id);
    }
}
