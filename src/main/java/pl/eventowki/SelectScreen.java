package pl.eventowki;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** GUI: klikasz przedmiot, zeby go zaznaczyc/odznaczyc. */
public class SelectScreen extends Screen {
	private static final int SLOT = 18;
	private int left;
	private int top;

	public SelectScreen() {
		super(Text.literal("Eventowki"));
	}

	@Override
	protected void init() {
		left = (width - 9 * SLOT) / 2;
		top = height / 2 - 50;
		int btnY = top + 4 * SLOT + 14;
		addDrawableChild(ButtonWidget.builder(Text.literal("Zaznacz wszystko"), b -> selectAll())
				.dimensions(left, btnY, 84, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Odznacz"), b -> Selection.SLOTS.clear())
				.dimensions(left + 88, btnY, 74, 20).build());
	}

	private void selectAll() {
		if (client == null || client.player == null) return;
		for (int i = 0; i < 36; i++) {
			if (!client.player.getInventory().getStack(i).isEmpty()) Selection.SLOTS.put(i, true);
		}
	}

	private int slotX(int slot) {
		return left + (slot % 9) * SLOT;
	}

	private int slotY(int slot) {
		if (slot < 9) return top + 3 * SLOT + 6; // hotbar na dole
		return top + ((slot - 9) / 9) * SLOT;
	}

	private int slotAt(double mx, double my) {
		for (int i = 0; i < 36; i++) {
			int x = slotX(i);
			int y = slotY(i);
			if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) return i;
		}
		return -1;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(left - 8, top - 24, left + 9 * SLOT + 8, top + 4 * SLOT + 40, 0xCC000000);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		if (client == null || client.player == null) return;

		context.drawCenteredTextWithShadow(textRenderer,
				Text.literal("LPM = klika lewym (L)   PPM = klika prawym (P)"), width / 2, top - 16, 0xFFFFFF);

		int hovered = slotAt(mouseX, mouseY);
		for (int i = 0; i < 36; i++) {
			int x = slotX(i);
			int y = slotY(i);
			context.fill(x, y, x + SLOT - 1, y + SLOT - 1, 0xFF373737);
			ItemStack stack = client.player.getInventory().getStack(i);
			Boolean mode = Selection.SLOTS.get(i);
			if (mode != null && !stack.isEmpty()) {
				// zielony = PPM, niebieski = LPM
				context.fill(x, y, x + SLOT - 1, y + SLOT - 1, mode ? 0xAA00CC00 : 0xAA3377FF);
			}
			if (!stack.isEmpty()) {
				context.drawItem(stack, x + 1, y + 1);
				context.drawStackOverlay(textRenderer, stack, x + 1, y + 1);
				if (mode != null) {
					context.drawText(textRenderer, mode ? "P" : "L", x + 1, y + 1, 0xFFFFFF, true);
				}
			}
		}
		if (hovered >= 0) {
			ItemStack stack = client.player.getInventory().getStack(hovered);
			if (!stack.isEmpty()) {
				context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int slot = slotAt(mouseX, mouseY);
		if (slot >= 0 && client != null && client.player != null
				&& !client.player.getInventory().getStack(slot).isEmpty()) {
			// LPM w GUI = ma klikac lewym, PPM w GUI = ma klikac prawym; ten sam przycisk drugi raz = odznacz
			if (button == 0 || button == 1) {
				boolean right = button == 1;
				Boolean current = Selection.SLOTS.get(slot);
				if (current != null && current == right) {
					Selection.SLOTS.remove(slot);
				} else {
					Selection.SLOTS.put(slot, right);
				}
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
