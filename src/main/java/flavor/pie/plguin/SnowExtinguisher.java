package flavor.pie.plguin;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class SnowExtinguisher {
    @Listener
    public void snowExtinguisher(CollideBlockEvent e, @First Snowball snowball) {
        Location<World> location = snowball.getLocation();
        if (location.getBlockType().equals(BlockTypes.FIRE)) {
            location.setBlockType(BlockTypes.AIR);
        }
    }
}