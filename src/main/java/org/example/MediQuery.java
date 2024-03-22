package org.example;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.example.resources.MediQueryResource;

public class MediQuery extends Application<MediQueryConfiguration> {

    public static void main(String[] args) throws Exception {
        new MediQuery().run(args);
    }

    @Override
    public void run(MediQueryConfiguration configuration, Environment environment) {
        // Register your resource classes here
        environment.jersey().register(new MediQueryResource());
    }
}
