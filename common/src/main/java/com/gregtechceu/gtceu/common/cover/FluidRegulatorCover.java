package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.misc.FluidAmountHandler;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FluidRegulatorCover extends PumpCover {
    private static final long MAX_STACK_SIZE = 2_048_000_000; // Capacity of quantum tank IX

    @Persisted @DescSynced @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;

    @Persisted
    private final FluidAmountHandler globalTransferSize;
    protected long fluidTransferBuffered = 0L;


    private Widget transferSizeInput;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);

        long transferRateMilliBuckets = this.transferRate.getMilliBuckets();
        this.globalTransferSize = new FluidAmountHandler(transferRateMilliBuckets, transferRateMilliBuckets, this::scheduleRenderUpdate);
    }

    //////////////////////////////////////
    //*****     Transfer Logic    ******//
    //////////////////////////////////////


    @Override
    protected long doTransferFluidsInternal(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        return switch (transferMode) {
            case TRANSFER_ANY -> transferAny(source, destination, platformTransferLimit);
            case TRANSFER_EXACT -> transferExact(source, destination, platformTransferLimit);
            case KEEP_EXACT -> keepExact(source, destination, platformTransferLimit);
        };
    }

    private long transferExact(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        long fluidLeftToTransfer = platformTransferLimit;

        for (int slot = 0; slot < source.getTanks(); slot++) {
            if (fluidLeftToTransfer <= 0L)
                break;

            FluidStack sourceFluid = source.getFluidInTank(slot).copy();
            long supplyAmount = getFilteredFluidAmount(sourceFluid) * MILLIBUCKET_SIZE;

            // If the remaining transferrable amount in this operation is not enough to transfer the full stack size,
            // the remaining amount for this operation will be buffered and added to the next operation's maximum.
            if (fluidLeftToTransfer + fluidTransferBuffered < supplyAmount) {
                this.fluidTransferBuffered += fluidLeftToTransfer;
                fluidLeftToTransfer = 0L;
                break;
            }

            if (sourceFluid.isEmpty() || supplyAmount <= 0L)
                continue;

            sourceFluid.setAmount(supplyAmount);
            FluidStack drained = source.drain(sourceFluid, true);

            if (drained.isEmpty() || drained.getAmount() < supplyAmount)
                continue;

            long insertableAmount = destination.fill(drained.copy(), true);
            if (insertableAmount <= 0)
                continue;

            drained.setAmount(insertableAmount);
            drained = source.drain(drained, false);

            if (!drained.isEmpty()) {
                destination.fill(drained, false);
                fluidLeftToTransfer -= (drained.getAmount() - fluidTransferBuffered);
            }

            fluidTransferBuffered = 0L;
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private long keepExact(IFluidTransfer source, IFluidTransfer destination, long platformTransferLimit) {
        long fluidLeftToTransfer = platformTransferLimit;

        final Map<FluidStack, Long> sourceAmounts = enumerateDistinctFluids(source, TransferDirection.EXTRACT);
        final Map<FluidStack, Long> destinationAmounts = enumerateDistinctFluids(destination, TransferDirection.INSERT);

        for (FluidStack fluidStack : sourceAmounts.keySet()) {
            if (fluidLeftToTransfer <= 0L)
                break;

            long amountToKeep = getFilteredFluidAmount(fluidStack) * MILLIBUCKET_SIZE;
            long amountInDest = destinationAmounts.getOrDefault(fluidStack, 0L);
            if (amountInDest >= amountToKeep)
                continue;

            FluidStack fluidToMove = fluidStack.copy();
            fluidToMove.setAmount(Math.min(fluidLeftToTransfer, amountToKeep - amountInDest));
            if (fluidToMove.getAmount() <= 0L)
                continue;

            FluidStack drained = source.drain(fluidToMove, true);
            long fillableAmount = destination.fill(drained, true);
            if (fillableAmount <= 0L)
                continue;

            fluidToMove.setAmount(Math.min(fluidToMove.getAmount(), fillableAmount));

            drained = source.drain(fluidToMove, false);
            long movedAmount = destination.fill(drained, false);

            fluidLeftToTransfer -= movedAmount;
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        configureTransferSizeInput();

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1L : MAX_STACK_SIZE);
        }

        configureTransferSizeInput();
    }

    private long getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferSize.getMilliBuckets();

        FluidFilter filter = filterHandler.getFilter();
        return (filter.isBlackList() ? globalTransferSize.getMilliBuckets() : filter.testFluidAmount(fluidStack)) * MILLIBUCKET_SIZE;
    }

    ///////////////////////////
    //*****     GUI    ******//
    ///////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.fluid_regulator.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));

        this.transferSizeInput = globalTransferSize.createUI(35, 45, 106, 20);
        group.addWidget(transferSizeInput);

        configureTransferSizeInput();
    }

    private void configureTransferSizeInput() {
        if (this.transferSizeInput == null)
            return;

        this.transferSizeInput.setVisible(shouldShowTransferSize());
    }

    private boolean shouldShowTransferSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidRegulatorCover.class, PumpCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}