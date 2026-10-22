package ma.affilatelink.common;

import java.time.LocalDateTime;

public record ApiResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}