package com.loan.loanapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;*/

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint {

    /*private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized",
                "Missing or invalid authentication token.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws IOException {
        writeError(response, HttpServletResponse.SC_FORBIDDEN, "forbidden",
                "You do not have permission to perform this action.");
    }

    private void writeError(HttpServletResponse response, int status, String error, String description)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDto body = ErrorResponseDto.builder()
                .error(error)
                .errorDescription(description)
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }*/
}
