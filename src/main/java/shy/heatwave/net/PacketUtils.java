package shy.heatwave.net;

import net.minecraft.network.PacketByteBuf;

import java.util.function.Function;

public final class PacketUtils {
    private PacketUtils() {}

    public interface PacketArrayWriter<T> {
        void write(PacketByteBuf buf, T t);
    }

    public interface PacketArrayReader<T> {
        T read(PacketByteBuf buf);
    }

    public static <T> void writeArray(PacketByteBuf buf, T[] arr, PacketArrayWriter<T> writer) {
        buf.writeVarInt(arr.length);
        for (int i = 0; i < arr.length; i++) {
            writer.write(buf, arr[i]);
        }
    }

    public static <T> T[] readArray(PacketByteBuf buf, Function<Integer, T[]> arrSupplier, PacketArrayReader<T> reader) {
        final int len = buf.readVarInt();
        T[] arr = arrSupplier.apply(len);
        for (int i = 0; i < len; i++) {
            arr[i] = reader.read(buf);
        }
        return arr;
    }
}
