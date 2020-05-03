package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.versions.version_1_15.command.CommandSpawnTamableFox;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// @TODO:

/* @CHANGELOG (1.6.0-SNAPSHOT):
 *    Fix a bug of duplicating water/lava buckets.
 */
public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;

    private boolean versionSupported = true;

    @Override
    public void onLoad() {
        plugin = this;

        LanguageConfig.getConfig().saveDefault();

        String version = Bukkit.getServer().getClass().getPackage().getName();
        if (!version.equals("org.bukkit.craftbukkit.v1_15_R1")) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
            versionSupported = false;
            return;
        }

        try { // Replace the fox entity
            Field field = EntityTypes.FOX.getClass().getDeclaredField("ba");
            field.setAccessible(true);

            // If the field is final, then make it non final
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
                Field fieldMutable = field.getClass().getDeclaredField("modifiers");
                fieldMutable.setAccessible(true);
                fieldMutable.set(field, fieldMutable.getInt(field) & ~Modifier.FINAL);
                fieldMutable.setAccessible(false);
            }

            field.set(EntityTypes.FOX, (EntityTypes.b<EntityFox>) (type, world) -> new EntityTamableFox(type, world));

            field.setAccessible(false);

            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            //e.printStackTrace();
            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
        }

    }

    @Override
    public void onEnable() {
        if (!versionSupported) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionDisable());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + LanguageConfig.getSavingFoxMessage());
    }


    public EntityTamableFox spawnTamableFox(Location loc, EntityFox.Type type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType(type);

        return tamableFox;
    }


    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
