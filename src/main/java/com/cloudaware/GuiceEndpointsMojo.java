package com.cloudaware;

import com.google.common.io.Files;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public final class GuiceEndpointsMojo extends AbstractMojo {
    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = true)
    private String outputClassName;

    @Parameter
    private String clientIdWhitelistEnabled;
    @Parameter
    private String urlPattern;

    @Parameter(required = true)
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

            final File tmpOutputDir = Files.createTempDir();
            final File tmpGenerated = new File(tmpOutputDir + "/" + outputSimpleClassName + ".java");
            final FileWriter writer = new FileWriter(tmpGenerated);
            fieldClassTemplate.merge(vc, writer);
            writer.flush();
            writer.close();
            final File generated = new File(outputPackageDir + "/" + outputSimpleClassName + ".java");
            getLog().info("tmpGenerated:" + tmpGenerated.getAbsolutePath());
            getLog().info("generated:" + generated.getAbsolutePath());
            if (!generated.exists() || !Files.equal(tmpGenerated, generated)) {
                getLog().info("copy file");
                Files.copy(tmpGenerated, generated);
            } else {
                getLog().info("Generated file equal previous");
            }
            project.addCompileSourceRoot(outputDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
