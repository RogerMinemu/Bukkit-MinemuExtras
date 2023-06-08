package net.minemu.extras;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{

    private Economy economy;
    private Connection connection;
    public FileConfiguration config;

    public void onEnable()
    {
        // Config Load
        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        // Economy Load
        if (!setupEconomy()) {
            getLogger().severe("No se pudo encontrar un proveedor de economía compatible con Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Database Load
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (SQLException e) {
            getLogger().severe("No se pudo establecer la conexión a la base de datos.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        // Success Loading
        System.out.println("Módulo: Async Minecoins CARGADO - Vault");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(economy, connection, config), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(economy, connection, config), this); 
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public void onDisable() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().severe("No se pudo cerrar la conexión a la base de datos.");
        }
    }
}