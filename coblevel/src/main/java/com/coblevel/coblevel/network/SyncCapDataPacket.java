package com.coblevel.coblevel.network;

import com.coblevel.coblevel.cap.PlayerCapCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCapDataPacket {

    private final int currentCap;
    private final int capturedPokemons;
    private final int defeatedTrainers;
    private final boolean gymCleared;
    private final long deliveredXp;

    public SyncCapDataPacket(int currentCap, int capturedPokemons,
                              int defeatedTrainers, boolean gymCleared, long deliveredXp) {
        this.currentCap = currentCap;
        this.capturedPokemons = capturedPokemons;
        this.defeatedTrainers = defeatedTrainers;
        this.gymCleared = gymCleared;
        this.deliveredXp = deliveredXp;
    }

    public static void encode(SyncCapDataPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.currentCap);
        buf.writeInt(packet.capturedPokemons);
        buf.writeInt(packet.defeatedTrainers);
        buf.writeBoolean(packet.gymCleared);
        buf.writeLong(packet.deliveredXp);
    }

    public static SyncCapDataPacket decode(FriendlyByteBuf buf) {
        return new SyncCapDataPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean(),
            buf.readLong()
        );
    }

    public static void handle(SyncCapDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            PlayerCapCapability.get(player).ifPresent(data -> {
                data.setCurrentCap(packet.currentCap);
                // Update local mission counters for HUD rendering
                // (We store them in PlayerCapData by deserializing a partial tag)
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                tag.putInt("currentCap", packet.currentCap);
                tag.putInt("capturedPokemons", packet.capturedPokemons);
                tag.putInt("defeatedTrainers", packet.defeatedTrainers);
                tag.putBoolean("gymCleared", packet.gymCleared);
                tag.putLong("deliveredXp", packet.deliveredXp);
                data.deserializeNBT(tag);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
