package net.ensouledsteel.ender_relay.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnderRelay extends Block {
	public EnderRelay(Settings settings){
		super(settings);
	}

	@Override
	public ActionResult onUse(
		BlockState state,
		World world,
		BlockPos pos,
		PlayerEntity player,
		Hand hand,
		BlockHitResult hit
	) {
		ItemStack heldByPlayer = player.getStackInHand(hand);
		if(heldByPlayer.getItem() == Items.COMPASS){
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1.0f, 5.0f);
			heldByPlayer.setCount(heldByPlayer.getCount()-1);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
}
