# CHIP8
 A side-project CHIP8 interpreter and disassembler for Java 11. Built using LWJGL 3.2.2 (managed via Maven).

## Usage
 This program is run on the command line, and has a GUI only for interacting with the game.
 
 To play a game, run with arguments `run <path-to-ROM> [options]`.  
 Some games handle drawing at display edges differently. By default, the interpreter wraps to the other side of the screen. If clipping of screen edges is required, use the `--clip-edges` option.
 
 To disassemble a ROM, run with arguments `disassemble <path-to-rom>`.

## Compatibility
 Compatibility is listed in the `COMPATIBILITY.txt` file, showing current status running various games from Zophar's Domain.