package com.gregtechceu.gtceu.data.pack;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class GTPackSource implements RepositorySource {

    private final String name;
    private final PackType type;
    private final Pack.Position position;
    private final Function<String, PackResources> resources;

    @Override
    public void loadPacks(Consumer<Pack> infoConsumer, Pack.PackConstructor infoFactory) {
        infoConsumer.accept(Pack.create(name,
            true,
            () -> resources.apply(name),
            infoFactory,
            position,
            PackSource.BUILT_IN)
        );

    }
}
