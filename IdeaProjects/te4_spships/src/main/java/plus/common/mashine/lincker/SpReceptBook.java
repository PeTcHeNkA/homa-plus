package plus.common.mashine.lincker;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import plus.common.mashine.block.SpMehModule;
import plus.common.mashine.item.*;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.common.mashine.item.recept.ReceptBook;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.spship.item.ItemCorpus;
import plus.common.spship.item.ItemSpArmor;
import plus.common.spship.item.craft.CraftCHunterProcessor;
import plus.common.spship.item.craft.CraftCRavenProcessor;
import plus.common.spship.item.craft.CraftSpArmorProcessor;


public class SpReceptBook extends ReceptBook<SpCraftContainer, PowerReceptProcessor> {
    public static SpReceptBook INST = new SpReceptBook();

    static abstract class SpCraftProc extends PowerReceptProcessor {
        public SpCraftProc(ItemStack[] items) {
            super(IItemValidator.buildBasic(items, 5));
        }
    }


    public ItemStack craftPreview(SpCraftContainer matrix, int shift, int pow) {
        ReseptKey key = matrix.toKey(this);
        PowerReceptProcessor processor = recepts.get(key);
        if (processor == null || !processor.valid(key)) return ItemStack.EMPTY;
        return processor.calcResult(key, shift, pow);
    }


    @Override
    public int calcHash(ItemStack[] items) {
        return ReseptKey.defHashCalc(items);
    }


    public SpReceptBook() {
        super(5);

        //компоненты 1,2,3
        add(new ItemStack[]{new ItemStack(Items.REDSTONE), new ItemStack(Items.REPEATER),
                new ItemStack(Items.REDSTONE), new ItemStack(BasicItem.COPPER_COIL),
                new ItemStack(BasicItem.PLATE_IRON)
        }, TehSubcraftItem.MICRO_NRM);

        add(new ItemStack[]{new ItemStack(Items.REPEATER), new ItemStack(BasicItem.COPPER_COIL),
                new ItemStack(Items.DIAMOND), new ItemStack(BasicItem.COPPER_COIL),
                new ItemStack(BasicItem.PLATE_MACHINE)
        }, TehSubcraftItem.MICRO_CPU);

        add(new ItemStack[]{new ItemStack(Items.REPEATER), new ItemStack(BasicItem.COPPER_COIL),
                new ItemStack(BasicItem.IRON_GEAR), new ItemStack(BasicItem.COPPER_COIL),
                new ItemStack(BasicItem.PLATE_IRON)
        }, TehSubcraftItem.MICRO_ADDON);


        //батарейки 4,5
        addTwisted(new ItemStack[]{new ItemStack(Items.REDSTONE), new ItemStack(BasicItem.PLATE_ALUMINE),
                new ItemStack(BasicItem.AETHER_CRYSTAL), new ItemStack(BasicItem.PLATE_COPPER),
                new ItemStack(Items.REDSTONE)
        }, ItemMssCapacitor.BATTERY_L1, ItemMssCapacitor.BATTERY_L2, 50);

        addTwisted(new ItemStack[]{new ItemStack(Items.REDSTONE, 2), new ItemStack(BasicItem.PLATE_ALUMINE),
                new ItemStack(BasicItem.AETHER_CRYSTAL), new ItemStack(BasicItem.PLATE_MEFRIL),
                new ItemStack(Items.REPEATER)
        }, ItemMssCapacitor.BATTERY_L3, ItemMssCapacitor.BATTERY_L4, 200);

        //массивы 6,7,8,9
        add(new ItemStack[]{new ItemStack(ItemMssCapacitor.BATTERY_L1), new ItemStack(ItemMssCapacitor.BATTERY_L1),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L1),
                new ItemStack(ItemMssCapacitor.BATTERY_L1), new ItemStack(ItemMssCapacitor.BATTERY_L1)
        }, ItemMssCapacitor.MASSIVE_L1);

        add(new ItemStack[]{new ItemStack(ItemMssCapacitor.BATTERY_L2), new ItemStack(ItemMssCapacitor.BATTERY_L2),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L1),
                new ItemStack(ItemMssCapacitor.BATTERY_L2), new ItemStack(ItemMssCapacitor.BATTERY_L2)
        }, ItemMssCapacitor.MASSIVE_L2);

        add(new ItemStack[]{new ItemStack(ItemMssCapacitor.BATTERY_L3), new ItemStack(ItemMssCapacitor.BATTERY_L3),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L2),
                new ItemStack(ItemMssCapacitor.BATTERY_L3), new ItemStack(ItemMssCapacitor.BATTERY_L3)
        }, ItemMssCapacitor.MASSIVE_L3);

        add(new ItemStack[]{new ItemStack(ItemMssCapacitor.BATTERY_L4), new ItemStack(ItemMssCapacitor.BATTERY_L4),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L2),
                new ItemStack(ItemMssCapacitor.BATTERY_L4), new ItemStack(ItemMssCapacitor.BATTERY_L4)
        }, ItemMssCapacitor.MASSIVE_L4);

        //ядра реакторов 10, 11, 12, 13

        add(new ItemStack[]{new ItemStack(TehSubcraftItem.MICRO_NRM), new ItemStack(TehSubcraftItem.MICRO_CPU),
                new ItemStack(ItemMssCapacitor.BATTERY_L1),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L1), new ItemStack(TehSubcraftItem.MICRO_NRM)
        }, ReactCore.CORE_L1);
        add(new ItemStack[]{new ItemStack(TehSubcraftItem.MICRO_NRM), new ItemStack(TehSubcraftItem.MICRO_CPU),
                new ItemStack(ItemMssCapacitor.BATTERY_L2),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L1), new ItemStack(TehSubcraftItem.MICRO_NRM)
        }, ReactCore.CORE_L2);

        add(new ItemStack[]{new ItemStack(TehSubcraftItem.MICRO_NRM), new ItemStack(TehSubcraftItem.MICRO_CPU),
                new ItemStack(ItemMssCapacitor.BATTERY_L3),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L2), new ItemStack(TehSubcraftItem.MICRO_NRM)
        }, ReactCore.CORE_L3);
        add(new ItemStack[]{new ItemStack(TehSubcraftItem.MICRO_NRM), new ItemStack(TehSubcraftItem.MICRO_CPU),
                new ItemStack(ItemMssCapacitor.BATTERY_L4),
                new ItemStack(ItemMssCapacitor.MASSIVE_FRAME_L2), new ItemStack(TehSubcraftItem.MICRO_NRM)
        }, ReactCore.CORE_L4);

        //стержни реакторов 14, 15, 16, 17
        add(new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE), new ItemStack(BasicItem.PLATE_GOLD),
                new ItemStack(BasicItem.AETHER_CRYSTAL, 5),
                new ItemStack(ItemMssCapacitor.BATTERY_L1), new ItemStack(TehBasicItem.PLATE_ALUMINE)
        }, ReactCore.ENERGY_L1);
        add(new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE), new ItemStack(BasicItem.PLATE_GOLD),
                new ItemStack(BasicItem.AETHER_CRYSTAL, 7),
                new ItemStack(ItemMssCapacitor.BATTERY_L2), new ItemStack(TehBasicItem.PLATE_ALUMINE)
        }, ReactCore.ENERGY_L2);
        add(new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE), new ItemStack(BasicItem.PLATE_GOLD, 2),
                new ItemStack(BasicItem.AETHER_CRYSTAL, 9),
                new ItemStack(ItemMssCapacitor.BATTERY_L3), new ItemStack(TehBasicItem.PLATE_ALUMINE)
        }, ReactCore.ENERGY_L3);
        add(new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE), new ItemStack(BasicItem.PLATE_GOLD, 2),
                new ItemStack(BasicItem.AETHER_CRYSTAL, 11),
                new ItemStack(ItemMssCapacitor.BATTERY_L4), new ItemStack(TehBasicItem.PLATE_ALUMINE)
        }, ReactCore.ENERGY_L4);

        //броня 18
        ItemStack[] items = new ItemStack[]{new ItemStack(Items.DYE),
                new ItemStack(BasicItem.PLATE_ALUMINE, 4),
                new ItemStack(BasicItem.PLATE_MACHINE, 11),
                new ItemStack(BasicItem.PLATE_MEFRIL, 2),
                new ItemStack(BasicItem.IRON_GEAR, 9),
        };
        add(new CraftSpArmorProcessor((IItemValidator.Single) i -> i.getItem() == Items.DYE,
                new IItemValidator.Basic(items[1]), new IItemValidator.Basic(items[2]),
                new IItemValidator.Basic(items[3]), new IItemValidator.Basic(items[4])
        ), items);
        //корабль охотник 19
        items = new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE, 7),
                new ItemStack(ItemSpArmor.SP_ARMOR), ItemCorpus.SP_MODEL.createSimple(1),
                new ItemStack(ItemSpArmor.SP_ARMOR), new ItemStack(SpMehModule.SP_MEH_MODULE, 9),
        };
        IItemValidator onlyMassive = (IItemValidator.Single) i -> i.getItem() instanceof ItemMssCapacitor;

        add(new CraftCHunterProcessor(
                new IItemValidator.Basic(items[0]), new IItemValidator.Basic(items[1]),
                onlyMassive,
                new IItemValidator.Basic(items[3]), new IItemValidator.Basic(items[4])
        ), items);
        //корабль ворон 20
        items = new ItemStack[]{new ItemStack(BasicItem.PLATE_ALUMINE, 5),
                new ItemStack(ItemSpArmor.SP_ARMOR), ItemCorpus.SP_MODEL.createSimple(17),
                new ItemStack(ItemSpArmor.SP_ARMOR), new ItemStack(SpMehModule.SP_MEH_MODULE, 11),
        };

        add(new CraftCRavenProcessor(
                new IItemValidator.Basic(items[0]), new IItemValidator.Basic(items[1]),
                onlyMassive,
                new IItemValidator.Basic(items[3]), new IItemValidator.Basic(items[4])
        ), items);


    }


    private void addTwisted(ItemStack[] items, ISimplePowerCraftable item1, ISimplePowerCraftable item2, int limit){
        add(new SpCraftProc(items) {
            @Override
            public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
                if(power<=limit) {
                    return item1.create(shift, power);
                } else {
                    return item2.create(shift, power-limit);
                }
            }
        }, items);
    }


    private void add(ItemStack[] items, ISimplePowerCraftable item) {
        add(new SpCraftProc(items) {
            @Override
            public ItemStack onCalcResult(ReseptKey key, int shift, int power) {
                return item.create(shift, power);
            }
        }, items);
    }
}