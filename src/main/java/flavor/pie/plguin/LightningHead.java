package flavor.pie.plguin;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public class LightningHead {
    @Listener
    void lightningHead(DropItemEvent.Destruct e, @First EntityDamageSource src) {
        Entity damager = src.getSource();
        if (damager instanceof Lightning) {
            Optional<Player> player_ = e.getCause().first(Player.class);
            if (player_.isPresent()) {
                Player player = player_.get();
                ItemStack stack = ItemStack.builder()
                        .itemType(ItemTypes.SKULL)
                        .add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
                        .add(Keys.REPRESENTED_PLAYER, player.getProfile()).build();
                Item item = (Item) e.getTargetWorld().createEntity(EntityTypes.ITEM, player.getLocation().getPosition()).get();
                item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
                e.getEntities().add(item);
            }
        }
    }
}
