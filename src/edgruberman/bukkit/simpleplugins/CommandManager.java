package edgruberman.bukkit.simpleplugins;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

//TODO unload .jar before loading it.
public class CommandManager implements CommandExecutor 
{
    private Main main;

    public CommandManager (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!sender.isOp()) {
            Main.messageManager.respond(sender, MessageLevel.RIGHTS, "You must be a server operator to issue that command.");
            return true;
        }
        
        String action = null;
        if (split.length >= 1) action = split[0].toLowerCase();
        
        PluginManager pluginManager = this.main.getServer().getPluginManager();
        String pluginName = null;
        Plugin plugin = null;
        if (split.length >= 2) {
            pluginName = split[1];
            plugin = pluginManager.getPlugin(pluginName);
            if (plugin == null && !(action.equals("refresh") || action.equals("load")) ) {
                Main.messageManager.respond(sender, MessageLevel.SEVERE, "Plugin \"" + pluginName + "\" not found.");
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
            Main.messageManager.respond(sender, MessageLevel.CONFIG, message);
            
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
            Main.messageManager.respond(sender, MessageLevel.WARNING
                , "Plugin \"" + plugin.getDescription().getName() + "\" is already enabled.");
            return;
        }
        
        pluginManager.enablePlugin(plugin);
        Main.messageManager.respond(sender, MessageLevel.CONFIG
            , "Plugin \"" + plugin.getDescription().getName() + "\" enabled.");
    }
    
    private void disablePlugin(PluginManager pluginManager, Plugin plugin, CommandSender sender) {
        if (plugin == null) return;
        
        if (plugin.getDescription().getName().equals(this.main.getDescription().getName())) {
            Main.messageManager.respond(sender, MessageLevel.WARNING
                , "You must manually remove this \"" + this.main.getDescription().getName() + "\" plugin to disable it.");
            return;
        }
        
        if (!plugin.isEnabled()) {
            Main.messageManager.respond(sender,  MessageLevel.WARNING
                , "Plugin \"" + plugin.getDescription().getName() + "\" is already disabled.");
            return;
        }
        
        pluginManager.disablePlugin(plugin);
        Main.messageManager.respond(sender, MessageLevel.CONFIG
            , "Plugin \"" + plugin.getDescription().getName() + "\" disabled.");
    }
    
    private void loadPlugin(PluginManager pluginManager, String pluginName, Plugin plugin, CommandSender sender) {
        this.disablePlugin(pluginManager, plugin, sender);
        
        try {
            pluginManager.loadPlugin(new File(this.main.getDataFolder().getParent(), pluginName + ".jar"));
            Main.messageManager.respond(sender, MessageLevel.CONFIG, "Plugin \"" + pluginName + "\" loaded.");
            this.enablePlugin(pluginManager, plugin, sender);
        } catch (Exception e) {
            Main.messageManager.log(MessageLevel.SEVERE, "Error loading \"" + pluginName + "\"", e);
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Error loading \"" + pluginName + "\"; See log for details.");
        }
    }
    
    private void loadPlugins(PluginManager pluginManager, CommandSender sender) {
        for (Plugin p : pluginManager.getPlugins()) {
            this.disablePlugin(pluginManager, p, sender);
        }
        
        // TODO This clears out even this plugin and cancels execution.
        // pluginManager.clearPlugins();
        
        try {
            pluginManager.loadPlugins(new File(this.main.getDataFolder().getParentFile().getPath()));
            Main.messageManager.respond(sender, MessageLevel.CONFIG
                , "" + pluginManager.getPlugins().length + " plugins loaded all from \"" + this.main.getDataFolder().getParentFile() + "\".");
        } catch (Exception e) {
            Main.messageManager.log(MessageLevel.SEVERE
                , "Error loading all plugins from \"" + this.main.getDataFolder().getParentFile().getPath() + "\"", e);
            Main.messageManager.respond(sender, MessageLevel.SEVERE
                , "Error loading all plugins from \"" + this.main.getDataFolder().getParentFile() + "\"; See log for details.");
        }
        
        for (Plugin p : pluginManager.getPlugins()) {
            this.enablePlugin(pluginManager, p, sender);
        }
    }
    
    private void showUsage(CommandSender sender, String label, String error) {
        Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: " + error);
        this.showUsage(sender, label);
    }
    
    private void showUsage(CommandSender sender, String label) {
        Main.messageManager.respond(sender, MessageLevel.NOTICE, this.main.getCommand(label).getUsage());
    }
}