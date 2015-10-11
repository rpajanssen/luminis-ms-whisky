package luminis.whisky;

import luminis.whisky.health.TemplateHealthCheck;
import luminis.whisky.resources.DemoResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import luminis.whisky.resources.Returns;

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
        // TODO: application initialization
    }

    @Override
    public void run(final ReturnsConfiguration configuration,
                    final Environment environment) {
        registerDemoResource(environment);
        registerReturns(environment);
    }

    private void registerDemoResource(Environment environment) {
        String template = "Hello, %s!";
        environment.jersey().register(new DemoResource(template));
        environment.healthChecks().register("template", new TemplateHealthCheck(template));
    }

    private void registerReturns(Environment environment) {
        environment.jersey().register(new Returns());
    }

}
