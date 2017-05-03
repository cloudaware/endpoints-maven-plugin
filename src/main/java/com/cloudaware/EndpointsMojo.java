package com.cloudaware;

import com.google.api.server.spi.tools.EndpointsTool;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal package
 * @phase package
 */
public final class EndpointsMojo extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    private File outputSwaggerDoc;

    /**
     * @parameter
     * @required
     */
    private File outputDiscoveryDocs;

    /**
     * @parameter
     * @required
     */
    private String hostname;

    /**
     * @parameter
     * @required
     */
    private String warPath;

    /**
     * @parameter
     * @required
     */
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
            args.addAll(apis);
            args.add("com.google.api.server.spi.discovery.ProxyingDiscoveryService");
            endpointsTool.execute(args.toArray(new String[args.size()]));
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
            args.add("--output=" + outputDiscoveryDocs);
            args.addAll(apis);
            endpointsTool.execute(args.toArray(new String[args.size()]));

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
