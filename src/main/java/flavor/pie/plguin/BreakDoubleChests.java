package flavor.pie.plguin;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BreakDoubleChests {
    @Listener
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
                            location.setBlockType(BlockTypes.AIR, true);
                        }
                    }
                }
            }
        }
    }
}