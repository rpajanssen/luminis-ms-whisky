package luminis.whisky;

import com.netflix.config.ConfigurationManager;
import io.federecio.dropwizard.swagger.SwaggerDropwizard;
import luminis.whisky.health.TemplateHealthCheck;
import luminis.whisky.resources.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import luminis.whisky.cli.ConsulDeployer;
import luminis.whisky.resources.handlers.*;
import luminis.whisky.resources.stubs.BillingStubResource;
import luminis.whisky.resources.stubs.ShippingStubResource;
import luminis.whisky.util.BoxFuseEnvironment;
import org.apache.commons.configuration.MapConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.annotation.PreDestroy;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class ReturnsApplication extends Application<ApplicationConfiguration> {
    private final SwaggerDropwizard<ApplicationConfiguration> swaggerDropwizard = new SwaggerDropwizard<>();

    public static void main(final String[] args) throws Exception {
        new ReturnsApplication().run(args);
    }

    @Override
    public String getName() {
        return "WhiskyReturns";
    }

    @Override
    public void initialize(final Bootstrap<ApplicationConfiguration> bootstrap) {
        ConsulDeployer.deployAndRun();

        swaggerDropwizard.onInitialize(bootstrap);
    }

    @PreDestroy
    public void cleanup() {
        // todo
    }

    @Override
    public void run(final ApplicationConfiguration configuration,
                    final Environment environment) {
        ConfigurationManager.install(new MapConfiguration(configuration.getDefaultHystrixConfig()));

        configureCORS(environment);

        registerDemoResource(environment);
        registerConsulResource(environment);
        registerReturns(environment);

        optionallyRegisterStubs(environment);

        registerExceptionHandlers(environment);

        // todo : configure instead of hardcoding
        if(BoxFuseEnvironment.isDevOrTest()) {
            swaggerDropwizard.onRun(configuration, environment, "localhost", BoxFuseEnvironment.getForwardedHttpPort());
        } else {
            swaggerDropwizard.onRun(configuration, environment, "whiskyreturns-rpajanssen.boxfuse.io", BoxFuseEnvironment.getHttpPort());
        }
    }

    // todo : delete this demo health check?
    private void registerDemoResource(Environment environment) {
        String template = "Hi, %s!";
        environment.jersey().register(new DemoResource(template));
        environment.healthChecks().register("template", new TemplateHealthCheck(template));
    }

    private void registerConsulResource(Environment environment) {
        environment.jersey().register(new ConsulFacadeResource());
    }

    private void optionallyRegisterStubs(Environment environment) {
        if(BoxFuseEnvironment.isDevOrTest()) {
            environment.jersey().register(new ShippingStubResource());
            environment.jersey().register(new BillingStubResource());
        }
    }

    private void registerReturns(Environment environment) {
        environment.jersey().register(new ReturnsResource());
    }

    private void registerExceptionHandlers(Environment environment) {
        environment.jersey().register(new DyingServiceExceptionHandler());
        environment.jersey().register(new InterruptedExceptionHandler());
        environment.jersey().register(new IllegalStateExceptionHandler());
        environment.jersey().register(new RuntimeExceptionHandler());
        environment.jersey().register(new ExceptionHandler());
    }

    private void configureCORS(Environment environment) {
        final FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS",
                CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }
}
