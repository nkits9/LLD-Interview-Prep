package patterns.behavioral.chainofresponsibility;

public class AuthFilter extends Filter {
    @Override
    protected void check(Request request) {
        if (request.authToken() == null || request.authToken().isBlank()) {
            throw new RequestRejectedException("auth", "missing token for user " + request.user());
        }
        System.out.println("[auth]  ok for " + request.user());
    }
}
