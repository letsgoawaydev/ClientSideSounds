package com.letsgoaway.clientsidesounds;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

// Command (/bps)
public class BPSCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "bps";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/bps";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        if (strings.length == 0) {
            if (ClientSideSounds.enabled && !ClientSideSounds.local) {
                iCommandSender.addChatMessage(
                        new ChatComponentText("Client Side Sounds are currently enabled. Do \"/bps toggle\" to disable it."));
            } else {
                iCommandSender.addChatMessage(
                        new ChatComponentText("Client Side Sounds are currently disabled. Do \"/bps toggle\" to enable it."));
            }
        } else if (strings[0].equals("toggle")) {
            if (ClientSideSounds.local) {
                iCommandSender.addChatMessage(new ChatComponentText("You don't need it on a local server."));
            } else {

                iCommandSender.addChatMessage(new ChatComponentText(
                        "Client Side Sounds are now " +
                                (ClientSideSounds.enabled ? "enabled" : "disabled")
                                + ", log out of current server for the toggle to take effect."));
                ClientSideSounds.enabled = !ClientSideSounds.enabled;
            }
        } else {
            iCommandSender.addChatMessage(new ChatComponentText(
                    "Unknown command, do \"/bps\" to check current toggle state, \"/bps toggle\" to toggle it."));
        }
    }
}