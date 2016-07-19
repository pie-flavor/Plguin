package flavor.pie.plguin;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import flavor.pie.plguin.data.ShakeData;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;

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
    @Inject
    Injector defaultInjector;
    Injector injector;
    CommentedConfigurationNode root;
    Key<Value<Boolean>> shake;

    @Listener
    public void onPreInit(GamePreInitializationEvent e) throws Exception {
        try {
            root = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Error loading config. Disabling.");
            disable();
            return;
        }
        shake = KeyFactory.makeSingleKey(Boolean.class, Value.class, DataQuery.of("shake"));
        injector = defaultInjector.createChildInjector(new AbstractModule(){
            public void configure() {
                bind(new TypeLiteral<Key<Value<Boolean>>>(){}).toInstance(shake);
            }
        });
        game.getDataManager().registerBuilder(ShakeData.class, injector.getInstance(ShakeData.ShakeDataBuilder.class));
        EventManager manager = game.getEventManager();
        //TODO Unimplemented! if (root.getNode("lightning-head").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.LightningHead")));
        if (root.getNode("underwater-potion").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.UnderwaterPotion")));
        if (root.getNode("lockable-chests").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.LockChest")));
        //TODO Does not work! if (root.getNode("flaming-creepers").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.FlamingCreepers")));
        if (root.getNode("break-double-chests").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.BreakDoubleChests")));
        if (root.getNode("shear-tall-grass").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.ShearTallGrass")));
        if (root.getNode("snow-extinguisher").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.SnowExtinguisher")));
        //TODO Does not work! if (root.getNode("shake-potions").getBoolean(false)) manager.registerListeners(this, injector.getInstance(Class.forName("flavor.pie.plguin.ShakePotions")));
        loader.save(root);
    }
    private void disable() {
        game.getEventManager().unregisterPluginListeners(this);
    }

}
