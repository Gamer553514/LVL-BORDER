package de.gamer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class LVLBorder implements ModInitializer {
	public static final HashMap<UUID, Integer> playerXpMap = new HashMap<>();

	public static final String MOD_ID = "lvl--border";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("LVLBorder is starting...");

		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> adjustWorldBorder(minecraftServer));

		ServerPlayConnectionEvents.JOIN.register(((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
			ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
			playerXpMap.put(player.getUuid(), player.experienceLevel);
		}));

		ServerPlayConnectionEvents.DISCONNECT.register(((serverPlayNetworkHandler, minecraftServer) -> {
			playerXpMap.remove(serverPlayNetworkHandler.getPlayer().getUuid());
		}));
	}

	private void adjustWorldBorder(MinecraftServer server) {
		WorldBorder worldBorder = server.getOverworld().getWorldBorder();

		int TotalXP = server.getPlayerManager().getPlayerList().stream()
				.mapToInt(player ->{
					int currentxp = player.experienceLevel;
					playerXpMap.put(player.getUuid(), currentxp);
					return currentxp;
				})
				.sum();

		double newBorderSize = Math.max(1, TotalXP);
		if (worldBorder.getSize() != newBorderSize) {
			worldBorder.interpolateSize(worldBorder.getSize(), newBorderSize, 1000);
		}

		worldBorder.setCenter(0,0);
	}
}