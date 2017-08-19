package io.github.rypofalem.antighost;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class AntiGhostPlugin extends JavaPlugin implements Listener, CommandExecutor {
    Map<Player, UpdateTask> map;
    boolean active = true;

    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
        map = new HashMap<>();
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event){
        if(!active) return;
        if(event.getPlayer().isOnGround()) return;
        UpdateTask task = map.get(event.getPlayer());
        try{
            if(task != null) task.cancel();
        } catch (IllegalStateException e){}
        task = new UpdateTask(event.getPlayer(), event.getBlock());
        task.runTaskLater(this, 2);
        map.put(event.getPlayer(), new UpdateTask(event.getPlayer(), event.getBlock()));
    }

    @AllArgsConstructor
    class UpdateTask extends BukkitRunnable {
        Player player;
        Block block;

        @Override
        public void run() {
            map.remove(player);
            if(!player.isOnline()) return;
            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if(args != null && args.length > 0){
            if(args[0].equalsIgnoreCase("toggle")){
                active = !active;
                for(Player p: Bukkit.getOnlinePlayers()){
                    if(p.isOp()){
                        p.sendMessage(String.format("Ghost Block protection is now %s", active ? "active" : "disabled"));
                    }
                }
                return true;
            }
            return false;
        }
        Player player = (Player)sender;
        ItemStack pic = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = pic.getItemMeta();
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        pic.setItemMeta(meta);
        player.getInventory().addItem(pic);
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60000, 2, true, false), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 60000, 1, true, false), true);
        return true;
    }
}
