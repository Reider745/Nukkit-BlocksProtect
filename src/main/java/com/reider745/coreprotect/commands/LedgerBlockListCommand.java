package com.reider745.coreprotect.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.scheduler.AsyncTask;
import com.reider745.coreprotect.MainCoreProtect;
import com.reider745.coreprotect.api.AreaDB;
import com.reider745.coreprotect.api.LevelDB;
import com.reider745.coreprotect.api.description.BaseBlockInfo;

public class LedgerBlockListCommand extends Command {
    private final MainCoreProtect main;

    public LedgerBlockListCommand(String name, MainCoreProtect main) {
        super(name, "print list use blocks");

        this.main = main;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender.isOp()){
            sender.getServer().getScheduler().scheduleAsyncTask(main, new AsyncTask() {
                @Override
                public void onRun() {
                    final LevelDB level = main.getLevel(sender.getLocation().level);

                    for(int x = -1087;x < -1014;x+=AreaDB.SIZE){
                        for(int z = -563;z < -471;z+=AreaDB.SIZE){
                            for (BaseBlockInfo info : level.getAreaAt(x, z).getAllInteractions())
                                sender.sendMessage(info.toMessage());
                        }
                    }
                }

                @Override
                public void onCompletion(Server server) {
                    System.out.println("end");
                    super.onCompletion(server);
                }
            });
            return true;
        }

        return false;
    }
}
