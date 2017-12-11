package alsender.sinkorswim.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * created by alsender on 4/12/17.
 */

@Mod(modid = SinkorSwim.modid, name = SinkorSwim.name, version = SinkorSwim.version)
@Mod.EventBusSubscriber

public class SinkorSwim {

    public static final String modid = "sinkorswim";
    public static final String name = "Sink or Swim";
    public static final String version = "0.0.1";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ConfigManager());
        Config.init(event.getSuggestedConfigurationFile());
    }

    @SubscribeEvent
    public static void livingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {

            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            World world = player.getEntityWorld();
            BlockPos pos = player.getPosition().down(1);
            BlockPos pos1 = player.getPosition().down(2);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (world.isRemote) {
                if (!player.isCreative()) {
                    if (player.isInWater()) {
                        if (checkPotions(player)) {
                            if (checkEnchants(player)) {
                                if (!isNotInArmor(player))
                                    if (block.isReplaceable(world, pos)) {
                                        player.motionY -= 0.03D;
                                    }

                                    if (isInBiome(world, pos)) {
                                        if (block.isReplaceable(world, pos1) || block.isReplaceable(world, pos)) {
                                            player.motionY -= 0.03D;
                                        }
                                    }

                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isInBiome(World world, BlockPos pos) {
        if (Config.biomeBlacklist.contains(world.getBiome(pos).getBiomeName()) ||
                Config.biomeBlacklist.contains("All")) {

            return true;
        }
        return false;
    }

    public static boolean isNotInArmor(EntityPlayer player) {
        return armorSlotCheck(player, 0) &&
                armorSlotCheck(player, 1) &&
                armorSlotCheck(player, 2) &&
                armorSlotCheck(player, 3);
    }

    public static boolean armorSlotCheck(EntityPlayer player, int i) {
        if (player.inventory.armorInventory == null) {
            return true;
        }

        ItemStack stack = player.inventory.armorInventory.get(3 - i);
        String s = stack.getItem().getRegistryName().toString();

        if (stack.isEmpty()) {
            return true;
        }

        switch (i) {
            case 0 : return Config.armorWhiteList.contains(s);
            case 1 : return Config.armorWhiteList.contains(s);
            case 2 : return Config.armorWhiteList.contains(s);
            case 3 : return Config.armorWhiteList.contains(s);
        }

        return true;
    }

    public static boolean checkEnchants(EntityPlayer player) {
        //return EnchantmentHelper.getRespirationModifier(player) <= 0 && EnchantmentHelper.getDepthStriderModifier(player) <= 0;

        List<ItemStack> armorStacks = new ArrayList<ItemStack>();
        List<String> enchantsString = new ArrayList<String>();

        for (int i =0; i <= 3; i ++) {
            if (player.inventory.armorInventory.get(i )!= null) {
                armorStacks.add(player.inventory.armorInventory.get(i));
            }
        }

        for (ItemStack armorStack : armorStacks) {
            List<Enchantment> enchants = new ArrayList<Enchantment>(EnchantmentHelper.getEnchantments(armorStack).keySet());

            for (Enchantment e : enchants) {
                enchantsString.add(e.getRegistryName().toString());
            }
        }

        return Collections.disjoint(enchantsString, Config.enchantWhitelist);
    }

    public static boolean checkPotions(EntityPlayer player) {
        List<String> potionEffects = new ArrayList<String>();

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            String effectName = potionEffect.getEffectName().substring(7);
            potionEffects.add(effectName);
        }

        return Collections.disjoint(potionEffects, Config.potionWhitelist);
    }
}
