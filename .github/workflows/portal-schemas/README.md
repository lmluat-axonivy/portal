# Portal schema Maven package

This standalone build bundles the generated Portal JSON schemas into a ZIP for GitHub Packages. It does not change the Portal application's Nexus deployment or run schema generation itself.

## Artifact

- Coordinates: `com.axonivy.portal:portal-schemas:14.0.2-SNAPSHOT:zip`
- Classifier: none
- Registry: `https://maven.pkg.github.com/lmluat-axonivy/portal`

The ZIP contains these files at its root:

```text
portal-dashboard-schema.json
portal-custom-statistic-schema.json
portal-dashboard-template-schema.json
```

The schemas retain the generator's public `$id` and `$ref` URLs. This package is not an offline-reference variant; referenced schemas must remain available at their public URLs.

## Generate and package

Run from the repository root with the Java and Maven versions required by Portal:

```sh
mvn -B -ntp -f AxonIvyPortal/portal/pom.xml -Pjson.schema clean process-classes
mvn -B -ntp -f .github/workflows/portal-schemas/pom.xml clean package
```

Schema generation requires Portal's dependencies, including the matching `portal-components` artifact, to be available through the configured Maven repositories or local repository.

The packaging version must match the Portal version. When changing the Portal version, update this build's version too. By default, it reads schemas from:

```text
AxonIvyPortal/portal/target/schemas/<package-version>/
```

Override the input directory with `-Dschemas.directory=/absolute/path/to/schemas` when needed. Always use freshly generated output matching the package version.

Output:

```text
.github/workflows/portal-schemas/target/portal-schemas-14.0.2-SNAPSHOT.zip
```

The build checks that all three input files exist. JSON Schema validation and reference checks are separate checks; this build does not perform them yet.

## Publish

Configure a Maven server with ID `github` in `~/.m2/settings.xml`. Supply credentials through environment variables; never commit a token:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>${env.GITHUB_ACTOR}</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```

For local publishing, use a personal access token (classic) with `write:packages` and the necessary repository access. A GitHub Actions publishing workflow can use its `GITHUB_TOKEN` with `packages: write` for this repository.

After generation and validation, publish with:

```sh
mvn -B -ntp -f .github/workflows/portal-schemas/pom.xml clean deploy
```

This publishes the ZIP and POM to GitHub Packages. Snapshot deployments use Maven's timestamped snapshot naming. Release versions should be treated as immutable.

Downloading from GitHub's Maven registry also requires authentication, even for public packages. GitHub Pages remains the public HTTP host for editor `$schema` URLs.
