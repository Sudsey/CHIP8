package me.sudsey.chip8.commons;

public class Options {

    private boolean clipEdges;

    public Options(boolean clipEdges) {
        this.clipEdges = clipEdges;
    }


    public static Options parseOptions(String[] options) {
        boolean clipEdges = false;

        for (String option : options) {
            if (option.equals("--clip-edges")) {
                clipEdges = true;
            }
        }

        return new Options(clipEdges);
    }


    public boolean isClipEdges() {
        return clipEdges;
    }

}
