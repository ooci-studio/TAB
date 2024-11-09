enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Guava, Kyori event, bStats, AuthLib, LuckPerms
        maven("https://repo.william278.net/releases/") // VelocityScoreboardAPI
        maven("https://repo.papermc.io/repository/maven-public/") // paperweight, Velocity
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate, Bungeecord-proxy
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.spongepowered.org/repository/maven-public/") // Sponge
        maven("https://jitpack.io") // PremiumVanish, Vault, YamlAssist, RedisBungee
        maven("https://repo.md-5.net/content/groups/public/") // LibsDisguises
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":velocity")
include(":bukkit")
include(":bukkit:paper")
include(":bungeecord")
include(":sponge7")
include(":sponge8")
include(":fabric")
include(":fabric:v1_14_4")
include(":fabric:v1_18_2")
include(":fabric:v1_20_3")
include(":fabric:v1_21_3")
include(":jar")