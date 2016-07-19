package flavor.pie.plguin;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import flavor.pie.plguin.data.ShakeData;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ShakePotions {
    @Inject
    Key<Value<Boolean>> key;
    @Inject
    Plguin plguin;
    @Inject
    Game game;
    Map<ItemStack, Integer> shakes = Maps.newHashMap();
    @Listener
    public void shakePotion(InteractBlockEvent.Primary e, @First Player p) {
        Optional<ItemStack> stack_ = p.getItemInHand(HandTypes.MAIN_HAND);
        if (stack_.isPresent()) {
            ItemStack stack = stack_.get();
            if (stack.getItem().equals(ItemTypes.POTION)) {
                int shakes = this.shakes.getOrDefault(stack, 0);
                if (shakes == 5) {
                    ShakeData data = stack.getOrCreate(ShakeData.class).get();
                    data.set(key, true);
                    Text text = Text.of(TextColors.GRAY, "Shaken");
                    stack.get(Keys.ITEM_LORE).get().add(text);
                    Task.builder().delay(30, TimeUnit.SECONDS).execute(() -> {data.set(key, false); stack.get(Keys.ITEM_LORE).get().remove(text);}).submit(plguin);
                    this.shakes.put(stack, 0);
                    p.playSound(SoundTypes.ENTITY_PLAYER_SPLASH, p.getLocation().getPosition(), 20);
                } else {
                    this.shakes.put(stack, shakes+1);
                    Task.builder().delay(2, TimeUnit.SECONDS).execute(() -> {if (this.shakes.getOrDefault(stack, 0) > 0) this.shakes.put(stack, this.shakes.get(stack)+1);}).submit(plguin);
                }
            }
        }
    }
    @Listener
    public void drinkPotion(ChangeEntityPotionEffectEvent.Gain e) {
        ItemStack stack;
        Optional<ItemStack> stack_ = e.getCause().first(ItemStack.class);
        if (stack_.isPresent()) {
            stack = stack_.get();
            if (!stack.getItem().equals(ItemTypes.POTION)) return;
        } else {
            Optional<ThrownPotion> potion_ = e.getCause().first(ThrownPotion.class);
            if (potion_.isPresent()) {
                ThrownPotion potion = potion_.get();
                stack = potion.getPotionItemData().item().get().createStack();
            } else {
                return;
            }
        }
        if (stack.getOrCreate(ShakeData.class).get().get(key).get()) {
            PotionEffect effect = PotionEffect.builder().from(e.getPotionEffect()).duration((int) (e.getPotionEffect().getDuration() * 1.05)).build();
            e.setCancelled(true);
            e.getTargetEntity().getValue(Keys.POTION_EFFECTS).get().add(effect);
        }
    }
}
