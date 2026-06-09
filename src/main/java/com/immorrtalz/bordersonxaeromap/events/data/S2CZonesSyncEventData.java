package com.immorrtalz.bordersonxaeromap.events.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CZonesSyncEventData(boolean isPartial, String zonesJson) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<S2CZonesSyncEventData> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("bordersonxaeromap", "s2c_zones_sync_event_data"));

	public static final StreamCodec<ByteBuf, S2CZonesSyncEventData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL,
		S2CZonesSyncEventData::isPartial,
		ByteBufCodecs.STRING_UTF8,
		S2CZonesSyncEventData::zonesJson,
		S2CZonesSyncEventData::new);
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}