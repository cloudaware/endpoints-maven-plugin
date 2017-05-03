# About

This plugin has 2 goals:

* `gen-docs` will generate discovery documents and openapi.json from your API classes 
* `gen-guice` will generate EndpointsModule for guice using the same set of classes 

# Usage

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.cloudaware</groupId>
                <artifactId>endpoints-maven-plugin</artifactId>
                <version>1.0.1</version>
                <configuration>
                    <apis>
                        <param>com.company.project.SomeApi</param>
                        <param>com.company.project.OtherApi</param>
                    </apis>
                </configuration>
                <executions>
                    <execution>
                        <id>gen-docs</id>
                        <goals>
                            <goal>package</goal>
                        </goals>
                        <configuration>
                            <hostname>yourapi.endpoints.yorproject.cloud.goog</hostname>
                            <warPath>${project.build.directory}/${project.build.finalName}</warPath>
                            <outputSwaggerDoc>${project.basedir}/swagger.json</outputSwaggerDoc>
                            <outputDiscoveryDocs>${project.basedir}/discovery</outputDiscoveryDocs>
                        </configuration>
                    </execution>
                    <execution>
                        <id>gen-guice</id>
                        <goals>
                            <goal>generate-sources</goal>
                        </goals>
                        <configuration>
                            <outputClassName>com.company.project.YourEndpointsModule</outputClassName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

# Changelog

* 1.0.1 - Less dependencies
* 1.0.0 - Initial release