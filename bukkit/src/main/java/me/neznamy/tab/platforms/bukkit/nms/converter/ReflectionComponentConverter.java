package me.neznamy.tab.platforms.bukkit.nms.converter;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.*;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Class for converting TAB component into NMS components (1.7+).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ReflectionComponentConverter extends ComponentConverter {

    private final FunctionWithException<String, Object> newTextComponent;
    private final FunctionWithException<String, Object> newTranslatableComponent;
    private final FunctionWithException<String, Object> newKeybindComponent;
    private final BiFunction<ChatModifier, Boolean, Object> convertModifier;

    private final Class<?> ChatModifier = BukkitReflection.getClass("network.chat.Style", "network.chat.ChatModifier", "ChatModifier");
    private final Class<Enum> EnumChatFormat = (Class<Enum>) BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
    private final Constructor<?> newChatModifier;
    private final Method ChatBaseComponent_addSibling;
    private final Field Component_modifier;
    private final List<Field> magicCodes = ReflectionUtils.getFields(ChatModifier, Boolean.class);

    // 1.15-
    private Method ChatModifier_setColor;

    // 1.16+
    private Method ChatHexColor_fromRGB;
    private Method ResourceLocation_tryParse;

    /**
     * Constructs new instance and loads all NMS classes, constructors and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public ReflectionComponentConverter() throws ReflectiveOperationException {
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        if (BukkitReflection.getMinorVersion() >= 19) {
            Method IChatBaseComponent_b = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"b", "literal"}, String.class);
            newTextComponent = text -> IChatBaseComponent_b.invoke(null, text);

            Method IChatBaseComponent_c = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"c", "translatable"}, String.class);
            newTranslatableComponent = text -> IChatBaseComponent_c.invoke(null, text);

            Method IChatBaseComponent_d = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"d", "keybind"}, String.class);
            newKeybindComponent = text -> IChatBaseComponent_d.invoke(null, text);

            Class<?> IChatMutableComponent = BukkitReflection.getClass("network.chat.MutableComponent", "network.chat.IChatMutableComponent", "IChatMutableComponent");
            Component_modifier = ReflectionUtils.getOnlyField(IChatMutableComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(IChatMutableComponent, IChatMutableComponent, IChatBaseComponent);
        } else {
            Class<?> ChatComponentText = BukkitReflection.getClass("network.chat.TextComponent", "network.chat.ChatComponentText", "ChatComponentText");
            Constructor<?> newChatComponentText = ChatComponentText.getConstructor(String.class);
            newTextComponent = newChatComponentText::newInstance;

            Class<?> ChatMessage = BukkitReflection.getClass("network.chat.TranslatableComponent", "network.chat.ChatMessage", "ChatMessage");
            Constructor<?> newChatMessage = ChatMessage.getConstructor(String.class, Object[].class);
            newTranslatableComponent = text -> newChatMessage.newInstance(text, new Object[0]);

            newKeybindComponent = text -> {
                throw new UnsupportedOperationException("Keybind component conversion is not implemented");
            };

            Class<?> ChatBaseComponent = BukkitReflection.getClass("network.chat.BaseComponent", "network.chat.ChatBaseComponent", "ChatBaseComponent");
            Component_modifier = ReflectionUtils.getOnlyField(ChatBaseComponent, ChatModifier);
            ChatBaseComponent_addSibling = ReflectionUtils.getOnlyMethod(ChatBaseComponent, IChatBaseComponent, IChatBaseComponent);
        }
        if (BukkitReflection.getMinorVersion() >= 16) {
            Class<?> chatHexColor = BukkitReflection.getClass("network.chat.TextColor", "network.chat.ChatHexColor", "ChatHexColor");
            Class<?> ResourceLocation = BukkitReflection.getClass("resources.ResourceLocation", "resources.MinecraftKey", "MinecraftKey");
            Class<?> chatClickable = BukkitReflection.getClass("network.chat.ClickEvent", "network.chat.ChatClickable", "ChatClickable");
            Class<?> chatHoverable = BukkitReflection.getClass("network.chat.HoverEvent", "network.chat.ChatHoverable", "ChatHoverable");
            ResourceLocation_tryParse = ReflectionUtils.getMethod(ResourceLocation, new String[]{"tryParse", "m_135820_", "a"}, String.class);
            ChatHexColor_fromRGB = ReflectionUtils.getMethods(chatHexColor, chatHexColor, int.class).get(0); // There should only be 1, but some mods add more
            if (BukkitReflection.is1_21_4Plus()) {
                // 1.21.4+
                newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Integer.class, Boolean.class, Boolean.class,
                        Boolean.class, Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            } else {
                // 1.21.3-
                newChatModifier = ReflectionUtils.setAccessible(ChatModifier.getDeclaredConstructor(chatHexColor, Boolean.class, Boolean.class, Boolean.class,
                        Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            }
            convertModifier = this::createModifierModern;
        } else {
            newChatModifier = ChatModifier.getConstructor();
            ChatModifier_setColor = ReflectionUtils.getOnlyMethod(ChatModifier, ChatModifier, EnumChatFormat);
            convertModifier = (modifier, protocolVersion) -> createModifierLegacy(modifier);
        }
    }

    @Override
    @SneakyThrows
    @NotNull
    public Object convert(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent) {
            return newTextComponent.apply(((SimpleComponent) component).getText());
        } else if (component instanceof StructuredComponent) {
            StructuredComponent component1 = (StructuredComponent) component;
            Object nmsComponent = newTextComponent.apply(component1.getText());
            Component_modifier.set(nmsComponent, convertModifier.apply(component1.getModifier(), modern));
            for (StructuredComponent extra : component1.getExtra()) {
                ChatBaseComponent_addSibling.invoke(nmsComponent, convert(extra, modern));
            }
            return nmsComponent;
        } else {
            return fromAdventure(((AdventureComponent)component).getComponent());
        }
    }

    @SneakyThrows
    @NotNull
    private Object fromAdventure(@NotNull Component component) {
        Object nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = newTextComponent.apply(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = newTranslatableComponent.apply(((TranslatableComponent)component).key());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = newKeybindComponent.apply(((KeybindComponent)component).keybind());
        } else {
            throw new IllegalStateException("Cannot convert " + component.getClass().getName());
        }

        net.kyori.adventure.text.format.TextColor color = component.color();
        Key font = component.style().font();
        Component_modifier.set(nmsComponent, newStyleModern(
                color == null ? null : ChatHexColor_fromRGB.invoke(null, color.value()),
                component.style().hasDecoration(TextDecoration.BOLD),
                component.style().hasDecoration(TextDecoration.ITALIC),
                component.style().hasDecoration(TextDecoration.UNDERLINED),
                component.style().hasDecoration(TextDecoration.STRIKETHROUGH),
                component.style().hasDecoration(TextDecoration.OBFUSCATED),
                font == null ? null : font.asString()
        ));
        for (Component extra : component.children()) {
            ChatBaseComponent_addSibling.invoke(nmsComponent, fromAdventure(extra));
        }
        return nmsComponent;
    }

    @SneakyThrows
    private Object createModifierModern(@NotNull ChatModifier modifier, boolean modern) {
        Object color = null;
        if (modifier.getColor() != null) {
            if (modern) {
                color = ChatHexColor_fromRGB.invoke(null, modifier.getColor().getRgb());
            } else {
                color = ChatHexColor_fromRGB.invoke(null, modifier.getColor().getLegacyColor().getRgb());
            }
        }
        return newStyleModern(
                color,
                modifier.isBold(),
                modifier.isItalic(),
                modifier.isUnderlined(),
                modifier.isStrikethrough(),
                modifier.isObfuscated(),
                modifier.getFont());
    }

    @SneakyThrows
    private Object createModifierLegacy(@NotNull ChatModifier modifier) {
        Object nmsModifier = newChatModifier.newInstance();
        if (modifier.getColor() != null) {
            ChatModifier_setColor.invoke(nmsModifier, Enum.valueOf(EnumChatFormat, modifier.getColor().getLegacyColor().name()));
        }
        if (modifier.isBold()) magicCodes.get(0).set(nmsModifier, true);
        if (modifier.isItalic()) magicCodes.get(1).set(nmsModifier, true);
        if (modifier.isStrikethrough()) magicCodes.get(2).set(nmsModifier, true);
        if (modifier.isUnderlined()) magicCodes.get(3).set(nmsModifier, true);
        if (modifier.isObfuscated()) magicCodes.get(4).set(nmsModifier, true);
        return nmsModifier;
    }

    @SneakyThrows
    @NotNull
    private Object newStyleModern(@Nullable Object color, boolean bold, boolean italic, boolean underlined,
                           boolean strikethrough, boolean obfuscated, @Nullable String font) {
        if (BukkitReflection.is1_21_4Plus()) {
            return newChatModifier.newInstance(
                    color,
                    null,
                    bold,
                    italic,
                    underlined,
                    strikethrough,
                    obfuscated,
                    null,
                    null,
                    null,
                    font == null ? null : ResourceLocation_tryParse.invoke(null, font)
            );
        } else {
            return newChatModifier.newInstance(
                    color,
                    bold,
                    italic,
                    underlined,
                    strikethrough,
                    obfuscated,
                    null,
                    null,
                    null,
                    font == null ? null : ResourceLocation_tryParse.invoke(null, font)
            );
        }
    }
}
