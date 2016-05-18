package flavor.pie.plguin;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
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
                            /*tracked.put(location, relative);
                            BlockSnapshot originalSnapshot = relative.createSnapshot();
                            BlockState originalState = originalSnapshot.getState();
                            BlockState proposedState = BlockState.builder().from(originalState).blockType(BlockTypes.AIR).build();
                            BlockSnapshot proposedSnapshot = BlockSnapshot.builder().from(originalSnapshot).blockState(proposedState).build();
                            ChangeBlockEvent.Break breakEvent = SpongeEventFactory.createChangeBlockEventBreak(e.getCause(), e.getTargetWorld(), Lists.newArrayList(new Transaction(originalSnapshot, proposedSnapshot)));
                            */
                            relative.getExtent().digBlock(relative.getBlockPosition(), e.getCause());
                        }
                    }
                }
            }
        }
    }
    //@Listener
    public void dropItems(DropItemEvent.Destruct e, @First BlockSpawnCause cause) {
        Optional<Location<World>> loc_ = cause.getBlockSnapshot().getLocation();
        if (loc_.isPresent()) {
            Location<World> loc = loc_.get();
            if (tracked.containsKey(loc)) {

            }
        }
    }
}