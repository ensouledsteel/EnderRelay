package net.ensouledsteel.ender_relay.block;

import net.ensouledsteel.ender_relay.EnderRelayMod;
import net.ensouledsteel.ender_relay.block_entity.EnderRelayBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class EnderRelayBlock extends Block implements BlockEntityProvider {

	// 0 - uncharged, 1-4 - Ender Crystal Charge, 5 - Nether Crystal Charge (does not reduce)
	public static final IntProperty RELAY_CHARGE = IntProperty.of("relay_charge",0,5);

	public EnderRelayBlock(Settings settings){
		super(settings);
		setDefaultState(getDefaultState().with(RELAY_CHARGE, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(RELAY_CHARGE);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		NbtCompound nbtCompound = BlockItem.getBlockEntityNbtFromStack(itemStack);
	}

	// TODO Simplify this, especially the compass logic
	@Override
	public ActionResult onUse(
		BlockState state,
		World world,
		BlockPos pos,
		PlayerEntity player,
		Hand hand,
		BlockHitResult hit
	) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof EnderRelayBlockEntity relayBlockEntity)) {
			// TODO handle this error more effectively
			return ActionResult.PASS;
		}

        ItemStack heldByPlayer = player.getStackInHand(hand);
		Item item = heldByPlayer.getItem();
		int relayCharge = state.get(RELAY_CHARGE);

		// if the item is a compass, try to put it in
		// if not, try to teleport to the location in the entity
		if (player.isSneaking()){
			if (!relayBlockEntity.isEmpty()) {
				relayBlockEntity.dropCompass();
			}
			return ActionResult.success(world.isClient);
		} else if(item == Items.COMPASS) {
			NbtCompound data = heldByPlayer.getNbt();
			if (data == null || !data.getBoolean(CompassItem.LODESTONE_TRACKED_KEY)) {
				return ActionResult.PASS;
			}

			if (!relayBlockEntity.isEmpty() && !world.isClient){
				relayBlockEntity.dropCompass();
			}

			if (!world.isClient) {
				world.playSound(
					null,
					pos,
					SoundEvents.ITEM_LODESTONE_COMPASS_LOCK,
					SoundCategory.BLOCKS,
					1.0f,
					1.0f
				);
				relayBlockEntity.setStack(0, heldByPlayer.copy().withCount(1));
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.create(player, state));
				world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
			}
			return ActionResult.success(world.isClient);
		} else if (item == Items.END_CRYSTAL) {
			if (relayCharge < 4){
				world.setBlockState(pos, state.with(RELAY_CHARGE, relayCharge+1));
				if (!world.isClient) {
					world.playSound(
						null,
						pos,
						SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
						SoundCategory.BLOCKS,
						1.0f,
						1.0f
					);
				}
				return ActionResult.success(true);
			}
			return ActionResult.PASS;
		} else if (item == Items.NETHER_STAR) {
			world.setBlockState(pos, state.with(RELAY_CHARGE, 5));
			if (!world.isClient) {
				world.playSound(
					null,
					pos,
					SoundEvents.ENTITY_WITHER_SPAWN,
					SoundCategory.BLOCKS,
					1.0f,
					1.0f
				);
			}
			return ActionResult.success(true);
		} else {
			if(!world.isClient) {
				doTeleport(state, relayBlockEntity, player, world, pos);
			}
			return ActionResult.success(true);
		}
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EnderRelayBlockEntity(pos, state);
	}

	public void doTeleport(BlockState state, EnderRelayBlockEntity blockEntity, PlayerEntity player, World world, BlockPos pos){
		int relayCharge = state.get(RELAY_CHARGE);

		if (relayCharge <= 0) {
			return;
		}

		BlockPos teleportPosition = NbtHelper.toBlockPos(
			Objects.requireNonNull(
				blockEntity.getStack(0).getNbt()).getCompound(CompassItem.LODESTONE_POS_KEY)
		);

		if (!world.getBlockState(teleportPosition).isOf(Blocks.LODESTONE)) {
			world.playSound(
				null,
				pos,
				SoundEvents.ENTITY_ENDER_EYE_DEATH,
				SoundCategory.BLOCKS,
				0.5f,
				0.2f
			);
			return;
		}

		// charged with nether star is finite
		if (relayCharge < 5) {
			world.setBlockState(pos, state.with(RELAY_CHARGE, relayCharge - 1));
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
			null,
			teleportPosition,
			SoundEvents.ENTITY_ENDERMAN_TELEPORT,
			SoundCategory.BLOCKS,
			0.5f,
			0.4f
		);
	}
}
