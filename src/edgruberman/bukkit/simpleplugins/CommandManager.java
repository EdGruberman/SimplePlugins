package edgruberman.bukkit.simpleplugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

//TODO unload .jar before loading it.
final class CommandManager implements CommandExecutor 
{
    private JavaPlugin plugin;

    CommandManager(final JavaPlugin plugin) {
        this.plugin = plugin;
        
        this.setExecutorOf("plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        Main.messageManager.log(
                ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                    + " issued command: " + label + " " + CommandManager.join(split)
                , MessageLevel.FINE
        );
        
        if (!sender.isOp()) {
            Main.messageManager.respond(sender, "You must be a server operator to use this command.", MessageLevel.RIGHTS);
            return false;
        }
        
        String action = null;
        if (split.length >= 1) action = split[0].toLowerCase();
        
        PluginManager pluginManager = this.plugin.getServer().getPluginManager();
        String pluginName = null;
        Plugin plugin = null;
        if (split.length >= 2) {
            pluginName = split[1];
            plugin = pluginManager.getPlugin(pluginName);
            if (plugin == null && !(action.equals("refresh") || action.equals("load")) ) {
                Main.messageManager.respond(sender, "Plugin \"" + pluginName + "\" not found.", MessageLevel.SEVERE);
                return true;
            }
        }
        
        if (action == null || action.equals("list")) {
            String message = "";
            for (Plugin loaded : pluginManager.getPlugins()) {
                if (!message.equals("")) message += "\n";
                message += "\"" + loaded.getDescription().getName() + "\""
                    + " v" + loaded.getDescription().getVersion()
                    + " (" + (loaded.isEnabled() ? "Enabled" : "Disabled") + ")";
            }
            Main.messageManager.respond(sender, message, MessageLevel.CONFIG);
            
        } else if (action.equals("enable"))  { this.enablePlugin(pluginManager, plugin, sender);
        } else if (action.equals("disable")) { this.disablePlugin(pluginManager, plugin, sender);
        } else if (action.equals("restart")) { this.disablePlugin(pluginManager, plugin, sender);
                                               this.enablePlugin(pluginManager, plugin, sender);
        } else if (action.equals("load"))    { this.loadPlugin(pluginManager, pluginName, plugin, sender);
        } else if (action.equals("refresh")) { this.loadPlugins(pluginManager, sender);
        } else { this.showUsage(sender, label, "Unrecognized action.");
        }
        
        return true;
    }
    
    private void enablePlugin(PluginManager pluginManager, Plugin plugin, CommandSender sender) {
        if (plugin == null) return;
        
        if (plugin.isEnabled()) {
            Main.messageManager.respond(sender, "Plugin \"" + plugin.getDescription().getName() + "\" is already enabled.", MessageLevel.WARNING);
            return;
        }
        
        pluginManager.enablePlugin(plugin);
        Main.messageManager.respond(sender, "Plugin \"" + plugin.getDescription().getName() + "\" enabled.", MessageLevel.CONFIG);
    }
    
    private void disablePlugin(PluginManager pluginManager, Plugin plugin, CommandSender sender) {
        if (plugin == null) return;
        
        if (plugin.getDescription().getName().equals(this.plugin.getDescription().getName())) {
            Main.messageManager.respond(sender, "You must manually remove this \"" + this.plugin.getDescription().getName() + "\" plugin to disable it.", MessageLevel.WARNING);
            return;
        }
        
        if (!plugin.isEnabled()) {
            Main.messageManager.respond(sender, "Plugin \"" + plugin.getDescription().getName() + "\" is already disabled.",  MessageLevel.WARNING);
            return;
        }
        
        pluginManager.disablePlugin(plugin);
        Main.messageManager.respond(sender, "Plugin \"" + plugin.getDescription().getName() + "\" disabled.", MessageLevel.CONFIG);
    }
    
    private void loadPlugin(PluginManager pluginManager, String pluginName, Plugin plugin, CommandSender sender) {
        this.disablePlugin(pluginManager, plugin, sender);
        
        try {
            pluginManager.loadPlugin(new File(this.plugin.getDataFolder().getParent(), pluginName + ".jar"));
            Main.messageManager.respond(sender, "Plugin \"" + pluginName + "\" loaded.", MessageLevel.CONFIG);
            this.enablePlugin(pluginManager, plugin, sender);
        } catch (Exception e) {
            Main.messageManager.log("Error loading \"" + pluginName + "\"", MessageLevel.SEVERE, e);
            Main.messageManager.respond(sender, "Error loading \"" + pluginName + "\"; See log for details.", MessageLevel.SEVERE);
        }
    }
    
    private void loadPlugins(PluginManager pluginManager, CommandSender sender) {
        for (Plugin p : pluginManager.getPlugins()) {
            this.disablePlugin(pluginManager, p, sender);
        }
        
        // TODO This clears out even this plugin and cancels execution.
        // pluginManager.clearPlugins();
        
        try {
            pluginManager.loadPlugins(new File(this.plugin.getDataFolder().getParentFile().getPath()));
            Main.messageManager.respond(sender
                , "" + pluginManager.getPlugins().length + " plugins loaded all from \"" + this.plugin.getDataFolder().getParentFile() + "\"."
                , MessageLevel.CONFIG
            );
        } catch (Exception e) {
            Main.messageManager.log("Error loading all plugins from \"" + this.plugin.getDataFolder().getParentFile().getPath() + "\"", MessageLevel.SEVERE, e);
            Main.messageManager.respond(sender, "Error loading all plugins from \"" + this.plugin.getDataFolder().getParentFile() + "\"; See log for details.", MessageLevel.SEVERE);
        }
        
        for (Plugin p : pluginManager.getPlugins()) {
            this.enablePlugin(pluginManager, p, sender);
        }
    }
    
    private void showUsage(CommandSender sender, String label, String error) {
        Main.messageManager.respond(sender, "Syntax Error: " + error, MessageLevel.SEVERE);
        this.showUsage(sender, label);
    }
    
    private void showUsage(CommandSender sender, String label) {
        Main.messageManager.respond(sender, this.plugin.getCommand(label).getUsage(), MessageLevel.NOTICE);
    }
    
    /**
     * Registers this class as executor for a chat/console command.
     * 
     * @param label Command label to register.
     */
    private void setExecutorOf(final String label) {
        PluginCommand command = this.plugin.getCommand(label);
        if (command == null) {
            Main.messageManager.log("Unable to register \"" + label + "\" command.", MessageLevel.WARNING);
            return;
        }
        
        command.setExecutor(this);
    }
    
    /**
     * Concatenate all string elements of an array together with a space.
     * 
     * @param s string array
     * @return concatenated elements
     */
    private static String join(final String[] s) {
        return join(Arrays.asList(s), " ");
    }
    
    /**
     * Combine all the elements of a list together with a delimiter between each.
     * 
     * @param list list of elements to join
     * @param delim delimiter to place between each element
     * @return string combined with all elements and delimiters
     */
    private static String join(final List<String> list, final String delim) {
        if (list == null || list.isEmpty()) return "";
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
}