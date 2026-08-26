package matteroverdrive.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    public static final Capability<IMatterStorage> MATTER =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IMatterStorage.class);
    }
}
