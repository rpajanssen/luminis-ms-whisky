package luminis.whisky;

import luminis.whisky.health.TemplateHealthCheck;
import luminis.whisky.resources.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import luminis.whisky.cli.ConsulDeployer;
import luminis.whisky.resources.stubs.BillingStubResource;
import luminis.whisky.resources.stubs.ShippingStubResource;
import luminis.whisky.util.BoxFuseEnvironment;

import javax.annotation.PreDestroy;

public class ReturnsApplication extends Application<ReturnsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new ReturnsApplication().run(args);
    }

    @Override
    public String getName() {
        return "WhiskyReturns";
    }

    @Override
    public void initialize(final Bootstrap<ReturnsConfiguration> bootstrap) {
        ConsulDeployer.deployAndRun();
    }

    @PreDestroy
    public void cleanup() {
        // todo
    }

    @Override
    public void run(final ReturnsConfiguration configuration,
                    final Environment environment) {
        registerDemoResource(environment);
        registerConsulResource(environment);
        registerReturns(environment);

        optionallyRegisterStubs(environment);

        registerExceptionHandlers(environment);
    }

    private void registerDemoResource(Environment environment) {
        String template = "Yoh, %s!";
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
        environment.jersey().register(new IllegalStateExceptionHandler());
        environment.jersey().register(new RuntimeExceptionHandler());
        environment.jersey().register(new ExceptionHandler());
    }
}
