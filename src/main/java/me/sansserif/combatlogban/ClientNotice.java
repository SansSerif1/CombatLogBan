package me.sansserif.combatlogban;

import net.fabricmc.api.ClientModInitializer;

public class ClientNotice implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CombatLogBanMod.LOGGER.warn("CombatLogBan initialized! However, this is supposed to be running on a server, so this mod isnt going to do anything in your instance at all.");
    }
}
