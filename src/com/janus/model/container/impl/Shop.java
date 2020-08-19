package com.janus.model.container.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.janus.engine.task.TaskManager;
import com.janus.engine.task.impl.ShopRestockTask;
import com.janus.model.GameMode;
import com.janus.model.Item;
import com.janus.model.PlayerRights;
import com.janus.model.Skill;
import com.janus.model.container.ItemContainer;
import com.janus.model.container.StackType;
import com.janus.model.definitions.ItemDefinition;
import com.janus.model.input.impl.EnterAmountToBuyFromShop;
import com.janus.model.input.impl.EnterAmountToSellToShop;
import com.janus.util.JsonLoader;
import com.janus.util.Misc;
import com.janus.world.World;
import com.janus.world.content.minigames.impl.RecipeForDisaster;
import com.janus.world.entity.impl.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Messy but perfect Shop System
 *
 * @author Gabriel Hannason
 */

public class Shop extends ItemContainer {

    /*
     * The shop constructor
     */
    public Shop(Player player, int id, String name, Item currency, Item[] stockItems) {
        super(player);
        if (stockItems.length > 42)
            throw new ArrayIndexOutOfBoundsException(
                    "Stock cannot have more than 40 items; check shop[" + id + "]: stockLength: " + stockItems.length);
        this.id = id;
        this.name = name.length() > 0 ? name : "General Store";
        this.currency = currency;
        this.originalStock = new Item[stockItems.length];
        for (int i = 0; i < stockItems.length; i++) {
            Item item = new Item(stockItems[i].getId(), stockItems[i].getAmount());
            add(item, false);
            this.originalStock[i] = item;
        }
    }

    private final int id;

    private String name;

    private Item currency;

    private Item[] originalStock;

    public Item[] getOriginalStock() {
        return this.originalStock;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public Shop setName(String name) {
        this.name = name;
        return this;
    }

    public Item getCurrency() {
        return currency;
    }

    public Shop setCurrency(Item currency) {
        this.currency = currency;
        return this;
    }

    private boolean restockingItems;

    public boolean isRestockingItems() {
        return restockingItems;
    }

    public void setRestockingItems(boolean restockingItems) {
        this.restockingItems = restockingItems;
    }

    /**
     * Opens a shop for a player
     *
     * @param player The player to open the shop for
     * @return The shop instance
     */
    public Shop open(Player player) {
        setPlayer(player);
        getPlayer().getPacketSender().sendInterfaceRemoval().sendClientRightClickRemoval();
        getPlayer().setShop(ShopManager.getShops().get(id)).setInterfaceId(INTERFACE_ID).setShopping(true);
        refreshItems();
        if (Misc.getMinutesPlayed(getPlayer()) <= 190)
            getPlayer().getPacketSender()
                    .sendMessage("Note: When selling an item to a store, it loses 15% of its original value.");
        return this;
    }

    /**
     * Refreshes a shop for every player who's viewing it
     */
    public void publicRefresh() {
        Shop publicShop = ShopManager.getShops().get(id);
        if (publicShop == null)
            return;
        publicShop.setItems(getItems());
        for (Player player : World.getPlayers()) {
            if (player == null)
                continue;
            if (player.getShop() != null && player.getShop().id == id && player.isShopping())
                player.getShop().setItems(publicShop.getItems());
        }
    }

    /**
     * Checks a value of an item in a shop
     *
     * @param player      The player who's checking the item's value
     * @param slot        The shop item's slot (in the shop!)
     * @param sellingItem Is the player selling the item?
     */
    public void checkValue(Player player, int slot, boolean sellingItem) {
        this.setPlayer(player);
        Item shopItem = new Item(getItems()[slot].getId());
        if (!player.isShopping()) {
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }
        Item item = sellingItem ? player.getInventory().getItems()[slot] : getItems()[slot];
        if (item.getId() == 995)
            return;
        if (sellingItem) {
            if (!shopBuysItem(id, item)) {
                player.getPacketSender().sendMessage("You cannot sell this item to this store.");
                return;
            }
        }
        int finalValue = 0;
        String finalString = sellingItem ? "" + ItemDefinition.forId(item.getId()).getName() + ": shop will buy for "
                : "" + ItemDefinition.forId(shopItem.getId()).getName() + " currently costs ";
        if (getCurrency().getId() != -1) {
            finalValue = ItemDefinition.forId(item.getId()).getValue();
            String s = currency.getDefinition().getName().toLowerCase().endsWith("s")
                    ? currency.getDefinition().getName().toLowerCase()
                    : currency.getDefinition().getName().toLowerCase() + "s";
            /** CUSTOM CURRENCY, CUSTOM SHOP VALUES **/
            if (id == TOKKUL_EXCHANGE_STORE || id == ENERGY_FRAGMENT_STORE || id == STARDUST_STORE || id == AGILITY_TICKET_STORE
                    || id == GRAVEYARD_STORE || id == HOLY_WATER_STORE || id == AFK_STORE) {
                Object[] obj = ShopManager.getCustomShopData(id, item.getId());
                if (obj == null)
                    return;
                finalValue = (int) obj[0];
                s = (String) obj[1];
            }
            if (sellingItem) {
                if (finalValue != 1) {
                    finalValue = (int) (finalValue * 0.85);
                }
            }
            finalString += "" + (int) finalValue + " " + s + "" + shopPriceEx((int) finalValue) + ".";
        } else {
            Object[] obj = ShopManager.getCustomShopData(id, item.getId());
            if (obj == null)
                return;
            finalValue = (int) obj[0];
            if (sellingItem) {
                if (finalValue != 1) {
                    finalValue = (int) (finalValue * 0.85);
                }
            }
            finalString += "" + finalValue + " " + (String) obj[1] + ".";
        }
        if (player != null && finalValue > 0) {
            player.getPacketSender().sendMessage(finalString);
            return;
        }
    }

    public void sellItem(Player player, int slot, int amountToSell) {
        this.setPlayer(player);
        if (!player.isShopping() || player.isBanking()) {
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }

        if (!player.isShopping() || player.isBanking()) {
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }
        Item itemToSell = player.getInventory().getItems()[slot];
        if (!itemToSell.sellable()) {
            player.getPacketSender().sendMessage("This item cannot be sold.");
            return;
        }
        if (!shopBuysItem(id, itemToSell)) {
            player.getPacketSender().sendMessage("You cannot sell this item to this store.");
            return;
        }
        if (!player.getInventory().contains(itemToSell.getId()) || itemToSell.getId() == 995)
            return;
        if (this.full(itemToSell.getId()))
            return;
        if (player.getInventory().getAmount(itemToSell.getId()) < amountToSell)
            amountToSell = player.getInventory().getAmount(itemToSell.getId());
        if (amountToSell == 0)
            return;
        /*
         * if(amountToSell > 300) { String s =
         * ItemDefinition.forId(itemToSell.getId()).getName().endsWith("s") ?
         * ItemDefinition.forId(itemToSell.getId()).getName() :
         * ItemDefinition.forId(itemToSell.getId()).getName() + "s";
         * player.getPacketSender().sendMessage("You can only sell 300 "+s+
         * " at a time."); return; }
         */
        int itemId = itemToSell.getId();
        boolean customShop = this.getCurrency().getId() == -1;
        boolean inventorySpace = customShop ? true : false;
        if (!customShop) {
            if (!itemToSell.getDefinition().isStackable()) {
                if (!player.getInventory().contains(this.getCurrency().getId()))
                    inventorySpace = true;
            }
            if (player.getInventory().getFreeSlots() <= 0
                    && player.getInventory().getAmount(this.getCurrency().getId()) > 0)
                inventorySpace = true;
            if (player.getInventory().getFreeSlots() > 0
                    || player.getInventory().getAmount(this.getCurrency().getId()) > 0)
                inventorySpace = true;
        }
        int itemValue = 0;
        if (getCurrency().getId() > 0) {
            itemValue = ItemDefinition.forId(itemToSell.getId()).getValue();
        } else {
            Object[] obj = ShopManager.getCustomShopData(id, itemToSell.getId());
            if (obj == null)
                return;
            itemValue = (int) obj[0];
        }
        if (itemValue <= 0)
            return;
        itemValue = (int) (itemValue * 0.85);
        if (itemValue <= 0) {
            itemValue = 1;
        }
        for (int i = amountToSell; i > 0; i--) {
            itemToSell = new Item(itemId);
            if (this.full(itemToSell.getId()) || !player.getInventory().contains(itemToSell.getId())
                    || !player.isShopping())
                break;
            if (!itemToSell.getDefinition().isStackable()) {
                if (inventorySpace) {
                    super.switchItem(player.getInventory(), this, itemToSell.getId(), -1);
                    if (!customShop) {
                        player.getInventory().add(new Item(getCurrency().getId(), itemValue), false);
                    } else {
                        // Return points here
                    }
                } else {
                    player.getPacketSender().sendMessage("Please free some inventory space before doing that.");
                    break;
                }
            } else {
                if (inventorySpace) {
                    super.switchItem(player.getInventory(), this, itemToSell.getId(), amountToSell);
                    if (!customShop) {
                        player.getInventory().add(new Item(getCurrency().getId(), itemValue * amountToSell), false);
                    } else {
                        // Return points here
                    }
                    break;
                } else {
                    player.getPacketSender().sendMessage("Please free some inventory space before doing that.");
                    break;
                }
            }
            amountToSell--;
        }
        if (customShop) {
            player.getPointsHandler().refreshPanel();
        }
        player.getInventory().refreshItems();
        fireRestockTask();
        refreshItems();
        publicRefresh();
    }

    /**
     * Buying an item from a shop
     */
    @Override
    public Shop switchItem(ItemContainer to, Item item, int slot, boolean sort, boolean refresh) {
        final Player player = getPlayer();
        if (player == null)
            return this;
        if (!player.isShopping() || player.isBanking()) {
            player.getPacketSender().sendInterfaceRemoval();
            return this;
        }
        if (this.id == GENERAL_STORE) {
            if (player.getGameMode() == GameMode.IRONMAN) {
                player.getPacketSender()
                        .sendMessage("Ironman-players are not allowed to buy items from the general-store.");
                return this;
            }
            if (player.getGameMode() == GameMode.HARDCORE_IRONMAN) {
                player.getPacketSender()
                        .sendMessage("Hardcore-ironman-players are not allowed to buy items from the general-store.");
                return this;
            }
        }
        if (!shopSellsItem(item))
            return this;

        if (getItems()[slot].getAmount() <= 1 && id != GENERAL_STORE) {

            player.getPacketSender()
                    .sendMessage("The shop can't be 1 items and needs to regenerate some items first..");

        }

        if (item.getAmount() > getItems()[slot].getAmount())
            item.setAmount(getItems()[slot].getAmount());
        int amountBuying = item.getAmount();
        if (id == 21) { //farming cheapfix
            if (getItems()[slot].getAmount() - amountBuying <= 1) {
                amountBuying = getItems()[slot].getAmount() - 1;
                while (getItems()[slot].getAmount() - amountBuying <= 1) {
                    if (getItems()[slot].getAmount() - amountBuying == 1) break;
                    amountBuying--;
                }
            }
        }
        if (getItems()[slot].getAmount() < amountBuying) {
            amountBuying = getItems()[slot].getAmount() - 101;
        }
        if (amountBuying == 0)
            return this;

        if (amountBuying > 5000) {
            player.getPacketSender().sendMessage(
                    "You can only buy 5000 " + ItemDefinition.forId(item.getId()).getName() + "s at a time.");
            return this;
        }
        boolean customShop = getCurrency().getId() == -1;
        boolean usePouch = false;
        int playerCurrencyAmount = 0;
        int value = ItemDefinition.forId(item.getId()).getValue();
        String currencyName = "";
        if (getCurrency().getId() != -1) {
            playerCurrencyAmount = player.getInventory().getAmount(currency.getId());
            currencyName = ItemDefinition.forId(currency.getId()).getName().toLowerCase();
            if (currency.getId() == 995) {
                if (player.getMoneyInPouch() >= value) {
                    playerCurrencyAmount = player.getMoneyInPouchAsInt();
                    if (!(player.getInventory().getFreeSlots() == 0
                            && player.getInventory().getAmount(currency.getId()) == value)) {
                        usePouch = true;
                    }
                }
            } else {
                /** CUSTOM CURRENCY, CUSTOM SHOP VALUES **/
                if (id == TOKKUL_EXCHANGE_STORE || id == ENERGY_FRAGMENT_STORE || id == STARDUST_STORE || id == AGILITY_TICKET_STORE
                        || id == GRAVEYARD_STORE || id == HOLY_WATER_STORE || id == AFK_STORE) {
                    value = (int) ShopManager.getCustomShopData(id, item.getId())[0];
                }
            }
        } else {
            Object[] obj = ShopManager.getCustomShopData(id, item.getId());
            if (obj == null)
                return this;
            value = (int) obj[0];
            currencyName = (String) obj[1];
            if (id == PKING_REWARDS_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getPkPoints();
            } else if (id == VOTING_REWARDS_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getVotingPoints();
            } else if (id == DUNGEONEERING_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getDungeoneeringTokens();
            } else if (id == DONATOR_STORE_1 || id == DONATOR_STORE_2 || id == DONATOR_STORE_3) {
                playerCurrencyAmount = player.getPointsHandler().getDonationPoints();
            } else if (id == TRIVIA_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getTriviaPoints();
            } else if (id == BOSS_POINT_STORE) {
                playerCurrencyAmount = player.getBossPoints();
            } else if (id == SLAYER_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getSlayerPoints();
            } else if (id == PRESTIGE_STORE) {
                playerCurrencyAmount = player.getPointsHandler().getPrestigePoints();
            }
        }
        if (value <= 0) {
            return this;
        }
        if (!hasInventorySpace(player, item, getCurrency().getId(), value)) {
            player.getPacketSender().sendMessage("You do not have any free inventory slots.");
            return this;
        }
        if (playerCurrencyAmount <= 0 || playerCurrencyAmount < value) {
            player.getPacketSender()
                    .sendMessage("You do not have enough "
                            + ((currencyName.endsWith("s") ? (currencyName) : (currencyName + "s")))
                            + " to purchase this item.");
            return this;
        }
        if (id == SKILLCAPE_STORE_1 || id == SKILLCAPE_STORE_2 || id == SKILLCAPE_STORE_3) {
            for (int i = 0; i < item.getDefinition().getRequirement().length; i++) {
                int req = item.getDefinition().getRequirement()[i];
                if ((i == 3 || i == 5) && req == 99)
                    req *= 10;
                if (req > player.getSkillManager().getMaxLevel(i)) {
                    player.getPacketSender().sendMessage("You need to have at least level 99 in "
                            + Misc.formatText(Skill.forId(i).toString().toLowerCase()) + " to buy this item.");
                    return this;
                }
            }
        } else if (id == GAMBLING_STORE) {
            if (item.getId() == 15084 || item.getId() == 299) {
                if (player.getRights() == PlayerRights.PLAYER) {
                    player.getPacketSender().sendMessage("You need to be a member to use these items.");
                    return this;
                }
            }
        }

        for (int i = amountBuying; i > 0; i--) {
            if (!shopSellsItem(item)) {
                break;
            }
            if (getItems()[slot].getAmount() < amountBuying) {
                amountBuying = getItems()[slot].getAmount() - 101;

            }

            if (getItems()[slot].getAmount() <= 1 && id != GENERAL_STORE) {

                player.getPacketSender()
                        .sendMessage("The shop can't be below 1 items and needs to regenerate some items first...");
                break;
            }
            if (!item.getDefinition().isStackable()) {
                if (playerCurrencyAmount >= value && hasInventorySpace(player, item, getCurrency().getId(), value)) {

                    if (!customShop) {
                        if (usePouch) {
                            player.setMoneyInPouch((player.getMoneyInPouch() - value));
                        } else {
                            player.getInventory().delete(currency.getId(), value, false);
                        }
                    } else {
                        if (id == PKING_REWARDS_STORE) {
                            player.getPointsHandler().setPkPoints(-value, true);
                        } else if (id == VOTING_REWARDS_STORE) {
                            player.getPointsHandler().setVotingPoints(-value, true);
                        } else if (id == DUNGEONEERING_STORE) {
                            player.getPointsHandler().setDungeoneeringTokens(-value, true);
                        } else if (id == DONATOR_STORE_1 || id == DONATOR_STORE_2 || id == DONATOR_STORE_3) {
                            player.getPointsHandler().setDonationPoints(-value, true);
                        } else if (id == BOSS_POINT_STORE) {
                            player.setBossPoints(player.getBossPoints() - value);
                        } else if (id == TRIVIA_STORE) {
                            player.getPointsHandler().setTriviaPoints(-value, true);
                        } else if (id == SLAYER_STORE) {
                            player.getPointsHandler().setSlayerPoints(-value, true);
                        } else if (id == PRESTIGE_STORE) {
                            player.getPointsHandler().setPrestigePoints(-value, true);
                        }
                    }

                    super.switchItem(to, new Item(item.getId(), 1), slot, false, false);

                    playerCurrencyAmount -= value;
                } else {
                    break;
                }
            } else {
                if (playerCurrencyAmount >= value && hasInventorySpace(player, item, getCurrency().getId(), value)) {

                    int canBeBought = playerCurrencyAmount / (value);
                    if (canBeBought >= amountBuying) {
                        canBeBought = amountBuying;
                    }
                    if (canBeBought == 0)
                        break;

                    if (!customShop) {
                        if (usePouch) {
                            player.setMoneyInPouch((player.getMoneyInPouch() - (value * canBeBought)));
                        } else {
                            player.getInventory().delete(currency.getId(), value * canBeBought, false);
                        }
                    } else {
                        if (id == PKING_REWARDS_STORE) {
                            player.getPointsHandler().setPkPoints(-value * canBeBought, true);
                        } else if (id == VOTING_REWARDS_STORE) {
                            player.getPointsHandler().setVotingPoints(-value * canBeBought, true);
                        } else if (id == DUNGEONEERING_STORE) {
                            player.getPointsHandler().setDungeoneeringTokens(-value * canBeBought, true);
                        } else if (id == DONATOR_STORE_1 || id == DONATOR_STORE_2 || id == DONATOR_STORE_3) {
                            player.getPointsHandler().setDonationPoints(-value * canBeBought, true);
                        } else if (id == TRIVIA_STORE) {
                            player.getPointsHandler().setTriviaPoints(-value * canBeBought, true);
                        } else if (id == BOSS_POINT_STORE) {
                            player.setBossPoints(player.getBossPoints() - (value * canBeBought));
                        } else if (id == SLAYER_STORE) {
                            player.getPointsHandler().setSlayerPoints(-value * canBeBought, true);
                        } else if (id == PRESTIGE_STORE) {
                            player.getPointsHandler().setPrestigePoints(-value * canBeBought, true);
                        }
                    }
                    super.switchItem(to, new Item(item.getId(), canBeBought), slot, false, false);
                    playerCurrencyAmount -= value;
                    break;
                } else {
                    break;
                }
            }
            amountBuying--;
        }
        if (!customShop) {
            if (usePouch) {
                player.getPacketSender().sendString(8135, "" + player.getMoneyInPouch()); // Update
                // the
                // money
                // pouch
            }
        } else {
            player.getPointsHandler().refreshPanel();
        }
        player.getInventory().refreshItems();
        fireRestockTask();
        refreshItems();
        publicRefresh();
        return this;
    }

    /**
     * Checks if a player has enough inventory space to buy an item
     *
     * @param item The item which the player is buying
     * @return true or false if the player has enough space to buy the item
     */
    public static boolean hasInventorySpace(Player player, Item item, int currency, int pricePerItem) {
        if (player.getInventory().getFreeSlots() >= 1) {
            return true;
        }
        if (item.getDefinition().isStackable()) {
            if (player.getInventory().contains(item.getId())) {
                return true;
            }
        }
        if (currency != -1) {
            if (player.getInventory().getFreeSlots() == 0
                    && player.getInventory().getAmount(currency) == pricePerItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Shop add(Item item, boolean refresh) {
        super.add(item, false);
        if (id != RECIPE_FOR_DISASTER_STORE)
            publicRefresh();
        return this;
    }

    @Override
    public int capacity() {
        return 42;
    }

    @Override
    public StackType stackType() {
        return StackType.STACKS;
    }

    @Override
    public Shop refreshItems() {
        if (id == RECIPE_FOR_DISASTER_STORE) {
            RecipeForDisaster.openRFDShop(getPlayer());
            return this;
        }
        for (Player player : World.getPlayers()) {
            if (player == null || !player.isShopping() || player.getShop() == null || player.getShop().id != id)
                continue;
            player.getPacketSender().sendItemContainer(player.getInventory(), INVENTORY_INTERFACE_ID);
            player.getPacketSender().sendItemContainer(ShopManager.getShops().get(id), ITEM_CHILD_ID);
            player.getPacketSender().sendString(NAME_INTERFACE_CHILD_ID, name);
            if (player.getInputHandling() == null || !(player.getInputHandling() instanceof EnterAmountToSellToShop
                    || player.getInputHandling() instanceof EnterAmountToBuyFromShop))
                player.getPacketSender().sendInterfaceSet(INTERFACE_ID, INVENTORY_INTERFACE_ID - 1);
        }
        return this;
    }

    @Override
    public Shop full() {
        getPlayer().getPacketSender().sendMessage("The shop is currently full. Please come back later.");
        return this;
    }

    public String shopPriceEx(int shopPrice) {
        String ShopAdd = "";
        if (shopPrice >= 1000 && shopPrice < 1000000) {
            ShopAdd = " (" + (shopPrice / 1000) + "K)";
        } else if (shopPrice >= 1000000) {
            ShopAdd = " (" + (shopPrice / 1000000) + " million)";
        }
        return ShopAdd;
    }

    private boolean shopSellsItem(Item item) {
        return contains(item.getId());
    }

    public void fireRestockTask() {
        if (isRestockingItems() || fullyRestocked())
            return;
        setRestockingItems(true);
        TaskManager.submit(new ShopRestockTask(this));
    }

    public void restockShop() {
        for (int shopItemIndex = 0; shopItemIndex < getOriginalStock().length; shopItemIndex++) {
            int currentStockAmount = getItems()[shopItemIndex].getAmount();
            add(getItems()[shopItemIndex].getId(), getOriginalStock()[shopItemIndex].getAmount() - currentStockAmount);
//			publicRefresh();
            refreshItems();
        }

    }

    public boolean fullyRestocked() {
        if (id == GENERAL_STORE) {
            return getValidItems().size() == 0;
        } else if (id == RECIPE_FOR_DISASTER_STORE) {
            return true;
        }
        if (getOriginalStock() != null) {
            for (int shopItemIndex = 0; shopItemIndex < getOriginalStock().length; shopItemIndex++) {
                if (getItems()[shopItemIndex].getAmount() != getOriginalStock()[shopItemIndex].getAmount())
                    return false;
            }
        }
        return true;
    }

    public static boolean shopBuysItem(int shopId, Item item) {
        if (shopId == GENERAL_STORE)
            return true;
        if (shopId == DUNGEONEERING_STORE || shopId == BOSS_POINT_STORE || shopId == TRIVIA_STORE
                || shopId == DONATOR_STORE_1 || shopId == DONATOR_STORE_2 || shopId == DONATOR_STORE_3 || shopId == PKING_REWARDS_STORE
                || shopId == VOTING_REWARDS_STORE || shopId == RECIPE_FOR_DISASTER_STORE || shopId == HOLY_WATER_STORE
                || shopId == ENERGY_FRAGMENT_STORE || shopId == AGILITY_TICKET_STORE || shopId == GRAVEYARD_STORE
                || shopId == TOKKUL_EXCHANGE_STORE || shopId == STARDUST_STORE || shopId == SLAYER_STORE || shopId == PRESTIGE_STORE || shopId == AFK_STORE)
            return false;
        Shop shop = ShopManager.getShops().get(shopId);
        if (shop != null && shop.getOriginalStock() != null) {
            for (Item it : shop.getOriginalStock()) {
                if (it != null && it.getId() == item.getId())
                    return true;
            }
        }
        return false;
    }

    public static class ShopManager {

        private static Map<Integer, Shop> shops = new HashMap<Integer, Shop>();

        public static Map<Integer, Shop> getShops() {
            return shops;
        }

        public static JsonLoader parseShops() {
            return new JsonLoader() {
                @Override
                public void load(JsonObject reader, Gson builder) {
                    int id = reader.get("id").getAsInt();
                    String name = reader.get("name").getAsString();
                    Item[] items = builder.fromJson(reader.get("items").getAsJsonArray(), Item[].class);
                    Item currency = new Item(reader.get("currency").getAsInt());
                    shops.put(id, new Shop(null, id, name, currency, items));
                }

                @Override
                public String filePath() {
                    return "./data/def/json/world_shops.json";
                }
            };
        }

        public static Object[] getCustomShopData(int shop, int item) {
            if (shop == VOTING_REWARDS_STORE) {
                switch (item) {
                    case 6500:
                        return new Object[]{125, "Voting points"};
                    case 10025:
                        return new Object[]{5, "Voting points"};
                    case 2581:
                        return new Object[]{20, "Voting points"};
                    case 19337:
                    case 19338:
                    case 19339:
                    case 19340:
                    case 19336:
                        return new Object[]{100, "Voting points"};
                    case 9813:
                    case 15356:
                    case 21026:
                        return new Object[]{250, "Voting points"};
                    case 20084:
                    case 19111:
                        return new Object[]{30, "Voting points"};

                    case 15018:
                    case 15019:
                    case 15020:
                    case 15220:
                        //case 15501:
                    case 11850:
                    case 11856:
                    case 11854:
                    case 11852:
                    case 11846:
                    case 11848:
                        return new Object[]{50, "Voting points"};
                    case 19335:
                        return new Object[]{60, "Voting points"};
                    case 6570:
                        return new Object[]{10, "Voting points"};
                    case 6199:
                        return new Object[]{25, "Voting points"};
                    case 14004:
                    case 14005:
                    case 14006:
                    case 14007:
                    case 15441:
                    case 15442:
                    case 15443:
                    case 15444:
                    case 20072:
                        return new Object[]{35, "Voting points"};
                    case 15332:
                        return new Object[]{2, "Voting points"};
                    case 14000:
                    case 14001:
                    case 14002:
                    case 14003:
                    case 2577:
                        return new Object[]{20, "Voting points"};
                    case 13663:
                        return new Object[]{6, "Voting points"};
                }
            } else if (shop == PRESTIGE_STORE) {
                switch (item) {
                    case 13845:
                    case 13846:
                    case 13847:
                    case 13848:
                    case 13849:
                    case 13850:
                    case 13851:
                    case 13852:
                    case 13853:
                    case 13854:
                    case 13855:
                    case 13856:
                    case 13857:
                        return new Object[]{10, "Prestige points"};
					/*case 15501:
						return new Object[]{40, "Prestige points"};*/
                    case 19335:
                        return new Object[]{30, "Prestige points"};
                    case 15220:
                    case 15020:
                    case 15019:
                    case 15018:
                        return new Object[]{20, "Prestige points"};
                    case 4084:
                    case 14020:
                    case 14021:
                        return new Object[]{100, "Prestige points"};
                    case 14595:
                    case 14603:
                    case 14602:
                    case 14605:
                        return new Object[]{15, "Prestige points"};

                }
            } else if (shop == PKING_REWARDS_STORE) {
                switch (item) {
                    case 6918:
                    case 6914:
                    case 6889:
                    case 2579:
                    case 13896:
                        return new Object[]{5, "Pk points"};
                    case 13887:
                    case 13893:
                        return new Object[]{30, "Pk points"};

                    case 10550:
                        return new Object[]{12, "Pk points"};
                    case 11283:
                        return new Object[]{25, "Pk points"};
                    case 20000:
                    case 20001:
                    case 20002:
                        return new Object[]{25, "Pk points"};
                    case 13884:
                    case 13890:
                        return new Object[]{25, "Pk points"};
                    case 4706:
                        return new Object[]{45, "Pk points"};
                    case 20555:
                        return new Object[]{55, "Pk points"};
                    case 6916:
                        return new Object[]{8, "Pk points"};
                    case 6924:
                        return new Object[]{6, "Pk points"};
                    case 6920:
                    case 6922:
                        return new Object[]{4, "Pk points"};
                    case 2581:
                        return new Object[]{5, "Pk points"};
                    case 11730:
                        return new Object[]{25, "Pk points"};
                    case 12601:
                        return new Object[]{25, "Pk points"};
                    case 2577:
                        return new Object[]{5, "Pk points"};
                    case 15486:
                        return new Object[]{20, "Pk points"};
                    case 19111:
                        return new Object[]{30, "Pk points"};
                    case 13879:
                    case 13883:
                    case 15243:
                    case 15332:
                        return new Object[]{1, "Pk points"};
                    case 15241:
                    case 17273:
                        return new Object[]{25, "Pk points"};
                    case 10548:
                    case 10547:
                    case 10551:
                        return new Object[]{12, "Pk points"};
                    case 6570:

                    case 11235:
                    case 4151:
                    case 20072:
                        return new Object[]{8, "Pk points"};

                    case 11696:
                    case 11698:
                    case 11700:
                        return new Object[]{35, "Pk points"};
                    case 11728:
                    case 15018:
                    case 15020:
                    case 15220:
                        return new Object[]{20, "Pk points"};
                    case 12926:
                    case 11694:
                        return new Object[]{65, "Pk points"};
                    case 19780:
                    case 14484:
                        return new Object[]{65, "Pk points"};
                }
            } else if (shop == STARDUST_STORE) {
                switch (item) {
                    case 18831:
                        return new Object[]{5, "stardust"};

                    case 6924:
                    case 6916:
                    case 6918:
                        return new Object[]{100, "stardust"};

                    case 6585:
                    case 2581:
                    case 2577:
                        return new Object[]{200, "stardust"};

                    case 15259:
                    case 13661:
                        return new Object[]{350, "stardust"};
                    case 2417:
                    case 2415:
                    case 2416:
                        return new Object[]{500, "stardust"};

                    case 11704:
                    case 11706:
                    case 11708:

                        return new Object[]{650, "stardust"};
                    case 11710:
                    case 11712:
                    case 11714:
                        return new Object[]{120, "stardust"};
                    case 15606:
                    case 15608:
                    case 15610:
                    case 15612:
                    case 15614:
                    case 15616:
                    case 15618:
                    case 15620:
                    case 15622:
                        return new Object[]{500, "stardust"};
                }
            } else if (shop == ENERGY_FRAGMENT_STORE) {
                switch (item) {
                    case 13632:
                    case 13633:
                    case 13634:
                    case 13635:
                    case 13636:
                    case 13637:
                    case 13638:
                    case 13639:
                    case 13640:
                        return new Object[]{500, "energy fragments"};
                    case 13641:
                        return new Object[]{750, "energy fragments"};
                    case 13642:
                        return new Object[]{1500, "energy fragments"};
                }
                return new Object[]{250, "energy fragments"};
            } else if (shop == HOLY_WATER_STORE) {
                switch (item) {
                    case 18337:
                        return new Object[]{350, "Holy Waters"};
                    case 20010:
                    case 20011:
                    case 20012:
                    case 20009:
                    case 20020:
                    case 10551:
                        return new Object[]{500, "Holy Waters"};
                    case 11720:
                    case 11718:
                    case 11722:
                    case 11724:
                    case 11726:
                    case 13239:
                    case 12708:
                    case 13235:
                        return new Object[]{650, "Holy Waters"};
                    case 9921:
                    case 9922:
                    case 9923:
                    case 9924:
                    case 9925:
                        return new Object[]{3750, "Holy Waters"};
                    case 4084:
                    case 10735:
                        return new Object[]{7500, "Holy Waters"};
                    case 19045:
                        return new Object[]{15000, "Holy Waters"};
                    case 18931:
                        return new Object[]{50000, "Holy Waters"};
                    case 962:
                        return new Object[]{250000, "Holy Waters"};
                }
                return new Object[]{10000, "Holy Waters"};
            } else if (shop == BOSS_POINT_STORE) {
                switch (item) {
                    case 21026: //ring of coins
                        return new Object[]{500, "Boss Points"};
                    case 14008: //torva full helm
                    case 14009: //torva platebody
                    case 14010: //torva platelegs
                    case 14014: //virtus mask
                    case 14015: //virtus robe top
                    case 14016: //virtus robe legs
                    case 14011: //pernix cowl
                    case 14012: //pernix top
                    case 14013:    //pernix chaps
                    case 12601:    //ring of the gods
                        return new Object[]{3000, "Boss Points"};
                    case 18782: //dragonkin lamp
                        return new Object[]{50, "Boss Points"};
                    case 20998: //twisted bow
                    case 18899:     //scythe of vigur
                        return new Object[]{30000, "Boss Points"};
                    case 15272: //rocktail
                    case 989: //crystal key
                        return new Object[]{10, "Boss Points"};
                    case 11718: //armadyl helmet
                    case 11720: //armadyl chestplate
                    case 11722: //armadyl chainskirt
                    case 11724: //bandos chestplate
                    case 11726:    //bandos tassets
                    case 13905: //vesta spear
                    case 13899: //vesta longsword
                    case 13887: //vesta chainbody
                    case 13893: //vesta chainskirt
                    case 13896:    //statius full helm
                    case 13884: //statius platebody
                    case 13890: //statius platelegs
                    case 13861://zuriel robe bottom
                    case 13858://zuriel robe top 
                    case 13864://zuriel hood 
                    case 20012: //trickster helm
                    case 20010: //trickster robe
                    case 20011: //trickster robe legs
                    case 20020: //trickster gloves
                    case 20019:    //trickster boots
                    case 13239: //primoridial boots
                    case 12708: //pegasian boots
                    case 13235:    //eternal boots
                    case 13051: //armadyl crossbow
                        return new Object[]{450, "Boss Points"};
                }
                return new Object[]{25000, "Boss Points"};
            } else if (shop == DONATOR_STORE_1 || shop == DONATOR_STORE_2 || shop == DONATOR_STORE_3) {
                switch (item) {
                    case 15426://candy cane
                    case 13887://vesta chainbody
                    case 13893://vesta plateskirt
                    case 13899://vesta longsword
                    case 13905://vesta spear
                    case 13896://statius full helm
                    case 13884://statius platebody  
                    case 13890://statius platelegs  
                    case 13902://statius warmhammer 
                    case 13861://zuriel robe bottom
                    case 13858://zuriel robe top 
                    case 13864://zuriel hood 
                    case 4716://dharoks
                    case 4718://dharoks
                    case 4720://dharoks
                    case 4722://dharoks
                    case 4724://guthans
                    case 4726://guthans
                    case 4728://guthans  
                    case 4730://guthans 
                    case 4732://karils
                    case 4734://karils
                    case 4736://karils
                    case 4738://karils  
                    case 4745://torags
                    case 4747://torags
                    case 4749://torags  
                    case 4751://torags
                    case 4753://veracs
                    case 4755://veracs
                    case 4757://varacs
                    case 4759://veracs    
                        return new Object[]{5, "Donation Points"};
                    case 4740://bolt rack
                        return new Object[]{1, "Donation Points"};
                    case 6830://10$ donation box
                    case 17291://blood necklace
                    case 16359://primal boots
                    case 16293://primal gauntlets
                        return new Object[]{10, "Donation Points"};
                    case 10336://3rd age vambraces
                    case 6831://15$ donation box
                    case 10374://zamorak coif
                    case 10370://zamorak body
                    case 10368://zamorak vambraces
                    case 10390://saradomin coif
                    case 10386://saradomin body
                    case 10384://saradomin vambraces
                    case 10388://saradomin chaps
                    case 10372://zamorak chaps
                    case 10362://amulet of glory (t)
                    case 7803://yin yang amulet
                        return new Object[]{15, "Donation Points"};
                    case 1038://Red Partyhat
                    case 1040://Yellow Partyhat
                    case 1046://Purple Partyhat
                    case 1042://Blue Partyhat
                    case 1048://White Partyhat
                    case 1044://Green Partyhat
                    case 1050:// santa hat
                    case 7449://Meat Tenderizer
                    case 5608://Fox
                    case 13666://Giant Hand
                    case 9946://cap and goggles
                    case 9945://bomber cap
                    case 9944://bomber jack
                    case 10732://rubber chicken
                    case 9634://vyrewatch top
                    case 9636://vyrewatch legs
                    case 9638://vyrewatch shoes
                    case 11789://grim reaper hood
                    case 15422://christmas ghost hood
                    case 15423://christmas ghost top
                    case 15425://christmas ghost bottoms
                    case 981:// disk of returning
                    case 9920://jack lantern mask
                    case 10507://reindeer hat
                    case 19314://3rd age druidic wreath
                    case 9472://gnome goggles
                        return new Object[]{20, "Donation Points"};
                    case 18891://justiciar helm
                    case 18892://justiciar platebody
                    case 18893://justiciar platelegs
                    case 18894://ghrazi rapier
                    case 18898://ring of suffering
                    case 18900://sanguinesti staff
                    case 18901://Avernic Defender
                    case 14008://Torva helm
                    case 14009://Torva Plate
                    case 14010://Torva Legs
                    case 14011://Pernix Helmet
                    case 14012://Pernix Plate
                    case 14013://Pernix Legs
                    case 14014://Virtus Helmet
                    case 14015://Virtus Plate
                    case 14016://Virtus Legs
                        return new Object[]{25, "Donation Points"};
                    case 10350://3rd age full helm
                    case 10348://3rd age platebody
                    case 10346://3rd age platelegs
                    case 10352://3rd age kiteshield
                    case 10342://3rd age mage hat
                    case 10338://3rd age robe top
                    case 10340://3rd age robe
                    case 10334://3rd age range coif
                    case 10330://3rd age range top
                    case 10332://3rd age range legs
                    case 20998://Twisted Bow
                    case 6832://30$ donation box
                        return new Object[]{30, "Donation Points"};
                    case 16711://primal full helm
                    case 17259://primal platebody
                    case 16689://primal platelegs
                    case 17361://primal kiteshield
                    case 18899://scythe of vitur
                        return new Object[]{35, "Donation Points"};
                    case 14044://Black Partyhat
                    case 14050://Black Santa Hat
                    case 6833://50$ donation box
                        return new Object[]{50, "Donation Points"};
                }
                return new Object[]{100, "Donation Points"}; // Default Value if we miss one
            } else if (shop == AGILITY_TICKET_STORE) {
                switch (item) {
                    case 14936:
                    case 14938:
                        return new Object[]{60, "agility tickets"};
                    case 10941:
                    case 10939:
                    case 10940:
                    case 10933:
                        return new Object[]{20, "agility tickets"};
                    case 13661:
                        return new Object[]{100, "agility tickets"};
                }
            } else if (shop == GRAVEYARD_STORE) {
                switch (item) {
                    case 18337:
                        return new Object[]{350, "zombie fragments"};
                    case 20010:
                    case 20011:
                    case 20012:
                    case 20009:
                    case 20020:
                    case 10551:
                        return new Object[]{500, "zombie fragments"};
                    case 10548:
                    case 10549:
                    case 10550:
                    case 11846:
                    case 11848:
                    case 11850:
                    case 11852:
                    case 11854:
                    case 11856:
                        return new Object[]{200, "zombie fragments"};
                    case 11842:
                    case 11844:
                    case 7592:
                    case 7593:
                    case 7594:
                    case 7595:
                    case 7596:
                        return new Object[]{150, "zombie fragments"};
                    case 15241:
                        return new Object[]{1250, "zombie fragments"};
                    case 18889:
                    case 18890:
                    case 18891:
                    case 18965:

                    case 16137:
                    case 13045:
                    case 13047:
                    case 16403:
                    case 16425:
                    case 16955:
                        return new Object[]{1500, "zombie fragments"};
                    case 1:
                    case 15243:
                        return new Object[]{2, "zombie fragments"};
                }
                return new Object[]{10000, "zombie fragments"};
            } else if (shop == TOKKUL_EXCHANGE_STORE) {
                switch (item) {
                    case 11978:
                        return new Object[]{300000, "tokkul"};
                    case 438:
                    case 436:
                        return new Object[]{10, "tokkul"};
                    case 440:
                        return new Object[]{25, "tokkul"};
                    case 453:
                        return new Object[]{30, "tokkul"};
                    case 442:
                        return new Object[]{30, "tokkul"};
                    case 444:
                        return new Object[]{40, "tokkul"};
                    case 447:
                        return new Object[]{70, "tokkul"};
                    case 449:
                        return new Object[]{120, "tokkul"};
                    case 451:
                        return new Object[]{250, "tokkul"};
                    case 1623:
                        return new Object[]{20, "tokkul"};
                    case 1621:
                        return new Object[]{40, "tokkul"};
                    case 1619:
                        return new Object[]{70, "tokkul"};
                    case 1617:
                        return new Object[]{150, "tokkul"};
                    case 1631:
                        return new Object[]{1600, "tokkul"};
                    case 6571:
                        return new Object[]{50000, "tokkul"};
                    case 11128:
                        return new Object[]{22000, "tokkul"};
                    case 6522:
                        return new Object[]{20, "tokkul"};
                    case 6524:
                    case 6523:
                    case 6526:
                        return new Object[]{5000, "tokkul"};
                    case 6528:
                    case 6568:
                        return new Object[]{800, "tokkul"};
                }
            } else if (shop == AFK_STORE) {
                switch (item) {
                    case 989://Crystal Key
                    case 18937://Bonus XP Scroll
                    case 2677://Clue Scroll
                    case 1543:
                        return new Object[]{7500, "AFK Tokens"};
                    case 10025://Charm Box
                    case 13845://Start Brawl Gloves
                    case 13846:
                    case 13847:
                    case 13848:
                    case 13849:
                    case 13850:
                    case 13851:
                    case 13852:
                    case 13853:
                    case 13854:
                    case 13855:
                    case 13856:
                    case 13857://End Brawl Gloves
                        return new Object[]{10000, "AFK Tokens"};
                    case 2946://Golden Tinder
                    case 10933://Start lumberjack
                    case 10939:
                    case 10940:
                    case 10941://End lumberjack
                        return new Object[]{30000, "AFK Tokens"};
                    case 6500://Charming Imp
                    case 18337://Bone Crusher
                    case 13661://Adze
                        return new Object[]{50000, "AFK Tokens"};
                    case 9813:
                        return new Object[]{100000, "AFK Tokens"};
                    case 6829:
                    case 7584:
                        return new Object[]{300000, "AFK Tokens"};
                    case 19890:
                        return new Object[]{605000, "AFK Tokens"};
                }
                return new Object[]{1000000, "AFK Tokens"};
            } else if (shop == DUNGEONEERING_STORE) {
                switch (item) {
                    //1000,0000 dungeoneering tokens
                    case 17259:
                    case 16689:
                    case 16711:
                    case 17361:
                    case 16034:
                    case 16001:
                    case 16152:
                    case 16359:
                    case 16293:
                        return new Object[]{10000000, "Dungeoneering tokens"};

                    //500,000 dungeoneering tokens
                    case 19669: //ring of vigour
                    case 21026: //ring of coins
                        return new Object[]{500000, "Dungeoneering tokens"};

                    //250,000 dungeoneering tokens
                    case 16955: //primal rapier
                    case 16425: //primal maul
                    case 16403: //primal longsword
                    case 16909: //primal 2h
                    case 16837: //primal dagger
                    case 17039: //primal warhammer
                    case 17143:     //primal spear
                    case 15773: //primal battleaxe
                    case 16315:     //primal pickaxe
                        return new Object[]{250000, "Dungeoneering tokens"};

                    //75,000 dungeoneering tokens
                    case 6500: //charming imp
                        return new Object[]{75000, "Dungeoneering tokens"};

                    //50,000 dungeoneering tokens
                    case 18351: //chaotic longsword
                    case 18349: //chaotic rapier
                    case 18353: //chaotic maul
                    case 18357: //chaotic crossbow
                    case 18355: //chaotic staff
                    case 18359: //chaotic kiteshield
                        return new Object[]{50000, "Dungeoneering tokens"};

                    //40,000 dungeoneering tokens
                    case 18361: //eagle eye kiteshield
                    case 18363: //farseer kiteshield
                    case 18335: //arcane stream necklace
                        return new Object[]{40000, "Dungeoneering tokens"};

                    //30,000 dungeoneering tokens
                    case 18337: //bonecrusher
                    case 19675: //herbicide
                    case 18338: //gem bag
                        return new Object[]{30000, "Dungeoneering tokens"};

                    // dungeoneering tokens
                    case 18348: //dungeoneering experience token
                        return new Object[]{500, "Dungeoneering tokens"};
                }
            } else if (shop == TRIVIA_STORE) {
                switch (item) {
                    case 10836:
                    case 10837:
                    case 10838:
                    case 10839:
                    case 5016:
                    case 10840:
                        return new Object[]{50, "Trivia Points"};
                    case 19708:
                    case 19707:
                    case 19706:
                        return new Object[]{75, "Trivia Points"};
                    case 2631:
                    case 2643:
                    case 2641:
                    case 2639:
                    case 2635:
                    case 2633:
                    case 2637:
                    case 6857:
                    case 9857:
                    case 6861:
                    case 6863:
                        return new Object[]{100, "Trivia Points"};
                    case 13354:
                    case 9470:
                    case 7673:
                    case 7671:
                    case 18746:
                    case 18744:
                    case 18745:
                    case 19323:
                    case 19331:
                    case 19325:
                        return new Object[]{250, "Trivia Points"};
                    case 15426:
                        return new Object[]{500, "Trivia Points"};
                    case 1053:
                    case 1055:
                    case 1057:
                        return new Object[]{1000, "Trivia Points"};
                }
                return new Object[]{100, "Trivia Points"};
            } else if (shop == SLAYER_STORE) {
                switch (item) {
                    case 13263:
                        return new Object[]{250, "Slayer points"};
                    case 13281:
                        return new Object[]{5, "Slayer points"};
                    case 15403:
                    case 11730:
                    case 10887:
                    case 15241:
                    case 11716:
                        return new Object[]{150, "Slayer points"};

                    case 15220:
                    case 15020:
                    case 15019:
                    case 15018:
                        return new Object[]{300, "Slayer points"};

                    case 11235:
                    case 4151:
                    case 15486:
                    case 18337:
                        return new Object[]{250, "Slayer points"};
                    case 15243:
                        return new Object[]{3, "Slayer points"};
                    case 10551:
                        return new Object[]{200, "Slayer points"};

                    case 2581:
                    case 2577:
                    case 6585:
                    case 11732:
                        return new Object[]{100, "Slayer points"};

                    case 2572:
                        return new Object[]{250, "Slayer points"};
                }
            }
            return null;
        }
    }

    /**
     * The shop interface id.
     */
    public static final int INTERFACE_ID = 3824;

    /**
     * The starting interface child id of items.
     */
    public static final int ITEM_CHILD_ID = 3900;

    /**
     * The interface child id of the shop's name.
     */
    public static final int NAME_INTERFACE_CHILD_ID = 3901;

    /**
     * The inventory interface id, used to set the items right click values to
     * 'sell'.
     */
    public static final int INVENTORY_INTERFACE_ID = 3823;

    /*
     * Declared shops
     */

    public static final int DONATOR_STORE_1 = 48;
    public static final int DONATOR_STORE_2 = 49;
    public static final int DONATOR_STORE_3 = 59;

    public static final int TRIVIA_STORE = 50;

    public static final int GENERAL_STORE = 12;
    public static final int RECIPE_FOR_DISASTER_STORE = 36;

    private static final int VOTING_REWARDS_STORE = 27;
    private static final int PKING_REWARDS_STORE = 26;
    private static final int ENERGY_FRAGMENT_STORE = 33;
    private static final int AGILITY_TICKET_STORE = 39;
    private static final int GRAVEYARD_STORE = 42;
    private static final int TOKKUL_EXCHANGE_STORE = 43;
    private static final int HOLY_WATER_STORE = 51;
    private static final int SKILLCAPE_STORE_1 = 8;
    private static final int SKILLCAPE_STORE_2 = 9;
    private static final int SKILLCAPE_STORE_3 = 10;
    private static final int GAMBLING_STORE = 41;
    private static final int DUNGEONEERING_STORE = 44;
    public static final int BOSS_POINT_STORE = 92;
    private static final int SLAYER_STORE = 47;
    public static final int STARDUST_STORE = 55;
    public static final int PRESTIGE_STORE = 46;
    public static final int AFK_STORE = 53;
}

