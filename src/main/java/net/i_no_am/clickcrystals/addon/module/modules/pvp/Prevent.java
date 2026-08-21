package net.i_no_am.clickcrystals.addon.module.modules.pvp;

import io.github.itzispyder.clickcrystals.modules.ModuleSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import io.github.itzispyder.clickcrystals.util.minecraft.HotbarUtils;
import net.i_no_am.clickcrystals.addon.module.AddonModule;
import net.i_no_am.clickcrystals.addon.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class Prevent extends AddonModule {
    public Prevent() {
        super("prevent", "Blocks accidental glowstone, anchor, or obsidian placements");
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

    public final ModuleSetting<Boolean> disableDoubleObsidian = scGeneral.add(createBoolSetting()
            .name("disable-placing-obsidian-on-obsidian")
            .description("Disable placing obsidian on top of another obsidian.")
            .def(true)
            .build()
    );

    public InteractionResult cannotPlace() {
        if (!isEnabled()) return InteractionResult.SUCCESS;

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

        // Prevent placing obsidian on top of another obsidian block (only on the top face)
        if (disableDoubleObsidian.getVal()
                && HotbarUtils.isHoldingEitherHand(Items.OBSIDIAN)
                && BlockUtils.isLookingAt(Blocks.OBSIDIAN)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult instanceof BlockHitResult blockHitResult) {
                if (blockHitResult.getDirection() == Direction.UP) {
                    return InteractionResult.FAIL;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
