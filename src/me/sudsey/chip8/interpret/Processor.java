package me.sudsey.chip8.interpret;

import me.sudsey.chip8.commons.Instruction;
import me.sudsey.chip8.commons.Opcode;
import me.sudsey.chip8.commons.Options;

import java.util.concurrent.*;

public class Processor {

    private ScheduledExecutorService scheduler;
    private Future clock;
    private Future timer;

    private Options options;

    private VirtualDisplay display;
    private VirtualKeyboard keyboard;
    private Speaker speaker;
    private Memory memory;

    private int[] regsVx;   // 16 8-bit general purpose registers
    private int regI;       // 16-bit memory register

    private int regDT;      // 8-bit delay timer
    private int regST;      // 8-bit sound timer

    private int pc;         // 16-bit program counter
    private int sp;         // 8-bit stack pointer

    private int[] stack;    // 16 16-bit values containing memory addresses

    public Processor(Options options, VirtualDisplay display, VirtualKeyboard keyboard, Speaker speaker,
                     Memory memory) {
        this.scheduler = Executors.newScheduledThreadPool(0);

        this.options = options;

        this.display = display;
        this.keyboard = keyboard;
        this.speaker = speaker;
        this.memory = memory;
    }


    public void start() {
        this.regsVx = new int[16];
        this.regI = 0;

        this.regDT = 0;
        this.regST = 0;

        this.pc = 0x200;
        this.sp = 0;

        this.stack = new int[16];

        // Ideally we would use scheduleAtFixedRate here, to make instruction blocks regular. However, that method
        // queues iterations when one is blocked, and so executes blocks in bursts after LD_Vx_K calls. With
        // scheduleWithFixedDelay, iterations happen e.g. 20ms *after* the previous finishes, so we only run the risk of
        // slowdown if blocks take too long (at the time of writing, they take ~0.01ms to execute, so this is a
        // non-issue).

        clock = scheduler.scheduleWithFixedDelay(() -> {
            try {
                processInstructionBlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }, 0, 20, TimeUnit.MILLISECONDS);

        timer = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (regDT > 0) {
                    regDT--;
                }
                if (regST > 0) {
                    regST--;

                    if (regST == 0) {
                        speaker.stopPlaying();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }, 0, 16667, TimeUnit.MICROSECONDS);
    }

    public void stop() {
        clock.cancel(true);
        timer.cancel(true);
    }


    private void processInstructionBlock() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            int instructionBytes = (memory.getLocation(pc) << 8) | memory.getLocation(pc + 1);
            Instruction instruction = Instruction.parseInstructionBytes(instructionBytes);

            if (instruction != null) {
                if (instruction.getOpcode() == Opcode.LD_Vx_K) {
                    display.push();
                }
                processInstruction(instruction);
            }

            pc = (pc + 2) & 0xFFFF;
        }

        display.push();
    }

    private void processInstruction(Instruction instruction) throws InterruptedException {
        int nnn = instruction.getNnn();
        int n = instruction.getN();
        int x = instruction.getX();
        int y = instruction.getY();
        int kk = instruction.getKk();

        switch (instruction.getOpcode()) {
            case CLS:           CLS();              break;
            case RET:           RET();              break;
            case JP:            JP(nnn);            break;
            case CALL:          CALL(nnn);          break;
            case SE_Vx_byte:    SE_Vx_byte(x, kk);  break;
            case SNE_Vx_byte:   SNE_Vx_byte(x, kk); break;
            case SE_Vx_Vy:      SE_Vx_Vy(x, y);     break;
            case LD_Vx_byte:    LD_Vx_byte(x, kk);  break;
            case ADD_Vx_byte:   ADD_Vx_byte(x, kk); break;
            case LD_Vx_Vy:      LD_Vx_Vy(x, y);     break;
            case OR:            OR(x, y);           break;
            case AND:           AND(x, y);          break;
            case XOR:           XOR(x, y);          break;
            case ADD_Vx_Vy:     ADD_Vx_Vy(x, y);    break;
            case SUB:           SUB(x, y);          break;
            case SHR:           SHR(x, y);          break;
            case SUBN:          SUBN(x, y);         break;
            case SHL:           SHL(x, y);          break;
            case SNE_Vx_Vy:     SNE_Vx_Vy(x, y);    break;
            case LD_I_addr:     LD_I_addr(nnn);     break;
            case JP_V0_addr:    JP_V0_addr(nnn);    break;
            case RND:           RND(x, kk);         break;
            case DRW:           DRW(x, y, n);       break;
            case SKP:           SKP(x);             break;
            case SKNP:          SKNP(x);            break;
            case LD_Vx_DT:      LD_Vx_DT(x);        break;
            case LD_Vx_K:       LD_Vx_K(x);         break;
            case LD_DT_Vx:      LD_DT_Vx(x);        break;
            case LD_ST_Vx:      LD_ST_Vx(x);        break;
            case ADD_I_Vx:      ADD_I_Vx(x);        break;
            case LD_F_Vx:       LD_F_Vx(x);         break;
            case LD_B_Vx:       LD_B_Vx(x);         break;
            case LD_I_Vx:       LD_I_Vx(x);         break;
            case LD_Vx_I:       LD_Vx_I(x);
        }
    }


    // Opcode information pulled from http://devernay.free.fr/hacks/chip8/C8TECH10.HTM

    /*
     * 00E0 - CLS
     * Clear the display.
     */
    private void CLS() {
        display.clear();
    }

    /*
     * 00EE - RET
     * Return from a subroutine.
     *
     * The interpreter sets the program counter to the address at the top of the stack, then subtracts 1 from the stack
     * pointer.
     */
    private void RET() {
        pc = stack[sp];
        sp = (sp - 1) & 0xFF;
    }

    /*
     * 1nnn - JP addr
     * Jump to location nnn.
     *
     * The interpreter sets the program counter to nnn.
     */
    private void JP(int nnn) {
        pc = (nnn - 2) & 0xFFFF;
    }

    /*
     * 2nnn - CALL addr
     * Call subroutine at nnn.
     *
     * The interpreter increments the stack pointer, then puts the current PC on the top of the stack. The PC is then
     * set to nnn.
     */
    private void CALL(int nnn) {
        sp = (sp + 1) & 0xFF;
        stack[sp] = pc;
        pc = (nnn - 2) & 0xFFFF;
    }

    /*
     * 3xkk - SE Vx, byte
     * Skip next instruction if Vx = kk.
     *
     * The interpreter compares register Vx to kk, and if they are equal, increments the program counter by 2.
     */
    private void SE_Vx_byte(int x, int kk) {
        if (regsVx[x] == kk) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * 4xkk - SNE Vx, byte
     * Skip next instruction if Vx != kk.
     *
     * The interpreter compares register Vx to kk, and if they are not equal, increments the program counter by 2.
     */
    private void SNE_Vx_byte(int x, int kk) {
        if (regsVx[x] != kk) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * 5xy0 - SE Vx, Vy
     * Skip next instruction if Vx = Vy.
     *
     * The interpreter compares register Vx to register Vy, and if they are equal, increments the program counter by 2.
     */
    private void SE_Vx_Vy(int x, int y) {
        if (regsVx[x] == regsVx[y]) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * 6xkk - LD Vx, byte
     * Set Vx = kk.
     *
     * The interpreter puts the value kk into register Vx.
     */
    private void LD_Vx_byte(int x, int kk) {
        regsVx[x] = kk;
    }

    /*
     * 7xkk - ADD Vx, byte
     * Set Vx = Vx + kk.
     *
     * Adds the value kk to the value of register Vx, then stores the result in Vx.
     */
    private void ADD_Vx_byte(int x, int kk) {
        regsVx[x] = (regsVx[x] + kk) & 0xFF;
    }

    /*
     * 8xy0 - LD Vx, Vy
     * Set Vx = Vy.
     *
     * Stores the value of register Vy in register Vx.
     */
    private void LD_Vx_Vy(int x, int y) {
        regsVx[x] = regsVx[y];
    }

    /*
     * 8xy1 - OR Vx, Vy
     * Set Vx = Vx OR Vy.
     *
     * Performs a bitwise OR on the values of Vx and Vy, then stores the result in Vx. A bitwise OR compares the
     * corresponding bits from two values, and if either bit is 1, then the same bit in the result is also 1. Otherwise,
     * it is 0.
     */
    private void OR(int x, int y) {
        regsVx[x] = regsVx[x] | regsVx[y];
    }

    /*
     * 8xy2 - AND Vx, Vy
     * Set Vx = Vx AND Vy.
     *
     * Performs a bitwise AND on the values of Vx and Vy, then stores the result in Vx. A bitwise AND compares the
     * corresponding bits from two values, and if both bits are 1, then the same bit in the result is also 1. Otherwise,
     * it is 0.
     */
    private void AND(int x, int y) {
        regsVx[x] = regsVx[x] & regsVx[y];
    }

    /*
     * 8xy3 - XOR Vx, Vy
     * Set Vx = Vx XOR Vy.
     *
     * Performs a bitwise exclusive OR on the values of Vx and Vy, then stores the result in Vx. An exclusive OR
     * compares the corresponding bits from two values, and if the bits are not both the same, then the corresponding
     * bit in the result is set to 1. Otherwise, it is 0.
     */
    private void XOR(int x, int y) {
        regsVx[x] = regsVx[x] ^ regsVx[y];
    }

    /*
     * 8xy4 - ADD Vx, Vy
     * Set Vx = Vx + Vy, set VF = carry.
     *
     * The values of Vx and Vy are added together. If the result is greater than 8 bits (i.e., > 255,) VF is set to 1,
     * otherwise 0. Only the lowest 8 bits of the result are kept, and stored in Vx.
     */
    private void ADD_Vx_Vy(int x, int y) {
        int result = regsVx[x] + regsVx[y];

        if (result > 255) {
            regsVx[0xF] = 1;
        } else {
            regsVx[0xF] = 0;
        }
        regsVx[x] = result & 0xFF;
    }

    /*
     * 8xy5 - SUB Vx, Vy
     * Set Vx = Vx - Vy, set VF = NOT borrow.
     *
     * If Vx > Vy, then VF is set to 1, otherwise 0. Then Vy is subtracted from Vx, and the results stored in Vx.
     */
    private void SUB(int x, int y) {
        int Vx = regsVx[x];
        int Vy = regsVx[y];

        if (Vy > Vx) {
            regsVx[0xF] = 0;
        } else {
            regsVx[0xF] = 1;
        }
        regsVx[x] = (Vx - Vy) & 0xFF;
    }

    /*
     * 8xy6 - SHR Vx {, Vy}
     * Set Vx = Vx SHR 1.
     *
     * If the least-significant bit of Vx is 1, then VF is set to 1, otherwise 0. Then Vx is divided by 2.
     *
     * IMPL. VARIANCE: Register behaviour differs between:
     *  - Vx = Vx >> 1, VF = LSB of Vx prior to shift.
     *  - Vx = Vy = Vy >> 1, VF = LSB of Vy prior to shift.
     *  - Vx = Vy >> 1, VF = LSB of Vy prior to shift.
     */
    private void SHR(int x, int y) {
        if ((regsVx[x] & 1) == 1) {
            regsVx[0xF] = 1;
        } else {
            regsVx[0xF] = 0;
        }
        regsVx[x] = regsVx[x] >>> 1;
        //regsVx[x] = regsVx[y] >>> 1;
        //regsVx[y] = regsVx[x];
    }

    /*
     * 8xy7 - SUBN Vx, Vy
     * Set Vx = Vy - Vx, set VF = NOT borrow.
     *
     * If Vy > Vx, then VF is set to 1, otherwise 0. Then Vx is subtracted from Vy, and the results stored in Vx.
     */
    private void SUBN(int x, int y) {
        int Vx = regsVx[x];
        int Vy = regsVx[y];

        if (Vx > Vy) {
            regsVx[0xF] = 0;
        } else {
            regsVx[0xF] = 1;
        }
        regsVx[x] = (Vy - Vx) & 0xFF;
    }

    /*
     * 8xyE - SHL Vx {, Vy}
     * Set Vx = Vx SHL 1.
     *
     * If the most-significant bit of Vx is 1, then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
     *
     * IMPL. VARIANCE: See SHR.
     */
    private void SHL(int x, int y) {
        if (((regsVx[x] >> 7) & 1) == 1) {
            regsVx[0xF] = 1;
        } else {
            regsVx[0xF] = 0;
        }
        regsVx[x] = (regsVx[x] << 1) & 0xFF;
        //regsVx[x] = (regsVx[y] << 1) & 0xFF;
        //regsVx[y] = regsVx[x];
    }

    /*
     * 9xy0 - SNE Vx, Vy
     * Skip next instruction if Vx != Vy.
     *
     * The values of Vx and Vy are compared, and if they are not equal, the program counter is increased by 2.
     */
    private void SNE_Vx_Vy(int x, int y) {
        if (regsVx[x] != regsVx[y]) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * Annn - LD I, addr
     * Set I = nnn.
     *
     * The value of register I is set to nnn.
     */
    private void LD_I_addr(int nnn) {
        regI = nnn;
    }

    /*
     * Bnnn - JP V0, addr
     * Jump to location nnn + V0.
     *
     * The program counter is set to nnn plus the value of V0.
     */
    private void JP_V0_addr(int nnn) {
        pc = (nnn + regsVx[0] - 2) & 0xFFFF;
    }

    /*
     * Cxkk - RND Vx, byte
     * Set Vx = random byte AND kk.
     *
     * The interpreter generates a random number from 0 to 255, which is then ANDed with the value kk. The results are
     * stored in Vx. See instruction 8xy2 for more information on AND.
     */
    private void RND(int x, int kk) {
        byte[] rand = new byte[1];
        ThreadLocalRandom.current().nextBytes(rand);

        regsVx[x] = rand[0] & kk;
    }

    /*
     * Dxyn - DRW Vx, Vy, nibble
     * Terminal n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
     *
     * The interpreter reads n bytes from memory, starting at the address stored in I. These bytes are then displayed as
     * sprites on screen at coordinates (Vx, Vy). Sprites are XORed onto the existing screen. If this causes any pixels
     * to be erased, VF is set to 1, otherwise it is set to 0. If the sprite is positioned so part of it is outside the
     * coordinates of the display, it wraps around to the opposite side of the screen. See instruction 8xy3 for more
     * information on XOR, and section 2.4, Terminal, for more information on the Chip-8 screen and sprites.
     *
     * IMPL. VARIANCE: Some games assume no wrapping occurs. This can be enforced with the `--clip-edges` launch option.
     */
    private void DRW(int x, int y, int n) {
        boolean collision = display.drawSprite(regsVx[x], regsVx[y], memory.getRange(regI, regI + n),
                options.isClipEdges());

        regsVx[0xF] = collision ? 1 : 0;
    }

    /*
     * Ex9E - SKP Vx
     * Skip next instruction if key with the value of Vx is pressed.
     *
     * Checks the keyboard, and if the key corresponding to the value of Vx is currently in the down position, PC is
     * increased by 2.
     */
    private void SKP(int x) {
        if (keyboard.isKeyPressed(regsVx[x])) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * ExA1 - SKNP Vx
     * Skip next instruction if key with the value of Vx is not pressed.
     *
     * Checks the keyboard, and if the key corresponding to the value of Vx is currently in the up position, PC is
     * increased by 2.
     */
    private void SKNP(int x) {
        if (!keyboard.isKeyPressed(regsVx[x])) {
            pc = (pc + 2) & 0xFFFF;
        }
    }

    /*
     * Fx07 - LD Vx, DT
     * Set Vx = delay timer value.
     *
     * The value of DT is placed into Vx.
     */
    private void LD_Vx_DT(int x) {
        regsVx[x] = regDT;
    }

    /*
     * Fx0A - LD Vx, K
     * Wait for a key press, store the value of the key in Vx.
     *
     * All execution stops until a key is pressed, then the value of that key is stored in Vx.
     */
    private void LD_Vx_K(int x) throws InterruptedException {
        regsVx[x] = keyboard.waitForKey();
    }

    /*
     * Fx15 - LD DT, Vx
     * Set delay timer = Vx.
     *
     * DT is set equal to the value of Vx.
     */
    private void LD_DT_Vx(int x) {
        regDT = regsVx[x];
    }

    /*
     * Fx18 - LD ST, Vx
     * Set sound timer = Vx.
     *
     * ST is set equal to the value of Vx.
     */
    private void LD_ST_Vx(int x) {
        regST = regsVx[x];

        if (regST > 0) {
            speaker.startPlaying();
        }
    }

    /*
     * Fx1E - ADD I, Vx
     * Set I = I + Vx.
     *
     * The values of I and Vx are added, and the results are stored in I.
     */
    private void ADD_I_Vx(int x) {
        regI = (regI + regsVx[x]) & 0xFFFF;
    }

    /*
     * Fx29 - LD F, Vx
     * Set I = location of sprite for digit Vx.
     *
     * The value of I is set to the location for the hexadecimal sprite corresponding to the value of Vx. See section
     * 2.4, Display, for more information on the Chip-8 hexadecimal font.
     */
    private void LD_F_Vx(int x) {
        regI = (0x5 * regsVx[x]) & 0xFFFF;
    }

    /*
     * Fx33 - LD B, Vx
     * Store BCD representation of Vx in memory locations I, I+1, and I+2.
     *
     * The interpreter takes the decimal value of Vx, and places the hundreds digit in memory at location in I, the tens
     * digit at location I+1, and the ones digit at location I+2.
     */
    private void LD_B_Vx(int x) {
        memory.setLocation(regI, (regsVx[x] / 100) % 10);
        memory.setLocation(regI + 1, (regsVx[x] / 10) % 10);
        memory.setLocation(regI + 2, (regsVx[x] % 10));
    }

    /*
     * Fx55 - LD [I], Vx
     * Store registers V0 through Vx in memory starting at location I.
     *
     * The interpreter copies the values of registers V0 through Vx into memory, starting at the address in I.
     *
     * IMPL. VARIANCE: Sometimes also sets I = I + x + 1.
     */
    private void LD_I_Vx(int x) {
        for (int i = 0; i <= x; i++) {
            memory.setLocation((regI + i) & 0xFFFF, regsVx[i]);
        }

        //regI = (regI + x + 1) & 0xFFFF;
    }

    /*
     * Fx65 - LD Vx, [I]
     * Read registers V0 through Vx from memory starting at location I.
     *
     * The interpreter reads values from memory starting at location I into registers V0 through Vx.
     *
     * IMPL. VARIANCE: Sometimes also sets I = I + x + 1.
     */
    private void LD_Vx_I(int x) {
        for (int i = 0; i <= x; i++) {
            regsVx[i] = memory.getLocation((regI + i) & 0xFFFF);
        }

        //regI = (regI + x + 1) & 0xFFFF;
    }

}
