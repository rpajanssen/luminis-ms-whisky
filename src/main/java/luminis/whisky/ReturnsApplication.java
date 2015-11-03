package luminis.whisky;

import com.netflix.config.ConfigurationManager;
import io.federecio.dropwizard.swagger.SwaggerDropwizard;
import luminis.whisky.client.ConsulClient;
import luminis.whisky.core.consul.ConsulServiceUrlFinder;
import luminis.whisky.health.TemplateHealthCheck;
import luminis.whisky.resources.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import luminis.whisky.cli.ConsulDeployer;
import luminis.whisky.resources.handlers.*;
import luminis.whisky.resources.stubs.BillingStubResource;
import luminis.whisky.resources.stubs.ShippingStubResource;
import luminis.whisky.util.RuntimeEnvironment;
import luminis.whisky.util.Metrics;
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
    public void cleanup() { }

    @Override
    public void run(final ApplicationConfiguration configuration, final Environment environment) {
        ConfigurationManager.install(new MapConfiguration(configuration.getDefaultHystrixConfig()));

        configureCORS(environment);

        registerDemoResource(environment);
        registerConsulResource(environment);
        registerReturns(environment);

        optionallyRegisterStubs(environment);

        registerExceptionHandlers(environment);

        deploySwagger(configuration, environment);
    }

    private void configureCORS(Environment environment) {
        final FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }

    private void registerDemoResource(Environment environment) {
        String template = "Hi, %s!";
        environment.jersey().register(new DemoResource(template));
        environment.healthChecks().register("template", new TemplateHealthCheck(template));
    }

    private void registerConsulResource(Environment environment) {
        environment.jersey().register(new ConsulFacadeResource(new ConsulClient()));
    }

    private void optionallyRegisterStubs(Environment environment) {
        if(RuntimeEnvironment.isDevOrTest() && !RuntimeEnvironment.withoutStubs()) {
            environment.jersey().register(new ShippingStubResource());
            environment.jersey().register(new BillingStubResource());
        }
    }

    private void registerReturns(Environment environment) {
        ConsulServiceUrlFinder consulServiceUrlFinder = new ConsulServiceUrlFinder();
        environment.jersey().register(new ReturnsResource(consulServiceUrlFinder, new Metrics(consulServiceUrlFinder)));
        environment.jersey().register(new ReturnsWithObservableAndFanOutResource(consulServiceUrlFinder, new Metrics(consulServiceUrlFinder)));
        environment.jersey().register(new ReturnsWithObservableResource(consulServiceUrlFinder, new Metrics(consulServiceUrlFinder)));
        environment.jersey().register(new ReturnsWithFuturesAndFanOutResource(consulServiceUrlFinder, new Metrics(consulServiceUrlFinder)));
    }

    private void registerExceptionHandlers(Environment environment) {
        environment.jersey().register(new DyingServiceExceptionHandler());
        environment.jersey().register(new UnavailableServiceExceptionHandler());
        environment.jersey().register(new ServiceResultExceptionHandler());
        environment.jersey().register(new UnableToCancelExceptionExceptionHandler());
        environment.jersey().register(new UncheckedExecutionExceptionHandler());
        environment.jersey().register(new InterruptedExceptionHandler());
        environment.jersey().register(new IllegalStateExceptionHandler());
        environment.jersey().register(new RuntimeExceptionHandler());
        environment.jersey().register(new ExceptionHandler());
    }

    private void deploySwagger(ApplicationConfiguration configuration, Environment environment) {
        if(RuntimeEnvironment.isDevOrTest()) {
            if(RuntimeEnvironment.isRunningOnAWS()) {
                System.out.println("running swagger for dev/test on AWS");
                swaggerDropwizard.onRun(
                        configuration, environment,
                        String.format("%s-%s-%s.boxfuse.io", RuntimeEnvironment.getApp(), RuntimeEnvironment.getEnv().toLowerCase(), RuntimeEnvironment.getAccount()),
                        RuntimeEnvironment.getHttpPort()
                );
            } else {
                System.out.println("running swagger for local dev/test deploy");
                swaggerDropwizard.onRun(configuration, environment, "localhost", RuntimeEnvironment.getForwardedHttpPort());
            }
        }
    }
}
