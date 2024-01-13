package net.ensouledsteel.ender_relay.block_entity;

import net.ensouledsteel.ender_relay.EnderRelayMod;
import net.ensouledsteel.ender_relay.common.ItemLibrary;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Clearable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class EnderRelayBlockEntity extends BlockEntity implements Clearable, Inventory {
	public static String COMPASS_ITEM_KEY = "CompassItem";
	private final DefaultedList<ItemStack> inventory;

	public EnderRelayBlockEntity(BlockPos position, BlockState state){
		super(ItemLibrary.ENDER_RELAY_BLOCK_ENTITY, position, state);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		if (!this.isEmpty()){
			EnderRelayMod.LOGGER.info("LITERALLY DETECTED INVENTORY DUDE");
			nbt.put(COMPASS_ITEM_KEY, inventory.get(0).writeNbt(new NbtCompound()));
		}
		EnderRelayMod.LOGGER.info("WRITING NBT");
		EnderRelayMod.LOGGER.info(nbt.toString());
		super.writeNbt(nbt);
	}

	@Override
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		EnderRelayMod.LOGGER.info("READING NBT");
		EnderRelayMod.LOGGER.info(nbt.toString());
		if (nbt.contains(COMPASS_ITEM_KEY)){
			EnderRelayMod.LOGGER.info("PUTTING SHIT IN DUDE");
			this.inventory.set(0, ItemStack.fromNbt(nbt.getCompound(COMPASS_ITEM_KEY)));
		}
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return stack.isIn(ItemTags.COMPASSES) && getStack(slot).isEmpty();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return inventory.get(0).isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return removeStack(slot);
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack itemStack = Objects.requireNonNullElse(inventory.get(slot), ItemStack.EMPTY);
		this.inventory.set(slot, ItemStack.EMPTY);
		markDirty();
		return itemStack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		if (isValid(slot, stack) && this.world != null && !this.world.isClient) {
			this.inventory.set(slot, stack);
			markDirty();
		}
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return Inventory.canPlayerUse(this, player);
	}

	public void dropCompass() {
		if (this.world != null && !this.world.isClient) {
			BlockPos blockPos = this.getPos();
			ItemStack itemStack = inventory.get(0);
			if (!itemStack.isEmpty()) {
				removeStack(0);
				Vec3d vec3d =
					Vec3d.offset(blockPos, 0.5, 1.01, 0.5)
					.addRandom(this.world.random, 0.7F);
				ItemStack itemStack2 = itemStack.copy();
				ItemEntity itemEntity =
					new ItemEntity(this.world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), itemStack2);
				itemEntity.setToDefaultPickupDelay();
				this.world.spawnEntity(itemEntity);
				this.clear();
			}
		}
	}

	@Override
	public void clear() { }
}
