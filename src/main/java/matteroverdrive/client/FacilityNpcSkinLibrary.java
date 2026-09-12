package matteroverdrive.client;

import com.mojang.blaze3d.platform.NativeImage;
import matteroverdrive.entity.FacilityNpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds original 64x64 wide-arm player skins for the facility staff at runtime.
 * Keeping the skins procedural makes their ownership/provenance explicit while
 * still using a normal Steve UV layout and silhouette.
 */
public final class FacilityNpcSkinLibrary {
    private static ResourceLocation researcher;
    private static ResourceLocation engineer;
    private static ResourceLocation security;

    private FacilityNpcSkinLibrary() {}

    public static ResourceLocation texture(FacilityNpcEntity.Role role) {
        return switch (role) {
            case RESEARCHER -> researcher == null ? researcher = register("mo_researcher", makeResearcher()) : researcher;
            case ENGINEER -> engineer == null ? engineer = register("mo_engineer", makeEngineer()) : engineer;
            case SECURITY -> security == null ? security = register("mo_security", makeSecurity()) : security;
        };
    }

    private static ResourceLocation register(String name, NativeImage image) {
        return Minecraft.getInstance().getTextureManager().register(name, new DynamicTexture(image));
    }

    private static NativeImage makeResearcher() {
        NativeImage image = new NativeImage(64, 64, true);
        int skin = rgba(196, 146, 112);
        int hair = rgba(52, 43, 48);
        int undersuit = rgba(30, 42, 52);
        int coat = rgba(211, 221, 224);
        int coatShade = rgba(155, 171, 177);
        int cyan = rgba(55, 212, 235);
        int orange = rgba(230, 118, 53);

        paintHead(image, skin, hair, cyan);
        paintTorso(image, coat, coatShade);
        paintArm(image, false, coat, coatShade);
        paintArm(image, true, coat, coatShade);
        paintLeg(image, false, undersuit, rgba(21, 29, 36));
        paintLeg(image, true, undersuit, rgba(21, 29, 36));
        // Lab coat seam, ID light and Matter Overdrive orange rank marker.
        rect(image, 23, 20, 1, 12, coatShade);
        rect(image, 24, 22, 2, 2, cyan);
        rect(image, 27, 21, 1, 5, orange);
        rect(image, 20, 30, 8, 2, coatShade);
        return image;
    }

    private static NativeImage makeEngineer() {
        NativeImage image = new NativeImage(64, 64, true);
        int skin = rgba(177, 126, 91);
        int hair = rgba(45, 31, 25);
        int suit = rgba(40, 45, 49);
        int dark = rgba(24, 28, 31);
        int orange = rgba(221, 113, 39);
        int cyan = rgba(57, 204, 224);

        paintHead(image, skin, hair, cyan);
        paintTorso(image, suit, dark);
        paintArm(image, false, suit, dark);
        paintArm(image, true, suit, dark);
        paintLeg(image, false, dark, rgba(16, 18, 21));
        paintLeg(image, true, dark, rgba(16, 18, 21));
        // Utility harness, chest interface and reinforced knees.
        rect(image, 20, 20, 2, 12, orange);
        rect(image, 26, 20, 2, 12, orange);
        rect(image, 22, 25, 4, 2, orange);
        rect(image, 23, 21, 2, 2, cyan);
        rect(image, 4, 28, 4, 3, rgba(66, 72, 76));
        rect(image, 20, 60, 4, 3, rgba(66, 72, 76));
        return image;
    }

    private static NativeImage makeSecurity() {
        NativeImage image = new NativeImage(64, 64, true);
        int skin = rgba(151, 105, 78);
        int hair = rgba(24, 27, 31);
        int armour = rgba(31, 40, 50);
        int armourDark = rgba(17, 23, 30);
        int plate = rgba(116, 133, 143);
        int cyan = rgba(58, 218, 241);
        int warning = rgba(220, 69, 63);

        paintHead(image, skin, hair, cyan);
        paintTorso(image, armour, armourDark);
        paintArm(image, false, armour, armourDark);
        paintArm(image, true, armour, armourDark);
        paintLeg(image, false, armourDark, rgba(10, 15, 20));
        paintLeg(image, true, armourDark, rgba(10, 15, 20));
        // Plate carrier and illuminated security identifier.
        rect(image, 21, 21, 6, 8, plate);
        rect(image, 22, 22, 4, 2, armourDark);
        rect(image, 23, 22, 2, 1, cyan);
        rect(image, 26, 20, 2, 2, warning);
        rect(image, 44, 24, 4, 3, plate);
        rect(image, 36, 56, 4, 3, plate);
        return image;
    }

    private static void paintHead(NativeImage image, int skin, int hair, int optic) {
        // All six base head faces.
        rect(image, 8, 0, 8, 8, hair);
        rect(image, 16, 0, 8, 8, skin);
        rect(image, 0, 8, 8, 8, skin);
        rect(image, 8, 8, 8, 8, skin);
        rect(image, 16, 8, 8, 8, skin);
        rect(image, 24, 8, 8, 8, hair);
        // Hairline and compact sci-fi eye/visor accents on the face.
        rect(image, 8, 8, 8, 2, hair);
        rect(image, 9, 12, 2, 1, optic);
        rect(image, 13, 12, 2, 1, optic);
    }

    private static void paintTorso(NativeImage image, int main, int shade) {
        rect(image, 20, 16, 8, 4, main);
        rect(image, 28, 16, 8, 4, shade);
        rect(image, 16, 20, 4, 12, shade);
        rect(image, 20, 20, 8, 12, main);
        rect(image, 28, 20, 4, 12, shade);
        rect(image, 32, 20, 8, 12, shade);
    }

    private static void paintArm(NativeImage image, boolean left, int main, int shade) {
        int x = left ? 32 : 40;
        int y = left ? 48 : 16;
        rect(image, x + 4, y, 4, 4, main);
        rect(image, x + 8, y, 4, 4, shade);
        rect(image, x, y + 4, 4, 12, shade);
        rect(image, x + 4, y + 4, 4, 12, main);
        rect(image, x + 8, y + 4, 4, 12, shade);
        rect(image, x + 12, y + 4, 4, 12, shade);
    }

    private static void paintLeg(NativeImage image, boolean left, int main, int shade) {
        int x = left ? 16 : 0;
        int y = left ? 48 : 16;
        rect(image, x + 4, y, 4, 4, main);
        rect(image, x + 8, y, 4, 4, shade);
        rect(image, x, y + 4, 4, 12, shade);
        rect(image, x + 4, y + 4, 4, 12, main);
        rect(image, x + 8, y + 4, 4, 12, shade);
        rect(image, x + 12, y + 4, 4, 12, shade);
    }

    private static void rect(NativeImage image, int x, int y, int width, int height, int colour) {
        image.fillRect(x, y, width, height, colour);
    }

    /** NativeImage 1.20.x stores RGBA pixels as ABGR ints. */
    private static int rgba(int red, int green, int blue) {
        return 0xFF000000 | (blue << 16) | (green << 8) | red;
    }
}
