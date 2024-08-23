package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.*;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlockEntities
 */
@SuppressWarnings("unchecked")
public class GTBlockEntities {

    public static final BlockEntityEntry<PipeBlockEntity> NEW_PIPE = REGISTRATE
            .blockEntity("pipe", PipeBlockEntity::new)
            .validBlocks(Stream.concat(GTBlocks.DUCT_PIPE_BLOCKS.values().stream(),
                    Stream.concat(Stream.of(GTBlocks.OPTICAL_PIPE),
                            Stream.concat(Stream.of(GTBlocks.LASER_PIPE),
                                    Stream.concat(GTBlocks.CABLE_BLOCKS.values().stream(),
                                            GTBlocks.MATERIAL_PIPE_BLOCKS.values().stream()))))
                    .toArray(NonNullSupplier[]::new))
            .register();

    public static final BlockEntityEntry<SignBlockEntity> GT_SIGN = REGISTRATE
            .<SignBlockEntity>blockEntity("sign", SignBlockEntity::new)
            .validBlocks(GTBlocks.RUBBER_SIGN,
                    GTBlocks.RUBBER_WALL_SIGN,
                    GTBlocks.TREATED_WOOD_SIGN,
                    GTBlocks.TREATED_WOOD_WALL_SIGN)
            .register();

    public static final BlockEntityEntry<GTHangingSignBlockEntity> GT_HANGING_SIGN = REGISTRATE
            .blockEntity("hanging_sign", GTHangingSignBlockEntity::new)
            .validBlocks(GTBlocks.RUBBER_HANGING_SIGN,
                    GTBlocks.RUBBER_WALL_HANGING_SIGN,
                    GTBlocks.TREATED_WOOD_HANGING_SIGN,
                    GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN)
            .register();

    public static void init() {}
}
