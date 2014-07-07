package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import me.desht.sensibletoolbox.items.components.GoldDust;
import me.desht.sensibletoolbox.items.components.IronDust;
import me.desht.sensibletoolbox.items.components.MachineFrame;
import me.desht.sensibletoolbox.items.components.SimpleCircuit;
import me.desht.sensibletoolbox.recipes.CustomRecipe;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.recipes.RecipeUtil;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Iterator;

public class Smelter extends AbstractIOMachine {
    private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.LIGHT_BLUE);

    private static int getProcessingTime(ItemStack stack) {
        if (stack.getType().isEdible()) {
            return 40;  // food cooks a lot quicker than ores etc.
        }
        return 120;
    }

    public Smelter() {
    }

    public Smelter(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public void addCustomRecipes(CustomRecipeManager crm) {
        // add a corresponding smelter recipe for every known vanilla furnace recipe
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe r = iter.next();
            if (r instanceof FurnaceRecipe) {
                FurnaceRecipe fr = (FurnaceRecipe) r;
                if (RecipeUtil.isVanillaSmelt(fr.getInput().getType())) {
                    crm.addCustomRecipe(new CustomRecipe(this, fr.getInput(), fr.getResult(), getProcessingTime(fr.getInput())));
                }
            }
        }

        // add a processing recipe for any STB item which reports itself as smeltable
        for (String key : BaseSTBItem.getItemIds()) {
            STBItem item = getItemById(key);
            if (item.getSmeltingResult() != null) {
                ItemStack stack = item.toItemStack();
                crm.addCustomRecipe(new CustomRecipe(this, stack, item.getSmeltingResult(), getProcessingTime(stack)));
            }
        }
    }

    @Override
    public int getProgressItemSlot() {
        return 12;
    }

    @Override
    public int getProgressCounterSlot() {
        return 3;
    }

    @Override
    public Material getProgressIcon() {
        return Material.FLINT_AND_STEEL;
    }

    @Override
    public MaterialData getMaterialData() {
        return md;
    }

    @Override
    public String getItemName() {
        return "Smelter";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Smelts items", "Like a Furnace, but", "faster and more efficient"};
    }

    @Override
    public Recipe getRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        MachineFrame mf = new MachineFrame();
        registerCustomIngredients(sc, mf);
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());
        recipe.shape("CSC", "IFI", "RGR");
        recipe.setIngredient('C', Material.BRICK);
        recipe.setIngredient('S', Material.FURNACE);
        recipe.setIngredient('I', sc.getMaterialData());
        recipe.setIngredient('F', mf.getMaterialData());
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    @Override
    public boolean acceptsItemType(ItemStack stack) {
        return CustomRecipeManager.getManager().hasRecipe(this, stack);// && CustomRecipeManager.validateCustomSmelt(stack);
    }

    @Override
    public int[] getInputSlots() {
        return new int[]{10};
    }

    @Override
    public int[] getOutputSlots() {
        return new int[]{14};
    }

    @Override
    public int[] getUpgradeSlots() {
        return new int[]{41, 42, 43, 44};
    }

    @Override
    public int getUpgradeLabelSlot() {
        return 40;
    }

    @Override
    public int getMaxCharge() {
        return 1000;
    }

    @Override
    public int getChargeRate() {
        return 20;
    }

    @Override
    public int getEnergyCellSlot() {
        return 36;
    }

    @Override
    public int getChargeDirectionSlot() {
        return 37;
    }

    @Override
    public int getInventoryGUISize() {
        return 45;
    }

    @Override
    protected void playStartupSound() {
        getLocation().getWorld().playSound(getLocation(), Sound.FIRE, 1.0f, 1.0f);
    }

    @Override
    protected void playActiveParticleEffect() {
        if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
            ParticleEffect.FLAME.play(getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0.001f, 7);
        }
    }
}
