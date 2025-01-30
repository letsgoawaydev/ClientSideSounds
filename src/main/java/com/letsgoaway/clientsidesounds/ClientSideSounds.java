/*
 * Client Side Block Placing Sound Mod
 * By asdfcube
 * */

package com.letsgoaway.clientsidesounds;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(modid = "clientsidesounds", version = "1", clientSideOnly = true)
public class ClientSideSounds {
    public static final ExecutorService executor = Executors.newFixedThreadPool(15);
    public static boolean enabled = true;
    // Switched from CopyOnWriteArrayList to ArrayList for performance even
    // exceptions might occur in rare cases

    private boolean inited = false;
    public static boolean local;
    // Multithreading B)



    // Initialize the mod, which only consists of a few event listeners and one
    // singular command
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new BPSCommand());
    }


    // Triggered when you join a server to initialize the packet handlers
    @SubscribeEvent
    public void ServerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Check if BPS is enabled
        local = event.isLocal;
        if (enabled) {
            // You don't need this on local server
            if (!local) {
                PacketHandler.blocks.clear();
                // Create a netty pipeline handler
                PacketHandler handler = new PacketHandler();
                // Register the handler before the Minecraft handler so that some packets can be 7ignored
                event.manager.channel().pipeline().addBefore("packet_handler", "asdfInHandler", handler);
                // Register the handler after the Minecraft handler to play sound
                event.manager.channel().pipeline().addAfter("packet_handler", "asdfOutHandler", handler);
                inited = true;
            } else {
                executor.execute(() -> {
                    while (Minecraft.getMinecraft().thePlayer == null)
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(
                            new ChatComponentText("BPS is automatically disabled on local servers."));
                });
            }
        } else {
            executor.execute(() -> {
                while (Minecraft.getMinecraft().thePlayer == null)
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(
                        new ChatComponentText("BPS was toggled off, do \"/bps toggle\" to toggle it back on."));
            });
        }
    }

    // Triggered when you leave a server to remove the packet handler
    @SubscribeEvent
    public void ServerQuitEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (inited) {
            inited = false;
            Channel channel = event.manager.channel();
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove("asdfInHandler");
                channel.pipeline().remove("asdfOutHandler");
                return null;
            });
        }
    }
}