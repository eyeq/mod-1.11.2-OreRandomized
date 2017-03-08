package eyeq.orerandomized;

import eyeq.orerandomized.block.BlockOreRandomized;
import eyeq.util.client.model.UModelCreator;
import eyeq.util.client.model.UModelLoader;
import eyeq.util.client.renderer.ResourceLocationFactory;
import eyeq.util.client.resource.ULanguageCreator;
import eyeq.util.client.resource.lang.LanguageResourceManager;
import eyeq.util.oredict.CategoryTypes;
import eyeq.util.oredict.UOreDictionary;
import eyeq.util.world.gen.WorldGenOre;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static eyeq.orerandomized.OreRandomized.MOD_ID;

@Mod(modid = MOD_ID, version = "1.0", dependencies = "after:eyeq_util")
@Mod.EventBusSubscriber
public class OreRandomized {
    public static final String MOD_ID = "eyeq_orerandomized";

    @Mod.Instance(MOD_ID)
    public static OreRandomized instance;

    private static final ResourceLocationFactory resource = new ResourceLocationFactory(MOD_ID);

    private static int weightSum;
    private static Map<Block, Integer> weights = new HashMap<>();

    public static Block oreRandomized;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        load(new Configuration(event.getSuggestedConfigurationFile()));
        addRecipes();
        if(event.getSide().isServer()) {
            return;
        }
        renderItemModels();
        createFiles();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new WorldGenOre(16, new WorldGenMinable(oreRandomized.getDefaultState(), 9), 0, 128), 8);
    }

    public static void load(Configuration config) {
        config.load();

        weights.put(Blocks.STONE, config.get("Int", "weightStone", 1).getInt());
        weights.put(Blocks.COAL_ORE, config.get("Int", "weightOreCoal", 250).getInt());
        weights.put(Blocks.IRON_ORE, config.get("Int", "weightOreIron", 200).getInt());
        weights.put(Blocks.REDSTONE_ORE, config.get("Int", "weightOreRedstone", 100).getInt());
        weights.put(Blocks.GOLD_ORE, config.get("Int", "weightOreGold", 100).getInt());
        weights.put(Blocks.LAPIS_ORE, config.get("Int", "weightOreLapis", 100).getInt());
        weights.put(Blocks.EMERALD_ORE, config.get("Int", "weightOreEmerald", 50).getInt());
        weights.put(Blocks.DIAMOND_ORE, config.get("Int", "weightOreDiamond", 50).getInt());

        weights.put(Blocks.COAL_BLOCK, config.get("Int", "weightBlockCoal", 1).getInt());
        weights.put(Blocks.IRON_BLOCK, config.get("Int", "weightBlockIron", 1).getInt());
        weights.put(Blocks.REDSTONE_BLOCK, config.get("Int", "weightBlockRedstone", 1).getInt());
        weights.put(Blocks.GOLD_BLOCK, config.get("Int", "weightBlockGold", 1).getInt());
        weights.put(Blocks.LAPIS_BLOCK, config.get("Int", "weightBlockLapis", 1).getInt());
        weights.put(Blocks.EMERALD_BLOCK, config.get("Int", "weightBlockEmerald", 1).getInt());
        weights.put(Blocks.DIAMOND_BLOCK, config.get("Int", "weightBlockDiamond", 1).getInt());

        weightSum = 0;
        for(Integer weight : weights.values()) {
            weightSum += weight;
        }

        if(config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    protected static void registerBlocks(RegistryEvent.Register<Block> event) {
        oreRandomized = new BlockOreRandomized().setHardness(3.0F).setResistance(100.0F).setUnlocalizedName("oreRandomized");

        GameRegistry.register(oreRandomized, resource.createResourceLocation("randomized_ore"));
    }

    @SubscribeEvent
    protected static void registerItems(RegistryEvent.Register<Item> event) {
        GameRegistry.register(new ItemBlock(oreRandomized), oreRandomized.getRegistryName());

        UOreDictionary.registerOre(CategoryTypes.PREFIX_ORE, "randomized", oreRandomized);
    }

    public static void addRecipes() {
        GameRegistry.addSmelting(oreRandomized, new ItemStack(Items.ENDER_PEARL), 0.35F);
    }

    @SideOnly(Side.CLIENT)
    public static void renderItemModels() {
        UModelLoader.setCustomModelResourceLocation(oreRandomized);
    }

    public static void createFiles() {
        File project = new File("../1.11.2-OreRandomized");

        LanguageResourceManager language = new LanguageResourceManager();

        language.register(LanguageResourceManager.EN_US, oreRandomized, "Randomized Ore");
        language.register(LanguageResourceManager.JA_JP, oreRandomized, "ランダム鉱石");

        ULanguageCreator.createLanguage(project, MOD_ID, language);

        UModelCreator.createBlockOreJson(project, oreRandomized);
    }

    public static Block getRandomBlockOre(Random rand) {
        int weight = rand.nextInt(weightSum);
        for(Block block : weights.keySet()) {
            weight -= weights.get(block);
            if(weight < 0) {
                return block;
            }
        }
        return Blocks.AIR;
    }
}
