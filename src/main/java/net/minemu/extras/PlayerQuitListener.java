package net.minemu.extras;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private Economy economy;
    private Connection connection;

    public PlayerQuitListener(Economy economy, Connection connection) {
        this.economy = economy;
        this.connection = connection;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        double balance = economy.getBalance(player);

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO survival_asyncmcs (username, minecoins) VALUES (?, ?);");
            statement.setString(1, player.getName());
            statement.setDouble(2, balance);

            statement.executeUpdate();
            System.out.println("Saved last Minecoins for " + player.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}