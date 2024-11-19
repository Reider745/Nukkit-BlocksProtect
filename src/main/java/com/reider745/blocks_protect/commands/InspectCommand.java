package com.reider745.blocks_protect.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.blocks_protect.MainBlocksProtect;

public class InspectCommand extends Command {
    private final MainBlocksProtect main;

    public InspectCommand(String name, MainBlocksProtect main) {
        super(name, "get information interaction block");
        this.setPermission("block_protect.inspect");
        this.main = main;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender.hasPermission(getPermission()) && sender instanceof Player player){
            final boolean enabled = main.setChangedInspect(player);

            if(enabled) sender.sendMessage("Enabled inspect!");
            else sender.sendMessage("Disabled inspect!");

            return true;
        }
        return false;
    }
}
