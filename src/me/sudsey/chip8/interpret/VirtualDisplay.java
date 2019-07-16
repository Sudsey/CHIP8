package me.sudsey.chip8.interpret;

public class VirtualDisplay {

    private Terminal terminal;

    private boolean[][] display;
    private boolean displayChanged;

    public VirtualDisplay(Terminal terminal) {
        this.terminal = terminal;

        clear();
    }


    public void init() {
        // Intentionally left blank
    }


    public void clear() {
        display = new boolean[64][32];
        displayChanged = true;
    }

    public boolean drawSprite(int xStart, int yStart, int[] sprite) {
        boolean collision = false;

        for (int i = 0; i < sprite.length; i++) {
            for (int j = 0; j < 8; j++) {
                int xPos = (xStart + j) % 64;
                int yPos = (yStart + i) % 32;

                if (display[xPos][yPos]) {
                    collision = true;
                }

                display[xPos][yPos] ^= ((sprite[i] >>> (7 - j)) & 1) == 1;
            }
        }

        displayChanged = true;

        return collision;
    }


    public void push() {
        if (displayChanged) {
            terminal.setDisplay(display);
            displayChanged = false;
        }
    }

}
