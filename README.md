# CombatLogBan
A fabric mod made for (temp) banning people for leaving midfight. Automatically messages both players when they should not log off, and when they can log off.
Made for 1.18.1 Fabric servers. (Feel free to copy the source code and port it to another version!)

## How to use
- Download the mod from releases
- Put it into your server mods/ folder
- Start the server
- Profit
- (Optional) Set custom combat timeout and ban length - [Read more](#Config)

Now when someone attacks someone, they both have to wait 7 seconds (or custom time in config) after last hit to log off, otherwise they will get banned for 1 day (can be also modified in config).

## Download
Go to [releases](https://github.com/SansSerif1/OriginEnforcer/releases) and download the latest jar file.

## Config
The mod automatically generates a config file on first server start (SERVERFOLDER/config/combatlogban.properties). It is empty by default, but you can customize some options listed below. The syntax is "key=value" per line.

| key        | = | value | description (DO NOT PASTE TO THE CONFIG FILE)                                                  |
|------------|---|-------|------------------------------------------------------------------------------------------------|
| hitTimeout | = | 7     | x seconds the player should wait after last attack activity. Defaults to 7 seconds.            |
| banMinutes | = | 1440  | x minutes the player gets banned for after leaving midfight. Defaults to 1 day (1440 minutes). |

## Why
Because I tried to find some mod like this for my server, but all of them were for bukkit based servers, and I personally do not want to use the crappy Cardboard, since it is not compatible with the Origins Mod.
