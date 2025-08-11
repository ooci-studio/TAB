package me.neznamy.chat.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A component of "keybind" type that contains bound key.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class KeybindComponent extends TabComponent {

    @NotNull
    protected final String keybind;

    @Override
    @NotNull
    public String toLegacyText() {
        return keybind;
    }
}
