package com.reider745.blocks_protect.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.AsyncTask;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.LevelDB;

public class InspectRadiusCommand extends Command {
    private final MainBlocksProtect main;

    public InspectRadiusCommand(String name, MainBlocksProtect main) {
        super(name, "get information for radius");
        this.setPermission("block_protect.inspect_radius");
        this.main = main;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender.hasPermission(getPermission()) && sender instanceof Player player){
            final Position position = player.getPosition();
            final LevelDB levelDB = main.getLevel(player.getLevel());

            sender.getServer().getScheduler().scheduleAsyncTask(main, new AsyncTask() {
                @Override
                public void onRun() {
                    int radius = 5;
                    try{
                        radius = Integer.parseInt(args[0]);
                    }catch (Exception ignore){}

                    final int floorX = position.getFloorX();
                    final int floorY = position.getFloorY();
                    final int floorZ = position.getFloorZ();

                    for(int x = floorX - radius;x < floorX + radius;x++)
                        for(int y = floorY - radius;y < floorY + radius;y++)
                            for(int z = floorZ - radius;z < floorZ + radius;z++)
                                main.message(levelDB, sender, x, y, z);
                }

                @Override
                public void onCompletion(Server server) {
                    sender.sendMessage("§2===Completion get information for radius===");
                }
            });

            return true;
        }
        return false;
    }
}
