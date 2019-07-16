package me.sudsey.chip8;

import me.sudsey.chip8.disassemble.Disassembler;
import me.sudsey.chip8.interpret.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        byte[] romBytes = Files.readAllBytes(Paths.get(args[1]));
        int[] rom = new int[romBytes.length];
        for (int i = 0; i < romBytes.length; i++) {
            rom[i] = romBytes[i] & 0xFF;
        }

        switch (args[0]) {
            case "run":         Interpreter.run(rom);           break;
            case "disassemble": Disassembler.disassemble(rom);  break;
        }
    }

}
