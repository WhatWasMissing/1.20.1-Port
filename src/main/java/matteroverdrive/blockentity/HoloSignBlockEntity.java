package matteroverdrive.blockentity;

import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HoloSignBlockEntity extends BlockEntity {
    private static final int MAX_TEXT_LENGTH = 256;
    private String text = "";

    public HoloSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModExtraBlockEntities.HOLO_SIGN.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        String next = value == null ? "" : value.strip();
        if (next.length() > MAX_TEXT_LENGTH) {
            next = next.substring(0, MAX_TEXT_LENGTH);
        }
        if (next.equals(text)) {
            return;
        }
        text = next;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Text", text);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        text = tag.getString("Text");
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
