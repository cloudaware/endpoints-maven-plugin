package com.cloudaware;

import com.google.api.server.spi.tools.EndpointsTool;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public final class EndpointsMojo extends AbstractMojo {

    @Parameter(required = true)
    private File outputSwaggerDoc;

    @Parameter(required = true)
    private File outputDiscoveryDocs;

    @Parameter
    private String hostname;

    @Parameter(required = true)
    private String warPath;

    @Parameter(required = true)
    private String basePath;

    @Parameter(required = true)
    private List<String> apis;

    public void execute() throws MojoExecutionException {
        try {
            Collections.sort(apis);
            final EndpointsTool endpointsTool = new EndpointsTool();
            List<String> args = new ArrayList<String>();
            args.add("get-swagger-doc");
            args.add("--hostname=" + hostname);
            args.add("--war=" + warPath);
            args.add("--output=" + outputSwaggerDoc);
            args.add("--path=" + basePath);
            args.addAll(apis);
            args.add("com.google.api.server.spi.discovery.ProxyingDiscoveryService");
            endpointsTool.execute(args.toArray(new String[args.size()]));

            // https://cloud.google.com/endpoints/docs/openapi/openapi-extensions#x-google-allow
            final String content = IOUtils.toString(new FileInputStream(outputSwaggerDoc), StandardCharsets.UTF_8.name());
            IOUtils.write(content.replaceFirst("\\{", "{\n \"x-google-allow\": \"all\","), new FileOutputStream(outputSwaggerDoc), StandardCharsets.UTF_8.name());

            if (!outputDiscoveryDocs.exists()) {
                outputDiscoveryDocs.mkdirs();
            } else if (!outputDiscoveryDocs.isDirectory()) {
                throw new Exception("Discovery is not folder");
            }

            for (final String path : outputDiscoveryDocs.list()) {
                final File file = new File(outputDiscoveryDocs, path);
                if (file.isFile()) {
                    file.delete();
                }
            }

            args = new ArrayList<String>();
            args.add("get-discovery-doc");
            args.add("--hostname=" + hostname);
            args.add("--war=" + warPath);
            args.add("--path=" + basePath);
            args.add("--output=" + outputDiscoveryDocs);
            args.addAll(apis);
            endpointsTool.execute(args.toArray(new String[args.size()]));
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
