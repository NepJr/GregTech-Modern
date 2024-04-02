package com.gregtechceu.gtceu.api.item.tool;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote GTToolItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTToolItem extends DiggerItem implements IGTTool {

    @Getter
    protected final GTToolType toolType;
    @Getter
    protected final int electricTier;
    @Getter
    protected final Material material;
    @Getter
    private IGTToolDefinition toolStats;


    protected GTToolItem(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition definition, Properties properties) {
        super(0, 0, tier, toolType.harvestTags.isEmpty() ? null : toolType.harvestTags.get(0), properties);
        this.toolType = toolType;
        this.material = material;
        this.electricTier = toolType.electricTier;
        this.toolStats = definition;
        if (Platform.isClient()) {
            ToolItemRenderer.create(this, toolType);
        }
        definition$init();
    }

    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition definition, Properties properties) {
        return new GTToolItem(toolType, tier, material, definition, properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return get();
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return super.hasCraftingRemainingItem();
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        return definition$onItemUseFirst(itemStack, context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return definition$onItemUse(context);
    }

    @Override
    public String getDescriptionId() {
        return toolType.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(toolType.getUnlocalizedName(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return definition$mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isElectric() {
        return electricTier > -1;
    }

    @Nullable
    @Override
    public SoundEntry getSound() {
        return toolType.soundEntry;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return toolType.playSoundOnBlockDestroy;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return definition$hurtEnemy(stack, target, attacker);
    }

    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return definition$onBlockStartBreak(stack, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        definition$appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return definition$canApplyAtEnchantingTable(stack, enchantment);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return definition$isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return definition$getDefaultAttributeModifiers(slot, stack);
    }

    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return definition$canDisableShield(shield, shield, entity, attacker);
    }

    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return definition$doesSneakBypassUse(stack, level, pos, player);
    }

    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return definition$hasCraftingRemainingItem(stack);
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return definition$getCraftingRemainingItem(itemStack);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public boolean isDamaged(ItemStack stack) {
        return definition$isDamaged(stack);
    }

    public int getDamage(ItemStack stack) {
        return definition$getDamage(stack);
    }

    public int getMaxDamage(ItemStack stack) {
        return definition$getMaxDamage(stack);
    }

    public void setDamage(ItemStack stack, int damage) {
        definition$setDamage(stack, damage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return definition$use(level, player, usedHand);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return this.definition$isCorrectToolForDrops(stack, state);
    }
}
