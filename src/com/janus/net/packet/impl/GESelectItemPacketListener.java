package com.janus.net.packet.impl;

import com.janus.model.Item;
import com.janus.model.definitions.ItemDefinition;
import com.janus.net.packet.Packet;
import com.janus.net.packet.PacketListener;
import com.janus.world.content.grandexchange.GrandExchange;
import com.janus.world.entity.impl.player.Player;

public class GESelectItemPacketListener implements PacketListener {

    @Override
    public void handleMessage(Player player, Packet packet) {
        int item = packet.readShort();
        if (item <= 0 || item >= ItemDefinition.getMaxAmountOfItems())
            return;
        ItemDefinition def = ItemDefinition.forId(item);
        if (def != null) {
            if (def.getValue() <= 0 || !Item.tradeable(item) || item == 995) {
                player.getPacketSender().sendMessage("This item can currently not be purchased or sold in the Grand Exchange.");
                return;
            }
            GrandExchange.setSelectedItem(player, item);
        }
    }

}
