package me.sudsey.chip8.interpret;

import me.sudsey.chip8.commons.Options;

public class Interpreter {

    public static void run(int[] rom, Options options) {
        Terminal terminal = new Terminal();
        terminal.init();

        Machine machine = new Machine(terminal, options);
        machine.init();

        machine.start(rom);
        terminal.start(); // Main loop
        machine.stop();
    }

}
