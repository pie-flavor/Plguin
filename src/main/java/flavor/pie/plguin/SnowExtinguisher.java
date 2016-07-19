package flavor.pie.plguin;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class SnowExtinguisher {
    @Inject
    Logger logger;
    @Listener
    public void snowExtinguisher(CollideBlockEvent e, @First Snowball snowball) {
        Location<World> location = snowball.getLocation();
        if (location.getBlockType().equals(BlockTypes.FIRE)) {
            location.setBlockType(BlockTypes.AIR);
        } else {
            location = location.getRelative(e.getTargetSide());
            if (location.equals(BlockTypes.FIRE)) {
                location.setBlockType(BlockTypes.AIR);
            }
        }
    }
}