package com.janus.net.packet.impl;

import com.janus.GameLoader;
import com.janus.GameServer;
import com.janus.GameSettings;
import com.janus.engine.task.Task;
import com.janus.engine.task.TaskManager;
import com.janus.engine.task.impl.PlayerDeathTask;
import com.janus.engine.task.impl.PoisonImmunityTask;
import com.janus.model.*;
import com.janus.model.Locations.Location;
import com.janus.model.container.impl.Bank;
import com.janus.model.container.impl.Equipment;
import com.janus.model.container.impl.Shop.ShopManager;
import com.janus.model.definitions.*;
import com.janus.net.packet.Packet;
import com.janus.net.packet.PacketListener;
import com.janus.net.security.ConnectionHandler;
import com.janus.util.Misc;
import com.janus.util.NameUtils;
import com.janus.util.RandomUtility;
import com.janus.util.playerSavingTimer;
import com.janus.world.World;
import com.janus.world.content.*;
import com.janus.world.content.PlayerPunishment.Jail;
import com.janus.world.content.clan.ClanChatManager;
import com.janus.world.content.combat.CombatFactory;
import com.janus.world.content.combat.CombatFormulas;
import com.janus.world.content.combat.DailyNPCTask;
import com.janus.world.content.combat.effect.CombatPoisonEffect;
import com.janus.world.content.combat.instancearena.InstanceArena;
import com.janus.world.content.combat.magic.Autocasting;
import com.janus.world.content.combat.prayer.CurseHandler;
import com.janus.world.content.combat.prayer.PrayerHandler;
import com.janus.world.content.combat.strategy.CombatStrategies;
import com.janus.world.content.combat.bossminigame.BossMinigameFunctions;
import com.janus.world.content.combat.weapon.CombatSpecial;
import com.janus.world.content.discord.DiscordMessenger;
import com.janus.world.content.grandexchange.GrandExchangeOffers;
import com.janus.world.content.minigames.impl.FreeForAll;
import com.janus.world.content.skill.SkillManager;
import com.janus.world.content.transportation.TeleportHandler;
import com.janus.world.content.transportation.TeleportType;
import com.janus.world.entity.impl.npc.NPC;
import com.janus.world.entity.impl.player.Player;
import com.janus.world.entity.impl.player.PlayerHandler;
import com.janus.world.entity.impl.player.PlayerSaving;
import mysql.MySQLController;


/**
 * This packet listener manages commands a player uses by using the command
 * console prompted by using the "`" char.
 *
 * @author Gabriel Hannason
 */

public class CommandPacketListener implements PacketListener {

    private static final String[] admin = {"admin", "administrator", "a d m i n"};
    private static final String[] mod = {"mod", "moderator", "m o d"};
    public static int config;

    private static void playerCommands(final Player player, String[] command, String wholeCommand) {

        if (command[0].contains("bug")) {
            String bugReport = (wholeCommand.substring(command[0].length() + 1)+" ");
            //String location = ("Loc: "+player.getPosition());
            System.out.println(" Bug : "+bugReport);
            DiscordMessenger.sendBug(bugReport,player);
        }


        if (command[0].startsWith("boss")) {
            TeleportHandler.teleportPlayer(player, BossMinigameFunctions.DOOR, player.getSpellbook().getTeleportType());
        }


        if (command[0].startsWith("resettier")) {
            BossMinigameFunctions.resetProgress(player);
        }


        if (command[0].equalsIgnoreCase("index")) {
            player.forceChat("My index number is: " + player.getIndex());
        }



        if (command[0].equalsIgnoreCase("notifications")) {
            if (player.getNotificationPreference()) {
                player.getPacketSender().sendMessage("@red@Notifications OFF");
                player.setNotificationPreference(false);
            } else {
                player.getPacketSender().sendMessage("@gre@Notifications ON");
                player.setNotificationPreference(true);
            }
        }

        if ((command[0].contains("instance")) || (command[0].equalsIgnoreCase("crazyman")) || (command[0].equalsIgnoreCase("instancekbd"))) {
            player.getPacketSender().sendRichPresenceState("Instance Arena");
            player.getPacketSender().sendSmallImageKey("attack");
            player.getPacketSender().sendRichPresenceSmallPictureText("Combat Lvl: " + player.getSkillManager().getCombatLevel());
            TeleportHandler.teleportPlayer(player, GameSettings.INSTANCE_ARENA.copy(), player.getSpellbook().getTeleportType());
        }
        if (command[0].equalsIgnoreCase("exit") && (player.getLocation() == Location.INSTANCE_ARENA)) {
            InstanceArena.destructArena(player);
        }

		/*if (player.getRights() == PlayerRights.YOUTUBER) {
			if (command[0].equalsIgnoreCase("youtuber")) {
				player.setTitle("@red@YouTuber");
				player.getUpdateFlag().flag(Flag.APPEARANCE);
			} else {
				player.getPacketSender().sendMessage("You need the YouTuber Rank to obtain this title");
			}
		}*/

        final int xpForMasterCape = 500000000;

        if (command[0].startsWith("masterhp")) {
            if (player.getSkillManager().getExperience(Skill.CONSTITUTION) > xpForMasterCape) {
                player.getInventory().add(21001, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m HP XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masteragility")) {
            if (player.getSkillManager().getExperience(Skill.AGILITY) > xpForMasterCape) {
                player.getInventory().add(21002, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Agility XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterattack")) {
            if (player.getSkillManager().getExperience(Skill.ATTACK) > xpForMasterCape) {
                player.getInventory().add(21003, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Attack XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterconstruction")) {
            if (player.getSkillManager().getExperience(Skill.CONSTRUCTION) > xpForMasterCape) {
                player.getInventory().add(21004, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Construction XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastercooking")) {
            if (player.getSkillManager().getExperience(Skill.COOKING) > xpForMasterCape) {
                player.getInventory().add(21005, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Cooking XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastercrafting")) {
            if (player.getSkillManager().getExperience(Skill.CRAFTING) > xpForMasterCape) {
                player.getInventory().add(21006, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Crafting XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterdefence")) {
            if (player.getSkillManager().getExperience(Skill.DEFENCE) > xpForMasterCape) {
                player.getInventory().add(21007, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Defence XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterfarming")) {
            if (player.getSkillManager().getExperience(Skill.FARMING) > xpForMasterCape) {
                player.getInventory().add(21008, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Farming XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterfiremaking")) {
            if (player.getSkillManager().getExperience(Skill.FIREMAKING) > xpForMasterCape) {
                player.getInventory().add(21009, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Firemaking XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterfishing")) {
            if (player.getSkillManager().getExperience(Skill.FISHING) > xpForMasterCape) {
                player.getInventory().add(21010, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Fishing XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterfletching")) {
            if (player.getSkillManager().getExperience(Skill.FLETCHING) > xpForMasterCape) {
                player.getInventory().add(21011, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Fletching XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterherblore")) {
            if (player.getSkillManager().getExperience(Skill.HERBLORE) > xpForMasterCape) {
                player.getInventory().add(21012, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Herblore XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterhunter")) {
            if (player.getSkillManager().getExperience(Skill.HUNTER) > xpForMasterCape) {
                player.getInventory().add(21013, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Hunter XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastermagic")) {
            if (player.getSkillManager().getExperience(Skill.MAGIC) > xpForMasterCape) {
                player.getInventory().add(21014, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Magic XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastermining")) {
            if (player.getSkillManager().getExperience(Skill.MINING) > xpForMasterCape) {
                player.getInventory().add(21015, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Mining XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterprayer")) {
            if (player.getSkillManager().getExperience(Skill.PRAYER) > xpForMasterCape) {
                player.getInventory().add(21016, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Prayer XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterrange")) {
            if (player.getSkillManager().getExperience(Skill.RANGED) > xpForMasterCape) {
                player.getInventory().add(21018, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Ranged XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterrunecrafting")) {
            if (player.getSkillManager().getExperience(Skill.RUNECRAFTING) > xpForMasterCape) {
                player.getInventory().add(21019, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Crafting XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterslayer")) {
            if (player.getSkillManager().getExperience(Skill.SLAYER) > xpForMasterCape) {
                player.getInventory().add(21020, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Slayer XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastersmithing")) {
            if (player.getSkillManager().getExperience(Skill.SMITHING) > xpForMasterCape) {
                player.getInventory().add(21021, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Smithing XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterstrength")) {
            if (player.getSkillManager().getExperience(Skill.STRENGTH) > xpForMasterCape) {
                player.getInventory().add(21022, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Strength XP to claim this cape.");
            }
        }

        if (command[0].startsWith("mastersummoning")) {
            if (player.getSkillManager().getExperience(Skill.SUMMONING) > xpForMasterCape) {
                player.getInventory().add(21023, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Summoning XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterthieving")) {
            if (player.getSkillManager().getExperience(Skill.THIEVING) > xpForMasterCape) {
                player.getInventory().add(21024, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Thieving XP to claim this cape.");
            }
        }

        if (command[0].startsWith("masterwoodcutting")) {
            if (player.getSkillManager().getExperience(Skill.WOODCUTTING) > xpForMasterCape) {
                player.getInventory().add(21025, 1);
            } else {
                player.getPacketSender().sendMessage("You need 500m Woodcutting XP to claim this cape.");
            }
        }

        if (command[0].equalsIgnoreCase("afk") || (command[0].equalsIgnoreCase("afkzone"))) {
            player.getPacketSender().sendRichPresenceState("AFK Zone");
            player.getPacketSender().sendSmallImageKey("afk");
            TeleportHandler.teleportPlayer(player, new Position(2730, 5329), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("drops")) {//newdroptable
            player.getPacketSender().sendMessage("Opening drops interface...");
            DropsInterface.open(player);
        }


        /*if (command[0].startsWith("reward") || (command[0].equalsIgnoreCase("voted")) || (command[0].equalsIgnoreCase("claimvote"))) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.getPacketSender().sendMessage("You need an inventory space free claim votes!");
            }
            new Thread(new FoxVoting.FoxVote(player)).start();


        }*/

        if (command[0].startsWith("reward") || command[0].startsWith("voted")) {
            if (command.length == 1) {
                player.getPacketSender().sendMessage("Please use [::reward], or [::voted].");
                return;
            }
            final String playerName = player.getUsername();
            final String id = command[1];
            final String amount = command.length == 3 ? command[2] : "1";

            com.everythingrs.vote.Vote.service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        com.everythingrs.vote.Vote[] reward = com.everythingrs.vote.Vote.reward("g8zvew8JyhebjWsePC1MFH6jSF3RqdC67YOUx76fmZkCJCWNhU0uijTJ8SDiP6tZwNYfg1p1",
                                playerName, id, "all"); //Used to say amount but now we only have 1 reward
                        if (reward[0].message != null) {
                            player.getPacketSender().sendMessage(reward[0].message);
                            return;
                        }
                        player.getInventory().add(reward[0].reward_id, reward[0].give_amount);
                        if (GameLoader.getDay() == GameLoader.MONDAY){

                        }
                        player.getPacketSender().sendMessage("Thank you for voting!");
                    } catch (Exception e) {
                        player.getPacketSender().sendMessage("Api Services are currently offline. Please check back shortly");
                        e.printStackTrace();
                    }
                }

            });
        }

        if (command[0].startsWith("claim") || command[0].equalsIgnoreCase("donated")) {
            new java.lang.Thread() {
                public void run() {
                    try {
                        com.everythingrs.donate.Donation[] donations = com.everythingrs.donate.Donation.donations("g8zvew8JyhebjWsePC1MFH6jSF3RqdC67YOUx76fmZkCJCWNhU0uijTJ8SDiP6tZwNYfg1p1",
                                player.getUsername());
                        if (donations.length == 0) {
                            player.getPacketSender().sendMessage("You currently don't have any items waiting. You must donate first!");
                            return;
                        }
                        if (donations[0].message != null) {
                            player.getPacketSender().sendMessage(donations[0].message);
                            return;
                        }
                        if (player.getInventory().getFreeSlots() <= 3) {
                            player.getPacketSender().sendMessage("Please try again when you have 3 slots free");
                            return;
                        }
                        for (com.everythingrs.donate.Donation donate: donations) {
                            player.getInventory().add(new Item(donate.product_id, donate.product_amount));
                        }
                        player.getPacketSender().sendMessage("Thank you for donating!");
                    } catch (Exception e) {
                        player.getPacketSender().sendMessage("Api Services are currently offline. Please check back shortly");
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        /*if (command[0].startsWith("claim") || (command[0].equalsIgnoreCase("donated"))) {
            if (player.getInventory().getFreeSlots() < 3) {
                player.getPacketSender().sendMessage("You need three inventory spaces to claim donations!");
            }
            new Thread(new FoxDonating(player)).start();

        }*/




        if (command[0].equalsIgnoreCase("ffa")) {
            FreeForAll.enterLobby(player);
        }
        if (command[0].equals("players")) {
            String playersOnlineMessage = ("There are currently " + World.getPlayers().size() + " players online!");
            //player.getPacketSender().sendMessage(playersOnlineMessage);
            player.forceChat(playersOnlineMessage);
        }


        if (wholeCommand.startsWith("profile")) {
            final String[] s = wholeCommand.split(" ");
            if (s.length < 2) {
                ProfileViewing.view(player, player);
                return;
            }
            final String name = wholeCommand.substring(8);
            final Player other = World.getPlayerByName(name);
            if (other == null) {
                player.sendMessage("Player not online: " + name);
                return;
            }
            ProfileViewing.view(player, other);
        }

        if (command[0].equals("skull")) {
            if (player.getSkullTimer() > 0) {
                player.setSkullTimer(0);
                player.setSkullIcon(0);
                player.getUpdateFlag().flag(Flag.APPEARANCE);
            } else {
                CombatFactory.skullPlayer(player);
            }
        }

        if (wholeCommand.startsWith("droplog")) {
            final String[] s = wholeCommand.split(" ");
            if (s.length < 2) {
                DropLog.open(player);
                return;
            }
            final String name = wholeCommand.substring(8);
            final Player other = World.getPlayerByName(name);
            if (other == null) {
                player.sendMessage("Player not found / online: " + name);
                return;
            }
            PlayerDropLog.sendDropLog(player, other);
        }

        if (wholeCommand.startsWith("drop")) {
            final String[] s = wholeCommand.split(" ");
            if (s.length < 2) {
                player.sendMessage("Enter npc name!");
                return;
            }
            final String name = wholeCommand.substring(5).toLowerCase();

            final int id = NpcDefinition.forName(name).getId();
            if (id == -1) {
                player.sendMessage("Npc not found: " + name);
                return;
            }
            MonsterDrops.sendNpcDrop(player, id, name);
        }


        if (command[0].equalsIgnoreCase("answer")) {
            String triviaAnswer = wholeCommand.substring(7);
            if (TriviaBot.acceptingQuestion()) {
                TriviaBot.attemptAnswer(player, triviaAnswer);
            }
        }

		/*if (command[0].equals("zul")) {
			if (player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstance.RegionInstanceType.ZULRAHS_SHRINE)) {
				player.getRegionInstance().destruct();
				TeleportHandler.teleportPlayer(player, GameSettings.DEFAULT_POSITION.copy(),
					player.getSpellbook().getTeleportType());
				player.getPacketSender().sendMessage("Sent home - you already had an instanced boss!");
			} else {
				if ((player.getWildernessLevel() < 20)) {
					GreenZulrah.spawn(player, 1, 5000);
					TeleportHandler.teleportPlayer(player, new Position(2268, 3070, (player.getIndex()) * 4), player.getSpellbook().getTeleportType());
				}
			}
		}*/

        /*if (command[0].equals("zul")) {
            if (player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstance.RegionInstanceType.BORINGZULRAHZONE)) {
                player.getRegionInstance().destruct();
                BoringZulrah.despawn(player);
                TeleportHandler.teleportPlayer(player, GameSettings.DEFAULT_POSITION.copy(),
                        player.getSpellbook().getTeleportType());
                player.getPacketSender().sendMessage("Sent home - you already had an instanced boss!");
            } else {
                if ((player.getWildernessLevel() < 20)) {
                    BoringZulrah.spawn(player);
                    player.getPacketSender().sendRichPresenceState("Slaying Zulrah!");
                    player.getPacketSender().sendSmallImageKey("ranged");
                    player.getPacketSender().sendRichPresenceSmallPictureText("Lvl: " + player.getSkillManager().getCurrentLevel(Skill.RANGED));
                    TeleportHandler.teleportPlayer(player, new Position(2268, 3070, (player.getIndex()) * 4), player.getSpellbook().getTeleportType());
                }
            }
        }*/

        if (command[0].equals("kraken")) {
            if ((player.getWildernessLevel() < 20)) {
                TeleportHandler.teleportPlayer(player, new Position(3683, 9888, (player.getIndex() + 1) * 4), player.getSpellbook().getTeleportType()); //kraken instance
            }
        }

        if (command[0].equalsIgnoreCase("gamble")) {
            TeleportHandler.teleportPlayer(player, new Position(2441, 3090, 0),
                    player.getSpellbook().getTeleportType());
            player.getPacketSender().sendRichPresenceState("Gambling");
            player.getPacketSender().sendMessage("@red@Video evidence is required to file a report.");
            player.getPacketSender().sendMessage("@red@Only Staff + Ranked Players in 'Dice' Can Middleman! No MM = No Refunds.");
        }



		/*if (wholeCommand.equalsIgnoreCase("benefits")) {
			player.getPacketSender().sendString(1, "www.janus.rip/forum/34-donation-information/");
			player.getPacketSender().sendMessage("Attempting to open: Donaor Information");
			//Achievements.finishAchievement(player, Achievements.AchievementData.OPEN_UP_THE_FORUMS);
		}*/

/*
		if (wholeCommand.equalsIgnoreCase("prices") || wholeCommand.equalsIgnoreCase("pc") || wholeCommand.equalsIgnoreCase("priceguide") || wholeCommand.equalsIgnoreCase("pricecheck")) {
			player.getPacketSender().sendString(1, "www.janus.rip/prices/");
			player.getPacketSender().sendMessage("@red@Check with staff if prices don't seem accurate!");
			//Achievements.finishAchievement(player, AchievementData.OPEN_UP_THE_FORUMS);
		}*/
	/*	if (wholeCommand.equalsIgnoreCase("rules")) {
			player.getPacketSender().sendString(1, "www.janus.rip/topic/110-janus-rules/");
			player.getPacketSender().sendMessage("Attempting to open: Janus Rules");
			//Achievements.finishAchievement(player, AchievementData.OPEN_UP_THE_FORUMS);
		}*/

/*		if (wholeCommand.equalsIgnoreCase("trades")) {
			player.getPacketSender().sendString(1, "www.janus.rip/trading/");
			player.getPacketSender().sendMessage("Search for all items for trade information!");
			//Achievements.finishAchievement(player, AchievementData.OPEN_UP_THE_FORUMS);
		}*/
        if (command[0].equals("zombie") || command[0].equals("zombies")) {
            player.getPacketSender().sendRichPresenceState("Slaying Zombies!");
            player.getPacketSender().sendSmallImageKey("attack");
            player.getPacketSender().sendRichPresenceSmallPictureText("Lvl: " + player.getSkillManager().getCurrentLevel(Skill.ATTACK));
            TeleportHandler.teleportPlayer(player, new Position(3503, 3563),
                    player.getSpellbook().getTeleportType());
        }


        if (command[0].equalsIgnoreCase("train")) {
            player.getPacketSender().sendRichPresenceState("Owning Rock Crabs");
            player.getPacketSender().sendSmallImageKey("attack");
            player.getPacketSender().sendRichPresenceSmallPictureText("Lvl: " + player.getSkillManager().getCurrentLevel(Skill.ATTACK));
            TeleportHandler.teleportPlayer(player, new Position(2679, 3714), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("commands")) {

            player.getPacketSender().sendMessage("::help - contacts staff for help");
            player.getPacketSender().sendMessage("::setemail email@domain.com - Used for account recovery");
            player.getPacketSender().sendMessage("::changepassword newpasshere");
            player.getPacketSender().sendMessage("::home - teleports you to home area");
            player.getPacketSender().sendMessage("::gamble - teleports you to the gamble area");
            player.getPacketSender().sendMessage("::vote - opens vote page");
            player.getPacketSender().sendMessage("::donate - opens donate page");
            player.getPacketSender().sendMessage("::claim or ::donated - claims a donation");
            player.getPacketSender().sendMessage("::voted - claims your votes!");
            player.getPacketSender().sendMessage("::train - teleports you to rock crabs");
            player.getPacketSender().sendMessage("::empty - empties your entire inventory");
            player.getPacketSender().sendMessage("::answer (answer) - answers the trivia");
            player.getPacketSender().sendMessage("::skull - skulls your player");
            player.getPacketSender().sendMessage("::Drops - View our amazing drop table!");
            player.getPacketSender().sendMessage("::players - View all online");
            player.getPacketSender().sendMessage("::profile username - view anyones profile");
            player.getPacketSender().sendMessage("::Droplog username - view a players drop log");
            player.getPacketSender().sendMessage("::maxhit - Find your max hits!");
            player.getPacketSender().sendMessage("::fountain - Teleport to Fountain of Goodwill");
            player.getPacketSender().sendMessage("::pray - Teleport to Alters");
            player.getPacketSender().sendMessage("::portals or ::teleports - Visit the City portals!");
            player.getPacketSender().sendMessage("::discord - Open Discord");

        }

        if (command[0].equalsIgnoreCase("setemail")) {
            String email = wholeCommand.substring(9);
            player.setEmailAddress(email);
            player.getPacketSender().sendMessage("You set your account's email adress to: [" + email + "] ");
            // Achievements.finishAchievement(player,
            // AchievementData.SET_AN_EMAIL_ADDRESS);
            PlayerPanel.refreshPanel(player);
        }

        if (command[0].equalsIgnoreCase("changepassword")) {
            String syntax = wholeCommand.substring(15);
            if (syntax == null || syntax.length() <= 2 || syntax.length() > 15 || !NameUtils.isValidName(syntax)) {
                player.getPacketSender().sendMessage("That password is invalid. Please try another password.");
                return;
            }
            if (syntax.contains("_")) {
                player.getPacketSender().sendMessage("Your password can not contain underscores.");
                return;
            }
            player.setPassword(syntax);
            player.getPacketSender().sendMessage("Your new password is: [" + syntax + "] Write it down!");

        }


        if (command[0].equalsIgnoreCase("countcash")) {
            player.getPacketSender().sendMessage(String.valueOf(player.getInventory().getAmount(995)));
        }

        if (command[0].equalsIgnoreCase("home")) {
            player.getPacketSender().sendRichPresenceState("At home");
            player.getPacketSender().sendSmallImageKey("home");
            player.getPacketSender().sendRichPresenceSmallPictureText("Combat Lvl: " + player.getSkillManager().getCombatLevel());
            TeleportHandler.teleportPlayer(player, GameSettings.DEFAULT_POSITION.copy(), player.getSpellbook().getTeleportType());
        }

        if (command[0].equalsIgnoreCase("destructinstance")) {
            BossMinigameFunctions.despawnNpcs(player);
            player.getPacketSender().sendMessage("Instances destructed");

        }

        if (command[0].equalsIgnoreCase("instanceinfo")) {
            System.out.println("Region Instance " + player.getRegionInstance().getType().toString());
            System.out.print("NPC List: " + player.getRegionInstance().getNpcsList().toString());
            System.out.println("Region Owner " + player.getRegionInstance().getOwner().getUsername());

        }


        if (command[0].equalsIgnoreCase("restorestats")) {
            BossMinigameFunctions.restoreOldStats(player);
        }

        if (command[0].equalsIgnoreCase("location")) {
            System.out.println("Location: " + player.getLocation().name());
        }

        if (command[0].equalsIgnoreCase("pray")) {
            player.getPacketSender().sendRichPresenceState("Praying to the Gods");
            player.getPacketSender().sendSmallImageKey("pray");
            player.getPacketSender().sendRichPresenceSmallPictureText("Lvl: " + player.getSkillManager().getCurrentLevel(Skill.PRAYER));
            TeleportHandler.teleportPlayer(player, new Position(2696, 5332), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("portals") || (command[0].equalsIgnoreCase("teleports"))) {
            TeleportHandler.teleportPlayer(player, new Position(2717, 5323), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("fountain")) {
            TeleportHandler.teleportPlayer(player, new Position(2715, 5357), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("edge")) {
            player.getPacketSender().sendRichPresenceState("Traversing Edgeville");
            TeleportHandler.teleportPlayer(player, new Position(3088, 3504), player.getSpellbook().getTeleportType());

        }

        if (command[0].equalsIgnoreCase("duel")) {
            if (player.getSummoning().getFamiliar() != null) {
                player.getPacketSender().sendMessage("You must dismiss your familiar before teleporting to the arena!");
            } else {
                player.getPacketSender().sendRichPresenceState("Visiting Duel Arena!");
                TeleportHandler.teleportPlayer(player, new Position(3364, 3267), player.getSpellbook().getTeleportType());
            }
        }
        if (command[0].equalsIgnoreCase("maxhit")) {
            int attack = CombatFormulas.getMeleeAttack(player) / 10;
            int range = CombatFormulas.getRangedAttack(player) / 10;
            int magic = CombatFormulas.getMagicAttack(player) / 10;
            player.getPacketSender().sendMessage("@bla@Melee attack: @or2@" + attack + "@bla@, ranged attack: @or2@"
                    + range + "@bla@, magic attack: @or2@" + magic);
        }
        if (command[0].equals("save")) {
            player.save();
            player.getPacketSender().sendMessage("Your progress has been saved.");
        }
        if (command[0].equals("dailyreward")) {
            player.getDailyReward().openInterface();
        }

        if (wholeCommand.equalsIgnoreCase("donate") || wholeCommand.equalsIgnoreCase("store")) {
            player.getPacketSender().sendRichPresenceState("Viewing Donation Page");
            player.getPacketSender().sendString(1, "http://janus.everythingrs.com/services/store");
            player.getPacketSender().sendMessage("@red@Make sure to enter your username in the box on the right<3");
        }


        if (command[0].equalsIgnoreCase("benefits")) {
            player.getPacketSender().sendString(1, "http://www.janus.rip/page/donator-benefits/");
            player.getPacketSender().sendMessage("Attempting to open the donator benefits page!");
        }

        if (command[0].equalsIgnoreCase("vote")) {
            player.getPacketSender().sendString(1, "http://janus.everythingrs.com/services/vote");
            player.getPacketSender().sendMessage("Attempting to open the vote panel! @red@Use ::voted to claim <3");
        }

        if (command[0].equalsIgnoreCase("hiscores")) {
            player.getPacketSender().sendString(1, "http://janus.everythingrs.com/services/hiscores");
            player.getPacketSender().sendMessage("Attempting to open the hiscores page!");
        }

        if (command[0].equalsIgnoreCase("disc") || command[0].equalsIgnoreCase("discord")) {
            player.getPacketSender().sendString(1, "http://discord.gg/CVN6jpy");
            player.getPacketSender().sendMessage("Attempting to open discord!");
        }

        if (command[0].equalsIgnoreCase("forums") || command[0].equalsIgnoreCase("website")) {
            player.getPacketSender().sendString(1, "http://www.janus.rip");
            player.getPacketSender().sendMessage("Attempting to open the site!");
        }

        if (command[0].equalsIgnoreCase("updates") || command[0].equalsIgnoreCase("news")) {
            player.getPacketSender().sendString(1, "http://www.janus.rip/updates/");
            player.getPacketSender().sendMessage("Attempting to open the latest updates!");
        }

        if (command[0].equalsIgnoreCase("mbox") || command[0].equalsIgnoreCase("mysterybox") || command[0].equalsIgnoreCase("loot")) {
            player.getPacketSender().sendString(1, "http://flub.link/mbox");
            player.getPacketSender().sendMessage("Attempting to view mbox loot!");
        }

        if (command[0].equals("help")) {
            if (player.getLastYell().elapsed(30000)) {
                World.sendStaffMessage("<col=FF0066><img=10> [TICKET SYSTEM]<col=6600FF> " + player.getUsername()
                        + " has requested help. Please help them!");
                player.getLastYell().reset();
                player.getPacketSender()
                        .sendMessage("<col=663300>Your help request has been received. Please be patient.");
            } else {
                player.getPacketSender().sendMessage("")
                        .sendMessage("<col=663300>You need to wait 30 seconds before using this again.").sendMessage(
                        "<col=663300>If it's an emergency, please private message a staff member directly instead.");
            }
        }
        if (command[0].equals("empty")) {
            player.getPacketSender().sendInterfaceRemoval().sendMessage("You clear your inventory.");
            player.getSkillManager().stopSkilling();
            player.getInventory().resetItems().refreshItems();
        }

        if (command[0].equalsIgnoreCase("[cn]")) {
            if (player.getInterfaceId() == 40172) {
                ClanChatManager.setName(player, wholeCommand.substring(wholeCommand.indexOf(command[1])));
            }
        }
    }

    private static void superDonator(final Player player, String[] command, String wholeCommand) {

        if (command[0].equalsIgnoreCase("pickup")) {

            int pickupValue = (Integer.parseInt(command[1].trim().toLowerCase().replaceAll("k", "000").replaceAll("m", "000000")
                    .replaceAll("b", "000000000")));

            player.setPickupValue(pickupValue);
            player.getPacketSender().sendMessage("@red@WE WILL PICKUP DROPS WORTH : " + Misc.setupMoney(pickupValue));


        }

        /*if (command[0].equalsIgnoreCase("kbdtest")) {
            KBDFight.StartKBDFight(player);
        }*/


        if (command[0].equalsIgnoreCase("title")) {
            player.getPacketSender().sendMessage("Use ::blacktitle, ::redtitle, ::bluetitle, ::greentitle");

        }
        if (command[0].equalsIgnoreCase("orangetitle")) {
            String title = wholeCommand.substring(12);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@or2@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }
        if (command[0].equalsIgnoreCase("blacktitle")) {
            String title = wholeCommand.substring(11);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@bla@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }

        if (command[0].equalsIgnoreCase("bluetitle")) {
            String title = wholeCommand.substring(10);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@blu@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }

        if (command[0].equalsIgnoreCase("greentitle")) {
            String title = wholeCommand.substring(11);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@gre@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }

        if (command[0].equalsIgnoreCase("yellowtitle")) {
            String title = wholeCommand.substring(12);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@yel@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }

        if (command[0].equalsIgnoreCase("redtitle")) {
            String title = wholeCommand.substring(9);
            if (title == null || title.length() <= 2 || title.length() > 9 || !NameUtils.isValidName(title)) {
                player.getPacketSender().sendMessage("You can not set your title to that!");
                return;
            }
            player.setTitle("@red@" + title);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }

        if (command[0].equals("szone")) {
            if (player.getRights().isStaff() || player.getRights() == PlayerRights.UBER_DONATOR
                    || player.getRights() == PlayerRights.LEGENDARY_DONATOR
                    || player.getRights() == PlayerRights.EXTREME_DONATOR
                    || player.getRights() == PlayerRights.SUPER_DONATOR || player.getRights() == PlayerRights.DONATOR)
                TeleportHandler.teleportPlayer(player, new Position(3363, 9638),
                        player.getSpellbook().getTeleportType());
        }
    }

    private static void uberDonator(final Player player, String[] command, String wholeCommand) {
        if (command[0].equals("uzone")) {
            if (player.getRights().isStaff() || player.getRights() == PlayerRights.UBER_DONATOR)
                TeleportHandler.teleportPlayer(player, new Position(2408, 4724), player.getSpellbook().getTeleportType());
        }
    }

    private static void legendaryDonator(final Player player, String[] command, String wholeCommand) {
        if (command[0].equals("lzone")) {
            if (player.getRights().isStaff() || player.getRights() == PlayerRights.UBER_DONATOR
                    || player.getRights() == PlayerRights.LEGENDARY_DONATOR)
                TeleportHandler.teleportPlayer(player, new Position(2313, 9810),
                        player.getSpellbook().getTeleportType());
        }
    }

    private static void extremeDonator(final Player player, String[] command, String wholeCommand) {

        if (command[0].equals("bank")) {
            if (player.getLocation() == Location.DUNGEONEERING || player.getLocation() == Location.FIGHT_PITS
                    || player.getLocation() == Location.FIGHT_CAVES || player.getLocation() == Location.DUEL_ARENA
                    || player.getLocation() == Location.RECIPE_FOR_DISASTER
                    || player.getLocation() == Location.WILDERNESS || player.getLocation() == Location.BOSS_TIER_LOCATION) {
                player.getPacketSender().sendMessage("You can not open your bank here!");
                return;
            }
            player.getBank(player.getCurrentBankTab()).open();
        }

        if (command[0].equals("ezone")) {
            if (player.getRights().ordinal() >= PlayerRights.EXTREME_DONATOR.ordinal() || player.getRights().isStaff())
                TeleportHandler.teleportPlayer(player, new Position(3429, 2919),
                        player.getSpellbook().getTeleportType());
        }
    }

    private static void memberCommands(final Player player, String[] command, String wholeCommand) {

        if (command[0].equalsIgnoreCase("donormessages")) {
            if (player.getDonorMessages()) {
                player.getPacketSender().sendMessage("@red@Donor messages turned OFF");
                player.setDonorMessages(false);
            } else {
                player.getPacketSender().sendMessage("@gre@Donor messages turned ON");
                player.setDonorMessages(true);
            }
        }


        if (command[0].equals("dzone")) {
            TeleportHandler.teleportPlayer(player, new Position(3363, 9638), player.getSpellbook().getTeleportType());
        }

        /*if (command[0].equals("tray") && (player.getNotificationPreference())) {
            player.getPacketSender().trayMessage(1, "1 test");
            player.getPacketSender().trayMessage(2, "2 test");
            player.getPacketSender().trayMessage(3, "3 test");
            player.getPacketSender().trayMessage(4, "4 test");
            player.getPacketSender().trayMessage(5, "5 test");
        }*/

        /*if (command[0].equals("trayminimised") && (player.getNotificationPreference())) {
            player.getPacketSender().minimisedTrayMessage(1, "1 test");
            player.getPacketSender().minimisedTrayMessage(2, "2 test");
            player.getPacketSender().minimisedTrayMessage(3, "3 test");
            player.getPacketSender().minimisedTrayMessage(4, "4 test");
            player.getPacketSender().minimisedTrayMessage(5, "5 test");
        }*/

        if (wholeCommand.toLowerCase().startsWith("yell")) {
            if (PlayerPunishment.muted(player.getUsername()) || PlayerPunishment.IPMuted(player.getHostAddress())) {
                player.getPacketSender().sendMessage("You are muted and cannot yell.");
                return;
            }
            int delay = player.getRights().getYellDelay();
            if (!player.getLastYell().elapsed((delay * 1000))) {
                player.getPacketSender().sendMessage(
                        "You must wait at least " + delay + " seconds between every yell-message you send.");
                return;
            }
            String yellMessage = wholeCommand.substring(4, wholeCommand.length());

            player.getLastYell().reset();
            // if (player.getUsername().equalsIgnoreCase("levi")) {
            // World.sendMessage("" + player.getRights().getYellPrefix() +
            // "<img=" + player.getRights().ordinal()
            // + ">@red@ [DEVELOPER] @bla@" + player.getUsername() + ":" +
            // yellMessage);
            // return;
            // }
            if (player.getRights() == PlayerRights.MODERATOR && player.getUsername().equalsIgnoreCase("metasploit")) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@gre@ [Global Mod] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
		/*	if (player.getRights() == PlayerRights.COMMUNITYMANAGER) {
								World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=31>"+"<col=9E138C><shad=1> [Community Manager]</shad></col> @bla@" + player.getUsername() + ":" + yellMessage);
								return;
			}*/
            if (player.getRights() == PlayerRights.SUPPORT && player.getUsername().equalsIgnoreCase("nico")) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Meme Bot] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.ADMINISTRATOR && player.getUsername().equalsIgnoreCase("jack")) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Web Dev/Manager] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.OWNER) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Owner] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.DEVELOPER) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Developer] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.SUPPORT) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Support] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }

            if (player.getRights() == PlayerRights.MODERATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Mod] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.ADMINISTRATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + ">@red@ [Admin] @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.UBER_DONATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + "><col=0EBFE9><shad=1> [Uber]</shad></col> @bla@" + player.getUsername() + ":" + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.LEGENDARY_DONATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + "><col=697998><shad=1> [Legendary]</shad></col> @bla@" + player.getUsername() + ":"
                        + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.EXTREME_DONATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + "><col=D9D919><shad=1> [Extreme]</shad></col> @bla@" + player.getUsername() + ":"
                        + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.SUPER_DONATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + "><col=787878><shad=1> [Super]</shad></col> @bla@" + player.getUsername() + ":"
                        + yellMessage);
                return;
            }
            if (player.getRights() == PlayerRights.DONATOR) {
                World.sendFilteredMessage("" + player.getRights().getYellPrefix() + "<img=" + player.getRights().ordinal()
                        + "><col=FF7F00><shad=1> [Donator]</shad></col> @bla@" + player.getUsername() + ":"
                        + yellMessage);
                return;
            }
            // TO-DO

        }

    }

    private static void helperCommands(final Player player, String[] command, String wholeCommand) {

        if (wholeCommand.equals("afk")) {
            World.sendFilteredMessage("<img=10> <col=FF0000><shad=0>" + player.getUsername()
                    + ": I am now away, please don't message me in-game; We're usually always on Discord!");
        }
        if (wholeCommand.equals("back")) {
            World.sendFilteredMessage("<img=10> <col=FF0000><shad=0>" + player.getUsername()
                    + ": I am now back, feel free to message me for support.");
        }


        if (command[0].equalsIgnoreCase("kick")) {
            String player2 = wholeCommand.substring(5);
            Player playerToKick = World.getPlayerByName(player2);
            if (playerToKick == null) {
                player.getPacketSender().sendConsoleMessage("Player " + player2 + " couldn't be found on Janus.");
                return;
            } else {
                World.deregister(playerToKick);
                PlayerHandler.handleLogout(playerToKick);
                player.getPacketSender().sendConsoleMessage("Kicked " + playerToKick.getUsername() + ".");
                PlayerLogs.log(player.getUsername(),
                        "" + player.getUsername() + " just kicked " + playerToKick.getUsername() + "!");
            }
            if (playerToKick.getLocation() == Location.INSTANCE_ARENA) {
                if (playerToKick.getRegionInstance() != null) {
                    InstanceArena.destructArena(playerToKick);
                } else {
                    player.moveTo(InstanceArena.ENTRANCE);
                }
            }
        }

        if (command[0].equals("bank")) {
            if (player.getLocation() == Location.DUNGEONEERING || player.getLocation() == Location.FIGHT_PITS
                    || player.getLocation() == Location.FIGHT_CAVES || player.getLocation() == Location.DUEL_ARENA
                    || player.getLocation() == Location.RECIPE_FOR_DISASTER
                    || player.getLocation() == Location.WILDERNESS || player.getLocation() == Location.BOSS_TIER_LOCATION) {
                player.getPacketSender().sendMessage("You can not open your bank here!");
                return;
            }
            player.getBank(player.getCurrentBankTab()).open();
        }

        if (command[0].equalsIgnoreCase("jail")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " "));
            if (player2 != null) {
                if (Jail.isJailed(player2)) {
                    player.getPacketSender().sendMessage("That player is already jailed!");
                    return;
                }
                if (player2.getLocation() == Location.INSTANCE_ARENA) {
                    if (player2.getRegionInstance() != null) {
                        InstanceArena.destructArena(player2);
                    }
                }
                if (Jail.jailPlayer(player2) && player2.getDueling().duelingStatus == 0) {
                    player2.getSkillManager().stopSkilling();
                    PlayerLogs.log(player.getUsername(),
                            player.getUsername() + " just jailed " + player2.getUsername() + "!");
                    player.getPacketSender().sendMessage("Jailed player: " + player2.getUsername());
                    player2.getPacketSender().sendMessage("You have been jailed by " + player.getUsername() + ".");
                    World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> " + player.getUsername()
                            + " just jailed " + player2.getUsername());
                    player2.performAnimation(new Animation(1994));
                    player.performGraphic(new Graphic(730));
                } else {
                    if (player2.getDueling().duelingStatus > 0) {
                        player.getPacketSender().sendMessage(
                                "That player is currently in a stake. Please wait before jailing them, or kick then jail.");
                        return;
                    } else {
                        player.getPacketSender().sendMessage("This shouldn't happen, message Flub. Error: JAIL45");
                    }
                }
            } else {
                player.getPacketSender().sendMessage("Could not find that player online.");
            }
        }

        if (command[0].equalsIgnoreCase("mma")) {
            TeleportHandler.teleportPlayer(player, new Position(2038, 4497), TeleportType.NORMAL);

        }

        if (command[0].equals("remindvote")) {
            World.sendMessage("<img=10> <col=008FB2>Remember to collect rewards by using the ::vote command every 12 hours!");
        }
        if (command[0].equalsIgnoreCase("unjail")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " "));
            if (player2 != null) {
                Jail.unjail(player2);
                PlayerLogs.log(player.getUsername(), "" + player.getUsername() + " just unjailed " + player2.getUsername() + "!");
                player.getPacketSender().sendMessage("Unjailed player: " + player2.getUsername() + "");
                player2.getPacketSender().sendMessage("You have been unjailed by " + player.getUsername() + ".");
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> [" + player.getUsername() + "] just unjailed [" + player2.getUsername() + "]");
                player2.performAnimation(new Animation(1993));
                player.performGraphic(new Graphic(730));
            } else {
                player.getPacketSender().sendMessage("Could not find \"" + wholeCommand.substring(command[0].length() + 1, wholeCommand.length()) + "\" online.");
            }
        }
        if (command[0].equals("staffzone")) {
            if (command.length > 1 && command[1].equals("all")) {
                for (Player players : World.getPlayers()) {
                    if (players != null) {
                        if (players.getRights().isStaff()) {
                            TeleportHandler.teleportPlayer(players, new Position(2846, 5147), TeleportType.NORMAL);
                        }
                    }
                }
            } else {
                TeleportHandler.teleportPlayer(player, new Position(2038, 4497), TeleportType.NORMAL);
            }
        }
        if (command[0].equalsIgnoreCase("saveall")) {
            World.savePlayers();
            player.getPacketSender().sendMessage("Saved players!");
        }
        if (command[0].equalsIgnoreCase("teleto")) {
            String playerToTele = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            ;
            Player player2 = World.getPlayerByName(playerToTele);

            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            } else {
                boolean canTele = TeleportHandler.checkReqs(player, player2.getPosition().copy())
                        && player.getRegionInstance() == null && player2.getRegionInstance() == null;
                if (player.getRights() == PlayerRights.SUPPORT || player.getRights() == PlayerRights.MODERATOR) {
                    if (player2.getMinigameAttributes().getDungeoneeringAttributes().getParty() != null) {
                        player.sendMessage("you can't teleport to someone who is in dungeonnering");
                        return;
                    }
                }

                if (canTele) {
                    TeleportHandler.teleportPlayer(player, player2.getPosition().copy(), TeleportType.NORMAL);
                    player.getPacketSender().sendConsoleMessage("Teleporting to player: " + player2.getUsername() + "");
                } else {
                    player.getPacketSender()
                            .sendConsoleMessage("You can not teleport to this player at the moment. Minigame maybe?");
                }
            }
        }
        if (command[0].equalsIgnoreCase("movehome")) {
            String player2 = command[1];
            player2 = Misc.formatText(player2.replaceAll("_", " "));
            if (command.length >= 3 && command[2] != null) {
                player2 += " " + Misc.formatText(command[2].replaceAll("_", " "));
            }
            Player playerToMove = World.getPlayerByName(player2);
            if (playerToMove != null) {
                playerToMove.moveTo(GameSettings.DEFAULT_POSITION.copy());
                playerToMove.getPacketSender()
                        .sendMessage("You've been teleported home by " + player.getUsername() + ".");
                player.getPacketSender()
                        .sendConsoleMessage("Sucessfully moved " + playerToMove.getUsername() + " to home.");
            }
        }

        /** Test command to prove the freeze theory **/
        if (command[0].equalsIgnoreCase("freeze")) {
            String playerToFreeze = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            Player player2 = World.getPlayerByName(playerToFreeze);

            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            }

            player2.setFreezeDelay(Integer.MAX_VALUE);
            player2.getPacketSender().sendMessage(player.getUsername() + " has frozen me!");
            player2.setResetMovementQueue(true);
        }

        if (command[0].equalsIgnoreCase("unfreeze")) {
            String playerToUnfreeze = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            Player player2 = World.getPlayerByName(playerToUnfreeze);

            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            }

            player2.setFreezeDelay(-1);
            player2.getPacketSender().sendMessage(player.getUsername() + " has unfrozen me!");
            player2.setResetMovementQueue(true);
        }



        if (command[0].equalsIgnoreCase("mute")) {
            try {
                String target = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
                if (!PlayerSaving.playerExists(target)) {
                    player.getPacketSender().sendMessage("Player: " + target + " does not exist.");
                    return;
                } else {
                    if (PlayerPunishment.muted(target)) {
                        player.getPacketSender().sendMessage("Player: " + target + " already has an active mute.");
                        return;
                    }
                    PlayerLogs.log(player.getUsername(), player.getUsername() + " just muted " + target + "!");
                    World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> [" + player.getUsername() + "] just muted [" + target + "]");
                    PlayerPunishment.mute(target);/*, GameSettings.Temp_Mute_Time);*/
                    player.getPacketSender().sendMessage("Player [" + target + "] was successfully muted");// for "+GameSettings.Temp_Mute_Time/1000/60/60+" hour(s). Command logs written.");
                    Player plr = World.getPlayerByName(target);
                    if (plr != null) {
                        plr.getPacketSender().sendMessage("You have been muted by [" + player.getUsername() + "]."); // for "+GameSettings.Temp_Mute_Time/1000/60/60+" hour(s).");
                    }
                }
            } catch (Exception e) {
                player.getPacketSender().sendMessage("The player must be online.");
            }
        }

        if (command[0].equalsIgnoreCase("unmute")) {
            String player2 = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            if (!PlayerSaving.playerExists(player2)) {
                player.getPacketSender().sendMessage("Player " + player2 + " does not exist.");
                return;
            } else {
                if (!PlayerPunishment.muted(player2)) {
                    player.getPacketSender().sendMessage("Player " + player2 + " is not muted!");
                    return;
                }
                PlayerLogs.log(player.getUsername(), player.getUsername() + " just unmuted " + player2 + "!");
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> [" + player.getUsername() + "] just unmuted [" + player2 + "]");
                PlayerPunishment.unmute(player2);
                player.getPacketSender().sendMessage("Player " + player2 + " was successfully unmuted. Command logs written.");
                Player plr = World.getPlayerByName(player2);
                if (plr != null) {
                    plr.getPacketSender().sendMessage("You have been unmuted by " + player.getUsername() + ".");
                }
            }
        }


    }

    private static void moderatorCommands(final Player player, String[] command, String wholeCommand) {


        if (command[0].equals("setboss")) {
            int bossID = Integer.parseInt(command[1]);
            if (bossID <= 1) {
                player.getPacketSender().sendMessage("You can't choose an ID less than 1");
                return;
            }
            GameSettings.CURRENT_BOSS_ID = bossID;
            player.getPacketSender().sendMessage("Current Boss assigned: " + GameSettings.CURRENT_BOSS_ID);
            World.sendMessage("@red@This weeks boss task has been changed to @blu@"+NpcDefinition.forId(GameSettings.CURRENT_BOSS_ID).getName()+"@red@!");
        }

        if (command[0].equalsIgnoreCase("givess") && player.getUsername().equalsIgnoreCase("Martijn")) {
            String name = wholeCommand.substring(7);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.SUPPORT);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "support.");
            }
        }

        if (command[0].equalsIgnoreCase("permban") || command[0].equalsIgnoreCase("permaban")) {
            try {
                Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
                if (player2 == null) {
                    player.getPacketSender().sendMessage("Target does not exist. Unable to permban.");
                    return;
                }


                String uuid = player2.getUUID();
                String mac = player2.getMac();
                String name = player2.getUsername();
                String bannedIP = player2.getHostAddress();

                World.sendStaffMessage("Perm banned " + name + " (" + bannedIP + "/" + mac + "/" + uuid + ")");
                PlayerLogs.log(player.getUsername(), "Has perm banned: " + name + "!");
                PlayerLogs.log(name, player + " perm banned: " + name + ".");

                PlayerPunishment.addBannedIP(bannedIP);
                ConnectionHandler.banUUID(name, uuid);
                ConnectionHandler.banMac(name, mac);
                PlayerPunishment.ban(name);

                if (player2 != null) {
                    World.deregister(player2);
                }

                for (Player playersToBan : World.getPlayers()) {
                    if (playersToBan == null)
                        continue;
                    if (playersToBan.getHostAddress() == bannedIP || playersToBan.getUUID() == uuid || playersToBan.getMac() == mac) {
                        PlayerLogs.log(player.getUsername(), player.getUsername() + " just caught " + playersToBan.getUsername() + " with permban!");
                        PlayerLogs.log(name, player + " perm banned: " + name + ", we were crossfire.");
                        World.sendStaffMessage(playersToBan.getUsername() + " was banned as well.");
                        PlayerPunishment.ban(playersToBan.getUsername());
                        World.deregister(playersToBan);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (command[0].contains("host")) {
            String plr = wholeCommand.substring(command[0].length() + 1);
            Player playr2 = World.getPlayerByName(plr);
            if (playr2 != null) {
                player.getPacketSender().sendConsoleMessage("" + playr2.getUsername() + " host IP: "
                        + playr2.getHostAddress() + ", serial number: " + playr2.getSerialNumber());
            } else {
                player.getPacketSender().sendConsoleMessage("Could not find player: " + plr);
            }
        }


        if (command[0].equalsIgnoreCase("findnpc")) {
            String name = wholeCommand.substring(command[0].length() + 1);
            player.getPacketSender().sendMessage("Finding item id for item - " + name);
            boolean found = false;
            for (int i = 0; i < NpcDefinition.getDefinitions().length; i++) {
                if (NpcDefinition.forId(i) == null || NpcDefinition.forId(i).getName() == null) {
                    continue;
                }
                if (NpcDefinition.forId(i).getName().toLowerCase().contains(name)) {
                    player.getPacketSender().sendMessage("Found NPC with name [" + NpcDefinition.forId(i).getName().toLowerCase() + "] - id: " + i);
                    found = true;
                }
            }
            if (!found) {
                player.getPacketSender().sendMessage("No NPC with name [" + name + "] has been found!");
            }
        }


        if (command[0].equals("find")) {
            String name = wholeCommand.substring(5).toLowerCase().replaceAll("_", " ");
            player.getPacketSender().sendConsoleMessage("Finding item id for item - " + name);
            boolean found = false;
            for (int i = 0; i < ItemDefinition.getMaxAmountOfItems(); i++) {
                if (ItemDefinition.forId(i).getName().toLowerCase().contains(name)) {
                    player.getPacketSender().sendConsoleMessage("Found item with name ["
                            + ItemDefinition.forId(i).getName().toLowerCase() + "] - id: " + i);
                    found = true;
                }
            }
            if (!found) {
                player.getPacketSender().sendConsoleMessage("No item with name [" + name + "] has been found!");
            }
        } else if (command[0].equals("id")) {
            String name = wholeCommand.substring(3).toLowerCase().replaceAll("_", " ");
            player.getPacketSender().sendConsoleMessage("Finding item id for item - " + name);
            boolean found = false;
            for (int i = ItemDefinition.getMaxAmountOfItems() - 1; i > 0; i--) {
                if (ItemDefinition.forId(i).getName().toLowerCase().contains(name)) {
                    player.getPacketSender().sendConsoleMessage("Found item with name ["
                            + ItemDefinition.forId(i).getName().toLowerCase() + "] - id: " + i);
                    found = true;
                }
            }
            if (!found) {
                player.getPacketSender().sendConsoleMessage("No item with name [" + name + "] has been found!");
            }
        }


        if (command[0].equals("tele")) {
            int x = Integer.valueOf(command[1]), y = Integer.valueOf(command[2]);
            int z = player.getPosition().getZ();
            if (command.length > 3) {
                z = Integer.valueOf(command[3]);
            }
            Position position = new Position(x, y, z);
            player.moveTo(position);
            player.getPacketSender().sendConsoleMessage("Teleporting to " + position.toString());
        }
        if (command[0].equalsIgnoreCase("ipmute")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " "));
            if (player2 == null) {
                player.getPacketSender().sendMessage("Could not find that player online.");
                return;
            } else {
                if (PlayerPunishment.IPMuted(player2.getHostAddress())) {
                    player.getPacketSender().sendMessage("Player " + player2.getUsername() + "'s IP is already IPMuted. Command logs written.");
                    return;
                }
                final String mutedIP = player2.getHostAddress();
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> " + player.getUsername() + " just IP Muted " + player2.getUsername() + " on " + mutedIP);
                PlayerPunishment.addMutedIP(mutedIP);
                player.getPacketSender().sendMessage("Player " + player2.getUsername() + " was successfully IPMuted. Command logs written.");
                player2.getPacketSender().sendMessage("You have been IPMuted by " + player.getUsername() + ".");
                PlayerLogs.log(player.getUsername(), player.getUsername() + " just IPMuted " + player2.getUsername() + "!");
            }
        }


        if (command[0].equalsIgnoreCase("ban")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " "));
            String playerUsername = player2.getUsername();
            player.sendMessage("playerToBan: " + player2);

            if (!PlayerSaving.playerExists(playerUsername)) {
                player.getPacketSender().sendMessage("Player " + playerUsername + " does not exist.");
                return;
            } else {
                if (PlayerPunishment.banned(playerUsername)) {
                    player.getPacketSender().sendMessage("Player " + playerUsername + " already has an active ban.");
                    return;
                }
                PlayerLogs.log(player.getUsername(), player.getUsername() + " just banned " + playerUsername + "!");
                PlayerPunishment.ban(playerUsername);
                player.getPacketSender().sendMessage("Player " + playerUsername + " was successfully banned. Command logs written.");
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> " + player.getUsername() + " just banned " + playerUsername + ".");
                if (player2 != null) {
                    World.deregister(player2);
                }
            }
        }

        if (command[0].equalsIgnoreCase("unban")) {
            String playerToBan = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            if (!PlayerSaving.playerExists(playerToBan)) {
                player.getPacketSender().sendMessage("Player " + playerToBan + " does not exist.");
                return;
            } else {
                if (!PlayerPunishment.banned(playerToBan)) {
                    player.getPacketSender().sendMessage("Player " + playerToBan + " is not banned!");
                    return;
                }
                PlayerLogs.log(player.getUsername(), "" + player.getUsername() + " just unbanned " + playerToBan + "!");
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> " + player.getUsername() + " just unbanned " + playerToBan + ".");
                PlayerPunishment.unban(playerToBan);
                player.getPacketSender().sendMessage("Player " + playerToBan + " was successfully unbanned. Command logs written.");
            }
        }
        if (command[0].equals("sql")) {
            MySQLController.toggle();
            if (player.getRights() == PlayerRights.OWNER) {
                player.getPacketSender().sendMessage("Sql toggled to status: " + GameSettings.MYSQL_ENABLED);
            } else {
                player.getPacketSender().sendMessage("Sql toggled to status: " + GameSettings.MYSQL_ENABLED + ".");
            }
        }


        if (command[0].equalsIgnoreCase("toggleinvis")) {
            player.setNpcTransformationId(player.getNpcTransformationId() > 0 ? -1 : 8254);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }


        if (command[0].equalsIgnoreCase("ipban")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " "));
            if (player2 == null) {
                player.getPacketSender().sendMessage("Could not find that player online.");
                return;
            } else {
                if (PlayerPunishment.IPBanned(player2.getHostAddress())) {
                    player.getPacketSender().sendMessage("Player " + player2.getUsername() + "'s IP is already banned.");
                    return;
                }
                final String bannedIP = player2.getHostAddress();
                PlayerPunishment.ban(player2.getUsername());
                PlayerPunishment.addBannedIP(bannedIP);
                player.getPacketSender().sendMessage("Player " + player2.getUsername() + "'s IP was successfully banned. Command logs written.");
                World.sendStaffMessage("<col=FF0066><img=2> [PUNISHMENTS]<col=6600FF> " + player.getUsername() + " just IP Banned " + player2.getUsername());
                for (Player playersToBan : World.getPlayers()) {
                    if (playersToBan == null)
                        continue;
                    if (playersToBan.getHostAddress() == bannedIP) {
                        PlayerLogs.log(player.getUsername(), "" + player.getUsername() + " just IPBanned " + playersToBan.getUsername() + "!");
                        PlayerPunishment.ban(playersToBan.getUsername());
                        World.deregister(playersToBan);
                        if (player2.getUsername() != playersToBan.getUsername())
                            player.getPacketSender().sendMessage("Player " + playersToBan.getUsername() + " was successfully IPBanned. Command logs written.");
                    }
                }
            }
        }
        if (command[0].equalsIgnoreCase("unipmute")) {
            player.getPacketSender().sendConsoleMessage("Unipmutes can only be handled manually by Flub.");
        }

        if (command[0].equalsIgnoreCase("teletome")) {
            String playerToTele = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            Player player2 = World.getPlayerByName(playerToTele);
            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            } else {
                boolean canTele = TeleportHandler.checkReqs(player, player2.getPosition().copy())
                        && player.getRegionInstance() == null && player2.getRegionInstance() == null;
                if (canTele) {
                    TeleportHandler.teleportPlayer(player2, player.getPosition().copy(), TeleportType.NORMAL);
                    player.getPacketSender()
                            .sendConsoleMessage("Teleporting " + player2.getUsername() + " to you");
                    player2.getPacketSender().sendMessage("You're being teleported to " + player.getUsername() + "...");
                } else {
                    player.getPacketSender().sendConsoleMessage(
                            "You can not teleport that player at the moment. Maybe you or they are in a minigame?");
                }
            }
        }
    }

    private static void administratorCommands(final Player player, String[] command, String wholeCommand) {

        if (command[0].equalsIgnoreCase("ffa-pure")) {
            FreeForAll.startEvent("pure");
        }
        if (command[0].equalsIgnoreCase("ffa-brid")) {
            FreeForAll.startEvent("brid");
        }
        if (command[0].equalsIgnoreCase("ffa-dharok")) {
            FreeForAll.startEvent("dharok");
        }

        if (command[0].equalsIgnoreCase("donamount")) {
            String name = wholeCommand.substring(10);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {

                player.getPacketSender().sendMessage("Player has donated: " + target.getAmountDonated() + " Dollars.");
            }
        }


        if (command[0].equals("reset")) {
            for (Skill skill : Skill.values()) {
                int level = skill.equals(Skill.CONSTITUTION) ? 100 : skill.equals(Skill.PRAYER) ? 10 : 1;
                player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill,
                        SkillManager.getExperienceForLevel(skill == Skill.CONSTITUTION ? 10 : 1));
            }
            player.getPacketSender().sendConsoleMessage("Your skill levels have now been reset.");
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }



        if (command[0].equals("emptyitem")) {
            if (player.getInterfaceId() > 0
                    || player.getLocation() != null && player.getLocation() == Location.WILDERNESS) {
                player.getPacketSender().sendMessage("You cannot do this at the moment.");
                return;
            }
            int item = Integer.parseInt(command[1]);
            int itemAmount = player.getInventory().getAmount(item);
            Item itemToDelete = new Item(item, itemAmount);
            player.getInventory().delete(itemToDelete).refreshItems();
        }
        if (command[0].equals("gold")) {
            Player p = World.getPlayerByName(wholeCommand.substring(5));
            if (p != null) {
                long gold = 0;
                for (Item item : p.getInventory().getItems()) {
                    if (item != null && item.getId() > 0 && item.tradeable()) {
                        gold += item.getDefinition().getValue();
                    }
                }
                for (Item item : p.getEquipment().getItems()) {
                    if (item != null && item.getId() > 0 && item.tradeable()) {
                        gold += item.getDefinition().getValue();
                    }
                }
                for (int i = 0; i < 9; i++) {
                    for (Item item : p.getBank(i).getItems()) {
                        if (item != null && item.getId() > 0 && item.tradeable()) {
                            gold += item.getDefinition().getValue();
                        }
                    }
                }
                gold += p.getMoneyInPouch();
                player.getPacketSender().sendMessage(
                        p.getUsername() + " has " + Misc.insertCommasToNumber(String.valueOf(gold)) + " coins.");
            } else {
                player.getPacketSender().sendMessage("Can not find player online.");
            }
        }

        if (command[0].equals("cashineco")) {
            int gold = 0, plrLoops = 0;
            for (Player p : World.getPlayers()) {
                if (p != null) {
                    for (Item item : p.getInventory().getItems()) {
                        if (item != null && item.getId() > 0 && item.tradeable()) {
                            gold += item.getDefinition().getValue();
                        }
                    }
                    for (Item item : p.getEquipment().getItems()) {
                        if (item != null && item.getId() > 0 && item.tradeable()) {
                            gold += item.getDefinition().getValue();
                        }
                    }
                    for (int i = 0; i < 9; i++) {
                        for (Item item : player.getBank(i).getItems()) {
                            if (item != null && item.getId() > 0 && item.tradeable()) {
                                gold += item.getDefinition().getValue();
                            }
                        }
                    }
                    gold += p.getMoneyInPouch();
                    plrLoops++;
                }
            }
            player.getPacketSender().sendMessage(
                    "Total gold in economy right now: " + gold + ", went through " + plrLoops + " players items.");
        }


    }

    private static void ownerCommands(final Player player, String[] command, String wholeCommand) {

        if (command[0].equals("poison")) {
            Player target = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
            target.setPoisonDamage(200);
            TaskManager.submit(new CombatPoisonEffect(target));
            target.getPacketSender().sendMessage("Flub has poisoned you lol");
        }

        if (command[0].equals("cure")) {
            Player target = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
            PoisonImmunityTask.makeImmune(target, 0);
            target.getPacketSender().sendMessage("Flub has cured you :)");
        }

        if (command[0].equalsIgnoreCase("setdailynpc")) {
            int NPC_ID = Integer.parseInt(command[1]);
            DailyNPCTask.CHOSEN_NPC_ID = NPC_ID;
            World.sendMessage("@red@Today's Daily NPC task is now:"
                    + DailyNPCTask.KILLS_REQUIRED
                    + " x "
                    + NpcDefinition.forId(DailyNPCTask.CHOSEN_NPC_ID));
        }

        if (command[0].equalsIgnoreCase("customdailytask")) {
            int killsRequired = Integer.parseInt(command[1]);
            int NPCID = Integer.parseInt(command[2]);
            DailyNPCTask.KILLS_REQUIRED = killsRequired;
            DailyNPCTask.CHOSEN_NPC_ID = NPCID;
            World.sendMessage("@red@Today's Daily NPC task is now:"
                    + DailyNPCTask.KILLS_REQUIRED
                    + " x "
                    + NpcDefinition.forId(DailyNPCTask.CHOSEN_NPC_ID).getName());

        }

        if (command[0].equalsIgnoreCase("resetdailytask")) {
            DailyNPCTask.resetDailyNPCGame();
        }

        if (command[0].equalsIgnoreCase("placeholders")) {
            player.getPacketSender().sendMessage("Placeholders: " + player.placeholdersEnabled());
            System.out.println("Placeholders: "+ player.placeholdersEnabled());
        }

        if (command[0].equals("rights")) {
            int rankId = Integer.parseInt(command[1]);
            if (player.getUsername().equalsIgnoreCase("server") && rankId != 10) {
                player.getPacketSender().sendMessage("You cannot do that.");
                return;
            }
            Player target = World
                    .getPlayerByName(wholeCommand.substring(rankId >= 10 ? 10 : 9, wholeCommand.length()));
            if (target == null) {
                player.getPacketSender().sendConsoleMessage("Player must be online to give them rights!");
            } else {
                target.setRights(PlayerRights.forId(rankId));
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                target.getPacketSender().sendRights();
            }
        }

        if (command[0].equalsIgnoreCase("testrandom")) {
            int truee = 0;
            int falsee = 0;
            for (int i = 0; i < 200; i++) {

                if (Misc.getRandom(1) > 0) {
                    System.out.println("TRUE");
                    truee += 1;
                } else {
                    System.out.println("FALSE");
                    falsee += 1;
                }
                if (i == 199) {
                    System.out.println("True: "+truee + " False: "+ falsee);
                }
            }
        }

        if (command[0].equals("poison")) {
            Player target = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
            target.setPoisonDamage(200);
            TaskManager.submit(new CombatPoisonEffect(target));
            target.getPacketSender().sendMessage("Flub has poisoned you lol");
        }

        if (command[0].equals("forcefollow")) {
            Player target = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
            target.getMovementQueue().setFollowCharacter(player);
            target.forceChat("I will follow you until I die!");
        }

        if (command[0].equals("forcefollowplayers")) {
            World.getPlayers().forEach(p -> p.getMovementQueue().setFollowCharacter(player));
            World.getPlayers().forEach(p -> p.getPacketSender().sendMessage("I will follow you until I die!"));
        }

        if (command[0].equals("forcefollownpcs")) {
            World.getNpcs().forEach(npc -> npc.getMovementQueue().setFollowCharacter(player));
        }

        if (command[0].equals("npcstome")) {
            World.getPlayers().forEach(p -> p.moveTo(player.getPosition()));
            World.getNpcs().forEach(npc -> npc.moveTo(player.getPosition()));
            World.getPlayers().forEach(p -> p.getMovementQueue().setFollowCharacter(player));
            World.getPlayers().forEach(p -> p.forceChat("I will follow you until I die!"));
            World.getNpcs().forEach(npc -> npc.getMovementQueue().setFollowCharacter(player));
            World.getNpcs().forEach(npc -> npc.forceChat("I will follow you until I die!"));
        }



        if (command[0].equals("cure")) {
            Player target = World.getPlayerByName(wholeCommand.substring(command[0].length() + 1));
            PoisonImmunityTask.makeImmune(target, 0);
            target.getPacketSender().sendMessage("Flub has cured you :)");
        }




        if (command[0].equalsIgnoreCase("toggledeveloper")) {
            GameSettings.DEVELOPERSERVER = false;
        }


        if (command[0].equalsIgnoreCase("xptracker")) { //COMMAND TO SHOW DIFFICULTY
            player.getPacketSender().sendInterface(23000);
            player.getPacketSender().sendString(23004, "999m");
            player.getPacketSender().sendString(23005, "40k");
            player.getPacketSender().sendString(23007, "98");
            player.getPacketSender().sendString(23008, "98%");
            player.getPacketSender().sendString(23009, "99");
        }






        if (command[0].equalsIgnoreCase("cluereward")) { //COMMAND TO SHOW DIFFICULTY
            player.getInventory().add(2714, 10);
        }

        if (command[0].equalsIgnoreCase("restorestats")) {
            BossMinigameFunctions.restoreOldStats(player);
        }

        if (command[0].equalsIgnoreCase("panel")) { //COMMAND TO SHOW DIFFICULTY
            PlayerPanel.refreshPanel(player);
        }

        if (command[0].equalsIgnoreCase("angle")) { //COMMAND TO SHOW DIFFICULTY
            player.getPacketSender().sendCameraAngle(20, 30, 1, 3, 90);
        }

        if (command[0].equalsIgnoreCase("shake")) { //COMMAND TO SHOW DIFFICULTY
            player.getPacketSender().sendCameraShake(20, 5, 60, 3);
        }

        if (command[0].equalsIgnoreCase("spin")) { //COMMAND TO SHOW DIFFICULTY
            player.getPacketSender().sendCameraSpin(20, 5, 60, 3, 40);
        }


        if (command[0].equalsIgnoreCase("difficulty")) {


            if (wholeCommand.substring(command[0].length() + 1).equalsIgnoreCase("easy")) {
                player.setDifficulty(Difficulty.Easy);
                player.getPacketSender().sendMessage("1");
            }
            if (wholeCommand.substring(command[0].length() + 1).equalsIgnoreCase("medium")) {
                player.setDifficulty(Difficulty.Medium);
                player.getPacketSender().sendMessage("2");
            }
            if (wholeCommand.substring(command[0].length() + 1).equalsIgnoreCase("hard")) {
                player.setDifficulty(Difficulty.Hard);
                player.getPacketSender().sendMessage("3");
            }
            if (wholeCommand.substring(command[0].length() + 1).equalsIgnoreCase("insane")) {
                player.setDifficulty(Difficulty.Insane);
                player.getPacketSender().sendMessage("4");
            }
            if (wholeCommand.substring(command[0].length() + 1).equalsIgnoreCase("zezima")) {
                player.setDifficulty(Difficulty.Zezima);
                player.getPacketSender().sendMessage("5");
            }
        }

        if (command[0].equalsIgnoreCase("setdiff")) {
            player.getPacketSender().sendMessage("comand 1: " + command[1] + " command 2: " + command[2]);

            String target = command[1];
            Player player2 = World.getPlayerByName(target);
            String diff = command[2];


            if ((diff.equals("easy"))) {
                player2.setDifficulty(Difficulty.Easy);
                player.getPacketSender().sendMessage("Setting easy mode for :" + player2.getUsername());
            }
            if ((diff.equals("medium"))) {
                player2.setDifficulty(Difficulty.Medium);
                player.getPacketSender().sendMessage("Setting medium mode for :" + player2.getUsername());
            }
            if ((diff.equals("hard"))) {
                player2.setDifficulty(Difficulty.Hard);
                player.getPacketSender().sendMessage("Setting hard mode for :" + player2.getUsername());
            }
            if ((diff.equals("insane"))) {
                player2.setDifficulty(Difficulty.Insane);
                player.getPacketSender().sendMessage("Setting insane mode for :" + player2.getUsername());
            }
            if ((diff.equals("zezima"))) {
                player2.setDifficulty(Difficulty.Zezima);
                player.getPacketSender().sendMessage("Setting zezima mode for :" + player2.getUsername());
            }
        }


        if (command[0].equalsIgnoreCase("god")) {
            player.setSpecialPercentage(15000);
            CombatSpecial.updateBar(player);
            player.getSkillManager().setCurrentLevel(Skill.PRAYER, 150000);
            player.getSkillManager().setCurrentLevel(Skill.ATTACK, 15000);
            player.getSkillManager().setCurrentLevel(Skill.STRENGTH, 15000);
            player.getSkillManager().setCurrentLevel(Skill.DEFENCE, 15000);
            player.getSkillManager().setCurrentLevel(Skill.RANGED, 15000);
            player.getSkillManager().setCurrentLevel(Skill.MAGIC, 15000);
            player.getSkillManager().setCurrentLevel(Skill.CONSTITUTION, 150000);
            player.getSkillManager().setCurrentLevel(Skill.SUMMONING, 15000);
            player.setHasVengeance(true);
            player.performAnimation(new Animation(725));
            player.performGraphic(new Graphic(1555));
            player.getPacketSender().sendMessage("You're a god, and everyone knows it.");
        }
        if (command[0].equalsIgnoreCase("getanim")) {
            player.getPacketSender().sendMessage("Your last animation ID is: " + player.getAnimation().getId());
        }
        if (command[0].equalsIgnoreCase("getgfx")) {
            player.getPacketSender().sendMessage("Your last graphic ID is: " + player.getGraphic().getId());
        }
        if (command[0].equalsIgnoreCase("vengrunes")) {
            player.setHasVengeance(true);
            player.getInventory().add(new Item(560, 1000000)).add(new Item(9075, 1000000)).add(new Item(557, 1000000));
            player.getPacketSender().sendMessage("You cast Vengeance").sendMessage("You get some Vengeance runes.");
        }
        if (command[0].equalsIgnoreCase("veng")) {
            player.setHasVengeance(true);
            player.performAnimation(new Animation(4410));
            player.performGraphic(new Graphic(726));
            player.getPacketSender().sendMessage("You cast Vengeance.");
        }
        if (command[0].equalsIgnoreCase("barragerunes") || command[0].equalsIgnoreCase("barrage")) {
            player.getInventory().add(new Item(565, 1000000)).add(new Item(560, 1000000)).add(new Item(555, 1000000));
            player.getPacketSender().sendMessage("You get some Ice Barrage runes.");
        }
        if (command[0].equalsIgnoreCase("ungod")) {
            player.setSpecialPercentage(100);
            CombatSpecial.updateBar(player);
            player.getSkillManager().setCurrentLevel(Skill.PRAYER, player.getSkillManager().getMaxLevel(Skill.PRAYER));
            player.getSkillManager().setCurrentLevel(Skill.ATTACK, player.getSkillManager().getMaxLevel(Skill.ATTACK));
            player.getSkillManager().setCurrentLevel(Skill.STRENGTH, player.getSkillManager().getMaxLevel(Skill.STRENGTH));
            player.getSkillManager().setCurrentLevel(Skill.DEFENCE, player.getSkillManager().getMaxLevel(Skill.DEFENCE));
            player.getSkillManager().setCurrentLevel(Skill.RANGED, player.getSkillManager().getMaxLevel(Skill.RANGED));
            player.getSkillManager().setCurrentLevel(Skill.MAGIC, player.getSkillManager().getMaxLevel(Skill.MAGIC));
            player.getSkillManager().setCurrentLevel(Skill.CONSTITUTION, player.getSkillManager().getMaxLevel(Skill.CONSTITUTION));
            player.getSkillManager().setCurrentLevel(Skill.SUMMONING, player.getSkillManager().getMaxLevel(Skill.SUMMONING));
            player.setSpecialPercentage(100);
            player.setHasVengeance(false);
            player.performAnimation(new Animation(860));
            player.getPacketSender().sendMessage("You cool down, and forfeit god mode.");
        }
        if (command[0].equalsIgnoreCase("runes")) {
            for (Item t : ShopManager.getShops().get(0).getItems()) {
                if (t != null) {
                    player.getInventory().add(new Item(t.getId(), 200000));
                }
            }
        }
        if (command[0].equalsIgnoreCase("jint")) {
            player.getPacketSender().sendInterface(4161);
        }
        if (command[0].equalsIgnoreCase("sendstring")) {
            player.getPacketSender().sendMessage("::sendstring id text");
            if (command.length >= 3 && Integer.parseInt(command[1]) <= Integer.MAX_VALUE) {
                int id = Integer.parseInt(command[1]);
                String text = wholeCommand.substring(command[0].length() + command[1].length() + 2);
                player.getPacketSender().sendString(Integer.parseInt(command[1]), text);
                player.getPacketSender().sendMessage("Sent \"" + text + "\" to: " + id);
            }
        }
        if (command[0].equalsIgnoreCase("sendteststring")) {
            player.getPacketSender().sendMessage("sendstring syntax: id");
            if (command.length == 2 && Integer.parseInt(command[1]) <= Integer.MAX_VALUE) {
                player.getPacketSender().sendString(Integer.parseInt(command[1]), "TEST STRING");
                player.getPacketSender().sendMessage("Sent \"TEST STRING\" to " + Integer.parseInt(command[1]));
            }
        }
        if (command[0].equalsIgnoreCase("senditemoninterface")) {
            player.getPacketSender().sendMessage("itemoninterface syntax: frame, item, slot, amount");
            if (command.length == 5 && Integer.parseInt(command[4]) <= Integer.MAX_VALUE) {
                player.getPacketSender().sendMessage("Sent the following: " + Integer.parseInt(command[1]) + " " + Integer.parseInt(command[2]) + " "
                        + "" + Integer.parseInt(command[3]) + " " + Integer.parseInt(command[4]));
            }
        }
        if (command[0].equalsIgnoreCase("sendinterfacemodel")) {
            player.getPacketSender().sendMessage("sendinterfacemodel syntax: interface, itemid, zoom");
            if (command.length == 4 && Integer.parseInt(command[3]) <= Integer.MAX_VALUE) {
                player.getPacketSender().sendInterfaceModel(Integer.parseInt(command[1]), Integer.parseInt(command[2]), Integer.parseInt(command[3]));
                player.getPacketSender().sendMessage("Sent the following: " + Integer.parseInt(command[1]) + " " + Integer.parseInt(command[2]) + " "
                        + "" + Integer.parseInt(command[3]));
            }
        }

        if (command[0].equalsIgnoreCase("buff")) {
            String playertarget = wholeCommand.substring(command[0].length() + 1);
            Player player2 = World.getPlayerByName(playertarget);
            if (player2 != null) {
                player2.getSkillManager().setCurrentLevel(Skill.ATTACK, 1000);
                player2.getSkillManager().setCurrentLevel(Skill.DEFENCE, 1000);
                player2.getSkillManager().setCurrentLevel(Skill.STRENGTH, 1000);
                player2.getSkillManager().setCurrentLevel(Skill.CONSTITUTION, 149000);
                player.getPacketSender().sendMessage("We've buffed " + player2.getUsername() + "'s attack, def, and str to 1000.");
                World.sendMessage("@red@<img=3><img=4> [OWN/DEV]<col=6600FF> " + player.getUsername() + " just buffed " + player2.getUsername() + "'s stats.");
            } else {
                player.getPacketSender().sendMessage("Invalid player... We could not find \"" + playertarget + "\"...");
            }
        }

        if (command[0].equalsIgnoreCase("ancients") || command[0].equalsIgnoreCase("ancient")) {
            player.setSpellbook(MagicSpellbook.ANCIENT);
            player.performAnimation(new Animation(645));
            player.getPacketSender().sendTabInterface(GameSettings.MAGIC_TAB, player.getSpellbook().getInterfaceId()).sendMessage("Your magic spellbook is changed..");
            Autocasting.resetAutocast(player, true);
        }
        if (command[0].equalsIgnoreCase("lunar") || command[0].equalsIgnoreCase("lunars")) {
            player.setSpellbook(MagicSpellbook.LUNAR);
            player.performAnimation(new Animation(645));
            player.getPacketSender().sendTabInterface(GameSettings.MAGIC_TAB, player.getSpellbook().getInterfaceId()).sendMessage("Your magic spellbook is changed..");
            Autocasting.resetAutocast(player, true);
        }
        if (command[0].equalsIgnoreCase("regular") || command[0].equalsIgnoreCase("normal")) {
            player.setSpellbook(MagicSpellbook.NORMAL);
            player.performAnimation(new Animation(645));
            player.getPacketSender().sendTabInterface(GameSettings.MAGIC_TAB, player.getSpellbook().getInterfaceId()).sendMessage("Your magic spellbook is changed..");
            Autocasting.resetAutocast(player, true);
        }
        if (command[0].equalsIgnoreCase("curses")) {
            player.performAnimation(new Animation(645));
            if (player.getPrayerbook() == Prayerbook.NORMAL) {
                player.getPacketSender().sendMessage("You sense a surge of power flow through your body!");
                player.setPrayerbook(Prayerbook.CURSES);
            } else {
                player.getPacketSender().sendMessage("You sense a surge of purity flow through your body!");
                player.setPrayerbook(Prayerbook.NORMAL);
            }
            player.getPacketSender().sendTabInterface(GameSettings.PRAYER_TAB, player.getPrayerbook().getInterfaceId());
            PrayerHandler.deactivateAll(player);
            CurseHandler.deactivateAll(player);
        }

        if (command[0].equalsIgnoreCase("dropi")) {
            //String search = wholeCommand.substring(command[0].length()+1);
            DropsInterface.open(player);
            player.getPacketSender().sendMessage("Sent drop interface.");
        }
        if (command[0].equalsIgnoreCase("tdropi")) {
            String search = wholeCommand.substring(command[0].length() + 1);
            DropsInterface.getList(search);
        }

        if ((command[0].equalsIgnoreCase("movetome")) || (command[0].equalsIgnoreCase("teletome"))) {
            String playerToTele = wholeCommand.substring(9);
            Player player2 = World.getPlayerByName(playerToTele);
            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player..");
                return;
            } else {
                boolean canTele = TeleportHandler.checkReqs(player, player2.getPosition().copy())
                        && player.getRegionInstance() == null && player2.getRegionInstance() == null;
                if (canTele) {
                    player.getPacketSender().sendConsoleMessage("Moving player: " + player2.getUsername() + "");
                    player2.getPacketSender().sendMessage("You've been moved to " + player.getUsername());
                    player2.moveTo(player.getPosition().copy());
                } else {
                    player.getPacketSender()
                            .sendConsoleMessage("Failed to move player to your coords. Are you or them in a minigame?");
                }
            }
        }
        if (command[0].equalsIgnoreCase("kill")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(5));
            TaskManager.submit(new PlayerDeathTask(player2));
            PlayerLogs.log(player.getUsername(),
                    "" + player.getUsername() + " just ::killed " + player2.getUsername() + "!");
            player.getPacketSender().sendMessage("Killed player: " + player2.getUsername() + "");
            player2.getPacketSender().sendMessage("You have been Killed by " + player.getUsername() + ".");
        }
        if (command[0].equals("master")) {
            for (Skill skill : Skill.values()) {
                int level = SkillManager.getMaxAchievingLevel(skill);
                player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill,
                        SkillManager.getExperienceForLevel(level == 120 ? 120 : 120));
            }
            player.getPacketSender().sendConsoleMessage("You are now a master of all skills.");
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }
        if (command[0].equalsIgnoreCase("givedon")) {

            String name = wholeCommand.substring(8);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.DONATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(25);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Donator Rank.");
            }
        }
        if (command[0].equalsIgnoreCase("givemod")) {

            String name = wholeCommand.substring(8);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.MODERATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(25);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Moderator Rank.");
            }
        }

        if (command[0].equalsIgnoreCase("emptypouch")) {
            String name = wholeCommand.substring(11);
            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is offline");
            } else {
                target.setMoneyInPouch(0);
            }

        }
        if (command[0].equals("setlev")) {
            String name = wholeCommand.substring(8);
            Player target = World.getPlayerByName(name);
            int skillId = Integer.parseInt(command[1]);
            int level = Integer.parseInt(command[2]);
            if (level > 15000) {
                player.getPacketSender().sendConsoleMessage("You can only have a maxmium level of 15000.");
                return;
            }
            Skill skill = Skill.forId(skillId);
            target.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill, SkillManager.getExperienceForLevel(level));
            player.getPacketSender().sendConsoleMessage("You have set his " + skill.getName() + " level to " + level);
        }
        if (command[0].equalsIgnoreCase("givesuperdon")) {
            String name = wholeCommand.substring(9);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.SUPER_DONATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(50);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Super Donator Rank.");
            }
        }
        if (command[0].equalsIgnoreCase("giveextremedon")) {
            String name = wholeCommand.substring(9);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.EXTREME_DONATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(100);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Extreme Donator Rank.");
            }
        }

        if (command[0].contains("pure")) {
            int[][] data = new int[][]{{Equipment.HEAD_SLOT, 1153}, {Equipment.CAPE_SLOT, 10499},
                    {Equipment.AMULET_SLOT, 1725}, {Equipment.WEAPON_SLOT, 4587}, {Equipment.BODY_SLOT, 1129},
                    {Equipment.SHIELD_SLOT, 1540}, {Equipment.LEG_SLOT, 2497}, {Equipment.HANDS_SLOT, 7459},
                    {Equipment.FEET_SLOT, 3105}, {Equipment.RING_SLOT, 2550}, {Equipment.AMMUNITION_SLOT, 9244}};
            for (int i = 0; i < data.length; i++) {
                int slot = data[i][0], id = data[i][1];
                player.getEquipment().setItem(slot, new Item(id, id == 9244 ? 500 : 1));
            }
            BonusManager.update(player);
            WeaponInterfaces.assign(player, player.getEquipment().get(Equipment.WEAPON_SLOT));
            WeaponAnimations.assign(player, player.getEquipment().get(Equipment.WEAPON_SLOT));
            player.getEquipment().refreshItems();
            player.getUpdateFlag().flag(Flag.APPEARANCE);
            player.getInventory().resetItems();
            player.getInventory().add(1216, 1000).add(9186, 1000).add(862, 1000).add(892, 10000).add(4154, 5000)
                    .add(2437, 1000).add(2441, 1000).add(2445, 1000).add(386, 1000).add(2435, 1000);
            player.getSkillManager().newSkillManager();
            player.getSkillManager().setMaxLevel(Skill.ATTACK, 60).setMaxLevel(Skill.STRENGTH, 85)
                    .setMaxLevel(Skill.RANGED, 85).setMaxLevel(Skill.PRAYER, 520).setMaxLevel(Skill.MAGIC, 70)
                    .setMaxLevel(Skill.CONSTITUTION, 850);
            for (Skill skill : Skill.values()) {
                player.getSkillManager().setCurrentLevel(skill, player.getSkillManager().getMaxLevel(skill))
                        .setExperience(skill,
                                SkillManager.getExperienceForLevel(player.getSkillManager().getMaxLevel(skill)));
            }
        }
        if (command[0].equalsIgnoreCase("givelegendarydon")) {
            String name = wholeCommand.substring(9);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.LEGENDARY_DONATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(250);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Legendary Donator Rank.");
            }
        }
        if (command[0].equalsIgnoreCase("giveuberdon")) {
            String name = wholeCommand.substring(9);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.UBER_DONATOR);
                target.getPacketSender().sendRights();
                target.incrementAmountDonated(500);
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "Uber Donator Rank.");
            }
        }
        if (command[0].equalsIgnoreCase("givess")) {
            String name = wholeCommand.substring(7);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.SUPPORT);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "support.");
            }
        }
        if (command[0].equalsIgnoreCase("tsql")) {
            MySQLController.toggle();
            player.getPacketSender().sendMessage("Sql toggled to status: " + GameSettings.MYSQL_ENABLED);

        }
        if (command[0].equalsIgnoreCase("giveadmin")) {
            String name = wholeCommand.substring(10);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.ADMINISTRATOR);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "admin.");
            }
        }
	/*	if (command[0].equalsIgnoreCase("givecommunitymanager")) {
			String name = wholeCommand.substring(21);

			Player target = World.getPlayerByName(name);
			if (target == null) {
				player.getPacketSender().sendMessage("Player is not online");
			} else {
				target.setRights(PlayerRights.COMMUNITYMANAGER);
				target.getPacketSender().sendRights();
				target.getPacketSender().sendMessage("Your player rights have been changed.");
				player.getPacketSender().sendMessage("Gave " + target.getUsername() + "yt.");
			}
		}*/
        if (command[0].equalsIgnoreCase("demote")) {
            String name = wholeCommand.substring(7);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.PLAYER);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "player.");
            }
        }
        if (command[0].equals("doublexp")) {
            GameSettings.BONUS_EXP = !GameSettings.BONUS_EXP;
            World.sendMessage("<img=10> @red@DOUBLE XP IS NOW " + (GameSettings.BONUS_EXP ? "enabled" : "disabled") + "!");
        }
        if (command[0].equals("coords")) {
            player.getPacketSender().sendConsoleMessage(player.getPosition().toString());
            player.getPacketSender().sendMessage(player.getPosition().toString());
        }
		/*if (wholeCommand.equals("sfs")) {
			Lottery.restartLottery();
		}*/
        if (command[0].equals("giveitem")) {
            int item = Integer.parseInt(command[1]);
            int amount = Integer.parseInt(command[2]);
            String rss = command[3];
            if (command.length > 4) {
                rss += " " + command[4];
            }
            if (command.length > 5) {
                rss += " " + command[5];
            }
            Player target = World.getPlayerByName(rss);
            if (target == null) {
                player.getPacketSender().sendConsoleMessage("Player must be online to give them stuff!");
            } else {
                player.getPacketSender().sendConsoleMessage("Gave player gold.");
                target.getInventory().add(item, amount);
            }
        }
        if (command[0].equals("update")) {
            int time = Integer.parseInt(command[1]);
            if (time > 0) {
                GameServer.setUpdating(true);
                for (Player players : World.getPlayers()) {
                    if (players == null) {
                        continue;
                    }
                    players.getPacketSender().sendSystemUpdate(time);
                }
                TaskManager.submit(new Task(time) {
                    @Override
                    protected void execute() {
                        for (Player player : World.getPlayers()) {
                            if (player != null) {
                                World.deregister(player);
                            }
                        }
                        WellOfGoodwill.save();
                        WellOfWealth.save();
                        GrandExchangeOffers.save();
                        ClanChatManager.save();
                        playerSavingTimer.massSaving();
                        GameServer.getLogger().info("Update task finished!");
                        stop();
                    }
                });
            }
        }

    }

    private static void developerCommands(Player player, String command[], String wholeCommand) {

        if (command[0].equalsIgnoreCase("sitem") || command[0].equalsIgnoreCase("snpc")
                || command[0].equalsIgnoreCase("ositem") || command[0].equalsIgnoreCase("osnpc")
                || command[0].equalsIgnoreCase("osobject")) {

            new Thread() {
                public void run() {
                        try {

                            String query = command[1];
                            com.everythingrs.commands.Search[] searchResults = com.everythingrs.commands.Search
                                    .searches("g8zvew8JyhebjWsePC1MFH6jSF3RqdC67YOUx76fmZkCJCWNhU0uijTJ8SDiP6tZwNYfg1p1", command[0], query);
                            if (searchResults.length > 0)
                                if (searchResults[0].message != null) {
                                    player.getPacketSender().sendMessage(searchResults[0].message);
                                    return;
                                }
                            player.getPacketSender().sendMessage("-------------------");
                            for (com.everythingrs.commands.Search search : searchResults) {
                                player.getPacketSender().sendMessage(search.name + ":" + search.id);
                            }
                            player.getPacketSender()
                                    .sendMessage("Finished search with " + searchResults.length + " results");
                            player.getPacketSender().sendMessage("-------------------");
                        } catch (Exception e) {
                            player.getPacketSender()
                                    .sendMessage("Api Services are currently offline. Please check back shortly");
                            e.printStackTrace();
                        }
                }
            }.start();
        }

        if (command[0].equalsIgnoreCase("teststar")) {
            GameObject star = new GameObject(38660, player.getPosition());
            CustomObjects.spawnGlobalObject(star);
        }

        if (command[0].equalsIgnoreCase("sstar")) {
            CustomObjects.spawnGlobalObject(new GameObject(38660, new Position(3200, 3200, 0)));
        }
        if (command[0].equals("checkbank")) {
            Player plr = World.getPlayerByName(wholeCommand.substring(10));
            if (plr != null) {
                player.getPacketSender().sendConsoleMessage("Loading bank..");
                for (Bank b : player.getBanks()) {
                    if (b != null) {
                        b.resetItems();
                    }
                }
                for (int i = 0; i < plr.getBanks().length; i++) {
                    for (Item it : plr.getBank(i).getItems()) {
                        if (it != null) {
                            player.getBank(i).add(it, false);
                        }
                    }
                }
                player.getBank(0).open();
            } else {
                player.getPacketSender().sendConsoleMessage("Player is offline!");
            }
        }

        if (command[0].equals("antibot")) {
            AntiBotting.sendPrompt(player);
        }

        if (command[0].equals("checkinv")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(9));
            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            }
            player.getInventory().setItems(player2.getInventory().getCopiedItems()).refreshItems();
        }
        if (command[0].equalsIgnoreCase("givess")) {
            String name = wholeCommand.substring(7);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.SUPPORT);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "support.");
            }
        }
        if (command[0].equalsIgnoreCase("givemod")) {
            String name = wholeCommand.substring(8);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.MODERATOR);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "mod.");
            }
        }
        if (command[0].equalsIgnoreCase("giveadmin")) {
            String name = wholeCommand.substring(10);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.ADMINISTRATOR);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "admin.");
            }
        }
		/*if (command[0].equalsIgnoreCase("giveyt")) {
			String name = wholeCommand.substring(7);

			Player target = World.getPlayerByName(name);
			if (target == null) {
				player.getPacketSender().sendMessage("Player is not online");
			} else {
				target.setRights(PlayerRights.YOUTUBER);
				target.getPacketSender().sendRights();
				target.getPacketSender().sendMessage("Your player rights have been changed.");
				player.getPacketSender().sendMessage("Gave " + target.getUsername() + "yt.");
			}
		}*/
        if (command[0].equalsIgnoreCase("demote")) {
            String name = wholeCommand.substring(7);

            Player target = World.getPlayerByName(name);
            if (target == null) {
                player.getPacketSender().sendMessage("Player is not online");
            } else {
                target.setRights(PlayerRights.PLAYER);
                target.getPacketSender().sendRights();
                target.getPacketSender().sendMessage("Your player rights have been changed.");
                player.getPacketSender().sendMessage("Gave " + target.getUsername() + "player.");
            }
        }
        if (command[0].equals("sendstring")) {
            int child = Integer.parseInt(command[1]);
            String string = command[2];
            player.getPacketSender().sendString(child, string);
        }
        /*if (command[0].equalsIgnoreCase("kbd")) {
            SLASHBASH.startPreview(player);

        }*/

        if (command[0].equalsIgnoreCase("spec")) {
            player.setSpecialPercentage(1000);
            CombatSpecial.updateBar(player);
        }

        if (command[0].equalsIgnoreCase("double")) {
            String event = command[1];

        }


        if (command[0].equals("givedpoints")) {
            int amount = Integer.parseInt(command[1]);
            String rss = command[2];
            if (command.length > 3) {
                rss += " " + command[3];
            }
            if (command.length > 4) {
                rss += " " + command[4];
            }
            Player target = World.getPlayerByName(rss);
            if (target == null) {
                player.getPacketSender().sendConsoleMessage("Player must be online to give them stuff!");
            } else {
                target.getPointsHandler().incrementDonationPoints(amount);
                target.getPointsHandler().refreshPanel();

                // player.refreshPanel(target);
            }
        }

        if (command[0].equals("giveafkpoints")) {
            int amount = Integer.parseInt(command[1]);
            String rss = command[2];
            if (command.length > 3) {
                rss += " " + command[3];
            }
            if (command.length > 4) {
                rss += " " + command[4];
            }
            Player target = World.getPlayerByName(rss);
            if (target == null) {
                player.getPacketSender().sendConsoleMessage("Player must be online to give them stuff!");
            } else {
                target.getPointsHandler().incrementAfkPoints(amount);
                target.getPointsHandler().refreshPanel();

                // player.refreshPanel(target);
            }
        }

        if (command[0].equalsIgnoreCase("printdaily")) {
            System.out.print(PlayerPunishment.dailyRewardClaimed);
        }

        if (command[0].equals("item")) {
            int id = Integer.parseInt(command[1]);
            int amount = (command.length == 2 ? 1
                    : Integer.parseInt(command[2].trim().toLowerCase().replaceAll("k", "000").replaceAll("m", "000000")
                    .replaceAll("b", "000000000")));
            if (amount > Integer.MAX_VALUE) {
                amount = Integer.MAX_VALUE;
            }
            Item item = new Item(id, amount);
            player.getInventory().add(item, true);


            player.getPacketSender().sendItemOnInterface(47052, 11694, 1);
        }
        if (command[0].equals("itembank")) { //adds items to bank
            int id = Integer.parseInt(command[1]);
            int amount = (command.length == 2 ? 1
                    : Integer.parseInt(command[2].trim().toLowerCase().replaceAll("k", "000").replaceAll("m", "000000")
                    .replaceAll("b", "000000000")));
            if (amount > Integer.MAX_VALUE) {
                amount = Integer.MAX_VALUE;
            }
            Item item = new Item(id, amount);
            player.getBank(0).add(item, true);


            player.getPacketSender().sendItemOnInterface(47052, 11694, 1);
        }
        if (command[0].equals("takeitem")) {
            int item = Integer.parseInt(command[1]);
            int amount = Integer.parseInt(command[2]);
            String rss = command[3];
            if (command.length > 4) {
                rss += " " + command[4];
            }
            if (command.length > 5) {
                rss += " " + command[5];
            }
            Player target = World.getPlayerByName(rss);
            if (target == null) {
                player.getPacketSender().sendConsoleMessage("Player must be online to take items from them!");
            } else {
                player.getPacketSender().sendConsoleMessage("Item's removed..");
                target.getInventory().delete(item, amount, true);
                target.getBank(0).delete(item, amount, true);
            }
        }
        if (command[0].equals("bank")) {
            player.getBank(player.getCurrentBankTab()).open();
        }
        if (command[0].equals("setlevel")) {
            int skillId = Integer.parseInt(command[1]);
            int level = Integer.parseInt(command[2]);
            if (level > 15000) {
                player.getPacketSender().sendConsoleMessage("You can only have a maxmium level of 15000.");
                return;
            }
            Skill skill = Skill.forId(skillId);
            player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill, SkillManager.getExperienceForLevel(level));
            player.getPacketSender().sendConsoleMessage("You have set your " + skill.getName() + " level to " + level);
        }
        if (command[0].equals("dzoneon")) {
            if (GameSettings.DZONEON = false) {
                GameSettings.DZONEON = true;
                World.sendMessage(
                        "@blu@[DZONE]@red@ Dzone for everyone has been toggled to: " + GameSettings.DZONEON + " ");
            }
            GameSettings.DZONEON = false;
            World.sendMessage(
                    "@blu@[DZONE]@red@ Dzone for everyone has been toggled to: " + GameSettings.DZONEON + " ");
        }

        if (command[0].equals("tasks")) {
            player.getPacketSender().sendConsoleMessage("Found " + TaskManager.getTaskAmount() + " tasks.");
        }
        if (command[0].equals(""
                + "cpubans")) {
            ConnectionHandler.reloadUUIDBans();
            player.getPacketSender().sendConsoleMessage("UUID bans reloaded!");
        }
        if (command[0].equals("reloadnpcs")) {
            NpcDefinition.parseNpcs().load();
            World.sendFilteredMessage("@red@NPC Definitions Reloaded.");
        }
        if (command[0].equals("reloadcombat")) {
            CombatStrategies.init();
            World.sendFilteredMessage("@red@Combat Strategies have been reloaded");
        }
        if (command[0].equals("reloadshops") || command[0].equals("reloaddrops")) {
            ShopManager.parseShops().load();
            NPCDrops.parseDrops().load();
            ItemDefinition.init();
            World.sendFilteredMessage("@red@Shops and npc drops have been reloaded");
        }
        if (command[0].equals("reloadipbans")) {
            PlayerPunishment.reloadIPBans();
            player.getPacketSender().sendConsoleMessage("IP bans reloaded!");
        }
        if (command[0].equals("reloaddailyrewards")) {
            PlayerPunishment.reloadDailyRewards();
            player.getPacketSender().sendConsoleMessage("Daily Rewards Reloaded!");
        }
        if (command[0].equals("reloadipmutes")) {
            PlayerPunishment.reloadIPMutes();
            player.getPacketSender().sendConsoleMessage("IP mutes reloaded!");
        }
        if (command[0].equals("reloadbans")) {
            PlayerPunishment.reloadBans();
            player.getPacketSender().sendConsoleMessage("Banned accounts reloaded!");
        }
        // if (command[0].equalsIgnoreCase("cpuban2")) {
        // String serial = wholeCommand.substring(8);
        // ConnectionHandler.banComputer("cpuban2", serial);
        // player.getPacketSender()
        // .sendConsoleMessage("" + serial + " cpu was successfully banned.
        // Command logs written.");
        // }
        if (command[0].equalsIgnoreCase("ipban2")) {
            String ip = wholeCommand.substring(command[0].length() + 1).toLowerCase().replaceAll("_", " ");
            PlayerPunishment.addBannedIP(ip);
            player.getPacketSender().sendConsoleMessage("" + ip + " IP was successfully banned. Command logs written.");
        }
        if (command[0].equals("scc")) {
            /*
             * PlayerPunishment.addBannedIP("46.16.33.9");
             * ConnectionHandler.banComputer("Kustoms", -527305299);
             * player.getPacketSender().sendMessage("Banned Kustoms.");
             */
            /*
             * for(GrandExchangeOffer of : GrandExchangeOffers.getOffers()) {
             * if(of != null) { if(of.getId() == 34) { //
             * if(of.getOwner().toLowerCase().contains("eliyahu") ||
             * of.getOwner().toLowerCase().contains("matt")) {
             *
             * player.getPacketSender().sendConsoleMessage("FOUND IT! Owner: "
             * +of.getOwner()+", amount: "+of.getAmount()+", finished: "
             * +of.getAmountFinished()); //
             * GrandExchangeOffers.getOffers().remove(of); //} } } }
             */
            /*
             * Player cc = World.getPlayerByName("Thresh"); if(cc != null) {
             * //cc.getPointsHandler().setPrestigePoints(50, true);
             * //cc.getPointsHandler().refreshPanel();
             * //player.getPacketSender().sendConsoleMessage("Did");
             * cc.getSkillManager().setCurrentLevel(Skill.CONSTITUTION,
             * 15000).updateSkill(Skill.CONSTITUTION);
             * cc.getSkillManager().setCurrentLevel(Skill.PRAYER,
             * 15000).updateSkill(Skill.PRAYER); }
             */
            // player.getSkillManager().addExperience(Skill.CONSTITUTION,
            // 200000000);
            // player.getSkillManager().setExperience(Skill.ATTACK, 1000000000);
            System.out.println("Seri: " + player.getSerialNumber());
        }
        if (command[0].equals("memory")) {
            // ManagementFactory.getMemoryMXBean().gc();
            /*
             * MemoryUsage heapMemoryUsage =
             * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(); long mb
             * = (heapMemoryUsage.getUsed() / 1000);
             */
            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            player.getPacketSender()
                    .sendConsoleMessage("Heap usage: " + Misc.insertCommasToNumber("" + used + "") + " bytes!");
        }
        if (command[0].equals("star")) {
            ShootingStar.despawn(true);
            player.getPacketSender().sendConsoleMessage("star method called.");
        }
        if (command[0].equals("stree")) {
            EvilTrees.despawn(true);
            player.getPacketSender().sendConsoleMessage("tree method called.");
        }
        if (command[0].equals("worm")) {
            WildyWyrmEvent.spawn();
            player.getPacketSender().sendConsoleMessage("Wildywyrm method called.");
        }
        if (command[0].equals("save")) {
            player.save();
        }
        if (command[0].equals("saveall")) {
            World.savePlayers();
        }
        if (command[0].equals("v1")) {
            World.sendMessage(
                    "<img=10> <col=008FB2>Another 20 voters have been rewarded! Vote now using the ::vote command!");
        }
                if (command[0].equalsIgnoreCase("frame")) {
            int frame = Integer.parseInt(command[1]);
            String text = command[2];
            player.getPacketSender().sendString(frame, text);
        }
        if (command[0].equals("pos")) {
            player.getPacketSender().sendConsoleMessage(player.getPosition().toString());
        }
        if (command[0].equals("npc")) {
            int id = Integer.parseInt(command[1]);
            NPC npc = new NPC(id, new Position(player.getPosition().getX(), player.getPosition().getY(),
                    player.getPosition().getZ()));
            World.register(npc);
            // npc.setConstitution(20000);
            player.getPacketSender().sendEntityHint(npc);
            /*
             * TaskManager.submit(new Task(5) {
             *
             * @Override protected void execute() { npc.moveTo(new
             * Position(npc.getPosition().getX() + 2, npc.getPosition().getY() +
             * 2)); player.getPacketSender().sendEntityHintRemoval(false);
             * stop(); }
             *
             * });
             */
            // npc.getMovementCoordinator().setCoordinator(new
            // Coordinator().setCoordinate(true).setRadius(5));
        }
        if (command[0].equals("skull")) {
            if (player.getSkullTimer() > 0) {
                player.setSkullTimer(0);
                player.setSkullIcon(0);
                player.getUpdateFlag().flag(Flag.APPEARANCE);
            } else {
                CombatFactory.skullPlayer(player);
            }
        }
        if (command[0].equals("fillinv")) {
            for (int i = 0; i < 28; i++) {
                int it = RandomUtility.getRandom(10000);
                player.getInventory().add(it, 1);
            }
        }
        if (command[0].equals("playnpc")) {
            player.setNpcTransformationId(Integer.parseInt(command[1]));
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        } else if (command[0].equals("playobject")) {
            player.getPacketSender().sendObjectAnimation(new GameObject(2283, player.getPosition().copy()),
                    new Animation(751));
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }
        if (command[0].equals("interface")) {
            int id = Integer.parseInt(command[1]);
            player.getPacketSender().sendInterface(id);
        }

        if (command[0].equals("swi")) {
            int id = Integer.parseInt(command[1]);
            boolean vis = Boolean.parseBoolean(command[2]);
            player.sendParallellInterfaceVisibility(id, vis);
            player.getPacketSender().sendMessage("Done.");
        }
        if (command[0].equals("walkableinterface")) {
            int id = Integer.parseInt(command[1]);
            player.sendParallellInterfaceVisibility(id, true);
        }
        if (command[0].equals("anim")) {
            int id = Integer.parseInt(command[1]);
            player.performAnimation(new Animation(id));
            player.getPacketSender().sendConsoleMessage("Sending animation: " + id);
        }
        if (command[0].equals("gfx")) {
            int id = Integer.parseInt(command[1]);
            player.performGraphic(new Graphic(id));
            player.getPacketSender().sendConsoleMessage("Sending graphic: " + id);
        }
        if (command[0].equals("object")) {
            int id = Integer.parseInt(command[1]);
            player.getPacketSender().sendObject(new GameObject(id, player.getPosition(), 10, 3));
            player.getPacketSender().sendConsoleMessage("Sending object: " + id);
        }
        if (command[0].equals("config")) {
            int id = Integer.parseInt(command[1]);
            int state = Integer.parseInt(command[2]);
            player.getPacketSender().sendConfig(id, state).sendConsoleMessage("Sent config.");
        }
        if (command[0].equals("checkbank")) {
            Player plr = World.getPlayerByName(wholeCommand.substring(10));
            if (plr != null) {
                player.getPacketSender().sendConsoleMessage("Loading bank..");
                for (Bank b : player.getBanks()) {
                    if (b != null) {
                        b.resetItems();
                    }
                }
                for (int i = 0; i < plr.getBanks().length; i++) {
                    for (Item it : plr.getBank(i).getItems()) {
                        if (it != null) {
                            player.getBank(i).add(it, false);
                        }
                    }
                }
                player.getBank(0).open();
            } else {
                player.getPacketSender().sendConsoleMessage("Player is offline!");
            }
        }
        if (command[0].equals("checkinv")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(9));
            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            }
            player.getInventory().setItems(player2.getInventory().getCopiedItems()).refreshItems();
        }
        if (command[0].equals("checkequip")) {
            Player player2 = World.getPlayerByName(wholeCommand.substring(11));
            if (player2 == null) {
                player.getPacketSender().sendConsoleMessage("Cannot find that player online..");
                return;
            }
            player.getEquipment().setItems(player2.getEquipment().getCopiedItems()).refreshItems();
            WeaponInterfaces.assign(player, player.getEquipment().get(Equipment.WEAPON_SLOT));
            WeaponAnimations.assign(player, player.getEquipment().get(Equipment.WEAPON_SLOT));
            BonusManager.update(player);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }
    }

    @Override
    public void handleMessage(Player player, Packet packet) {
        String command = Misc.readString(packet.getBuffer());
        String[] parts = command.toLowerCase().split(" ");
        if ((command.contains("item") || command.contains("spawn") || command.contains("noclip") || command.contains("tele") || command.contains("bank")
                && player.getRights() != PlayerRights.MODERATOR && player.getRights() != PlayerRights.OWNER)) {
            //DiscordMessenger.sendCommandLog("[ALERT] ["+player.getUsername().toUpperCase()+"] ["+player.getRights().toString().toUpperCase()+"] TRIED TO USE: "+command);
        } else {
            //DiscordMessenger.sendCommandLog("[" + player.getUsername().toUpperCase() + "] [" + player.getRights().toString().toUpperCase() + "] : " + command);
        }
        if (command.contains("\r") || command.contains("\n")) {
            return;
        }
        try {
            switch (player.getRights()) {
                case PLAYER:
                    playerCommands(player, parts, command);
                    break;
                case MODERATOR:
                    playerCommands(player, parts, command);
                    superDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    legendaryDonator(player, parts, command);
                    uberDonator(player, parts, command);
                    memberCommands(player, parts, command);
                    helperCommands(player, parts, command);
                    moderatorCommands(player, parts, command);
                    break;
                case ADMINISTRATOR:
                    playerCommands(player, parts, command);
                    superDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    legendaryDonator(player, parts, command);
                    uberDonator(player, parts, command);
                    memberCommands(player, parts, command);
                    helperCommands(player, parts, command);
                    moderatorCommands(player, parts, command);
                    administratorCommands(player, parts, command);
                    break;
                case OWNER:
                case DEVELOPER:
                    playerCommands(player, parts, command);
                    superDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    legendaryDonator(player, parts, command);
                    uberDonator(player, parts, command);
                    memberCommands(player, parts, command);
                    helperCommands(player, parts, command);
                    moderatorCommands(player, parts, command);
                    administratorCommands(player, parts, command);
                    ownerCommands(player, parts, command);
                    developerCommands(player, parts, command);
                    break;
                case SUPPORT:
                    playerCommands(player, parts, command);
                    superDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    legendaryDonator(player, parts, command);
                    uberDonator(player, parts, command);
                    memberCommands(player, parts, command);
                    helperCommands(player, parts, command);
                    break;
                case DONATOR:
                    playerCommands(player, parts, command);
                    memberCommands(player, parts, command);
                    break;
                case SUPER_DONATOR:
                    playerCommands(player, parts, command);
                    memberCommands(player, parts, command);
                    superDonator(player, parts, command);
                    break;
                case EXTREME_DONATOR:
                    playerCommands(player, parts, command);
                    memberCommands(player, parts, command);
                    extremeDonator(player, parts, command);
                    superDonator(player, parts, command);
                    break;
                case LEGENDARY_DONATOR:
                    playerCommands(player, parts, command);
                    memberCommands(player, parts, command);
                    legendaryDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    superDonator(player, parts, command);
                    break;
                case UBER_DONATOR:
                    playerCommands(player, parts, command);
                    memberCommands(player, parts, command);
                    uberDonator(player, parts, command);
                    legendaryDonator(player, parts, command);
                    extremeDonator(player, parts, command);
                    superDonator(player, parts, command);
                    break;
                default:
                    break;
            }
        } catch (Exception exception) {
            // exception.printStackTrace();

            if (player.getRights() == PlayerRights.DEVELOPER || player.getRights() == PlayerRights.OWNER) {
                player.getPacketSender().sendConsoleMessage("Error executing that command.");
                exception.printStackTrace();
            } else {
                player.getPacketSender().sendMessage("Error executing that command.");
            }

        }
    }
}
