package com.eldenringcore.client;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.BossState;
import com.eldenringcore.boss.EldenBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Generic high-performance 3D Mesh Boss Renderer for authentic low-poly GLB/OBJ Elden Ring models.
 * Supports automatic Phase 1 -> Phase 2 mesh transformation (e.g. Malenia sprouting Scarlet Rot wings!).
 */
public class MeshBossRenderer<T extends EldenBossEntity> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    private final ResourceLocation phase1MeshPath;
    private final ResourceLocation phase2MeshPath;
    private float[] phase1Verts;
    private float[] phase2Verts;
    private boolean loaded = false;

    public MeshBossRenderer(EntityRendererProvider.Context ctx, String textureName, String phase1Mesh, String phase2Mesh) {
        super(ctx);
        this.shadowRadius = 0.9f;
        this.texture = new ResourceLocation(EldenRingCore.MOD_ID, "textures/entity/" + textureName);
        this.phase1MeshPath = new ResourceLocation(EldenRingCore.MOD_ID, "models/mesh/" + phase1Mesh);
        this.phase2MeshPath = phase2Mesh != null
                ? new ResourceLocation(EldenRingCore.MOD_ID, "models/mesh/" + phase2Mesh)
                : null;
    }

    private void ensureLoaded() {
        if (this.loaded) return;
        this.loaded = true;
        this.phase1Verts = loadMesh(this.phase1MeshPath);
        if (this.phase2MeshPath != null) {
            this.phase2Verts = loadMesh(this.phase2MeshPath);
        }
    }

    private float[] loadMesh(ResourceLocation path) {
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(path);
            if (res.isEmpty()) return new float[0];
            try (InputStream is = res.get().open(); DataInputStream dis = new DataInputStream(is)) {
                int vertCount = dis.readInt();
                float[] data = new float[vertCount * 8];
                for (int i = 0; i < data.length; i++) {
                    data[i] = dis.readFloat();
                }
                return data;
            }
        } catch (Exception e) {
            EldenRingCore.LOGGER.error("Failed to load 3D boss mesh: {}", path, e);
            return new float[0];
        }
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ensureLoaded();
        float[] verts = (entity.isSyncedPhaseTwo() && this.phase2Verts != null && this.phase2Verts.length > 0)
                ? this.phase2Verts
                : this.phase1Verts;
        if (verts == null || verts.length < 24) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            return;
        }

        poseStack.pushPose();

        // Rotate boss to face its look direction
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyYaw));

        // Procedural combat motion (idle breathing, attack lunge/spin, staggered kneel)
        float time = entity.tickCount + partialTicks;
        BossState state = entity.getSyncedBossState();
        if (state == BossState.STAGGERED) {
            poseStack.translate(0.0, -0.35, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(22.0f));
        } else if (state == BossState.PHASE_TRANSITION) {
            poseStack.translate(0.0, 0.35 + Math.sin(time * 0.4) * 0.15, 0.0);
        } else if (state == BossState.ATTACKING) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.sin(time * 1.1) * 35.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(12.0f));
        } else {
            poseStack.translate(0.0, Math.sin(time * 0.12) * 0.04, 0.0);
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(this.texture));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int overlay = entity.hurtTime > 0 ? OverlayTexture.pack(OverlayTexture.u(0.0f), OverlayTexture.v(true)) : OverlayTexture.NO_OVERLAY;

        // Emit triangles as degenerate quads (v0, v1, v2, v2) for full shader & Minecraft compatibility
        int vertCount = verts.length / 8;
        for (int i = 0; i + 2 < vertCount; i += 3) {
            emitVertex(vc, pose, normal, verts, i, overlay, packedLight);
            emitVertex(vc, pose, normal, verts, i + 1, overlay, packedLight);
            emitVertex(vc, pose, normal, verts, i + 2, overlay, packedLight);
            emitVertex(vc, pose, normal, verts, i + 2, overlay, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void emitVertex(VertexConsumer vc, Matrix4f pose, Matrix3f normal, float[] data, int idx, int overlay, int light) {
        int o = idx * 8;
        vc.vertex(pose, data[o], data[o + 1], data[o + 2])
                .color(255, 255, 255, 255)
                .uv(data[o + 3], data[o + 4])
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, data[o + 5], data[o + 6], data[o + 7])
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }
}
