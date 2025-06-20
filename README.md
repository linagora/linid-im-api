# dm-api

LinID Directory Manager API (dm-api) provides a plugin-based, dynamic-entity REST service with external configuration,
plugins, and translations.

## Configuration

The application reads settings from Spring properties, typically via environment variables or a configuration file (
`application.properties` or `application.yaml`). Key properties:

* `configuration.path=${CONFIGURATION_PATH}`
  Path to the YAML configuration file defining entities, providers, routes, tasks, etc.
  Example:

  ```properties
  configuration.path=/home/config/config.yaml
  ```

  You can set the environment variable before launch:

  ```bash
  export CONFIGURATION_PATH=/home/config/config.yaml
  ```

* `plugin.loader.path=${PLUGIN_LOADER_PATH}`
  Directory where plugin JARs are located. The service will scan this folder at startup (and possibly at runtime).
  Example:

  ```properties
  plugin.loader.path=/home/plugins
  ```

  Environment variable:

  ```bash
  export PLUGIN_LOADER_PATH=/home/plugins
  ```

* `i18n.external.path=${I18N_EXTERNAL_PATH}`
  Directory containing external i18n JSON files (e.g., `en.json`, `fr.json`). These translations are merged according to
  the merge order.
  Example:

  ```properties
  i18n.external.path=/home/i18n
  ```

  Environment variable:

  ```bash
  export I18N_EXTERNAL_PATH=/home/i18n
  ```

* `i18n.merge.order=${I18N_MERGE_ORDER}`
  Comma-separated list defining merge precedence for translations. Default: `plugin,external,internal`.
  Example:

  ```properties
  i18n.merge.order=plugin,external,internal
  ```

  Environment variable:

  ```bash
  export I18N_MERGE_ORDER="plugin,external,internal"
  ```
* `authorization.accept.allow.all=${AUTHORIZATION_ACCEPT_ALLOW_ALL:false}`
  Boolean flag to accept using the plugin that authorizes all requests without restrictions.
  Default: `false`
  Example:

  ```properties
  authorization.accept.allow.all=true
  ```

  Environment variable:

  ```bash
  export AUTHORIZATION_ACCEPT_ALLOW_ALL=true
  ```

* `copyright.mode=${COPYRIGHT_MODE:default}`
  Defines the copyright mode. Possible values:

    * `default`: applies default copyright enforcement.
    * `custom`: applies a custom copyright message.
    * `none`: disables copyright enforcement.
      Default: `default`
      Example:

  ```properties
  copyright.mode=custom
  ```

  Environment variable:

  ```bash
  export COPYRIGHT_MODE=custom
  ```

* `copyright.custom=${COPYRIGHT_CUSTOM:Your copyright here}`
  Custom copyright message to apply when `copyright.mode=custom`.
  Example:

  ```properties
  copyright.custom=Your copyright here
  ```

  Environment variable:

  ```bash
  export COPYRIGHT_CUSTOM="Your copyright here"
  ```

---

In a Spring Boot `application.properties` or `application.yaml`, you might have:

```properties
configuration.path=/home/config/config.yaml
plugin.loader.path=/home/plugins
i18n.external.path=/home/i18n
i18n.merge.order=plugin,external,internal
authorization.accept.allow.all=false
copyright.mode=default
copyright.custom=Your copyright here
```

## Building

The project provides different environments under the `docker` folder, each with its own `docker-compose.yml` and `.env`
file.

* `docker/dev/`
* `docker/integration/`
* `docker/production/`

You can build and run the project using either Maven or Docker.

### 1. Maven

From the project root (where `pom.xml` resides), simply run:

```bash
mvn clean package
```

This produces an executable JAR under `target/`, e.g. `dm-api-<version>.jar`.

To run:

```bash
export CONFIGURATION_PATH=/home/config/config.yaml
export PLUGIN_LOADER_PATH=/home/plugins
export I18N_EXTERNAL_PATH=/home/i18n
export I18N_MERGE_ORDER="plugin,external,internal"

java -jar target/dm-api-<version>.jar
```

### 2. Docker

A Docker image can encapsulate the built JAR. Expect a Dockerfile that copies the JAR and sets entrypoint to
`java -jar ...`. To run with external config, plugins, translations, mount host directories as volumes.

#### Build Docker image

```bash
docker build -f docker/Dockerfile -t dm-api .
```

Start the desired environment:

```bash
docker compose -f docker/prod/docker-compose.yml --env-file docker/prod/.env up
```

## 📂 Environments

The project defines three environments:

* `dev`
* `integration`
* `production`

All related configurations are located in the `docker/` folder.

📌 Each environment (dev, integration, production) has its own `.env` file inside the `docker/<env>` folder. These files
are loaded automatically when using Docker Compose for that environment.

🚨 **For production deployment**, make sure to properly configure the `docker/production/.env` file with the correct
values before running the application.
