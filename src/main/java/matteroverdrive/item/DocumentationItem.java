package matteroverdrive.item;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DocumentationItem extends Item {
    public enum Document {
        TESTING_CHECKLIST(0, "M2 Testing Checklist", "docs/to_test.txt"),
        FEATURE_REFERENCE(1, "Current Feature Reference", "docs/current_features.txt"),
        SYSTEM_GUIDE(2, "Matter Overdrive System Guide", "docs/system_guide.txt");

        public final int id;
        public final String title;
        public final String resourcePath;

        Document(int id, String title, String resourcePath) {
            this.id = id;
            this.title = title;
            this.resourcePath = resourcePath;
        }

        public static Document fromId(int id) {
            for (Document document : values()) {
                if (document.id == id) {
                    return document;
                }
            }
            return TESTING_CHECKLIST;
        }
    }

    private final Document document;

    public DocumentationItem(Properties properties, Document document) {
        super(properties.stacksTo(1));
        this.document = document;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetwork.openDocumentation(serverPlayer, document.id);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to open " + document.title + ".")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Bundled with Matter Overdrive " + MatterOverdrive.DISPLAY_VERSION)
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
