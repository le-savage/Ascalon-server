package com.janus.world.content.combat.tieredbosses;

import com.janus.model.Position;
import com.janus.world.World;
import com.janus.world.entity.impl.npc.NPC;
import com.janus.world.entity.impl.player.Player;

public class KBDFight {


    public static void StartKBDFight(Player player) {

        LevelSetups.LevelOneSetup lvl1 = LevelSetups.LevelOneSetup.KBD;
        LevelSetups.LevelTwoSetup lvl2 = LevelSetups.LevelTwoSetup.KBD;
        LevelSetups.LevelThreeSetup lvl3 = LevelSetups.LevelThreeSetup.KBD;
        LevelSetups.LevelFourSetup lvl4 = LevelSetups.LevelFourSetup.KBD;
        LevelSetups.LevelFiveSetup lvl5 = LevelSetups.LevelFiveSetup.KBD;

        int[] stats1 = {lvl1.getAttack(), lvl1.getDefence(), lvl1.getStrength(), lvl1.getRanged(), lvl1.getMagic(), lvl1.getConstitution(), lvl1.getPrayer()};
        int[] stats2 = {lvl2.getAttack(), lvl2.getDefence(), lvl2.getStrength(), lvl2.getRanged(), lvl2.getMagic(), lvl2.getConstitution(), lvl2.getPrayer()};
        int[] stats3 = {lvl3.getAttack(), lvl3.getDefence(), lvl3.getStrength(), lvl3.getRanged(), lvl3.getMagic(), lvl3.getConstitution(), lvl3.getPrayer()};
        int[] stats4 = {lvl4.getAttack(), lvl4.getDefence(), lvl4.getStrength(), lvl4.getRanged(), lvl4.getMagic(), lvl4.getConstitution(), lvl4.getPrayer()};
        int[] stats5 = {lvl5.getAttack(), lvl5.getDefence(), lvl5.getStrength(), lvl5.getRanged(), lvl5.getMagic(), lvl5.getConstitution(), lvl5.getPrayer()};


        EquipmentSetups gear1 = EquipmentSetups.ONE;
        EquipmentSetups gear2 = EquipmentSetups.TWO;
        EquipmentSetups gear3 = EquipmentSetups.THREE;
        EquipmentSetups gear4 = EquipmentSetups.FOUR;
        EquipmentSetups gear5 = EquipmentSetups.FIVE;

        int[] gearOne = {gear1.getWeapon(), gear1.getShield(), gear1.getHelm(), gear1.getBody(), gear1.getLegs(), gear1.getNeck(), gear1.getCape(), gear1.getHands(), gear1.getFeet()};
        int[] gearTwo = {gear2.getWeapon(), gear2.getShield(), gear2.getHelm(), gear2.getBody(), gear2.getLegs(), gear2.getNeck(), gear2.getCape(), gear2.getHands(), gear2.getFeet()};
        int[] gearThree = {gear3.getWeapon(), gear3.getShield(), gear3.getHelm(), gear3.getBody(), gear3.getLegs(), gear3.getNeck(), gear3.getCape(), gear3.getHands(), gear3.getFeet()};
        int[] gearFour = {gear4.getWeapon(), gear4.getShield(), gear4.getHelm(), gear4.getBody(), gear4.getLegs(), gear4.getNeck(), gear4.getCape(), gear4.getHands(), gear4.getFeet()};
        int[] gearFive = {gear5.getWeapon(), gear5.getShield(), gear5.getHelm(), gear5.getBody(), gear5.getLegs(), gear5.getNeck(), gear5.getCape(), gear5.getHands(), gear5.getFeet()};


        int x = BossFunctions.ARENA_CENTRE.getX();
        int y = BossFunctions.ARENA_CENTRE.getY();
        int z = player.getPosition().getZ();

        NPC level1 = new NPC(BossData.KING_BLACK_DRAGON.getLevel1ID(), new Position(x, y, z)).setSpawnedFor(player);
        NPC level2 = new NPC(BossData.KING_BLACK_DRAGON.getLevel2ID(), new Position(x, y, z)).setSpawnedFor(player);
        NPC level3 = new NPC(BossData.KING_BLACK_DRAGON.getLevel3ID(), new Position(x, y, z)).setSpawnedFor(player);
        NPC level4 = new NPC(BossData.KING_BLACK_DRAGON.getLevel4ID(), new Position(x, y, z)).setSpawnedFor(player);
        NPC level5 = new NPC(BossData.KING_BLACK_DRAGON.getLevel5ID(), new Position(x, y, z)).setSpawnedFor(player);


        switch (player.getKbdTier()) {
            case 0:
                BossFunctions.setNewStats(player, stats1[0], stats1[1], stats1[2], stats1[3], stats1[4], stats1[5], stats1[6]);
                BossFunctions.setEquipment(player, gearOne[0], gearOne[1], gearOne[2], gearOne[3], gearOne[4], gearOne[5], gearOne[6], gearOne[7], gearOne[8]);
                World.register(level1);
                player.getRegionInstance().getNpcsList().add(level1);
                break;

            case 1:
                BossFunctions.setNewStats(player, stats2[0], stats2[1], stats2[2], stats2[3], stats2[4], stats2[5], stats2[6]);
                BossFunctions.setEquipment(player, gearTwo[0], gearTwo[1], gearTwo[2], gearTwo[3], gearTwo[4], gearTwo[5], gearTwo[6], gearTwo[7], gearTwo[8]);
                World.register(level2);
                player.getRegionInstance().getNpcsList().add(level2);
                break;

            case 2:
                BossFunctions.setNewStats(player, stats3[0], stats3[1], stats3[2], stats3[3], stats3[4], stats3[5], stats3[6]);
                BossFunctions.setEquipment(player, gearThree[0], gearThree[1], gearThree[2], gearThree[3], gearThree[4], gearThree[5], gearThree[6], gearThree[7], gearThree[8]);
                World.register(level3);
                player.getRegionInstance().getNpcsList().add(level3);
                break;

            case 3:
                BossFunctions.setNewStats(player, stats4[0], stats4[1], stats4[2], stats4[3], stats4[4], stats4[5], stats4[6]);
                BossFunctions.setEquipment(player, gearFour[0], gearFour[1], gearFour[2], gearFour[3], gearFour[4], gearFour[5], gearFour[6], gearFour[7], gearFour[8]);
                World.register(level4);
                player.getRegionInstance().getNpcsList().add(level4);
                break;

            case 4:
                BossFunctions.setNewStats(player, stats5[0], stats5[1], stats5[2], stats5[3], stats5[4], stats5[5], stats5[6]);
                BossFunctions.setEquipment(player, gearFive[0], gearFive[1], gearFive[2], gearFive[3], gearFive[4], gearFive[5], gearFive[6], gearFive[7], gearFive[8]);
                World.register(level5);
                player.getRegionInstance().getNpcsList().add(level5);
                break;
        }
        GiveInventoryItems.giveItems(player);
    }
}
