package net.i_no_am.clickcrystals.addon.module.modules.pvp;

import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.HotbarUtils;
import net.i_no_am.clickcrystals.addon.module.AddonModule;
import net.i_no_am.clickcrystals.addon.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class Prevent extends AddonModule {
    public Prevent() {
        super("prevent", "Blocks accidental glowstone or anchor placements");
    }

    private final SettingSection scGeneral = getGeneralSection();

    public final ModuleSetting<Boolean> disableGlowstonePlacement = scGeneral.add(createBoolSetting()
            .name("disable-placing-glowstone")
            .description("Disable placing glowstone unless targeting a respawn anchor.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableAnchorOnGlowstone = scGeneral.add(createBoolSetting()
            .name("disable-placing-anchor-on-glowstone")
            .description("Disable placing a respawn anchor on glowstone.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableDoubleAnchor = scGeneral.add(createBoolSetting()
            .name("disable-placing-anchor-on-anchor")
            .description("Disable placing a respawn anchor on top of another anchor.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableDoubleGlowstone = scGeneral.add(createBoolSetting()
            .name("disable-placing-glowstone-on-glowstone")
            .description("Disable placing glowstone unless not targeting another glowstone.")
            .def(false)
            .build()
    );

    public final ModuleSetting<Boolean> disableAnchorWithoutTotem = scGeneral.add(createBoolSetting()
            .name("disable-anchor-without-totem")
            .description("Bypass safety if Offhand OR Slot 9 has a totem. Also bypassed if 0 totems left.")
            .def(true)
            .build()
    );

    public InteractionResult cannotPlace() {
        if (!isEnabled()) return InteractionResult.SUCCESS;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return InteractionResult.PASS;

        // --- 1.21 OFFICIAL MAPPINGS TOTEM SAFETY ---
        if (disableAnchorWithoutTotem.getVal() && BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR)) {
            
            // Check Offhand
            boolean hasOffhandTotem = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            
            // Check Slot 9 (Index 8)
            boolean hasSlot9Totem = mc.player.getInventory().getItem(8).is(Items.TOTEM_OF_UNDYING);

            // If neither has a totem, check for backups in main inventory
            if (!hasOffhandTotem && !hasSlot9Totem) {
                int backupTotems = 0;
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = mc.player.getInventory().getItem(i);
                    if (stack.is(Items.TOTEM_OF_UNDYING)) {
                        backupTotems += stack.getCount();
                    }
                }

                if (backupTotems > 0) {
                    return InteractionResult.FAIL;
                }
            }
        }

        // --- REMAINING CHECKS ---
        if (disableDoubleAnchor.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.RESPAWN_ANCHOR)
                && (BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR) && !BlockUtils.isAnchorLoaded(1))) {
            return InteractionResult.FAIL;
        }

        if (disableDoubleGlowstone.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.GLOWSTONE)
                && BlockUtils.isLookingAt(Blocks.GLOWSTONE)) {
            return InteractionResult.FAIL;
        }

        if (disableAnchorOnGlowstone.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.RESPAWN_ANCHOR)
                && BlockUtils.isLookingAt(Blocks.GLOWSTONE)) {
            return InteractionResult.FAIL;
        }

        if (disableGlowstonePlacement.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.GLOWSTONE)
                && (!BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR) || BlockUtils.isAnchorLoaded(1))) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }
}