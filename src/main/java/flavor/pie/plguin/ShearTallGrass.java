package flavor.pie.plguin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class ShearTallGrass {
    @Inject
    PluginContainer container;
    @Listener
    public void shearTallGrass(InteractBlockEvent.Secondary e, @First Player p) {
        Optional<ItemStack> stack_ = p.getItemInHand(HandTypes.MAIN_HAND);
        if (stack_.isPresent()) {
            ItemStack stack = stack_.get();
            if (!stack.getItem().equals(ItemTypes.SHEARS)) {
                stack_ = p.getItemInHand(HandTypes.OFF_HAND);
                if (!stack_.isPresent()) return;
                stack = stack_.get();
                if (stack.getItem().equals(ItemTypes.SHEARS)) return;
            }
            BlockSnapshot snapshot = e.getTargetBlock();
            if (snapshot.getState().getType().equals(BlockTypes.DOUBLE_PLANT)) {
                Location<World> location = snapshot.getLocation().get();
                DoublePlantType type = snapshot.get(Keys.DOUBLE_PLANT_TYPE).get();
                ShrubType shrubType;
                if (type.equals(DoublePlantTypes.FERN)) {
                    shrubType = ShrubTypes.FERN;
                } else if (type.equals(DoublePlantTypes.GRASS)) {
                    shrubType = ShrubTypes.TALL_GRASS;
                } else return;
                p.playSound(SoundTypes.ENTITY_SHEEP_SHEAR, location.getPosition(), 10.0);
                location.setBlockType(BlockTypes.TALLGRASS);
                location.offer(Keys.SHRUB_TYPE, shrubType);
                ItemStack toSpawn = ItemStack.builder().itemType(ItemTypes.TALLGRASS).add(Keys.SHRUB_TYPE, shrubType).build();
                Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition()).get();
                item.offer(Keys.REPRESENTED_ITEM, toSpawn.createSnapshot());
                List<NamedCause> causeList = Lists.newArrayList();
                e.getCause().getNamedCauses().forEach((str, obj) -> causeList.add(NamedCause.of(str, obj)));
                location.getExtent().spawnEntity(item, Cause.builder().named("SpawnCause", EntitySpawnCause.builder().type(SpawnTypes.PLUGIN).entity(item).build()).named("Plugin", container).addAll(causeList).build());
            }
        }
    }
}
