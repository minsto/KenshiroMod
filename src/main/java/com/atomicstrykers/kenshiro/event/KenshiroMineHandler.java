package com.atomicstrykers.kenshiro.event;

import com.atomicstrykers.kenshiro.KenshiroMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class KenshiroMineHandler {

    public static void doMine(ServerPlayer player, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockState state = level.getBlockState(pos);

        // rien / bedrock → on touche pas
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return;
        }

        // Autorisé uniquement si cassable par pioche, hache ou pelle
        if (!(state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || state.is(BlockTags.MINEABLE_WITH_HOE)
                || state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof LeavesBlock)) {
            return;
        }

        // Casse instant avec loot → effet “outil netherite turbo”
        level.destroyBlock(pos, true, player);

        KenshiroMod.LOGGER.debug("Kenshiro mine {} à {}", state, pos);
    }
}