package com.immorrtalz.bordersonxaeromap.events.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SJoinedWorldEventData(String playerUuid) implements CustomPacketPayload
{
	// we want to pass a Player type in the event
	public static final CustomPacketPayload.Type<C2SJoinedWorldEventData> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("bordersonxaeromap", "c2s_joined_world_event_data"));

	// Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
	// 'name' will be encoded and decoded as a string
	// 'age' will be encoded and decoded as an integer
	// The final parameter takes in the previous parameters in the order they are provided to construct the payload object
	public static final StreamCodec<ByteBuf, C2SJoinedWorldEventData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		C2SJoinedWorldEventData::playerUuid,
		C2SJoinedWorldEventData::new
	);
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}