package me.sudsey.chip8.interpret;

public class VirtualDisplay {

    private Terminal terminal;

    private boolean clipEdges;

    private boolean[][] display;
    private boolean displayChanged;

    public VirtualDisplay(Terminal terminal, boolean clipEdges) {
        this.terminal = terminal;
        this.clipEdges = clipEdges;

        clear();
    }


    public void clear() {
        display = new boolean[64][32];
        displayChanged = true;
    }

    public boolean drawSprite(int xStart, int yStart, int[] sprite) {
        boolean collision = false;

        for (int i = 0; i < sprite.length; i++) {
            for (int j = 0; j < 8; j++) {
                int xPos = xStart + j;
                int yPos = yStart + i;

                if (clipEdges && (xPos >= 64 || yPos >= 32)) {
                    continue;
                }

                boolean togglePixel = (sprite[i] & (1 << (7 - j))) != 0;

                if (display[xPos % 64][yPos % 32] && togglePixel) {
                    collision = true;
                }
                display[xPos % 64][yPos % 32] ^= togglePixel;
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
