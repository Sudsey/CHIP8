package me.sudsey.chip8.interpret;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public class Speaker {

    private long device;
    private long context;

    private int buffer;
    private int source;


    public void init() {
        String deviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        device = alcOpenDevice(deviceName);

        int[] attributes = {0};
        context = alcCreateContext(device, attributes);
        alcMakeContextCurrent(context);

        AL.createCapabilities(ALC.createCapabilities(device));

        ShortBuffer data = buildTone(440, 1, 44100);
        buffer = alGenBuffers();
        alBufferData(buffer, AL_FORMAT_MONO16, data, 44100);

        source = alGenSources();
        alSourcei(source, AL_BUFFER, buffer);
        alSourcef(source, AL_GAIN, 0.0f);
        alSourcei(source, AL_LOOPING, AL_TRUE);

        alSourcePlay(source);
    }

    public void startPlaying() {
        alSourcef(source, AL_GAIN, 0.2f);
    }

    public void stopPlaying() {
        alSourcef(source, AL_GAIN, 0.0f);
    }

    public void destroy() {
        alSourceStop(source);

        alcCloseDevice(device);
        alcDestroyContext(context);
        alDeleteSources(buffer);
        alDeleteBuffers(source);
    }


    private ShortBuffer buildTone(int frequency, int duration, int sampleRate) {
        int samples = duration * sampleRate;

        ShortBuffer tone = BufferUtils.createShortBuffer(samples);

        for (int i = 0; i < samples; i++) {
            //tone.put((short) (32767 * Math.sin(2 * Math.PI * frequency * i / sampleRate)));
            tone.put((short) (32767 * (2 / Math.PI) * Math.asin(Math.sin(2 * Math.PI * frequency * i / sampleRate))));
        }

        tone.flip();
        return tone;
    }

}
