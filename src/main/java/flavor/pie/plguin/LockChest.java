package flavor.pie.plguin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
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
    @Inject
    Logger logger;
    @Listener
    public void startLockChest(InteractBlockEvent.Secondary e, @Root Player p) {
        if (p.get(Keys.IS_SNEAKING).get()) {
            Optional<String> string = e.getTargetBlock().get(Keys.LOCK_TOKEN);
            if (!string.isPresent() || string.get().equals("")) {
                Optional<ItemStack> stack_ = p.getItemInHand(HandTypes.MAIN_HAND);
                if (stack_.isPresent()) {
                    ItemStack stack = stack_.get();
                    Optional<Text> text_ = stack.get(Keys.DISPLAY_NAME);
                    if (text_.isPresent()) {
                        Text text = text_.get();
                        if (!toLock.contains(p.getUniqueId())) {
                            DataTransactionResult result = e.getTargetBlock().getLocation().get().offer(Keys.LOCK_TOKEN, "");
                            if (result.isSuccessful()) {
                                toLock.add(p.getUniqueId());
                                p.sendMessage(Text.of("Sneak-left-click the ", Text.of(e.getTargetBlock().getState().getType()), " to lock it."));
                                Task.builder().execute(() -> {
                                    if (toLock.contains(p.getUniqueId())) {
                                        toLock.remove(p.getUniqueId());
                                        p.sendMessage(Text.of("Canceled locking."));
                                    }
                                }).delay(5, TimeUnit.SECONDS).name(plugin.getId() + "-CancelLock-" + p.getUniqueId()).submit(plugin.getInstance().get());
                            }
                        }
                    }
                }
            }
        }
    }
    @Listener
    public void lockChest(InteractBlockEvent.Primary e, @Root Player p) {
        if (p.get(Keys.IS_SNEAKING).get()) {
            Optional<String> string = e.getTargetBlock().get(Keys.LOCK_TOKEN);
            if (!string.isPresent() || string.get().equals("")) {
                Optional<ItemStack> stack_ = p.getItemInHand(HandTypes.MAIN_HAND);
                if (stack_.isPresent()) {
                    ItemStack stack = stack_.get();
                    Optional<Text> text_ = stack.get(Keys.DISPLAY_NAME);
                    if (text_.isPresent()) {
                        Text text = text_.get();
                        if (toLock.contains(p.getUniqueId())) {
                            DataTransactionResult result = e.getTargetBlock().getLocation().get().offer(Keys.LOCK_TOKEN, text.toPlain());
                            if (result.isSuccessful()) {
                                toLock.remove(p.getUniqueId());
                                p.sendMessage(Text.of("You have locked the ", Text.of(e.getTargetBlock().getState().getType()), ". Don't lose your key!"));
                            }
                        }
                    }
                }
            }
        }
    }
}
