package net.minemu.extras;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class PlayerJoinListener implements Listener{

    private Economy economy;
    private Connection connection;
    private FileConfiguration config;

    public PlayerJoinListener(Economy economy, Connection connection, FileConfiguration config)
    {
        this.economy = economy;
        this.connection = connection;
        this.config = config;
    }

    public PlayerJoinListener(Economy economy, Connection connection)
    {
        this.economy = economy;
        this.connection = connection;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) 
    {
        Player player = event.getPlayer();
        double newBalance = economy.getBalance(player);
        double oldBalance = 0;
        boolean skip = false;

        // Leer el balance del jugador desde la tabla "balances"
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT minecoins FROM " + config.getString("mysql.table") + "_asyncmcs WHERE username=?");
            statement.setString(1, player.getName());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                oldBalance = result.getDouble("minecoins");

                // Una vez ya hayamos usado el valor, borramos la fila
                PreparedStatement statement2 = connection.prepareStatement("DELETE FROM " + config.getString("mysql.table") + "_asyncmcs WHERE username=?");
                statement2.setString(1, player.getName());

                statement2.executeUpdate();

            } else {
                // Primera vez que juega con Minemu Extra Async
                skip = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Debug: old: " + oldBalance + " || new: " + newBalance);
        
        if(!skip && oldBalance < newBalance)
        {
            double diff = newBalance - oldBalance;

            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000); // Espera 1 segundo
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Ejecuta el código de forma asíncrona
                    Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Main.class), () -> {
                        economy.depositPlayer(player, diff);
                        player.sendMessage(ChatColor.GREEN + "¡Has ganado: " + ChatColor.GOLD + diff + ChatColor.GREEN + " MC" + " mientras estabas offline!");

                        // Guardamos en la tabla el log de la transacción
                        try{
                            PreparedStatement st = connection.prepareStatement("INSERT INTO " + config.getString("mysql.table") + "_asyncmcs_log (username, oldminecoins, minecoins) VALUES (?, ?, ?);");
                            st.setString(1, player.getName());
                            st.setDouble(2, economy.getBalance(player));
                            st.setDouble(3, diff);

                            st.execute();
                        } catch (SQLException e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
            }.runTaskAsynchronously(JavaPlugin.getPlugin(Main.class));
        }
    }
}
