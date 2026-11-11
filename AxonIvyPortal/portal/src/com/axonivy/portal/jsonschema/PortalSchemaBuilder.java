package com.axonivy.portal.jsonschema;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.axonivy.portal.jsonschema.definition.PortalCustomStatistic;
import com.axonivy.portal.jsonschema.definition.PortalDashboard;
import com.axonivy.portal.jsonschema.definition.PortalDashboardTemplate;
import com.axonivy.portal.jsonschema.generator.PortalSchemaGenerator;

public class PortalSchemaBuilder {
  private record SchemaDefinition(String fileName, Class<?> rootType) {}

  private static final List<SchemaDefinition> SCHEMAS = List.of(
      new SchemaDefinition("portal-dashboard-schema.json", PortalDashboard.class),
      new SchemaDefinition("portal-statistic-schema.json", PortalCustomStatistic.class),
      new SchemaDefinition("portal-dashboard-template-schema.json", PortalDashboardTemplate.class));

  public static void main(String[] args) throws IOException {
    if (args.length != 3 || args[0].isBlank() || args[1].isBlank() || args[2].isBlank()) {
      throw new IllegalArgumentException(
          "Usage: PortalSchemaBuilder <output-directory> <portal-version> <public-base-url>");
    }
    Path outDir = Path.of(args[0]);
    String portalVersion = args[1];
    URI baseUri = URI.create(args[2].endsWith("/") ? args[2] : args[2] + "/");
    if (!("https".equalsIgnoreCase(baseUri.getScheme()) || "http".equalsIgnoreCase(baseUri.getScheme()))
        || baseUri.getHost() == null || baseUri.getRawQuery() != null || baseUri.getRawFragment() != null) {
      throw new IllegalArgumentException("Public base URL must be an absolute HTTP(S) URL without a query or fragment");
    }
    URI versionUri = baseUri.resolve(portalVersion + "/");
    Path versionDir = outDir.resolve(portalVersion);
    Files.createDirectories(versionDir);
    var generator = new PortalSchemaGenerator();
    for (SchemaDefinition def : SCHEMAS) {
      writeSchema(generator, def, versionDir, versionUri);
    }
  }

  private static void writeSchema(PortalSchemaGenerator generator, SchemaDefinition definition, Path outputDir,
      URI versionUri) throws IOException {
    Path outputFile = outputDir.resolve(definition.fileName());
    var schema = generator.generateSchema(definition.rootType());
    schema.put("$id", versionUri.resolve(definition.fileName()).toString());
    Files.writeString(
        outputFile,
        schema.toPrettyString() + System.lineSeparator(),
        StandardCharsets.UTF_8);

    System.out.println("Generated schema: " + outputFile.toAbsolutePath());
  }
}
