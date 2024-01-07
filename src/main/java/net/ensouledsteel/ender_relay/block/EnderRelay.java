package net.ensouledsteel.ender_relay.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundCategory;
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
		Item item = heldByPlayer.getItem();
		if(item == Items.COMPASS){
			NbtCompound data = heldByPlayer.getNbt();
			if (data == null || !data.getBoolean(CompassItem.LODESTONE_TRACKED_KEY)) { return ActionResult.PASS; }

			BlockPos teleportPosition = NbtHelper.toBlockPos(data.getCompound(CompassItem.LODESTONE_POS_KEY));
			if (!world.getBlockState(teleportPosition).isOf(Blocks.LODESTONE)) {
				world.playSound(
					pos,
					SoundEvents.ENTITY_ENDER_EYE_DEATH,
					SoundCategory.BLOCKS,
					0.5f,
					0.2f,
					false
				);
				return ActionResult.PASS;
			}
			// We adjust the position so that you land on top of the lodestone instead of off in the corner where you
			// might fall off
			player.tryTeleportAndDismount(
				teleportPosition.getX()+0.5,
				teleportPosition.getY()+1,
				teleportPosition.getZ()+0.5f
			);
			player.resetFallDistance();
			world.playSound(
				teleportPosition,
				SoundEvents.ENTITY_ENDERMAN_TELEPORT,
				SoundCategory.BLOCKS,
				0.5f,
				0.4f,
				false
			);
			if(!player.isCreative()){ heldByPlayer.setCount(heldByPlayer.getCount()-1); }
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
}
