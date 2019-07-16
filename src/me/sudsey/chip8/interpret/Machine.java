package me.sudsey.chip8.interpret;

public class Machine {

    private VirtualDisplay display;
    private VirtualKeyboard keyboard;
    private Memory memory;

    private Processor processor;

    public Machine(Terminal terminal) {
        this.display = new VirtualDisplay(terminal);
        this.keyboard = new VirtualKeyboard(terminal);
        this.memory = new Memory();

        this.processor = new Processor(this.display, this.keyboard, this.memory);
    }


    public void init() {
        display.init();
        keyboard.init();
    }


    public void start(int[] rom) {
        display.clear();
        display.push();
        memory.loadROM(rom);

        processor.start();
    }

    public void stop() {
        processor.stop();
    }

}
