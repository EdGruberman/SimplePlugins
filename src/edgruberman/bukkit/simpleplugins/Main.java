package edgruberman.bukkit.simpleplugins;

import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager = null;
	
    public void onLoad() {
        Configuration.load(this);
    }
    
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
                
        this.getCommand("plugin").setExecutor(new CommandManager(this));

        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        this.getCommand("plugin").setExecutor(null);
        
        Main.messageManager.log("Plugin Disabled");
    }
}
