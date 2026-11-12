package com.axonivy.portal.schema.helper;

import java.net.URI;
import java.util.List;

import com.axonivy.portal.dto.definition.PortalDefinition;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public final class PortalSchemaHelper {

  private static final String REF = "$ref";

  private PortalSchemaHelper() {
  }

  /**
   * Rewrites each relative {@code $ref} in the schema to the published URL of the given portal version.
   * Empty references, absolute URLs and local references ({@code #/...}) are left unchanged.
   */
  public static void resolveRemoteReferences(ObjectNode schema, String portalVersion) {
    if (portalVersion == null || portalVersion.isBlank()) {
      throw new IllegalArgumentException("Portal version must not be blank");
    }
    resolveRemoteReferences(schema, URI.create(PortalDefinition.SCHEMA_BASE_URL + portalVersion + "/"));
  }

  public static void resolveRemoteReferences(ObjectNode schema, URI versionUri) {
    resolveNode(schema, versionUri);
  }

  private static void resolveNode(JsonNode node, URI versionUri) {
    if (node instanceof ObjectNode object) {
      // copy the names first, so the object can be updated while iterating
      for (String name : List.copyOf(object.propertyNames())) {
        JsonNode child = object.get(name);
        if (REF.equals(name) && child.isString() && isRemoteReference(child.asString())) {
          object.put(REF, versionUri.resolve(child.asString()).toString());
        } else {
          resolveNode(child, versionUri);
        }
      }
    } else if (node != null && node.isArray()) {
      for (JsonNode element : node) {
        resolveNode(element, versionUri);
      }
    }
  }

  private static boolean isRemoteReference(String ref) {
    return !ref.isBlank() && !ref.contains("://") && !ref.startsWith("#");
  }

}
