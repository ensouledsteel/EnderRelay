package net.ensouledsteel.ender_relay.common;

import net.ensouledsteel.ender_relay.block.EnderRelayBlock;
import net.ensouledsteel.ender_relay.block_entity.EnderRelayBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class ItemLibrary {
	public static final String MOD_NAME = "ender_relay";
	public static final Block ENDER_RELAY = new EnderRelayBlock(QuiltBlockSettings.create().strength(2.0f));
	public static final BlockEntityType<EnderRelayBlockEntity> ENDER_RELAY_BLOCK_ENTITY = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		new Identifier(MOD_NAME, "ender_relay_block_entity"),
		QuiltBlockEntityTypeBuilder.create(EnderRelayBlockEntity::new, ENDER_RELAY).build()
	);

	public static void onInitialize(){
		Registry.register(
			Registries.BLOCK,
			new Identifier(MOD_NAME, "ender_relay"),
			ENDER_RELAY);
		Registry.register(
			Registries.ITEM,
			new Identifier(MOD_NAME, "ender_relay"),
			new BlockItem(ENDER_RELAY, new QuiltItemSettings())
		);
	}
}
