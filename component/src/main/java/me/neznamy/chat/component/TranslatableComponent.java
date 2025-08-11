package me.neznamy.chat.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A component of "translate" type that contains key to translate.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TranslatableComponent extends TabComponent {

    @NotNull
    protected final String key;

    @Override
    @NotNull
    public String toLegacyText() {
        return key;
    }
}
