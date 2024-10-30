package com.reider745.coreprotect.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.coreprotect.MainCoreProtect;

public class LedgerCommand extends Command {
    private final MainCoreProtect main;

    public LedgerCommand(String name, MainCoreProtect main) {
        super(name, "get information interaction block");

        this.main = main;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender.isPlayer() || sender.isOp()){
            final boolean enabled = main.setChangedLedger(sender.asPlayer());

            if(enabled) sender.sendMessage("Enabled ledger!");
            else sender.sendMessage("Disabled ledger!");

            return true;
        }
        return false;
    }
}
