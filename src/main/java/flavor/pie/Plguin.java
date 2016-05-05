package flavor.pie;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id="plguin",name="Plguin",version="1.0-SNAPSHOT",authors="pie_flavor",description="Random unrelated crap.")
public class Plguin {
    @Inject
    Game game;
    @Inject
    Logger logger;
    @Inject @DefaultConfig(sharedRoot = false)
    ConfigurationLoader<CommentedConfigurationNode> loader;
    @Inject
    PluginContainer plugin;
    CommentedConfigurationNode root;
    List<UUID> toLock;
    @Listener
    public void onPreInit(GamePreInitializationEvent e) {
        try {
            root = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Error loading config. Disabling.");
            disable();
            return;
        }
        EventManager manager = game.getEventManager();
        if (root.getNode("lightning-head").getBoolean(false)) manager.registerListener(this, DropItemEvent.Pre.class, this::lightningHead);
        if (root.getNode("underwater-potion").getBoolean(false)) manager.registerListener(this, UseItemStackEvent.Finish.class, this::underwaterPotion);
        if (root.getNode("lockable-chests").getBoolean(false)) manager.registerListener(this, InteractBlockEvent.Secondary.class, Order.LATE, this::lockChest);
        if (root.getNode("flaming-creepers").getBoolean(false)) manager.registerListener(this, DamageEntityEvent.class, this::flamingCreepers);
        if (root.getNode("break-double-chests").getBoolean(false)) manager.registerListener(this, ChangeBlockEvent.Break.class, this::breakDoubleChests);
        if (root.getNode("shear-tall-grass").getBoolean(false)) manager.registerListener(this, InteractBlockEvent.Secondary.class, this::shearTallGrass);
        if (root.getNode("snow-extinguisher").getBoolean(false)) manager.registerListener(this, CollideBlockEvent.class, this::snowExtinguisher);
        toLock = new ArrayList<>();
    }
    private void disable() {
        game.getEventManager().unregisterPluginListeners(this);
    }
    void lightningHead(DropItemEvent.Pre e) {
        Optional<DamageSource> src_ = e.getCause().first(DamageSource.class);
        if (src_.isPresent()) {
            DamageSource src = src_.get();
            if (src instanceof EntityDamageSource) {
                Entity damager = ((EntityDamageSource) src).getSource();
                if (damager instanceof Lightning) {
                    Optional<Entity> entity_ = e.getCause().first(Entity.class);
                    if (entity_.isPresent()) {
                        Entity entity = entity_.get();
                        if (entity instanceof Player) {
                            ItemStack stack = ItemStack.builder()
                                    .itemType(ItemTypes.SKULL)
                                    .add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
                                    .add(Keys.REPRESENTED_PLAYER, ((Player) entity).getProfile()).build();
                            e.getDroppedItems().add(stack.createSnapshot());
                        }
                    }
                }
            }
        }
    }
    void underwaterPotion(UseItemStackEvent.Finish e) {
        Transaction<ItemStackSnapshot> transaction = e.getItemStackResult();
        ItemStackSnapshot original = transaction.getOriginal();
        if (original.getType().equals(ItemTypes.POTION)) {
            ItemStackSnapshot proposed = transaction.getFinal();
            if (proposed.getType().equals(ItemTypes.GLASS_BOTTLE)) {
                ItemStackSnapshot snapshot = ItemStack.builder()
                        .itemType(ItemTypes.POTION)
                        .build().createSnapshot();
                transaction.setCustom(snapshot);
            }
        }
    }
    void lockChest(InteractBlockEvent.Secondary e) {
        if (e.getTargetBlock().supports(Keys.LOCK_TOKEN)) {
            Optional<Player> p_ = e.getCause().first(Player.class);
            if (p_.isPresent()) {
                Player p = p_.get();
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
    void flamingCreepers(DamageEntityEvent e) {
        DamageSource src = e.getCause().first(DamageSource.class).get();
        Entity entity = e.getTargetEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = ((Creeper) entity);
            if (src instanceof EntityDamageSource) {
                EntityDamageSource esrc = ((EntityDamageSource) src);
                Entity damager = esrc.getSource();
                if (damager instanceof ArmorEquipable) {
                    ArmorEquipable equipable = ((ArmorEquipable) damager);
                    Optional<ItemStack> item_ = equipable.getItemInHand();
                    if (item_.isPresent()) {
                        ItemStack item = item_.get();
                        List<ItemEnchantment> enchantments = item.get(Keys.ITEM_ENCHANTMENTS).get();
                        for (ItemEnchantment ench : enchantments) {
                            if (ench.getEnchantment().equals(Enchantments.FIRE_ASPECT)) {
                                creeper.ignite();
                                return;
                            }
                        }
                    }
                } else {
                    if (damager instanceof Projectile) {
                        if (damager.get(Keys.IS_AFLAME).get()) {
                            creeper.ignite();
                        }
                    }
                }
            }
        }
    }
    void breakDoubleChests(ChangeBlockEvent.Break e) {
        Optional<Player> p_ = e.getCause().first(Player.class);
        if (p_.isPresent()) {
            Player p = p_.get();
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
    void shearTallGrass(InteractBlockEvent.Secondary e) {
        Optional<Player> p_ = e.getCause().first(Player.class);
        if (p_.isPresent()) {
            Player p = p_.get();
            Optional<ItemStack> stack_ = p.getItemInHand();
            if (stack_.isPresent()) {
                ItemStack stack = stack_.get();
                if (stack.getItem().equals(ItemTypes.SHEARS)) {
                    BlockSnapshot snapshot = e.getTargetBlock();
                    if (snapshot.getState().getType().equals(BlockTypes.DOUBLE_PLANT)) {
                        Location<World> location = snapshot.getLocation().get();
                        p.playSound(SoundTypes.SHEEP_SHEAR, location.getPosition(), 10.0);
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
    void snowExtinguisher(CollideBlockEvent e) {
        Optional<Snowball> snowball_ = e.getCause().first(Snowball.class);
        if (snowball_.isPresent()) {
            Snowball snowball = snowball_.get();
            Location<World> location = snowball.getLocation();
            if (location.getBlockType().equals(BlockTypes.FIRE)) {
                location.setBlockType(BlockTypes.AIR);
            }
        }
    }
}
