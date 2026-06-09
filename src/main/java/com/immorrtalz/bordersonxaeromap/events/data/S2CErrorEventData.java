package com.immorrtalz.bordersonxaeromap.events.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CErrorEventData(String message) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<S2CErrorEventData> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("bordersonxaeromap", "s2c_error_event_data"));

	public static final StreamCodec<ByteBuf, S2CErrorEventData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		S2CErrorEventData::message,
		S2CErrorEventData::new);
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}