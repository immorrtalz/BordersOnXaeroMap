package com.immorrtalz.bordersonxaeromap.zone;

import java.util.HashSet;
import java.util.Set;

public record ChunkRect(int left, int top, int right, int bottom) {
	public static ChunkRect of(int x1, int z1, int x2, int z2)
		{ return new ChunkRect(Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2)); }

	public static ChunkRect single(int chunkX, int chunkZ)
		{ return new ChunkRect(chunkX, chunkZ, chunkX, chunkZ); }

	public int width() { return right - left + 1; }
	public int height() { return bottom - top + 1; }
	public int area() { return width() * height(); }

	public Set<Long> toChunkKeys()
	{
		Set<Long> result = new HashSet<>(Math.max(area(), 1));

		for (int x = left; x <= right; x++)
		{
			for (int z = top; z <= bottom; z++)
			{
				result.add(ZoneRepository.packChunk(x, z));
			}
		}

		return result;
	}
}