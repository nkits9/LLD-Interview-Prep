package patterns.behavioral.chainofresponsibility;

/** Immutable value passed along the chain. */
public final class Request {
    private final String user;
    private final String authToken;
    private final String payload;

    public Request(String user, String authToken, String payload) {
        this.user = user;
        this.authToken = authToken;
        this.payload = payload;
    }

    public String user() {
        return user;
    }

    public String authToken() {
        return authToken;
    }

    public String payload() {
        return payload;
    }
}
