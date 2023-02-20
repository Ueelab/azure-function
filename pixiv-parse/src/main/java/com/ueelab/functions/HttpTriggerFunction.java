package com.ueelab.functions;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerFunction {
    
    private static final Pattern ID_PATTERN = Pattern.compile("\\d{8,9}");
    
    @FunctionName("PixivParse")
    public HttpResponseMessage run(@HttpTrigger(route = "/",
            name = "pixiv",
            methods = {HttpMethod.GET},
            authLevel = AuthorizationLevel.ANONYMOUS)
                                   HttpRequestMessage<Optional<String>> request) {
        final String path = request.getUri().getPath();
        String target = "https://pixiv.net";
        Matcher matcher = ID_PATTERN.matcher(path);
        if (matcher.find()) {
            target += "/i/" + matcher.group();
        }
        return request.createResponseBuilder(HttpStatus.SEE_OTHER).header("location", target).build();
    }
}
