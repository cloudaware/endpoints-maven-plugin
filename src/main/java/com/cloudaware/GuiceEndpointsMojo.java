package com.cloudaware;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * @goal generate-sources
 * @phase generate-sources
 */
public final class GuiceEndpointsMojo extends AbstractMojo {
    /**
     * @parameter property="project"
     * @required
     */
    private MavenProject project;

    /**
     * @parameter
     * @required
     */
    private String outputClassName;

    /**
     * @parameter
     */
    private String clientIdWhitelistEnabled;
    /**
     * @parameter
     */
    private String urlPattern;

    /**
     * @parameter
     * @required
     */
    private List<String> apis;

    public void execute() throws MojoExecutionException {
        try {
            final String outputDirectory = project.getBuild().getDirectory() + "/generated-sources/api-guice";
            final String outputPackageName = outputClassName.substring(0, outputClassName.lastIndexOf("."));
            final String outputSimpleClassName = outputClassName.substring(outputClassName.lastIndexOf(".") + 1);
            final File outputPackageDir = new File(outputDirectory, outputPackageName.replaceAll("\\.", File.separator));
            if (!outputPackageDir.exists()) {
                outputPackageDir.mkdirs();
            }
            final VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            ve.init();
            final VelocityContext vc = new VelocityContext();
            vc.put("outputPackageName", outputPackageName);
            vc.put("clientIdWhitelistEnabled", clientIdWhitelistEnabled == null ? "true" : clientIdWhitelistEnabled);
            vc.put("urlPattern", urlPattern == null ? "/_ah/api/*" : urlPattern);
            vc.put("outputSimpleClassName", outputSimpleClassName);
            vc.put("apis", apis);
            final Template fieldClassTemplate = ve.getTemplate("com/cloudaware/GuiceSssModule.java.vm", "utf-8");
            final FileWriter writer = new FileWriter(outputPackageDir + "/" + outputSimpleClassName + ".java");
            fieldClassTemplate.merge(vc, writer);
            writer.flush();
            writer.close();
            project.addCompileSourceRoot(outputPackageDir.getPath());
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
