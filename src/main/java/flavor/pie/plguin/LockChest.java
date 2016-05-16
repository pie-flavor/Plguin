package flavor.pie.plguin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LockChest {
    List<UUID> toLock = Lists.newArrayList();
    @Inject
    PluginContainer plugin;
    @Inject
    Game game;
    @Listener
    public void lockChest(InteractBlockEvent.Secondary e, @First Player p) {
        if (e.getTargetBlock().supports(Keys.LOCK_TOKEN)) {
            if (p.get(Keys.IS_SNEAKING).get()) {
                Optional<String> string = e.getTargetBlock().get(Keys.LOCK_TOKEN);
                if (!string.isPresent() || string.get().equals("")) {
                    Optional<ItemStack> stack_ = p.getItemInHand();
                    if (stack_.isPresent()) {
                        ItemStack stack = stack_.get();
                        Optional<Text> text_ = stack.get(Keys.DISPLAY_NAME);
                        if (text_.isPresent()) {
                            Text text = text_.get();
                            if (toLock.contains(p.getUniqueId())) {
                                toLock.remove(p.getUniqueId());
                                p.sendMessage(Text.of("You have locked the ", Text.of(e.getTargetBlock().getState().getType()), ". Don't lose your key!"));
                                e.getTargetBlock().getLocation().get().offer(Keys.LOCK_TOKEN, text.toPlain());
                            } else {
                                toLock.add(p.getUniqueId());
                                p.sendMessage(Text.of("Sneak-right-click the ", Text.of(e.getTargetBlock().getState().getType()), " again to lock it."));
                                game.getScheduler().createTaskBuilder().execute(() -> {
                                    toLock.remove(p.getUniqueId());
                                    p.sendMessage(Text.of("Canceled locking."));
                                }).delay(5, TimeUnit.SECONDS).name(plugin.getId() + "-CancelLock-" + p.getUniqueId()).submit(this);
                            }
                        }
                    }
                }
            }
        }
    }
}
