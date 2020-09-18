package com.janus.world.content.teleport;

import com.janus.model.definitions.NPCDrops;
import com.janus.model.definitions.NpcDefinition;
import com.janus.world.content.KillsTracker;
import com.janus.world.content.transportation.TeleportHandler;
import com.janus.world.entity.impl.player.Player;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TeleportInterface {
    private Categories currentCategory = Categories.MONSTER;
    private TeleportData selectedTeleport, previousTeleport;
    private List<TeleportData> teleportDataList = null;
    public void open() {
        sendDataForCategory(currentCategory);
        player.getPacketSender().sendInterface(38000);
    }

    private void sendDataForCategory(Categories category) {
        teleportDataList = TeleportRepository.filterByCategory(category);
        sendNpcNames(teleportDataList);
        try {
            sendNpc(teleportDataList.get(0));
            selectedTeleport = teleportDataList.get(0);
        } catch(Exception e) {

        }
    }
    private void sendNpc(int index) {
        try {
            selectedTeleport = teleportDataList.get(index);
            /*System.out.println("");
            System.out.println("SendNpc(int index): selectedTeleport = " + selectedTeleport.getName());
            System.out.println("");*/
            sendNpc(selectedTeleport);
        } catch(Exception e) {

        }
    }
    private void sendNpcNames(List<TeleportData> data) {
        AtomicInteger start = new AtomicInteger(38121);

        for(int i = start.get(); i <= 38141; i++) {
            player.getPacketSender().sendString(i, "");
        }
        data.forEach(teleportData -> player.getPacketSender().sendString(start.getAndIncrement(), teleportData.getName()));
    }
    private void sendNpc(TeleportData teleportData) {
        System.out.println("");
        System.out.println("SendNpc(TeleportData): selectedTeleport = " + selectedTeleport.getName());
        System.out.println("");
        String underLine = "";
        switch (currentCategory) {
            case BOSSES:
               underLine= "Current Kills:";
            break;
            case MONSTER:
                KillsTracker.KillsEntry entry = KillsTracker.forId(player, teleportData.getNpcId());
                underLine = entry == null ? "Current Kills: 0" : "Current Kills: " + entry.amount;
                break;
                case SKILLING:
                underLine = "Level:";
                break;
                case MINIGAMES:
                underLine = "Times Completed: ";
                break;
                case WILDERNESS:
                underLine = "PVP Kills: " + player.getPlayerKillingAttributes().getPlayerKills();
                break;
        }
        player.getPacketSender().sendString(38009, underLine);
        for(int i = 0; i < 54; i++) {
            player.getPA().sendItemOnInterfaceInt(38005, -1, i, 0);
        }
        player.getPacketSender().sendString(38008, NpcDefinition.forId(teleportData.getNpcId()).getName());
        player.getPacketSender().sendNpcOnInterface(38011, teleportData.getNpcId());
        List<NPCDrops.NpcDropItem> drops = new ArrayList<>();
        for(NPCDrops.NpcDropItem item : NPCDrops.forId(teleportData.getNpcId()).getDropList()) {
            drops.add(item);
        }
        drops.forEach(drop -> {
            player.getPacketSender().sendItemOnInterfaceInt(38005, drop.getItem().getId(), drops.indexOf(drop), drop.getCount()[drop.getCount().length - 1]);
        });
    }
    public boolean handleButton(int id) {
        if(id >= -27415 && id <= -27395) {
            sendNpc(27415 + id);
            return true;
        }
        if(id >= -27524 && id <= -27519) {
            currentCategory = Categories.values()[27524 + id];
            sendDataForCategory(currentCategory);
            return true;
        }
        if(id == -27534) {
            System.out.println("Currently category = " + currentCategory.name() + " - current teleport = " + selectedTeleport.getName() + " - current teleport location = " + selectedTeleport.getLocation().toString());
            TeleportHandler.teleportPlayer(player, selectedTeleport.getLocation(), player.getSpellbook().getTeleportType());
            previousTeleport = selectedTeleport;
            return true;
        }
        if(id == -27533) {
            TeleportHandler.teleportPlayer(player, previousTeleport.getLocation(), player.getSpellbook().getTeleportType());
            return true;
        }
        return false;
    }
    private final Player player;
}
