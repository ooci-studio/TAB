package me.neznamy.tab.shared.config.helper;

import lombok.NonNull;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.types.SortingType;

import java.io.File;
import java.util.Set;

/**
 * Class for printing startup warns when features are
 * clearly not configured properly when loading them.
 */
public class StartupWarnPrinter {

    /** Amount of logged warns on startup */
    private int warnCount;

    /**
     * Sends a console warn that entered skin definition does not match
     * any of the supported patterns.
     *
     * @param   definition
     *          Configured skin definition
     */
    public void invalidLayoutSkinDefinition(@NonNull String definition) {
        startupWarn("Invalid skin definition: \"" + definition + "\". Supported patterns are:",
                "#1 - \"player:<name>\" for skin of player with specified name",
                "#2 - \"mineskin:<id>\" for UUID of chosen skin from mineskin.org",
                "#3 - \"texture:<texture>\" for raw texture string");
    }

    public void invalidSortingTypeElement(@NonNull String element, @NonNull Set<String> validTypes) {
        startupWarn("\"" + element + "\" is not a valid sorting type element. Valid options are: " + validTypes + ".");
    }

    public void invalidSortingPlaceholder(@NonNull String placeholder, @NonNull SortingType type) {
        startupWarn("\"" + placeholder + "\" is not a valid placeholder for " + type.getClass().getSimpleName() + " sorting type");
    }

    public void invalidConditionPattern(@NonNull String conditionName, @NonNull String line) {
        startupWarn("Line \"" + line + "\" in condition " + conditionName + " is not a valid condition pattern.");
    }

    public void incompleteSortingLine(@NonNull String configuredLine) {
        startupWarn("Sorting line \"" + configuredLine + "\" is incomplete.");
    }

    /**
     * Sends a startup warn message into console
     *
     * @param   messages
     *          messages to print into console
     */
    public void startupWarn(@NonNull String... messages) {
        warnCount++;
        for (String message : messages) {
            TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text(message));
        }
    }

    public void startupWarn(@NonNull File file, @NonNull String message) {
        warnCount++;
        TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text("[" + file.getName() + "] " + message));
    }

    /**
     * Prints amount of warns logged into console previously, if more than 0.
     */
    public void printWarnCount() {
        if (warnCount == 0) return;
        TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text("Found a total of " + warnCount + " issues."));
        // Reset after printing to prevent count going up on each reload
        warnCount = 0;
    }
}
