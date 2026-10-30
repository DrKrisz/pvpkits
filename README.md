# PaperKits

A small Java plugin for Paper servers: players can claim named item kits, while
trusted editors create and update kits through an inventory GUI and chat naming.

## Stack and version scope

- Java 21, Maven, Paper/Bukkit API, YAML configuration.
- The POM compiles against **Paper API 1.21.4-R0.1-SNAPSHOT**.
- `plugin.yml` declares API version `1.21`. This is not a tested compatibility matrix;
  check the exact server version before deployment.

## Build and run

Install a JDK 21 and Maven, then run from the repository root:

```bash
mvn -f paper-kits/pom.xml clean verify
```

Stop your test server, copy `paper-kits/target/paper-kits-0.1.0.jar` into its
`plugins/` directory, and start it again. Build from source: generated JARs are
not maintained in this repository. Back up existing plugin configuration before
replacing a running installation.

The plugin generates `starter`, `mid`, and `pro` kits on first startup when
the `kits` configuration section is absent. Kits are saved in
`plugins/PaperKits/config.yml`.

## Commands and permissions

`/kits` is an alias of `/kit`. Commands are player-only.

| Command | Purpose | Required permission |
| --- | --- | --- |
| `/kit list` | List kit names | None |
| `/kit <name>` | Receive a kit | `kits.use` (everyone by default) |
| `/kit create` | Open a new editor | `kits.create` (operators by default) |
| `/kit edit <name>` | Edit an existing kit | `kits.create` (operators by default) |

`kits.admin` defaults to operators but is **reserved and not checked by the
Java code**. It does not bypass `kits.use` or `kits.create`. Grant
`kits.create` explicitly to trusted non-operator editors through your permission
manager. See [Paper's permission defaults](https://docs.papermc.io/paper/dev/plugin-yml/#permissions).

Editing is checked both at the command and during inventory interaction.
A player who loses `kits.create` while editing cannot save through the GUI.
Existing explicit permission grants still apply after updating the plugin.

To create a kit, set its name first, then place items into slots 0–44 and save.
Saving under an existing name overwrites that kit.

## Structure

- `KitsPlugin`: startup, command handling and tab completion.
- `KitManager`: YAML persistence, default kits and item delivery.
- `KitEditorGUI` / `KitEditorSession`: inventory controls and chat naming state.
- `Util`: item, colour and prefix helpers.
- `paper-kits/src/main/resources/`: plugin descriptor and initial configuration.

## Tests and manual check

`mvn -f paper-kits/pom.xml clean verify` runs the unit tests and builds the JAR.
The tests load the packaged permission descriptor and exercise the GUI's
authorization guard with mocked Paper interfaces.

Before using it on a live server, also check on the target Paper version:

1. An ordinary non-operator can list and claim kits but cannot create or edit.
2. An operator or explicitly authorized editor can create and save a kit.
3. Revoke `kits.create` while the editor is open: clicking Save must be denied.
4. Restart the server and confirm the saved kit remains available.

Mocked unit tests do not validate inventory mechanics, chat timing or server
version compatibility.

## Development history

DrKrisz's [initial commit](https://github.com/DrKrisz/pvpkits/commit/4ad6e6a9772364b51bc49044e88b1085d58d0510)
records the Java implementation and build files. The code provides concrete
examples of command handling, event listeners and configuration persistence.

## Current limits

- No claim cooldown, per-kit authorization, cost or deletion command.
- Full inventories cause leftover kit items to drop at the player's location.
- The editor is a prototype: inventory drag/shift-click behaviour and async chat
  session handling need further server-level testing.
- `kits.create` permits editing every kit, not just an editor's own kits.

