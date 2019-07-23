package me.sudsey.chip8.interpret;

import me.sudsey.chip8.commons.Options;

public class Interpreter {

    public static void run(Options options, int[] rom) {
        Terminal terminal = new Terminal();
        Speaker speaker = new Speaker();
        terminal.init();
        speaker.init();

        Machine machine = new Machine(options, terminal, speaker);
        machine.init();

        machine.start(rom);
        terminal.start(); // Main loop
        machine.stop();

        speaker.destroy();
    }

}
