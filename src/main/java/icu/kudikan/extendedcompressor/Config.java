package icu.kudikan.extendedcompressor;

import icu.kudikan.extendedcompressor.util.ConfigBuilder;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final Config INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new ConfigBuilder();
        INSTANCE = new Config(builder);
        SPEC = builder.build();
    }

    public ModConfigSpec.DoubleValue extendedCompressorPowerCapMultiplier;
    public ModConfigSpec.DoubleValue extendedCompressorPowerRateMultiplier;
    public ModConfigSpec.IntValue extendedCompressorBreakProtectionThreshold;

    private Config(ConfigBuilder builder) {
        builder.pushSection("ExtendedQuantumCompression", "Settings for the Extended Quantum Compressor.");
        extendedCompressorPowerCapMultiplier = builder.start(
                        "powerCapacityMultiplier",
                        "Extended Quantum Compressor Power Capacity Multiplier",
                        "Multiplier for the FE capacity of the Extended Quantum Compressor relative to the Quantum Compressor."
                ).gameRestart()
                .defineInRange("powerCapacityMultiplier", 4D, 0D, Integer.MAX_VALUE);

        extendedCompressorPowerRateMultiplier = builder.start(
                        "powerRateMultiplier",
                        "Extended Quantum Compressor Power Rate Multiplier",
                        "Multiplier for the default FE/t consumption rate of the Extended Quantum Compressor relative to the Quantum Compressor."
                ).gameRestart()
                .defineInRange("powerRateMultiplier", 2D, 0D, Integer.MAX_VALUE);
        extendedCompressorBreakProtectionThreshold = builder.start(
                "breakProtectionThreshold",
                "Break Protection Item Threshold",
                "Minimum stored item count before breaking a Extended Compressor requires sneaking to confirm."
        ).defineInRange("breakProtectionThreshold", 4000, 1, Integer.MAX_VALUE);
        builder.popSection();
    }
}
