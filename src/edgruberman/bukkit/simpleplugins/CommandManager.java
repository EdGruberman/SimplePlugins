package edgruberman.bukkit.simpleplugins;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.simpleplugins.MessageManager.MessageLevel;

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
            if (plugin == null) {
                Main.messageManager.respond(sender, MessageLevel.SEVERE, "Plugin \"" + pluginName + "\" not found.");
                return true;
            }
        }
        
        if (action == null) {
            String message = "";
            for (Plugin loaded : pluginManager.getPlugins()) {
                if (!message.equals("")) message += "\n";
                message += "\"" + loaded.getDescription().getName() + "\""
                    + " v" + loaded.getDescription().getVersion()
                    + " (" + (loaded.isEnabled() ? "Enabled" : "Disabled") + ")";
            }
            Main.messageManager.respond(sender, MessageLevel.CONFIG, message);
            
        } else if (action.equals("enable")) {
            if (pluginManager.isPluginEnabled(pluginName)) {
                Main.messageManager.respond(sender, MessageLevel.WARNING, "Plugin \"" + pluginName + "\" is already enabled.");
                return true;
            }
            
            pluginManager.enablePlugin(plugin);
            Main.messageManager.respond(sender, MessageLevel.CONFIG, "Plugin \"" + pluginName + "\" now enabled.");
            
        } else if (action.equals("disable")) {
            if (pluginName.equals(this.main.getDescription().getName())) {
                Main.messageManager.respond(sender, MessageLevel.WARNING, "You must manually remove this plugin to disable it.");
                return true;
            }
            
            if (!pluginManager.isPluginEnabled(pluginName)) {
                Main.messageManager.respond(sender,  MessageLevel.WARNING, "Plugin \"" + pluginName + "\" is already disabled.");
                return true;
            }
            
            pluginManager.disablePlugin(plugin);
            Main.messageManager.respond(sender, MessageLevel.CONFIG, "Plugin \"" + pluginName + "\" now disabled.");
            
        } else if (action.equals("restart")) {
            if (pluginManager.isPluginEnabled(pluginName)) {
                pluginManager.disablePlugin(plugin);
                Main.messageManager.respond(sender, MessageLevel.CONFIG, "Plugin \"" + pluginName + "\" now disabled.");
            } else {
                Main.messageManager.respond(sender,  MessageLevel.WARNING, "Plugin \"" + pluginName + "\" is not currently enabled.");
            }
            pluginManager.enablePlugin(plugin);
            Main.messageManager.respond(sender, MessageLevel.CONFIG, "Plugin \"" + pluginName + "\" now enabled.");
            
        } else {
            this.showUsage(sender, label, "Unrecognized action.");
        }
        
        return true;
    }
    
    private void showUsage(CommandSender sender, String label, String error) {
        Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: " + error);
        this.showUsage(sender, label);
    }
    
    private void showUsage(CommandSender sender, String label) {
        Main.messageManager.respond(sender, MessageLevel.NOTICE, this.main.getCommand(label).getUsage());
    }
}