package org.example;

        import com.fasterxml.jackson.annotation.JsonProperty;
        import io.dropwizard.Configuration;
        import io.dropwizard.jetty.HttpConnectorFactory;
        import io.dropwizard.server.DefaultServerFactory;

        import javax.validation.Valid;
        import javax.validation.constraints.NotNull;
        import java.util.Collections;

public class MediQueryConfiguration extends Configuration {

    @Valid
    @NotNull
    private HttpConnectorFactory httpConnector = new HttpConnectorFactory();

    @JsonProperty("server")
    public DefaultServerFactory getServerFactory() {
        DefaultServerFactory serverFactory = new DefaultServerFactory();
        serverFactory.setApplicationConnectors(Collections.singletonList(httpConnector));
        return serverFactory;
    }

    @JsonProperty("server")
    public void setServerFactory(DefaultServerFactory serverFactory) {
        // This method is necessary for Dropwizard to correctly parse the configuration
    }

    // You can add other configuration properties here
}
