package flavor.pie.plguin;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ShearTallGrass {
    @Listener
    public void shearTallGrass(InteractBlockEvent.Secondary e, @First Player p) {
        Optional<ItemStack> stack_ = p.getItemInHand();
        if (stack_.isPresent()) {
            ItemStack stack = stack_.get();
            if (stack.getItem().equals(ItemTypes.SHEARS)) {
                BlockSnapshot snapshot = e.getTargetBlock();
                if (snapshot.getState().getType().equals(BlockTypes.DOUBLE_PLANT)) {
                    Location<World> location = snapshot.getLocation().get();
                    p.playSound(SoundTypes.ENTITY_SHEEP_SHEAR, location.getPosition(), 10.0);
                    DoublePlantType type = snapshot.get(Keys.DOUBLE_PLANT_TYPE).get();
                    if (type.equals(DoublePlantTypes.FERN)) {
                        location.setBlockType(BlockTypes.TALLGRASS);
                        location.offer(Keys.SHRUB_TYPE, ShrubTypes.TALL_GRASS);
                    } else if (type.equals(DoublePlantTypes.FERN)) {
                        location.setBlockType(BlockTypes.TALLGRASS);
                        location.offer(Keys.SHRUB_TYPE, ShrubTypes.FERN);
                    }
                }
            }
        }
    }
}
