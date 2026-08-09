package cnytez.reddit.app.dto.response;

public record ApiResponse<T>(
        boolean success,
        T data
) {

}