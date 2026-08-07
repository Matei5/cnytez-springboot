package cnytez.reddit.app.response;

public record ApiResponse<T>(
        boolean success,
        T data
) {

}