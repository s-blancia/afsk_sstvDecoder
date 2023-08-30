/*
 Copyright 2015 Ahmet Inan <xdsopl@googlemail.com>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package xdsopl.robot36;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;

public class Decoder {
    private boolean drawImage = true, quitThread = false;
    private boolean enableAnalyzer = true;
    private final MainActivity activity;
    private final ImageView image;
    private final SpectrumView spectrum;
    private final SpectrumView spectrogram;
    private final VUMeterView meter;
    private final AudioRecord audio;
    private final int audioSource = MediaRecorder.AudioSource.MIC;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int[] sampleRates = { 44100, 48000, 22050, 16000, 11025, 8000 };
    private final int sampleRate;
    private final int maxHeight = freeRunReserve(616);
    private final int maxWidth = 800;
    private final short[] audioBuffer;
    private final int[] pixelBuffer;
    private final int[] spectrumBuffer;
    private final int[] spectrogramBuffer;
    private final int[] currentMode;
    private final int[] savedBuffer;
    private final int[] savedWidth;
    private final int[] savedHeight;
    private final float[] volume;
    private int updateRate = 1;

    private final RenderScript rs;
    private final Allocation rsDecoderAudioBuffer;
    private final Allocation rsDecoderPixelBuffer;
    private final Allocation rsDecoderSpectrumBuffer;
    private final Allocation rsDecoderSpectrogramBuffer;
    private final Allocation rsDecoderValueBuffer;
    private final Allocation rsDecoderCurrentMode;
    private final Allocation rsDecoderSavedBuffer;
    private final Allocation rsDecoderSavedWidth;
    private final Allocation rsDecoderSavedHeight;
    private final Allocation rsDecoderVolume;
    private final ScriptC_decoder rsDecoder;


    private final int mode_robot36 = 1;
    private final int mode_robot72 = 2;


    private final Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (quitThread)
                        return;
                    if (drawImage) {
                        image.drawCanvas();
                        if (enableAnalyzer) {
                            spectrum.drawCanvas();
                            spectrogram.drawCanvas();
                            meter.drawCanvas();
                        }
                    }
                }
                decode();
            }
        }
    };

    public Decoder(MainActivity activity, SpectrumView spectrum, SpectrumView spectrogram, ImageView image, VUMeterView meter) throws Exception {
        this.image = image;
        this.spectrogram = spectrogram;
        this.spectrum = spectrum;
        this.meter = meter;
        this.activity = activity;
        pixelBuffer = new int[maxWidth * maxHeight];
        spectrumBuffer = new int[spectrum.bitmap.getWidth() * spectrum.bitmap.getHeight()];
        spectrogramBuffer = new int[spectrogram.bitmap.getWidth() * spectrogram.bitmap.getHeight()];

        short[] tmpBuffer = null;
        AudioRecord tmpAudio = null;
        int tmpRate = -1;
        for (int testRate : sampleRates) {
            int bufferSizeInBytes = AudioRecord.getMinBufferSize(testRate, channelConfig, audioFormat);
            if (bufferSizeInBytes <= 0)
                continue;
            int bufferSizeInSamples = bufferSizeInBytes / 2;
            int framesPerSecond = Math.max(1, testRate / bufferSizeInSamples);
            tmpBuffer = new short[framesPerSecond * bufferSizeInSamples];
            try {
                tmpAudio = new AudioRecord(audioSource, testRate, channelConfig, audioFormat, tmpBuffer.length * 2);
                if (tmpAudio.getState() == AudioRecord.STATE_INITIALIZED) {
                    tmpRate = testRate;
                    break;
                }
                tmpAudio.release();
            } catch (IllegalArgumentException ignore) {
            }
            tmpAudio = null;
            tmpBuffer = null;
        }
        if (tmpAudio == null)
            throw new Exception("Unable to open audio.\nPlease send a bug report.");
        sampleRate = tmpRate;
        audioBuffer = tmpBuffer;
        audio = tmpAudio;
        audio.startRecording();
        if (audio.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            audio.stop();
            audio.release();
            throw new Exception("Unable to start recording.\nMaybe another app is recording?");
        }
        int minValueBufferLength = 2 * sampleRate;
        int valueBufferLength = Integer.highestOneBit(minValueBufferLength);
        if (minValueBufferLength > valueBufferLength)
            valueBufferLength <<= 1;

        currentMode = new int[1];
        savedWidth = new int[1];
        savedHeight = new int[1];
        volume = new float[1];
        savedBuffer = new int[pixelBuffer.length];

        rs = RenderScript.create(activity.getApplicationContext());
        rsDecoderAudioBuffer = Allocation.createSized(rs, Element.I16(rs), audioBuffer.length, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderValueBuffer = Allocation.createSized(rs, Element.U8(rs), valueBufferLength, Allocation.USAGE_SCRIPT);
        rsDecoderPixelBuffer = Allocation.createSized(rs, Element.I32(rs), pixelBuffer.length, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderSpectrumBuffer = Allocation.createSized(rs, Element.I32(rs), spectrumBuffer.length, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderSpectrogramBuffer = Allocation.createSized(rs, Element.I32(rs), spectrogramBuffer.length, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderCurrentMode = Allocation.createSized(rs, Element.I32(rs), 1, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderSavedWidth = Allocation.createSized(rs, Element.I32(rs), 1, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderSavedHeight = Allocation.createSized(rs, Element.I32(rs), 1, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderVolume = Allocation.createSized(rs, Element.F32(rs), 1, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoderSavedBuffer = Allocation.createSized(rs, Element.I32(rs), savedBuffer.length, Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        rsDecoder = new ScriptC_decoder(rs);
        rsDecoder.bind_audio_buffer(rsDecoderAudioBuffer);
        rsDecoder.bind_value_buffer(rsDecoderValueBuffer);
        rsDecoder.bind_pixel_buffer(rsDecoderPixelBuffer);
        rsDecoder.bind_spectrum_buffer(rsDecoderSpectrumBuffer);
        rsDecoder.bind_spectrogram_buffer(rsDecoderSpectrogramBuffer);
        rsDecoder.bind_current_mode(rsDecoderCurrentMode);
        rsDecoder.bind_saved_width(rsDecoderSavedWidth);
        rsDecoder.bind_saved_height(rsDecoderSavedHeight);
        rsDecoder.bind_volume(rsDecoderVolume);
        rsDecoder.bind_saved_buffer(rsDecoderSavedBuffer);
        rsDecoder.invoke_initialize(sampleRate, valueBufferLength, maxWidth, maxHeight,
                spectrum.bitmap.getWidth(), spectrum.bitmap.getHeight(),
                spectrogram.bitmap.getWidth(), spectrogram.bitmap.getHeight());

        thread.start();
    }

    void clear_image() { rsDecoder.invoke_reset_buffer(); }
    void toggle_scaling() { image.intScale ^= true; }
    void adjust_blur(int blur) { rsDecoder.invoke_adjust_blur(blur); }
    void toggle_debug() { rsDecoder.invoke_toggle_debug(); }
    void enable_analyzer(boolean enable) { rsDecoder.invoke_enable_analyzer((enableAnalyzer = enable) ? 1 : 0); }
    void auto_mode() { rsDecoder.invoke_auto_mode(1); }
    void raw_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_raw_mode(); }
    void robot36_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_robot36_mode(); }
    void robot72_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_robot72_mode(); }
    void martin1_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_martin1_mode(); }
    void martin2_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_martin2_mode(); }
    void scottie1_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_scottie1_mode(); }
    void scottie2_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_scottie2_mode(); }
    void scottieDX_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_scottieDX_mode(); }
    void wraaseSC2_180_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_wraaseSC2_180_mode(); }
    void pd50_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd50_mode(); }
    void pd90_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd90_mode(); }
    void pd120_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd120_mode(); }
    void pd160_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd160_mode(); }
    void pd180_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd180_mode(); }
    void pd240_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd240_mode(); }
    void pd290_mode() { rsDecoder.invoke_auto_mode(0); rsDecoder.invoke_pd290_mode(); }

    int freeRunReserve(int height) { return (height * 3) / 2; }
    void setUpdateRate(int rate) { updateRate = Math.max(0, Math.min(4, rate)); }
    void updateTitle(int id) { activity.updateTitle(activity.getString(id)); }

    void switch_mode(int mode)
    {
        switch (mode) {

            case mode_robot36:
                image.setImageResolution(320, freeRunReserve(240));
                updateTitle(R.string.action_robot36_mode);
                break;
            case mode_robot72:
                image.setImageResolution(320, freeRunReserve(240));
                updateTitle(R.string.action_robot72_mode);
                break;

            default:
                break;
        }
    }

    void pause() {
        synchronized (thread) {
            drawImage = false;
        }
    }

    void resume() {
        synchronized (thread) {
            drawImage = true;
        }
    }

    void destroy() {
        synchronized (thread) {
            quitThread = true;
        }
        try {
            thread.join();
        } catch (InterruptedException ignore) {
        }
        audio.stop();
        audio.release();
    }

    void decode() {
        int samples = audio.read(audioBuffer, 0, audioBuffer.length >> updateRate);
        if (samples <= 0)
            return;

        rsDecoderAudioBuffer.copyFrom(audioBuffer);
        rsDecoder.invoke_decode(samples);

        rsDecoderCurrentMode.copyTo(currentMode);
        switch_mode(currentMode[0]);

        rsDecoderPixelBuffer.copyTo(pixelBuffer);
        image.setPixels(pixelBuffer);

        rsDecoderVolume.copyTo(volume);
        meter.volume = volume[0];

        rsDecoderSavedHeight.copyTo(savedHeight);
        if (savedHeight[0] > 0) {
            rsDecoderSavedWidth.copyTo(savedWidth);
            rsDecoderSavedBuffer.copyTo(savedBuffer);
            activity.storeBitmap(Bitmap.createBitmap(savedBuffer, savedWidth[0], savedHeight[0], Bitmap.Config.ARGB_8888));
        }

        if (enableAnalyzer) {
            rsDecoderSpectrumBuffer.copyTo(spectrumBuffer);
            spectrum.bitmap.setPixels(spectrumBuffer, 0, spectrum.bitmap.getWidth(), 0, 0, spectrum.bitmap.getWidth(), spectrum.bitmap.getHeight());
            rsDecoderSpectrogramBuffer.copyTo(spectrogramBuffer);
            spectrogram.bitmap.setPixels(spectrogramBuffer, 0, spectrogram.bitmap.getWidth(), 0, 0, spectrogram.bitmap.getWidth(), spectrogram.bitmap.getHeight());
        }
    }
}