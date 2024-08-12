/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.execution.buffer;

import io.airlift.compress.v2.Compressor;
import io.airlift.compress.v2.Decompressor;
import io.airlift.compress.v2.lz4.Lz4Compressor;
import io.airlift.compress.v2.lz4.Lz4Decompressor;
import io.airlift.compress.v2.zstd.ZstdCompressor;
import io.airlift.compress.v2.zstd.ZstdDecompressor;
import io.trino.spi.block.BlockEncodingSerde;

import javax.crypto.SecretKey;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class PagesSerdeFactory
{
    private static final int SERIALIZED_PAGE_DEFAULT_BLOCK_SIZE_IN_BYTES = 64 * 1024;

    private final BlockEncodingSerde blockEncodingSerde;
    private final CompressionCodec compressionCodec;

    public PagesSerdeFactory(BlockEncodingSerde blockEncodingSerde, CompressionCodec compressionCodec)
    {
        this.blockEncodingSerde = requireNonNull(blockEncodingSerde, "blockEncodingSerde is null");
        this.compressionCodec = requireNonNull(compressionCodec, "compressionCodec is null");
    }

    public PageSerializer createSerializer(Optional<SecretKey> encryptionKey)
    {
        return new PageSerializer(blockEncodingSerde, createCompressor(compressionCodec), encryptionKey, SERIALIZED_PAGE_DEFAULT_BLOCK_SIZE_IN_BYTES);
    }

    public PageDeserializer createDeserializer(Optional<SecretKey> encryptionKey)
    {
        return new PageDeserializer(blockEncodingSerde, createDecompressor(compressionCodec), encryptionKey, SERIALIZED_PAGE_DEFAULT_BLOCK_SIZE_IN_BYTES);
    }

    public static Optional<Compressor> createCompressor(CompressionCodec compressionCodec)
    {
        return switch (compressionCodec) {
            case NONE -> Optional.empty();
            case LZ4 -> Optional.of(Lz4Compressor.create());
            case ZSTD -> Optional.of(ZstdCompressor.create());
        };
    }

    public static Optional<Decompressor> createDecompressor(CompressionCodec compressionCodec)
    {
        return switch (compressionCodec) {
            case NONE -> Optional.empty();
            case LZ4 -> Optional.of(Lz4Decompressor.create());
            case ZSTD -> Optional.of(ZstdDecompressor.create());
        };
    }
}
