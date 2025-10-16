#pvpkits

paper 1.21.9 compatible kits plugin with in game kit creator.

## build
- install java 21
- run: `mvn -q -e -U clean package`
- copy `target/paper-kits-0.1.0.jar` to your server `plugins/` folder

## commands
- `/kit list` list available kits
- `/kit <name>` give yourself that kit
- `/kit create` open the kit editor gui
- `/kit edit <name>` open the kit editor for an existing kit

## permissions
- `kits.use` use /kit and receive kits
- `kits.create` create or edit kits
- `kits.admin` bypass checks
