package net.minemu.extras;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    @EventHandler
    public void blockBreak(BlockBreakEvent event)
    {
        System.out.println("Block broke");
    }
    
}
