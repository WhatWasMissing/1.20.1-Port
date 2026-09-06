package matteroverdrive.client;

import matteroverdrive.entity.DroneEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** 1.20 reconstruction of the authoritative 1.12 ModelDrone geometry. */
public final class DroneModel extends HierarchicalModel<DroneEntity> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart flapUpper;
    private final ModelPart flapBottom;
    private final ModelPart flapLeft;
    private final ModelPart flapRight;
    private final ModelPart exhaustLeft;
    private final ModelPart exhaustRight;

    public DroneModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.flapUpper = body.getChild("flap_upper");
        this.flapBottom = body.getChild("flap_bottom");
        this.flapLeft = body.getChild("flap_left");
        this.flapRight = body.getChild("flap_right");
        this.exhaustLeft = body.getChild("exhaust_left");
        this.exhaustRight = body.getChild("exhaust_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.0F, -3.0F, -3.0F, 6.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        body.addOrReplaceChild("flap_upper",
                CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, -Mth.QUARTER_PI, 0.0F, 0.0F));
        body.addOrReplaceChild("flap_bottom",
                CubeListBuilder.create().texOffs(7, 19).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -3.0F, Mth.QUARTER_PI, 0.0F, 0.0F));
        body.addOrReplaceChild("flap_left",
                CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, -3.0F, Mth.QUARTER_PI, 0.0F, -Mth.HALF_PI));
        body.addOrReplaceChild("flap_right",
                CubeListBuilder.create().texOffs(7, 13).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, -3.0F, -Mth.QUARTER_PI, 0.0F, -Mth.HALF_PI));
        body.addOrReplaceChild("exhaust_left",
                CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-1.0F, -1.0F, 0.0F, 2.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 2.0F, -0.6108652F, 0.0F, 0.0F));
        body.addOrReplaceChild("exhaust_right",
                CubeListBuilder.create().texOffs(26, 7).mirror().addBox(-1.0F, -1.0F, 0.0F, 2.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 2.0F, -0.6108652F, 0.0F, 0.0F));
        body.addOrReplaceChild("eye",
                CubeListBuilder.create().texOffs(14, 13).mirror().addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -3.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DroneEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        body.xRot = headPitch * Mth.DEG_TO_RAD;
        body.yRot = netHeadYaw * Mth.DEG_TO_RAD;

        float flap = Mth.clamp(limbSwingAmount, 0.0F, 1.0F) * 0.6F;
        flapUpper.xRot = -Mth.QUARTER_PI - flap;
        flapBottom.xRot = Mth.QUARTER_PI + flap;
        flapLeft.xRot = Mth.QUARTER_PI + flap;
        flapLeft.zRot = -Mth.HALF_PI;
        flapRight.xRot = -Mth.QUARTER_PI - flap;
        flapRight.zRot = -Mth.HALF_PI;

        float exhaustPitch = -1.0F + flap - body.xRot;
        exhaustLeft.xRot = exhaustPitch;
        exhaustRight.xRot = exhaustPitch;
    }
}
