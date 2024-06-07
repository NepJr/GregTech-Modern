package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.item.armor.IStepAssist;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract Iterable<ItemStack> getArmorSlots();

    @Shadow
    public abstract void setItemSlot(EquipmentSlot slot, ItemStack stack);

    @Inject(method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private void gtceu$adjustArmorAbsorption(DamageSource damageSource, float damageAmount,
                                             CallbackInfoReturnable<Float> cir) {
        float armorDamage = Math.max(1.0F, damageAmount / 4.0F);
        int i = 0;
        for (ItemStack itemStack : this.getArmorSlots()) {
            if (itemStack.getItem() instanceof ArmorComponentItem armorItem) {
                EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                armorItem.damageArmor((LivingEntity) (Object) this, itemStack, damageSource, (int) armorDamage, slot);
                if (itemStack.getCount() == 0) {
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            ++i;
        }
    }

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "onEquipItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private void gtceu$onEquipArmor(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci,
                                    @Local Equipable equipable) {
        if (equipable == null && pSlot == EquipmentSlot.FEET &&
                pOldItem.getItem() instanceof ArmorComponentItem) {
            AttributeInstance attribute = ((LivingEntity) (Object) this).getAttribute(Attributes.STEP_HEIGHT);
            if (attribute != null && attribute.hasModifier(IStepAssist.STEP_ASSIST_MODIFIER)) {
                attribute.removeModifier(IStepAssist.STEP_ASSIST_MODIFIER);
            }
        }
    }
}
