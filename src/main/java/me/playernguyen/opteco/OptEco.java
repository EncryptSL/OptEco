package me.playernguyen.opteco;

import me.playernguyen.opteco.account.IAccountManager;
import me.playernguyen.opteco.account.mysql.MySQLAccountDatabase;
import me.playernguyen.opteco.account.sqlite.SQLiteAccountDatabase;
import me.playernguyen.opteco.bStats.Metrics;
import me.playernguyen.opteco.bossshoppro.OptEcoBossShopPro;
import me.playernguyen.opteco.command.CommandManager;
import me.playernguyen.opteco.command.OptEcoCommand;
import me.playernguyen.opteco.configuration.OptEcoConfigurationLoader;
import me.playernguyen.opteco.configuration.OptEcoLanguageLoader;
import me.playernguyen.opteco.configuration.StorageType;
import me.playernguyen.opteco.listener.ListenerManager;
import me.playernguyen.opteco.listener.OptEcoListener;
import me.playernguyen.opteco.listener.OptEcoPlayerJoinListener;
import me.playernguyen.opteco.logger.Debugger;
import me.playernguyen.opteco.logger.OptEcoDebugger;
import me.playernguyen.opteco.manager.ManagerSet;
import me.playernguyen.opteco.placeholderapi.OptEcoExpansion;
import me.playernguyen.opteco.transaction.TransactionManager;
import me.playernguyen.opteco.updater.OptEcoUpdater;
import me.playernguyen.opteco.utils.MessageFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Main class of OptEco plugin
 */
public class OptEco extends JavaPlugin {

    public static OptEco instance;

    public static final String PLUGIN_NAME = "OptEco";
    private static final String UPDATE_ID = "76179";
    private static final int METRICS_ID = 6793;

    private final Logger logger = this.getLogger();

    private ListenerManager listenerManager;
    private CommandManager commandManager;
    private boolean isHookPlaceholder;
    private OptEcoConfigurationLoader optEcoConfigurationLoader;
    private OptEcoLanguageLoader optEcoLanguageLoader;
    private IAccountManager accountManager;
    private StorageType storageType;
    private Debugger debugger;
    private MessageFormat messageFormat;
    private TransactionManager transactionManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        this.setupInstance();
        this.setupLoader();
        this.setupManager();
        this.setupUpdater();
        this.setupStorage();
        this.setupAccount();
        this.hookPlaceHolderAPI();
        this.hookBossShopPro();
        this.setupMetric();
        this.waterMarkPrint();
    }

    private void hookBossShopPro() {
        // Find BossShopPro
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BossShopPro");
        // Whether found BossShopPro
        if (plugin != null) {
            // Announce to user
            logger.info("[Hooker] Found BossShopPro ~ v"
                    + plugin.getDescription().getVersion() + ". Creating hook. Please make sure you has " +
                    "config this (PointsPlugin: 'OptEco')");
            // Register API
            new OptEcoBossShopPro(this).register();
        }
    }

    @Override
    public void onDisable() {
        // Unregister listen
        listenerManager.unregisterAll();
        // Command manager
        commandManager.getContainer().clear();
    }

    private void setupMetric() {
        this.metrics = new Metrics(getPlugin(), METRICS_ID);
        // Create pies
        getMetrics().addCustomChart(new Metrics.SimplePie(
                "storage_option_type",
                () -> getStorageType().toString())
        );
    }

    private void setupInstance() {
        instance = this;
    }

    private void setupManager() {
        // Setting debugger
        this.debugger = new OptEcoDebugger(this);
        // Setting message format
        this.messageFormat = new MessageFormat();
    }

    private void setupLoader() {
        // Configuration Loader
        this.optEcoConfigurationLoader = new OptEcoConfigurationLoader();
        // Language Loader
        this.optEcoLanguageLoader = new OptEcoLanguageLoader(getConfigurationLoader().getString(OptEcoConfiguration.LANGUAGE_FILE));
    }

    private void setupAccount() {
        this.logger.info("Setup storage and accounts...");
        this.registerAccountManager();
        this.registerListener();
        this.registerExecutors();
        this.registerTransaction();
    }

    private void registerTransaction() {
        // Setting transaction manager
        this.transactionManager = new TransactionManager();
    }

    private void waterMarkPrint() {
        logger.info("Succeed setup OptEco v." + this.getDescription().getVersion() + " on your server");
        ArrayList<String> waterMarks = new ArrayList<>();
        waterMarks.add("                        ");
        waterMarks.add(" ___     ___   _______  ");
        waterMarks.add("|   |   |    )    |     " + ChatColor.DARK_GRAY + "Support Bukkit - Spigot - PaperMC");
        waterMarks.add("|   |   |---/     |     " + ChatColor.DARK_GRAY + "__________ ");
        waterMarks.add("|___/   |         |     Eco" + ChatColor.RED + " v" + getDescription().getVersion());
        waterMarks.add("                     ");
        for (String waterMark : waterMarks) {
            getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + waterMark);
        }
        waterMarks.clear();
    }

    private void hookPlaceHolderAPI() {
        this.isHookPlaceholder = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (isHookPlaceholder()) {
            this.logger.info("[Hooker] Detected PlaceholderAPI!");
            this.logger.info("[Hooker] Hooking with PlaceholderAPI...");
            this.logger.info("[Hooker] Register parameters with PlaceholderAPI...");
            OptEcoExpansion expansion = new OptEcoExpansion(this);

            this.logger.info("[Hooker] Succeed PlaceholderAPI hook!");
            expansion.register();
        }
    }

    public boolean isHookPlaceholder() {
        return isHookPlaceholder;
    }

    /**
     * Metrics class (bStats)
     *
     * @return the {@link Metrics} class
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Setting to the updater
     */
    private void setupUpdater() {
        if (getConfigurationLoader().getBool(OptEcoConfiguration.CHECK_FOR_UPDATE)) {
            this.checkForUpdates();
        }
    }

    /**
     * @return the instance of plugin
     * @deprecated use {@link #getInstance()} instead of this
     */
    public static OptEco getPlugin() {
        return (OptEco) Bukkit.getServer().getPluginManager().getPlugin(PLUGIN_NAME);
    }

    public OptEcoConfigurationLoader getConfigurationLoader() {
        return optEcoConfigurationLoader;
    }

    public OptEcoLanguageLoader getLanguageLoader() {
        return optEcoLanguageLoader;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Perform check for update. Using spigot resource file
     */
    private void checkForUpdates() {
        OptEcoUpdater updater = new OptEcoUpdater(Integer.parseInt(UPDATE_ID));
        updater.getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Bukkit.getConsoleSender()
                        .sendMessage(String.format(
                                ChatColor.YELLOW +
                                        "Detected new update (%s), download at https://www.spigotmc.org/resources/76179",
                                version)
                        );
            } else {
                logger.fine("Nothing to update!");
            }
        });
    }

    private void setupStorage() {
        logger.info("Loading storage type.");
        this.storageType = StorageType.valueOf(getConfigurationLoader().getString(OptEcoConfiguration.STORAGE_TYPE));
        logger.info(String.format("Current storage type: %s", storageType.name().toLowerCase()));
    }

    private void registerAccountManager() {
        switch (storageType) {
            case SQLITE: {
                this.accountManager = new SQLiteAccountDatabase();
                break;
            }
            case MYSQL: {
                this.accountManager = new MySQLAccountDatabase();
                break;
            }
        }
    }

    private void registerListener() {
        // Initial the class
        logger.info("Loading listeners.");
        this.listenerManager = new ListenerManager(this);
        // Listener adding here...
        getListenerManager().add(new OptEcoPlayerJoinListener());
    }

    private void registerExecutors() {
        // Initial the command manager
        logger.info("Loading commands.");
        this.commandManager = new CommandManager();
        // Append new commands
        getCommandManager().add(new OptEcoCommand());
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public static OptEco getInstance() {
        return instance;
    }

    public IAccountManager getAccountManager() {
        return accountManager;
    }

    public ManagerSet<OptEcoListener> getListenerManager() {
        return listenerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
