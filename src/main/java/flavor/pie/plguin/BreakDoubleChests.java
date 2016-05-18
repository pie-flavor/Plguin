package flavor.pie.plguin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BreakDoubleChests {
    Map<Location, Location> tracked = Maps.newHashMap();
    @Inject
    Plguin plguin;
    @Inject
    Game game;
    @Listener(order= Order.BEFORE_POST)
    public void breakDoubleChests(ChangeBlockEvent.Break e, @First Player p) {
        List<Transaction<BlockSnapshot>> list = e.getTransactions();
        for (Transaction<BlockSnapshot> transaction : list) {
            BlockSnapshot snapshot = transaction.getOriginal();
            if (snapshot.getState().getType().equals(BlockTypes.CHEST) || snapshot.getState().getType().equals(BlockTypes.TRAPPED_CHEST)) {
                Optional<Set<Direction>> set_ = snapshot.get(Keys.CONNECTED_DIRECTIONS);
                if (set_.isPresent()) {
                    Set<Direction> set = set_.get();
                    if (!set.isEmpty()) {
                        Location<World> location = snapshot.getLocation().get();
                        for (Direction direction : set) {
                            Location<World> relative = location.getRelative(direction);
                            tracked.put(location, relative);
                            BlockSnapshot originalSnapshot = relative.createSnapshot();
                            BlockState originalState = originalSnapshot.getState();
                            BlockState proposedState = BlockState.builder().from(originalState).blockType(BlockTypes.AIR).build();
                            BlockSnapshot proposedSnapshot = BlockSnapshot.builder().from(originalSnapshot).blockState(proposedState).build();
                            ChangeBlockEvent.Break breakEvent = SpongeEventFactory.createChangeBlockEventBreak(e.getCause(), e.getTargetWorld(), Lists.newArrayList(new Transaction<>(originalSnapshot, proposedSnapshot)));
                            boolean cancelled = game.getEventManager().post(breakEvent);
                            if (!cancelled) {
                                for (Transaction<BlockSnapshot> newTransaction: breakEvent.getTransactions()) {
                                    newTransaction.getOriginal().getLocation().get().setBlock(newTransaction.getFinal().getState());
                                }
                            }
                            Task.builder().delayTicks(1).execute(() -> tracked.remove(location)).submit(plguin);
                        }
                    }
                }
            }
        }
    }
    @Listener
    public void dropItems(DropItemEvent.Destruct e, @First BlockSpawnCause cause) {
        Optional<Location<World>> loc_ = cause.getBlockSnapshot().getLocation();
        if (loc_.isPresent()) {
            Location<World> loc = loc_.get();
            if (tracked.containsKey(loc)) {
                Location<World> relative = tracked.get(loc);
                BlockSnapshot snapshot = relative.createSnapshot();
                BlockSpawnCause newCause = BlockSpawnCause.builder().from(cause).block(snapshot).build();
                BlockType blockType = cause.getBlockSnapshot().getState().getType();
                ItemType itemType;
                if (blockType.equals(BlockTypes.CHEST)) {
                    itemType = ItemTypes.CHEST;
                } else if (blockType.equals(BlockTypes.TRAPPED_CHEST)) {
                    itemType = ItemTypes.TRAPPED_CHEST;
                } else {
                    return;
                }
                ItemStack stack = ItemStack.builder().itemType(itemType).build();
                Item item = (Item) relative.getExtent().createEntity(EntityTypes.ITEM, relative.getPosition()).get();
                item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
                DropItemEvent.Destruct newEvent = SpongeEventFactory.createDropItemEventDestruct(e.getCause(), Lists.newArrayList(item), relative.getExtent());
                boolean cancelled = game.getEventManager().post(newEvent);
                if (!cancelled) {
                    for (Entity entity : newEvent.getEntities()) {
                        newEvent.getTargetWorld().spawnEntity(entity, Cause.of(NamedCause.source(newCause)));
                    }
                }
            }
        }
    }
}