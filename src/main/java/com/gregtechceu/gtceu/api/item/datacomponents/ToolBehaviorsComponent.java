package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.utils.StreamCodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public record ToolBehaviorsComponent(Map<ToolBehaviorType<?>, IToolBehavior<?>> behaviors) {

    public static final ToolBehaviorsComponent EMPTY = new ToolBehaviorsComponent(Map.of());

    public static final Codec<ToolBehaviorsComponent> CODEC = Codec.dispatchedMap(GTRegistries.TOOL_BEHAVIORS.codec(), type -> (Codec<IToolBehavior<?>>) type.getCodec())
            .xmap(ToolBehaviorsComponent::new, ToolBehaviorsComponent::behaviors);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolBehaviorsComponent> STREAM_CODEC = StreamCodecUtils.dispatchMap(
                    size -> (Map<ToolBehaviorType<?>, IToolBehavior<?>>) new HashMap<ToolBehaviorType<?>, IToolBehavior<?>>(size),
                    GTRegistries.TOOL_BEHAVIORS.streamCodec(),
                    type -> (StreamCodec<? super RegistryFriendlyByteBuf, IToolBehavior<?>>) type.getStreamCodec())
            .map(ToolBehaviorsComponent::new, ToolBehaviorsComponent::behaviors);

    public ToolBehaviorsComponent(List<IToolBehavior<?>> behaviors) {
        this(behaviors.stream().collect(Collectors.toMap(IToolBehavior::getType, Function.identity())));
    }

    public boolean hasBehavior(ToolBehaviorType<?> type) {
        return behaviors.containsKey(type);
    }

    public <T extends IToolBehavior<T>> T getBehavior(ToolBehaviorType<T> type) {
        return (T) this.behaviors.get(type);
    }

    public ToolBehaviorsComponent withBehavior(IToolBehavior<?> behavior) {
        Map<ToolBehaviorType<?>, IToolBehavior<?>> behaviors = new HashMap<>(this.behaviors);
        behaviors.put(behavior.getType(), behavior);
        return new ToolBehaviorsComponent(behaviors);
    }
}
