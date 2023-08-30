
/*
Copyright 2014 Ahmet Inan <xdsopl@googlemail.com>

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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;

import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private Decoder decoder;
    private ImageView image;
    private Bitmap bitmap;
    private NotificationManager manager;
    private ShareActionProvider share;
    private final int notifyID = 1;
    private final int permissionsID = 2;
    private static final String channelID = "Robot36";
    private boolean enableAnalyzer = true;
    private Menu menu;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int SAMPLE_RATE = 44100;
    private static final double THRESHOLD = 0.1;
    private static final int DURATION_THRESHOLD = 50;
    private static final int MIN_BIT_COUNT = 8;

    private boolean isAFSKDecodingActive = true; // Add this flag

    double specialFrequencySSTVactivator = 4500.0; // The special frequency for activating SSTV encoder
    double specialFrequencySSTVdeactivator = 4700.0; // The special frequency for Deactivating SSTV encoder

    double specialFrequencyAFSKactivator = 4800.0; // The special frequency for activating AFSK encoder
    double specialFrequencyAFSKdeactivator = 5000.0; // The special frequency for deactivating AFSK encoder

    double frequencyTolerance = 50.0; // Tolerance for frequency matching

    private boolean isAFSKDecoding = true; // Initialize as true to start decoding


    private TextView decodedTextView;
    private ViewFlipper viewFlipper;
    private GestureDetector gestureDetector;
    private Handler handler;
    private boolean isShowingDecodingProgress = false;
    // Declare a variable to keep track of the active decoder
    private Decoder activeDecoder = null;
    private AudioRecord audioRecord = null; // Declare a single AudioRecord instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // changeLayoutOrientation(getResources().getConfiguration());

        image = findViewById(R.id.image);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        decodedTextView = findViewById(R.id.text_decoded);
        handler = new Handler(Looper.getMainLooper());

        viewFlipper = findViewById(R.id.viewFlipper);
        gestureDetector = new GestureDetector(this, this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }

        // Start decoding with specific mark and space frequencies
        double markFrequency = 1200.0; // Frequency of the "1" bit in Hz
        double spaceFrequency = 2200.0; // Frequency of the "0" bit in Hz
        double specialFrequency = 4500.0;


        startAFSKdecoder(markFrequency, spaceFrequency, specialFrequency);


        //startSSTVDecoder();

    }


    // THIS METHOD IS SSTV ENCODER

    protected void startSSTVDecoder() {

                try {
                    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        // Request the missing permission here or handle the case where the permission is not granted
                        return;
                    }
                    audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                    try {
                        audioRecord.startRecording();
                        Log.d("SSTV_Decoder", "Audio recording started successfully");
                        Log.d("SSTV_Decoder", "Detected Activation Sequence");

                        decoder = new Decoder(this,
                                findViewById(R.id.spectrum),
                                findViewById(R.id.spectrogram),
                                findViewById(R.id.image),
                                findViewById(R.id.meter)
                        );
                        activeDecoder.enable_analyzer(enableAnalyzer);
                        showNotification();
                        updateMenuButtons();

                    } catch (IllegalStateException e) {
                        Log.e("SSTV_Decoder", "Failed to start audio recording: " + e.getMessage());
                        return;
                    }

//                    Log.d("SSTV_Decoder", "Detected Activation Sequence");
//
//                    decoder = new Decoder(this,
//                            findViewById(R.id.spectrum),
//                            findViewById(R.id.spectrogram),
//                            findViewById(R.id.image),
//                            findViewById(R.id.meter)
//                    );
//                    activeDecoder.enable_analyzer(enableAnalyzer);
//                    showNotification();
//                    updateMenuButtons();

                } catch (Exception e) {
                    showErrorMessage(getString(R.string.decoder_error), e.getMessage());
                }
            }



// This Method is for AFSK decoder
    private void startAFSKdecoder(double markFrequency, double spaceFrequency, double specialFrequency) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                if (!isAFSKDecoding) {
                    stopAFSKDecoding();
                } else {
                   //  Create an AudioRecord instance to capture audio
                    int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        // Request the missing permission here or handle the case where the permission is not granted
                        return;
                    }
                    AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);


                    // Check if the audioRecord instance is null or not recording
                    if (audioRecord == null || audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                        // Create an AudioRecord instance to capture audio
                         bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            // Request the missing permission here or handle the case where the permission is not granted
                            return;
                        }
                        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                        // Start audio capture
                        try {
                            audioRecord.startRecording();
                        } catch (IllegalStateException e) {
                            // Handle startRecording() exception
                            Log.e("Decoding", "Failed to start audio recording: " + e.getMessage());
                            return;
                        }

                        // Decoding variables
                        StringBuilder binaryText = new StringBuilder();
                        boolean previousBit = false;
                        long startTime = System.currentTimeMillis();

                        while (isAFSKDecodingActive) {
                            // Read audio samples into the buffer
                            short[] buffer = new short[bufferSize];
                            int bytesRead = audioRecord.read(buffer, 0, bufferSize);
                            if (bytesRead <= 0) {
                                continue;
                            }

                            // Calculate the instantaneous frequency
                            double frequency = calculateInstantaneousFrequency(buffer, SAMPLE_RATE);

                            // Check if the calculated frequency matches the special frequency
                            if (Math.abs(frequency - specialFrequencySSTVactivator) <= frequencyTolerance) {
                                // Detected the special frequency, trigger SSTV encoder or desired action
                                Log.d("AFSK_Decoder", "Detected Special Frequency - Switching to SSTV");

                                isAFSKDecodingActive = false; // Set the flag to stop AFSK decoding

                                // Stop and release the AFSK audio recording
                                if (audioRecord != null) {
                                    try {
                                        audioRecord.stop();
                                        audioRecord.release();
                                        audioRecord = null;
                                    } catch (IllegalStateException e) {
                                        Log.e("AFSK_Decoder", "Failed to stop audio recording: " + e.getMessage());
                                    }
                                }

                                // Perform other resource cleanup related to AFSK decoding here
                                // For example, close any streams, release buffers, etc.

                                decoder = null; // Release the AFSK decoder instance

                                // Start the SSTV decoder
                                startSSTVDecoder();
                                break; // Exit the loop since we've started SSTV decoding
                            }
                            else {

                                // Perform decoding logic based on the received audio samples
                                for (int i = 0; i < bytesRead; i++) {
                                    // Calculate the sample amplitude in the range [-1, 1]
                                    double amplitude = (double) buffer[i] / Short.MAX_VALUE;

                                    // Apply basic noise suppression by setting small amplitudes to 0
                                    if (Math.abs(amplitude) < THRESHOLD) {
                                        amplitude = 0;
                                    }

                                    // Detect bit transition (rising or falling edge)
                                    boolean currentBit = amplitude > 0;

                                    // Detect bit transition (rising or falling edge)
                                    if (currentBit != previousBit) {
                                        if (currentBit) {
                                            // Rising edge (mark bit)
                                            binaryText.append("1");
                                            Log.d("AFSK_Decoder", "Detected Rising Edge (Mark Bit).");
                                        } else {
                                            // Falling edge (space bit)
                                            binaryText.append("0");
                                            Log.d("AFSK_Decoder", "Detected Falling Edge (Space Bit).");

                                        }
                                    }

                                    previousBit = currentBit;
                                }

                                // Check if a full character has been received
                                if (binaryText.length() >= MIN_BIT_COUNT && System.currentTimeMillis() - startTime > DURATION_THRESHOLD) {
                                    // Convert binary text to characters and append to the decoded text
                                    String decodedText = getDecodedText(binaryText.toString(), markFrequency, spaceFrequency, buffer, SAMPLE_RATE);


                                    // Display the decoded text in the UI or perform further processing
                                    appendToDecodedText(decodedText);

                                    // Reset variables for the next character
                                    binaryText.setLength(0);
                                    startTime = System.currentTimeMillis();
                                }
                            }
                        }
                    }
                }
            }
        });

        thread.start();
    }

    private void stopAFSKDecoding() {
        isAFSKDecoding = false;

        // Stop and release the AudioRecord instance if it is recording
        if (audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        startSSTVDecoder();
    }


    protected void stopDecoder() {
        if (decoder == null)
            return;
        decoder.destroy();
        decoder = null;
        manager.cancel(notifyID);
        updateMenuButtons();
    }

    private void appendToDecodedText(String decodedText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String previousDecodedText = decodedTextView.getText().toString();
                StringBuilder decodedTextBuilder = new StringBuilder(previousDecodedText);

                // Check if the decoded text is a continuation of the previous decoded text
                if (!previousDecodedText.endsWith(decodedText)) {
                    decodedTextBuilder.append(decodedText);
                }

                decodedTextView.setText(decodedTextBuilder.toString());
            }
        });
    }

    private String getDecodedText(String binaryText, double markFrequency, double spaceFrequency, short[] samples, int sampleRate) {
        StringBuilder decodedText = new StringBuilder();
        int textLength = binaryText.length();
        int startIndex = 0;

        // Process the binary text in chunks of MIN_BIT_COUNT
        while (startIndex + MIN_BIT_COUNT <= textLength) {
            String chunk = binaryText.substring(startIndex, startIndex + MIN_BIT_COUNT);
            char decodedCharacter = getCharacterForBinaryText(chunk, markFrequency, spaceFrequency, samples, sampleRate);

            // Check if the decoded character is the same as the last character in the decoded text
            if (decodedText.length() >= 1 && decodedCharacter == decodedText.charAt(decodedText.length() - 1)) {
                // Skip adding the repeated character
                startIndex += MIN_BIT_COUNT;
                continue;
            }

            decodedText.append(decodedCharacter);
            startIndex += MIN_BIT_COUNT;
        }

        return decodedText.toString();
    }

    private double calculateInstantaneousFrequency(short[] samples, int sampleRate) {
        int bufferSize = samples.length;
        double[] magnitude = new double[bufferSize / 2];
        double maxMagnitude = Double.MIN_VALUE;
        int maxIndex = -1;

        // Apply window function to the samples (e.g., Hamming window)

        // Perform the FFT
        DoubleFFT_1D transformer = new DoubleFFT_1D(bufferSize);
        double[] transformedSamples = new double[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            transformedSamples[i] = samples[i];
        }
        transformer.realForward(transformedSamples);

        // Calculate the magnitudes of the FFT bins
        for (int i = 0; i < bufferSize / 2; i++) {
            double real = transformedSamples[2 * i];
            double imaginary = transformedSamples[2 * i + 1];
            magnitude[i] = Math.sqrt(real * real + imaginary * imaginary);

            // Track the bin with the maximum magnitude
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i];
                maxIndex = i;
            }
        }

        // Calculate the frequency corresponding to the bin with the maximum magnitude
        double frequency = maxIndex * (double) sampleRate / bufferSize;

        return frequency;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Swipe right to show decoding progress
        if (e1.getX() - e2.getX() > 50 && !isShowingDecodingProgress) {
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.showNext();
            isShowingDecodingProgress = true;
            decodedTextView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            return true;
        }
        // Swipe left to hide decoding progress
        if (e2.getX() - e1.getX() > 50 && isShowingDecodingProgress) {
            viewFlipper.setInAnimation(this, R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
            viewFlipper.showPrevious();
            isShowingDecodingProgress = false;
            decodedTextView.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);

            return true;
        }
        return false;
    }





    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flags = PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(channelID);
            if (channel == null) {
                channel = new NotificationChannel(channelID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(getString(R.string.decoder_running));
                manager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.decoder_running))
                .setContentIntent(pending)
                .setOngoing(true);
        manager.notify(notifyID, builder.build());
    }

    void updateTitle(final String newTitle)
    {
        if (getTitle() != newTitle) {
            runOnUiThread(() -> setTitle(newTitle));
        }
    }

    void storeBitmap(Bitmap image) {
        bitmap = image;
        runOnUiThread(() -> {
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
            @SuppressLint("SimpleDateFormat") String title = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            ContentValues values = new ContentValues();
            File dir;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!dir.exists())
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                File file;
                try {
                    file = File.createTempFile(name + "_", ".png", dir);
                    name = file.getName();
                } catch (IOException ignore) {
                    return;
                }
                FileOutputStream stream;
                try {
                    stream = new FileOutputStream(file);
                } catch (IOException ignore) {
                    return;
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                try {
                    stream.close();
                } catch (IOException ignore) {
                    return;
                }
                values.put(MediaStore.Images.ImageColumns.DATA, file.toString());
            } else {
                name += ".png";
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }
            values.put(MediaStore.Images.ImageColumns.TITLE, title);
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileOutputStream stream;
                try {
                    ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(uri,"w");
                    stream = new FileOutputStream(descriptor.getFileDescriptor());
                    descriptor.close();
                } catch (IOException ignore) {
                    return;
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                try {
                    stream.close();
                } catch (IOException ignore) {
                    return;
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setType("image/png");
            share.setShareIntent(intent);
            Toast toast = Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        });
    }


    private void updateMenuButtons() {
        if (menu != null) {
            if (decoder != null) {
                menu.findItem(R.id.action_toggle_decoder).setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause));
                menu.setGroupEnabled(R.id.group_decoder, true);
            } else {
                menu.findItem(R.id.action_toggle_decoder).setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_media_play));
                menu.setGroupEnabled(R.id.group_decoder, false);
            }
        }
    }


    private void showErrorMessage(final String title, final String shortText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(shortText);
        builder.setNeutralButton(getString(R.string.btn_ok), null);
        builder.show();
    }

    private void showPrivacyPolicy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.privacy_policy));
        builder.setMessage(getString(R.string.privacy_policy_text));
        builder.setNeutralButton(getString(R.string.btn_ok), null);
        builder.show();
    }



    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO
        };

        List<String> missingPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != permissionsID)
            return;
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED)
                return;
        startSSTVDecoder();

        permissions = new String[]{
                Manifest.permission.RECORD_AUDIO
        };

        List<String> missingPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start decoding

                // Start decoding with specific mark and space frequencies
                double markFrequency = 1200.0; // Frequency of the "1" bit in Hz
                double spaceFrequency = 2200.0; // Frequency of the "0" bit in Hz
                double specialFrequency = 4500.0;
                startAFSKdecoder(markFrequency, spaceFrequency, specialFrequency);
            } else {
                Toast.makeText(this, "Permissions required to decode audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean permissionsGranted() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.RECORD_AUDIO);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissions.isEmpty())
            return true;
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), permissionsID);
        return false;
    }

    protected void toggleDecoder() {
        if (decoder == null)
            startSSTVDecoder();
        else
            stopDecoder();
    }

    @Override
    protected void onDestroy () {
        stopDecoder();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (decoder != null)
            decoder.pause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        if (decoder != null)
            decoder.resume();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        updateMenuButtons();
        MenuItem item = menu.findItem(R.id.menu_item_share);
        share = (ShareActionProvider)MenuItemCompat.getActionProvider(item);
        return true;
    }

//    @Override
//    public void onConfigurationChanged(@NonNull Configuration config) {
//        super.onConfigurationChanged(config);
//        changeLayoutOrientation(config);
//    }

//    private void changeLayoutOrientation(Configuration config) {
//        boolean horizontal = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
//        View analysis = findViewById(R.id.analysis);
//        analysis.setVisibility(enableAnalyzer ? View.VISIBLE : View.GONE);
//        analysis.setLayoutParams(
//                new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT, horizontal ? 1.0f : 10.0f));
//        int content_orientation = horizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
//        ((LinearLayout)findViewById(R.id.content)).setOrientation(content_orientation);
//        int analysis_orientation = horizontal ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
//        ((LinearLayout)findViewById(R.id.analysis)).setOrientation(analysis_orientation);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_decoder) {
            toggleDecoder();
            return true;
        }
        if (id == R.id.action_save_image) {
            storeBitmap(image.bitmap);
            return true;
        }
        if (id == R.id.action_clear_image) {
            decoder.clear_image();
            return true;
        }
        if (id == R.id.action_sharpest_image) {
            decoder.adjust_blur(-3);
            return true;
        }
        if (id == R.id.action_sharper_image) {
            decoder.adjust_blur(-2);
            return true;
        }
        if (id == R.id.action_sharp_image) {
            decoder.adjust_blur(-1);
            return true;
        }
        if (id == R.id.action_neutral_image) {
            decoder.adjust_blur(0);
            return true;
        }
        if (id == R.id.action_soft_image) {
            decoder.adjust_blur(1);
            return true;
        }
        if (id == R.id.action_softer_image) {
            decoder.adjust_blur(2);
            return true;
        }
        if (id == R.id.action_softest_image) {
            decoder.adjust_blur(3);
            return true;
        }
        if (id == R.id.action_toggle_scaling) {
            decoder.toggle_scaling();
            return true;
        }
        if (id == R.id.action_toggle_debug) {
            decoder.toggle_debug();
            return true;
        }
        if (id == R.id.action_auto_mode) {
            decoder.auto_mode();
            return true;
        }
//        if (id == R.id.action_toggle_analyzer) {
//            decoder.enable_analyzer(enableAnalyzer ^= true);
//            changeLayoutOrientation(getResources().getConfiguration());
//            return true;
//        }
        if (id == R.id.action_slow_update_rate) {
            decoder.setUpdateRate(0);
            return true;
        }
        if (id == R.id.action_normal_update_rate) {
            decoder.setUpdateRate(1);
            return true;
        }
        if (id == R.id.action_fast_update_rate) {
            decoder.setUpdateRate(2);
            return true;
        }
        if (id == R.id.action_faster_update_rate) {
            decoder.setUpdateRate(3);
            return true;
        }
        if (id == R.id.action_fastest_update_rate) {
            decoder.setUpdateRate(4);
            return true;
        }
        if (id == R.id.action_raw_mode) {
            decoder.raw_mode();
            return true;
        }
        if (id == R.id.action_robot36_mode) {
            decoder.robot36_mode();
            return true;
        }
        if (id == R.id.action_robot72_mode) {
            decoder.robot72_mode();
            return true;
        }
        if (id == R.id.action_martin1_mode) {
            decoder.martin1_mode();
            return true;
        }
        if (id == R.id.action_martin2_mode) {
            decoder.martin2_mode();
            return true;
        }
        if (id == R.id.action_scottie1_mode) {
            decoder.scottie1_mode();
            return true;
        }
        if (id == R.id.action_scottie2_mode) {
            decoder.scottie2_mode();
            return true;
        }
        if (id == R.id.action_scottieDX_mode) {
            decoder.scottieDX_mode();
            return true;
        }
        if (id == R.id.action_wraaseSC2_180_mode) {
            decoder.wraaseSC2_180_mode();
            return true;
        }
        if (id == R.id.action_pd50_mode) {
            decoder.pd50_mode();
            return true;
        }
        if (id == R.id.action_pd90_mode) {
            decoder.pd90_mode();
            return true;
        }
        if (id == R.id.action_pd120_mode) {
            decoder.pd120_mode();
            return true;
        }
        if (id == R.id.action_pd160_mode) {
            decoder.pd160_mode();
            return true;
        }
        if (id == R.id.action_pd180_mode) {
            decoder.pd180_mode();
            return true;
        }
        if (id == R.id.action_pd240_mode) {
            decoder.pd240_mode();
            return true;
        }
        if (id == R.id.action_pd290_mode) {
            decoder.pd290_mode();
            return true;
        }
        if (id == R.id.action_privacy_policy) {
            showPrivacyPolicy();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    private char getCharacterForBinaryText(String s, double markFrequency, double spaceFrequency, short[] samples, int sampleRate) {
        double frequency = calculateInstantaneousFrequency(samples, sampleRate);
        double tolerance = 50.0; // Adjust the tolerance as needed

        if (Math.abs(frequency - markFrequency) <= tolerance) {
            return '|';
        }
        // Numbers
        else if (Math.abs(frequency - 2000.0) <= tolerance) {
            return '0';
        }else if (Math.abs(frequency - 2050.0) <= tolerance) {
            return '1';
        }else if (Math.abs(frequency - 2100.0) <= tolerance) {
            return '2';
        }else if (Math.abs(frequency - 2150.0) <= tolerance) {
            return '3';
        } else if (Math.abs(frequency - 2200.0) <= tolerance) {
            return '4';
        } else if (Math.abs(frequency - 2250.0) <= tolerance) {
            return '5';
        } else if (Math.abs(frequency - 2300.0) <= tolerance) {
            return '6';
        } else if (Math.abs(frequency - 2350.0) <= tolerance) {
            return '7';
        } else if (Math.abs(frequency - 2400.0) <= tolerance) {
            return '8';
        } else if (Math.abs(frequency - 2450.0) <= tolerance) {
            return '9';
        }
        //special Characters
        else if (Math.abs(frequency - 4300.0) <= tolerance) {
            return '/';
        }else if (Math.abs(frequency - 4350.0) <= tolerance) {
            return '=';
        }else if (Math.abs(frequency - 4400.0) <= tolerance) {
            return ':';
        }else if (Math.abs(frequency - 4450.0) <= tolerance) {
            return '?';
        }else if (Math.abs(frequency - 4500.0) <= tolerance) {
            return ',';
        }
        else if (Math.abs(frequency - 4550.0) <= tolerance) {
            return '.';
        }
        else if (Math.abs(frequency - 4700.0) <= tolerance) {
            return ' ';
        }
        //Alphabet
        else if (Math.abs(frequency - 2850.0) <= tolerance) {
            return 'a';
        } else if (Math.abs(frequency - 2900.0) <= tolerance) {
            return 'b';
        } else if (Math.abs(frequency - 2950.0) <= tolerance) {
            return 'c';
        } else if (Math.abs(frequency - 3000.0) <= tolerance) {
            return 'd';
        } else if (Math.abs(frequency - 3050.0) <= tolerance) {
            return 'e';
        } else if (Math.abs(frequency - 3100.0) <= tolerance) {
            return 'f';
        } else if (Math.abs(frequency - 3150.0) <= tolerance) {
            return 'g';
        } else if (Math.abs(frequency - 3200.0) <= tolerance) {
            return 'h';
        } else if (Math.abs(frequency - 3250.0) <= tolerance) {
            return 'i';
        } else if (Math.abs(frequency - 3300.0) <= tolerance) {
            return 'j';
        } else if (Math.abs(frequency - 3350.0) <= tolerance) {
            return 'k';
        } else if (Math.abs(frequency - 3400.0) <= tolerance) {
            return 'l';
        } else if (Math.abs(frequency - 3450.0) <= tolerance) {
            return 'm';
        } else if (Math.abs(frequency - 3500.0) <= tolerance) {
            return 'n';
        } else if (Math.abs(frequency - 3550.0) <= tolerance) {
            return 'o';
        } else if (Math.abs(frequency - 3600.0) <= tolerance) {
            return 'p';
        } else if (Math.abs(frequency - 3650.0) <= tolerance) {
            return 'q';
        } else if (Math.abs(frequency - 3700.0) <= tolerance) {
            return 'r';
        } else if (Math.abs(frequency - 3750.0) <= tolerance) {
            return 's';
        } else if (Math.abs(frequency - 3800.0) <= tolerance) {
            return 't';
        } else if (Math.abs(frequency - 3850.0) <= tolerance) {
            return 'u';
        } else if (Math.abs(frequency - 3900.0) <= tolerance) {
            return 'v';
        } else if (Math.abs(frequency - 3950.0) <= tolerance) {
            return 'w';
        } else if (Math.abs(frequency - 4000.0) <= tolerance) {
            return 'x';
        } else if (Math.abs(frequency - 4050.0) <= tolerance) {
            return 'y';
        } else if (Math.abs(frequency - 4100.0) <= tolerance) {
            return 'z';
        } else if (Math.abs(frequency - spaceFrequency) <= tolerance) {
            return ' ';
        } else {
            return ' '; // Return a space for unrecognized frequencies
        }
    }



}
