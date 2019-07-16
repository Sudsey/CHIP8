package me.sudsey.chip8.interpret;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

// Most of the graphics code in this class is poor and out of date. I'm still trying to work my head around how OpenGL
// works.
// In particular, I know the way I draw the quad (with glBegin) is deprecated.
// Using synchronised() { } is also really slow I think

public class Terminal {

    private long window;

    private final boolean[][] displayBuffer;
    private boolean displayChanged;

    public Terminal() {
        this.displayBuffer = new boolean[64][32];
        this.displayChanged = true;
    }


    public void init() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(640, 320, "CHIP8Interp", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowSizeCallback(window, (window, width, height) ->
            glViewport(0, 0, width, height)
        );

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, glGenTextures());
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 32, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                getBufferFromDisplay(displayBuffer));
    }

    public void start() {
        glfwShowWindow(window);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            if (displayChanged) {
                synchronized (displayBuffer) {
                    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 64, 32, GL_RGBA, GL_UNSIGNED_BYTE,
                            getBufferFromDisplay(displayBuffer));
                    displayChanged = false;
                }
            }

            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(-1, 1);
            glTexCoord2f(1, 0);
            glVertex2f(1, 1);
            glTexCoord2f(1, 1);
            glVertex2f(1, -1);
            glTexCoord2f(0, 1);
            glVertex2f(-1, -1);
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }


    public void setKeyCallbacks(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(window, callback);
    }

    public void setDisplay(boolean[][] display) {
        synchronized (displayBuffer) {
            for (int i = 0; i < display.length; i++) {
                System.arraycopy(display[i], 0, displayBuffer[i], 0, display[i].length);
            }
            displayChanged = true;
        }
    }


    private ByteBuffer getBufferFromDisplay(boolean[][] display) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(64 * 32 * 4);

        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (display[x][y]) {
                    buffer.put((byte) 31);
                    buffer.put((byte) 36);
                    buffer.put((byte) 10);
                } else {
                    buffer.put((byte) 143);
                    buffer.put((byte) 158);
                    buffer.put((byte) 102);
                }
                buffer.put((byte) 0);
            }
        }

        buffer.flip();

        return buffer;
    }

}
