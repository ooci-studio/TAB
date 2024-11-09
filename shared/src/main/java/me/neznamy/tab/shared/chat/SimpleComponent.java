package me.neznamy.tab.shared.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Simple component with only text using legacy colors and nothing else.
 */
@Getter
@RequiredArgsConstructor
public class SimpleComponent extends TabComponent {

    @NotNull
    private final String text;

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @NotNull
    public String toFlatText() {
        return text;
    }

    @Override
    @NotNull
    public String toRawText() {
        return text;
    }

    @Override
    @NotNull
    protected TextColor fetchLastColor() {
        if (text.isEmpty()) return TextColor.legacy(EnumChatFormat.WHITE);
        String last = EnumChatFormat.getLastColors(text);
        if (!last.isEmpty()) {
            char c = last.toCharArray()[1];
            for (EnumChatFormat e : EnumChatFormat.VALUES) {
                if (e.getCharacter() == c) return TextColor.legacy(e);
            }
        }
        return TextColor.legacy(EnumChatFormat.WHITE);
    }
}
