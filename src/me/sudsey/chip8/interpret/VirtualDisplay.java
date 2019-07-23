package me.sudsey.chip8.interpret;

public class VirtualDisplay {

    private Terminal terminal;

    private boolean[][] display;

    public VirtualDisplay(Terminal terminal) {
        this.terminal = terminal;

        clear();
    }


    public void clear() {
        display = new boolean[64][32];

        push();
    }

    public boolean drawSprite(int xStart, int yStart, int[] sprite, boolean clipEdges) {
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

        push();
        return collision;
    }


    private void push() {
        terminal.setDisplay(display);
    }

}
