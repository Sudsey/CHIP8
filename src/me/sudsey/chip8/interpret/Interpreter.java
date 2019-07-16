package me.sudsey.chip8.interpret;

public class Interpreter {

    public static void run(int[] rom) {
        Terminal terminal = new Terminal();
        terminal.init();

        Machine machine = new Machine(terminal);
        machine.init();

        machine.start(rom);
        terminal.start(); // Main loop
        machine.stop();
    }

}
