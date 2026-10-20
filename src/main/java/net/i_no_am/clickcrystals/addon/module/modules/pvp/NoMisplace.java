package net.i_no_am.clickcrystals.addon.module.modules.pvp;

import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.HotbarUtils;
import net.i_no_am.clickcrystals.addon.module.AddonModule;
import net.i_no_am.clickcrystals.addon.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class NoMisplace extends AddonModule {
    public NoMisplace() {
        super("no-misplace", "Blocks the glowstone and anchor placements that waste an anchor");
    }

    private final SettingSection scGeneral = getGeneralSection();

    public final ModuleSetting<Boolean> disableGlowstonePlacement = scGeneral.add(createBoolSetting()
            .name("no-loose-glowstone")
            .description("Only place glowstone into an unloaded respawn anchor.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableAnchorOnGlowstone = scGeneral.add(createBoolSetting()
            .name("no-anchor-on-glowstone")
            .description("Never place a respawn anchor against glowstone.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableDoubleAnchor = scGeneral.add(createBoolSetting()
            .name("no-anchor-on-anchor")
            .description("Never place a respawn anchor against another anchor.")
            .def(true)
            .build()
    );
    public final ModuleSetting<Boolean> disableDoubleGlowstone = scGeneral.add(createBoolSetting()
            .name("no-glowstone-on-glowstone")
            .description("Never place glowstone against other glowstone.")
            .def(false)
            .build()
    );

    public final ModuleSetting<Boolean> disableDoubleObsidian = scGeneral.add(createBoolSetting()
            .name("no-obsidian-on-obsidian")
            .description("Never place obsidian on top of another obsidian block.")
            .def(true)
            .build()
    );

    public final ModuleSetting<Boolean> disableAnchorWithoutTotem = scGeneral.add(createBoolSetting()
            .name("no-anchor-without-totem")
            .description("Block anchor use unless the offhand or slot 9 holds a totem. Ignored if you have no totems left.")
            .def(true)
            .build()
    );

    public InteractionResult cannotPlace() {
        if (!isEnabled()) return InteractionResult.SUCCESS;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return InteractionResult.PASS;

        // Totem safety: with backup totems in the inventory, require one in the offhand or slot 9
        if (disableAnchorWithoutTotem.getVal() && BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR)) {
            boolean hasOffhandTotem = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
            boolean hasSlot9Totem = mc.player.getInventory().getItem(8).is(Items.TOTEM_OF_UNDYING);

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

        // Prevent placing a respawn anchor on another anchor
        if (disableDoubleAnchor.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.RESPAWN_ANCHOR)
                && (BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR) && !BlockUtils.isAnchorLoaded(1))) {
            return InteractionResult.FAIL;
        }

        // Prevent placing a glowstone on another glowstone
        if (disableDoubleGlowstone.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.GLOWSTONE)
                && BlockUtils.isLookingAt(Blocks.GLOWSTONE)) {
            return InteractionResult.FAIL;
        }

        // Prevent placing a respawn anchor on glowstone
        if (disableAnchorOnGlowstone.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.RESPAWN_ANCHOR)
                && BlockUtils.isLookingAt(Blocks.GLOWSTONE)) {
            return InteractionResult.FAIL;
        }

        // Prevent placing glowstone if not targeting anchor or anchor is loaded
        if (disableGlowstonePlacement.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.GLOWSTONE)
                && (!BlockUtils.isLookingAt(Blocks.RESPAWN_ANCHOR) || BlockUtils.isAnchorLoaded(1))) {
            return InteractionResult.FAIL;
        }

        // Prevent placing obsidian on top of another obsidian block (top face only)
        if (disableDoubleObsidian.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.OBSIDIAN)
                && BlockUtils.isLookingAt(Blocks.OBSIDIAN)
                && mc.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getDirection() == Direction.UP) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }
}