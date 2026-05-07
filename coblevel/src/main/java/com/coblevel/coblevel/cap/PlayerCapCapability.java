package com.coblevel.coblevel.cap;

import com.coblevel.coblevel.CobLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID)
public class PlayerCapCapability {

    public static final Capability<PlayerCapData> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation ID =
            new ResourceLocation(CobLevel.MOD_ID, "player_cap_data");

    // ---- Helper ----

    public static LazyOptional<PlayerCapData> get(Player player) {
        return player.getCapability(CAP);
    }

    // ---- Attach event ----

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        if (event.getCapabilities().containsKey(ID)) return;

        PlayerCapProvider provider = new PlayerCapProvider();
        event.addCapability(ID, provider);
        event.addListener(provider::invalidate);
    }

    // ---- Clone on death/dimension change ----

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() || !event.isWasDeath()) { // always copy
            Player original = event.getOriginal();
            Player clone = event.getEntity();

            original.reviveCaps();
            get(original).ifPresent(oldData -> {
                get(clone).ifPresent(newData -> {
                    newData.deserializeNBT(oldData.serializeNBT());
                });
            });
            original.invalidateCaps();
        }
    }

    // ---- Provider ----

    public static class PlayerCapProvider implements ICapabilitySerializable<CompoundTag> {

        private final PlayerCapData data = new PlayerCapData();
        private final LazyOptional<PlayerCapData> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CAP.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.deserializeNBT(tag);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
