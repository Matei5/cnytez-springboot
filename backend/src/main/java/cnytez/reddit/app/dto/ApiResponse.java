package cnytez.reddit.app.dto;

public record ApiResponse<T>(
        boolean success,
        T data
) {

}