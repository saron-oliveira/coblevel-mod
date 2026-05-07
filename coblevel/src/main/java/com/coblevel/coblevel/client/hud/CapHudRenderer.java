package com.coblevel.coblevel.client.hud;

import com.coblevel.coblevel.cap.CapProgression;
import com.coblevel.coblevel.cap.MissionRequirements;
import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapHudRenderer {

    private static final int PADDING       = 6;
    private static final int LINE_HEIGHT   = 10;
    private static final int BAR_WIDTH     = 80;
    private static final int BAR_HEIGHT    = 5;

    // Colors
    private static final int COLOR_BG      = 0xAA000000; // semi-transparent black
    private static final int COLOR_TITLE   = 0xFFFFD700; // gold
    private static final int COLOR_DONE    = 0xFF55FF55; // green
    private static final int COLOR_TODO    = 0xFFFF5555; // red
    private static final int COLOR_LABEL   = 0xFFCCCCCC; // light gray
    private static final int BAR_BG        = 0xFF333333;
    private static final int BAR_FILL_DONE = 0xFF44AA44;
    private static final int BAR_FILL_PROG = 0xFF4488FF;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.renderDebug) return;
        if (mc.screen != null) return; // hide when any menu is open

        PlayerCapCapability.get(player).ifPresent(data -> {
            int cap        = data.getCurrentCap();
            int phaseIndex = CapProgression.getPhaseForCap(cap);
            MissionRequirements req = MissionRequirements.forPhase(Math.max(0, phaseIndex));

            int captures  = data.getCapturedPokemons();
            int trainers  = data.getDefeatedTrainers();
            boolean gym   = data.isGymCleared();
            long xp       = data.getDeliveredXp();
            boolean maxed = CapProgression.isMaxCap(cap);

            // --- Layout ---
            // Title + 4 mission rows = 5 lines + spacing
            int rows       = maxed ? 1 : 5;
            int panelH     = PADDING * 2 + rows * (LINE_HEIGHT + 2) + (maxed ? 0 : rows * (BAR_HEIGHT + 2));
            int panelW     = 110;

            GuiGraphics gfx   = event.getGuiGraphics();
            int screenW       = mc.getWindow().getGuiScaledWidth();
            int panelX        = screenW - panelW - 4;
            int panelY        = 4;

            // Background
            gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_BG);

            int x = panelX + PADDING;
            int y = panelY + PADDING;

            if (maxed) {
                gfx.drawString(mc.font, "✦ Cap MAX: " + cap, x, y, COLOR_TITLE, false);
                return;
            }

            // Title
            gfx.drawString(mc.font, "Cap: " + cap + "  →  " + CapProgression.getNextCap(cap), x, y, COLOR_TITLE, false);
            y += LINE_HEIGHT + 4;

            // Captures
            y = drawMissionRow(gfx, mc, x, y, panelW - PADDING * 2,
                "Capturas", captures, req.captureCount());

            // Trainers
            y = drawMissionRow(gfx, mc, x, y, panelW - PADDING * 2,
                "Treinadores", trainers, req.trainerCount());

            // Gym
            y = drawBoolRow(gfx, mc, x, y, "Ginásio", gym);

            // XP
            y = drawMissionRow(gfx, mc, x, y, panelW - PADDING * 2,
                "XP", (int) Math.min(xp, Integer.MAX_VALUE), (int) Math.min(req.xpAmount(), Integer.MAX_VALUE));
        });
    }

    /**
     * Draws a label + numeric progress bar row.
     * Returns the new Y position after drawing.
     */
    private static int drawMissionRow(GuiGraphics gfx, Minecraft mc,
                                       int x, int y, int maxW,
                                       String label, int current, int required) {
        boolean done  = current >= required;
        int textColor = done ? COLOR_DONE : COLOR_LABEL;
        String check  = done ? "✔ " : "✘ ";
        String text   = check + label + ": " + formatNum(current) + "/" + formatNum(required);

        gfx.drawString(mc.font, text, x, y, textColor, false);
        y += LINE_HEIGHT + 1;

        // Progress bar
        float pct = required > 0 ? Math.min(1f, (float) current / required) : 1f;
        int barX  = x;
        int barW  = BAR_WIDTH;

        gfx.fill(barX, y, barX + barW, y + BAR_HEIGHT, BAR_BG);
        int fillColor = done ? BAR_FILL_DONE : BAR_FILL_PROG;
        gfx.fill(barX, y, barX + (int)(barW * pct), y + BAR_HEIGHT, fillColor);

        y += BAR_HEIGHT + 3;
        return y;
    }

    /**
     * Draws a boolean (done/not done) row without a progress bar.
     */
    private static int drawBoolRow(GuiGraphics gfx, Minecraft mc,
                                    int x, int y,
                                    String label, boolean done) {
        int textColor = done ? COLOR_DONE : COLOR_LABEL;
        String check  = done ? "✔ " : "✘ ";
        gfx.drawString(mc.font, check + label + ": " + (done ? "OK" : "Pendente"), x, y, textColor, false);
        y += LINE_HEIGHT + 1;

        // Static bar (full green if done, empty if not)
        int barX = x;
        gfx.fill(barX, y, barX + BAR_WIDTH, y + BAR_HEIGHT, BAR_BG);
        if (done) gfx.fill(barX, y, barX + BAR_WIDTH, y + BAR_HEIGHT, BAR_FILL_DONE);

        y += BAR_HEIGHT + 3;
        return y;
    }

    private static String formatNum(int n) {
        if (n >= 1_000_000) return (n / 1_000_000) + "M";
        if (n >= 1_000)     return (n / 1_000) + "k";
        return String.valueOf(n);
    }
}
