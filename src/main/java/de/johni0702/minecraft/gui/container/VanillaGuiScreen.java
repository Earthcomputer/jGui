package de.johni0702.minecraft.gui.container;

import de.johni0702.minecraft.gui.function.Draggable;
import de.johni0702.minecraft.gui.function.Scrollable;
import de.johni0702.minecraft.gui.function.Typeable;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.ReadablePoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VanillaGuiScreen extends GuiScreen implements Draggable, Typeable, Scrollable {

    public static VanillaGuiScreen setup(net.minecraft.client.gui.GuiScreen originalGuiScreen) {
        VanillaGuiScreen gui = new VanillaGuiScreen(originalGuiScreen);
        gui.register();
        return gui;
    }

    private final net.minecraft.client.gui.GuiScreen mcScreen;
    private final EventHandler eventHandler = new EventHandler();

    private static List<EventHandler> listeners = new ArrayList<>();

    public VanillaGuiScreen(net.minecraft.client.gui.GuiScreen mcScreen) {
        this.mcScreen = mcScreen;

        super.setBackground(Background.NONE);
    }

    // Needs to be called from or after GuiInitEvent.Post, will auto-unregister on any GuiOpenEvent
    public void register() {
        if (!eventHandler.active) {
            eventHandler.active = true;

            listeners.add(eventHandler);

            getSuperMcGui().setWorldAndResolution(Minecraft.getMinecraft(), mcScreen.width, mcScreen.height);
            getSuperMcGui().initGui();
        }
    }

    public void display() {
        getMinecraft().displayGuiScreen(mcScreen);
        register();
    }

    @Override
    public net.minecraft.client.gui.GuiScreen toMinecraft() {
        return mcScreen;
    }

    @Override
    public void setBackground(Background background) {
        throw new UnsupportedOperationException("Cannot set background of vanilla gui screen.");
    }

    private net.minecraft.client.gui.GuiScreen getSuperMcGui() {
        return super.toMinecraft();
    }

    @Override
    public boolean mouseClick(ReadablePoint position, int button) {
        forwardMouseInput();
        return false;
    }

    @Override
    public boolean mouseDrag(ReadablePoint position, int button, long timeSinceLastCall) {
        forwardMouseInput();
        return false;
    }

    @Override
    public boolean mouseRelease(ReadablePoint position, int button) {
        forwardMouseInput();
        return false;
    }

    @Override
    public boolean scroll(ReadablePoint mousePosition, int dWheel) {
        forwardMouseInput();
        return false;
    }

    private void forwardMouseInput() {
        try {
            mcScreen.handleMouseInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
        try {
            mcScreen.handleKeyboardInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static void onGuiClosed() {
        for (int i = listeners.size() - 1; i >= 0; i--)
            listeners.get(i).onGuiClosed();
    }

    public static void onGuiRender(int mouseX, int mouseY, float partialTicks) {
        listeners.forEach(listener -> listener.onGuiRender(mouseX, mouseY, partialTicks));
    }

    public static void onTickOverlay() {
        listeners.forEach(EventHandler::tickOverlay);
    }

    public static boolean onMouseInput() throws IOException {
        boolean result = true;
        for (EventHandler listener : listeners)
            result &= listener.onMouseInput();
        return result;
    }

    public static boolean onKeyboardInput() throws IOException {
        boolean result = true;
        for (EventHandler listener : listeners)
            result &= listener.onKeyboardInput();
        return result;
    }

    // Used when wrapping an already existing mc.GuiScreen
    private class EventHandler {
        private boolean active;

        public void onGuiClosed() {
            listeners.remove(this);

            if (active) {
                active = false;
                getSuperMcGui().onGuiClosed();
            }
        }

        public void onGuiRender(int mouseX, int mouseY, float partialTicks) {
            getSuperMcGui().drawScreen(mouseX, mouseY, partialTicks);
        }

        public void tickOverlay() {
            getSuperMcGui().updateScreen();
        }

        public boolean onMouseInput() throws IOException {
            getSuperMcGui().handleMouseInput();
            return false;

            // TODO: Forge support
            /*
            if (mcScreen.equals(getMinecraft().currentScreen)) {
                MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.MouseInputEvent.Post(mcScreen));
            }
            */
        }

        public boolean onKeyboardInput() throws IOException {
            getSuperMcGui().handleKeyboardInput();
            return false;

            // TODO: Forge support
            /*
            if (mcScreen.equals(getMinecraft().currentScreen)) {
                MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.KeyboardInputEvent.Post(mcScreen));
            }
            */
        }
    }
}
