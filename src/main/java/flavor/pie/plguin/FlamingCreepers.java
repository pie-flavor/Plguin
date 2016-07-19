package flavor.pie.plguin;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class FlamingCreepers {
    @Listener
    public void flamingCreepers(DamageEntityEvent e, @Root EntityDamageSource src) {
        Entity entity = e.getTargetEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = ((Creeper) entity);
            Entity damager = src.getSource();
            if (damager instanceof ArmorEquipable) {
                ArmorEquipable equipable = ((ArmorEquipable) damager);
                Optional<ItemStack> item_ = equipable.getItemInHand(HandTypes.MAIN_HAND);
                if (item_.isPresent()) {
                    ItemStack item = item_.get();
                    List<ItemEnchantment> enchantments = item.get(Keys.ITEM_ENCHANTMENTS).get();
                    for (ItemEnchantment ench : enchantments) {
                        if (ench.getEnchantment().equals(Enchantments.FIRE_ASPECT)) {
                            if (creeper.isPrimed()) {
                                creeper.detonate(e.getCause());
                            } else {
                                creeper.prime(e.getCause());
                            }
                            return;
                        }
                    }
                }
            } else {
                if (damager instanceof Projectile) {
                    if (damager.get(Keys.IS_AFLAME).get()) {
                        creeper.prime(e.getCause());
                    }
                }
            }
        }
    }
}
