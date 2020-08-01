package com.janus.model.input.impl;

import com.janus.model.input.EnterAmount;
import com.janus.world.content.skill.impl.crafting.Tanning;
import com.janus.world.entity.impl.player.Player;

public class EnterAmountOfHidesToTan extends EnterAmount {

    private int button;

    public EnterAmountOfHidesToTan(int button) {
        this.button = button;
    }

    @Override
    public void handleAmount(Player player, int amount) {
        Tanning.tanHide(player, button, amount);
    }

}
