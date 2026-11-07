package pl.eventowki;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * G  = otwiera GUI i zaznaczasz przedmioty z ekwipunku.
 * R  = (trzymaj) szybko uzywa zaznaczonych przedmiotow po kolei na graczu, na ktorego patrzysz.
 *      Jesli nic nie zaznaczyles, uzywa przedmiotu z reki.
 */
public class EventowkiClient implements ClientModInitializer {
	/** Co ile tickow klikac. 1 = 20 klikniec/s (najszybciej). */
	private static final int CLICK_EVERY_TICKS = 1;

	private static KeyBinding clickKey;
	private static KeyBinding guiKey;
	private int tickCounter = 0;
	private int cursor = 0;

	@Override
	public void onInitializeClient() {
		clickKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.eventowki.click", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.eventowki"));
		guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.eventowki.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.eventowki"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (guiKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					client.setScreen(new SelectScreen());
				}
			}

			if (client.player == null || client.interactionManager == null || client.currentScreen != null
					|| !clickKey.isPressed()) {
				tickCounter = 0;
				return;
			}
			if (++tickCounter < CLICK_EVERY_TICKS) return;
			tickCounter = 0;

			Entity target = client.targetedEntity;
			if (!(target instanceof PlayerEntity)) return;

			List<Integer> slots = new ArrayList<>();
			for (int s : Selection.SLOTS.keySet()) {
				if (!client.player.getInventory().getStack(s).isEmpty()) slots.add(s);
			}
			slots.sort(Integer::compare);

			if (slots.isEmpty()) {
				interact(client, target, true);
			} else {
				cursor = (cursor + 1) % slots.size();
				int slot = slots.get(cursor);
				clickWith(client, slot, Selection.SLOTS.get(slot), target);
			}
		});
	}

	/** right = true: PPM (uzyj na graczu), false: LPM (uderz gracza). */
	private static void interact(MinecraftClient client, Entity target, boolean right) {
		if (right) {
			client.interactionManager.interactEntity(client.player, target, Hand.MAIN_HAND);
		} else {
			client.interactionManager.attackEntity(client.player, target);
		}
		client.player.swingHand(Hand.MAIN_HAND);
	}

	private static void clickWith(MinecraftClient client, int slot, boolean right, Entity target) {
		ClientPlayerEntity p = client.player;
		int old = p.getInventory().selectedSlot;
		if (slot < 9) {
			p.getInventory().selectedSlot = slot;
			interact(client, target, right);
		} else {
			// przedmiot z glownego ekwipunku: na chwile wymieniamy go z hotbarem slot 9 (indeks 8)
			int syncId = p.playerScreenHandler.syncId;
			client.interactionManager.clickSlot(syncId, slot, 8, SlotActionType.SWAP, p);
			p.getInventory().selectedSlot = 8;
			interact(client, target, right);
			client.interactionManager.clickSlot(syncId, slot, 8, SlotActionType.SWAP, p);
			p.getInventory().selectedSlot = old;
		}
	}
}
