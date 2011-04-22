package edgruberman.bukkit.simpleplugins;

import edgruberman.bukkit.simpleplugins.MessageManager.MessageLevel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager = null;
       
    private static final String DEFAULT_LOG_LEVEL       = "RIGHTS";
    private static final String DEFAULT_SEND_LEVEL      = "RIGHTS";
    private static final String DEFAULT_BROADCAST_LEVEL = "RIGHTS";
	
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Configuration.load(this);
        
        Main.messageManager.setLogLevel(MessageLevel.parse(      this.getConfiguration().getString("logLevel",       Main.DEFAULT_LOG_LEVEL)));
        Main.messageManager.setSendLevel(MessageLevel.parse(     this.getConfiguration().getString("sendLevel",      Main.DEFAULT_SEND_LEVEL)));
        Main.messageManager.setBroadcastLevel(MessageLevel.parse(this.getConfiguration().getString("broadcastLevel", Main.DEFAULT_BROADCAST_LEVEL)));
        
        this.getCommand("plugin").setExecutor(new CommandManager(this));

        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        this.getCommand("plugin").setExecutor(null);
        
        Main.messageManager.log("Plugin Disabled");
    }
}
