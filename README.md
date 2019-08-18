# JackBottles
A Minecraft 1.14 Spigot plugin that stores XP in bottles.

## Commands

| Command  | Description |
|---|---|
| /jackbottles | Base command for managing the plugin |
| /jackbottles ? | Shows the help |
| /jackbottles help | Shows the help |
| /jackbottles version | Shows version information and plugin description. Needs the permission node **jackbottles.admin** |
| /jackbottles debug | Toggles debug mode. Needs the permission node **jackbottles.admin** |
| /jackbottles reload | Reloads config and message file. Needs the permission node **jackbottles.admin** |
| /jackbottles save | Save config and message file to disk. Needs the permission node **jackbottles.admin** | 
| /withdrawxp ? | Shows the help. Needs the permission node **jackbottles.withdraw**|
| /withdrawxp &lt;amount&gt; | Create Bottle with given amount. Needs the permission node **jackbottles.withdraw** |
| /withdrawxp all | Create Bottle with all your XP. Needs the permission node **jackbottles.withdraw** |

## Permission nodes

| Permissions | User group |
|---|---|
| jackbottles.admin | admin |
| jackbottles.withdraw | user |
| jackbottles.withdraw | user | 

## Config
```yaml
# Don't touch pls
ConfigVersion: "${project.version}"

# Debug options, don't touch unless needed
#
# DebugLevel: 0 = no debug, 1 = debug, 2 = debug with stack traces
#
DebugLevel: 0
ForcedLanguage: EN

# Minimum amount to store in a bottle
MinimumAmount: 1
```