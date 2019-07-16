package me.sudsey.chip8.interpret;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class VirtualKeyboard {

    private static HashMap<Integer, Integer> qwertyToChip8Key;

    static {
        qwertyToChip8Key = new HashMap<>();
        qwertyToChip8Key.put(GLFW_KEY_1, 0x1);
        qwertyToChip8Key.put(GLFW_KEY_2, 0x2);
        qwertyToChip8Key.put(GLFW_KEY_3, 0x3);
        qwertyToChip8Key.put(GLFW_KEY_4, 0xC);
        qwertyToChip8Key.put(GLFW_KEY_Q, 0x4);
        qwertyToChip8Key.put(GLFW_KEY_W, 0x5);
        qwertyToChip8Key.put(GLFW_KEY_E, 0x6);
        qwertyToChip8Key.put(GLFW_KEY_R, 0xD);
        qwertyToChip8Key.put(GLFW_KEY_A, 0x7);
        qwertyToChip8Key.put(GLFW_KEY_S, 0x8);
        qwertyToChip8Key.put(GLFW_KEY_D, 0x9);
        qwertyToChip8Key.put(GLFW_KEY_F, 0xE);
        qwertyToChip8Key.put(GLFW_KEY_Z, 0xA);
        qwertyToChip8Key.put(GLFW_KEY_X, 0x0);
        qwertyToChip8Key.put(GLFW_KEY_C, 0xB);
        qwertyToChip8Key.put(GLFW_KEY_V, 0xF);
    }


    private Terminal terminal;

    private boolean[] keys;
    private int lastKey;

    public VirtualKeyboard(Terminal terminal) {
        this.terminal = terminal;

        this.keys = new boolean[16];
    }


    public void init() {
        terminal.setKeyCallbacks((window, qwertyKey, scancode, action, mods) -> {
            Integer chip8Key = qwertyToChip8Key.get(qwertyKey);

            if (chip8Key != null) {
                synchronized (this) {
                    if (action == GLFW_PRESS) {
                        keys[chip8Key] = true;
                        lastKey = chip8Key;
                        this.notify();
                    } else if (action == GLFW_RELEASE) {
                        keys[chip8Key] = false;
                    }
                }
            }
        });
    }


    public boolean isKeyPressed(int x) {
        return keys[x];
    }

    public int waitForKey() throws InterruptedException {
        synchronized (this) {
            this.wait();

            return lastKey;
        }
    }

}
