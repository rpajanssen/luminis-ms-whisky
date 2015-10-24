package luminis.whisky;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class ApplicationConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private Map<String, Object> defaultHystrixConfig;

//    @JsonProperty("swagger")
//    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    public Map<String, Object> getDefaultHystrixConfig() {
        return defaultHystrixConfig;
    }

//    public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
//        return swaggerBundleConfiguration;
//    }
}
