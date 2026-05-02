package com.example.heal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeartRateActivity extends AppCompatActivity {

    private static final String TAG = "HeartRatePPG";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int MEASUREMENT_DURATION_MS = 18000; // 18 seconds
    private static final int SAMPLE_INTERVAL_MS = 33;         // ~30 fps
    private static final double MIN_HR_HZ = 0.7;              // 42 BPM
    private static final double MAX_HR_HZ = 4.0;              // 240 BPM

    // UI
    private TextureView textureView;
    private TextView tvInstruction, tvSubInstruction, tvSignalQuality;
    private TextView tvProgress, tvBpm, tvBpmCategory, btnStartMeasure;
    private View viewSignalIndicator, signalDot;
    private LinearLayout llSignalQuality;
    private ProgressBar progressMeasure;
    private CardView cardResult;

    // Camera2
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private String cameraId;

    // Measurement state
    private boolean isMeasuring = false;
    private final List<Double> greenSamples = new ArrayList<>();
    private final List<Long> timestamps = new ArrayList<>();
    private long measureStartTime;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        uiHandler = new Handler(getMainLooper());

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        textureView = findViewById(R.id.textureView);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvSubInstruction = findViewById(R.id.tvSubInstruction);
        tvSignalQuality = findViewById(R.id.tvSignalQuality);
        viewSignalIndicator = findViewById(R.id.viewSignalIndicator);
        signalDot = findViewById(R.id.signalDot);
        llSignalQuality = findViewById(R.id.llSignalQuality);
        progressMeasure = findViewById(R.id.progressMeasure);
        tvProgress = findViewById(R.id.tvProgress);
        tvBpm = findViewById(R.id.tvBpm);
        tvBpmCategory = findViewById(R.id.tvBpmCategory);
        btnStartMeasure = findViewById(R.id.btnStartMeasure);
        cardResult = findViewById(R.id.cardResult);

        btnStartMeasure.setOnClickListener(v -> {
            if (isMeasuring) {
                stopMeasurement();
            } else {
                startMeasurement();
            }
        });
    }

    // ═══════════════════════════════════════════════════
    //  PERMISSION & CAMERA LIFECYCLE
    // ═══════════════════════════════════════════════════

    private void startMeasurement() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }
        beginCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginCamera();
            } else {
                Toast.makeText(this, "Camera permission is required for heart rate measurement.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void beginCamera() {
        isMeasuring = true;
        greenSamples.clear();
        timestamps.clear();

        cardResult.setVisibility(View.GONE);
        progressMeasure.setVisibility(View.VISIBLE);
        progressMeasure.setProgress(0);
        tvProgress.setVisibility(View.VISIBLE);
        tvProgress.setText("Measuring... 0%");
        llSignalQuality.setVisibility(View.VISIBLE);
        signalDot.setVisibility(View.VISIBLE);
        btnStartMeasure.setText("Cancel");
        tvInstruction.setText("Place your finger over the rear camera");
        tvSubInstruction.setText("Press gently \u2022 Do not move");

        startCameraThread();
        openCamera();
    }

    private void stopMeasurement() {
        isMeasuring = false;
        btnStartMeasure.setText("Start Measurement");
        progressMeasure.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        llSignalQuality.setVisibility(View.GONE);
        signalDot.setVisibility(View.GONE);
        tvInstruction.setText("Place your finger over the rear camera");
        tvSubInstruction.setText("Press gently \u2022 Do not move");
        closeCamera();
    }

    // ═══════════════════════════════════════════════════
    //  CAMERA2 API
    // ═══════════════════════════════════════════════════

    private void startCameraThread() {
        cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // Find rear camera with flash
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                Boolean flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }
            if (cameraId == null) {
                Toast.makeText(this, "No rear camera found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access error", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCaptureSession();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Log.e(TAG, "Camera error: " + error);
        }
    };

    private void createCaptureSession() {
        try {
            // Small ImageReader for pixel sampling (we don't need high res)
            imageReader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, cameraHandler);

            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            if (surfaceTexture == null) {
                // TextureView not ready yet; use ImageReader only
                Surface imageSurface = imageReader.getSurface();

                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(imageSurface);

                // Turn on the flash (torch mode) for PPG
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                cameraDevice.createCaptureSession(Arrays.asList(imageSurface),
                        sessionCallback, cameraHandler);
            } else {
                surfaceTexture.setDefaultBufferSize(320, 240);
                Surface previewSurface = new Surface(surfaceTexture);
                Surface imageSurface = imageReader.getSurface();

                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(previewSurface);
                captureRequestBuilder.addTarget(imageSurface);

                // Turn on the flash (torch mode) for PPG
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageSurface),
                        sessionCallback, cameraHandler);
            }

            measureStartTime = System.currentTimeMillis();

        } catch (CameraAccessException e) {
            Log.e(TAG, "Capture session error", e);
        }
    }

    private final CameraCaptureSession.StateCallback sessionCallback =
            new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            captureSession = session;
            try {
                session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Repeating request error", e);
            }
        }
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Session configuration failed");
        }
    };

    // ═══════════════════════════════════════════════════
    //  IMAGE PROCESSING — GREEN CHANNEL EXTRACTION
    // ═══════════════════════════════════════════════════

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
        Image image = reader.acquireLatestImage();
        if (image == null || !isMeasuring) return;

        try {
            // Extract green channel average from center ROI of YUV image
            // In YUV_420_888: Y plane has luminance. For green channel approximation
            // we use the Y plane center ROI (which correlates strongly with green under
            // flash illumination on skin).
            Image.Plane yPlane = image.getPlanes()[0];
            ByteBuffer yBuffer = yPlane.getBuffer();
            int width = image.getWidth();
            int height = image.getHeight();
            int rowStride = yPlane.getRowStride();

            // ROI: center 40% of the image (closest to flash, brightest area)
            int roiX = (int) (width * 0.3);
            int roiY = (int) (height * 0.3);
            int roiW = (int) (width * 0.4);
            int roiH = (int) (height * 0.4);

            long sum = 0;
            int count = 0;
            for (int y = roiY; y < roiY + roiH; y++) {
                for (int x = roiX; x < roiX + roiW; x++) {
                    int index = y * rowStride + x;
                    if (index < yBuffer.capacity()) {
                        sum += (yBuffer.get(index) & 0xFF);
                        count++;
                    }
                }
            }

            if (count == 0) { image.close(); return; }

            double avgGreen = (double) sum / count;
            long now = System.currentTimeMillis();

            synchronized (greenSamples) {
                greenSamples.add(avgGreen);
                timestamps.add(now);
            }

            // Update UI on main thread
            long elapsed = now - measureStartTime;
            int progress = (int) ((elapsed * 100) / MEASUREMENT_DURATION_MS);
            if (progress > 100) progress = 100;

            // Detect finger presence: average brightness > 50 when flash is on finger
            boolean fingerDetected = avgGreen > 50;
            int finalProgress = progress;

            uiHandler.post(() -> {
                progressMeasure.setProgress(finalProgress);
                tvProgress.setText("Measuring... " + finalProgress + "%");

                if (fingerDetected && avgGreen > 100) {
                    tvSignalQuality.setText("Good signal");
                    viewSignalIndicator.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                    signalDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFF4CAF50));
                    tvInstruction.setText("Reading your heartbeat...");
                } else if (fingerDetected) {
                    tvSignalQuality.setText("Weak signal - press firmer");
                    viewSignalIndicator.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFFF9800));
                    signalDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFFF9800));
                } else {
                    tvSignalQuality.setText("No finger detected");
                    viewSignalIndicator.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFF44336));
                    signalDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFF44336));
                    tvInstruction.setText("Place your finger over the rear camera");
                }
            });

            // Check if measurement complete
            if (elapsed >= MEASUREMENT_DURATION_MS) {
                isMeasuring = false;
                uiHandler.post(this::finishMeasurement);
            }

        } finally {
            image.close();
        }
    };

    // ═══════════════════════════════════════════════════
    //  SIGNAL PROCESSING — BANDPASS FILTER + PEAK DETECT
    // ═══════════════════════════════════════════════════

    private void finishMeasurement() {
        closeCamera();

        List<Double> samples;
        List<Long> times;
        synchronized (greenSamples) {
            samples = new ArrayList<>(greenSamples);
            times = new ArrayList<>(timestamps);
        }

        if (samples.size() < 60) {
            showError("Not enough data collected. Please try again with your finger firmly on the camera.");
            return;
        }

        // Calculate sample rate from actual timestamps
        long totalDuration = times.get(times.size() - 1) - times.get(0);
        double sampleRate = (samples.size() - 1) * 1000.0 / totalDuration;

        // 1. Remove DC component (subtract mean)
        double mean = 0;
        for (double s : samples) mean += s;
        mean /= samples.size();

        double[] signal = new double[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            signal[i] = samples.get(i) - mean;
        }

        // 2. Apply simple moving average smoothing (noise reduction)
        int windowSize = Math.max(3, (int) (sampleRate / 10));
        double[] smoothed = movingAverage(signal, windowSize);

        // 3. Bandpass filter using FFT-like frequency analysis
        //    We use autocorrelation-based frequency detection (more robust than peak counting)
        int bpm = calculateBpmAutocorrelation(smoothed, sampleRate);

        if (bpm < 40 || bpm > 200) {
            showError("Could not get a reliable reading. Ensure your finger covers the entire camera lens and flashlight.");
            return;
        }

        // Show result
        showResult(bpm);
    }

    /**
     * Autocorrelation-based BPM calculation.
     * Finds the dominant periodic component in the valid heart rate range.
     */
    private int calculateBpmAutocorrelation(double[] signal, double sampleRate) {
        int n = signal.length;

        // Min and max lag corresponding to heart rate range
        int minLag = (int) (sampleRate / MAX_HR_HZ); // 4.0 Hz → shortest period
        int maxLag = (int) (sampleRate / MIN_HR_HZ); // 0.7 Hz → longest period

        if (maxLag >= n / 2) maxLag = n / 2 - 1;
        if (minLag < 1) minLag = 1;

        double maxCorr = Double.NEGATIVE_INFINITY;
        int bestLag = minLag;

        // Normalized autocorrelation
        double energy = 0;
        for (double v : signal) energy += v * v;
        if (energy == 0) return 0;

        for (int lag = minLag; lag <= maxLag; lag++) {
            double corr = 0;
            for (int i = 0; i < n - lag; i++) {
                corr += signal[i] * signal[i + lag];
            }
            corr /= energy; // normalize

            if (corr > maxCorr) {
                maxCorr = corr;
                bestLag = lag;
            }
        }

        // Convert lag to BPM
        double frequencyHz = sampleRate / bestLag;
        int bpm = (int) Math.round(frequencyHz * 60.0);

        Log.d(TAG, "Autocorrelation: bestLag=" + bestLag + " freq=" + frequencyHz
                + "Hz bpm=" + bpm + " sampleRate=" + sampleRate
                + " samples=" + signal.length + " maxCorr=" + maxCorr);

        // If correlation is too weak, result is unreliable
        if (maxCorr < 0.1) {
            Log.w(TAG, "Weak autocorrelation, result may be inaccurate");
        }

        return bpm;
    }

    private double[] movingAverage(double[] data, int window) {
        double[] result = new double[data.length];
        int half = window / 2;
        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - half); j <= Math.min(data.length - 1, i + half); j++) {
                sum += data[j];
                count++;
            }
            result[i] = sum / count;
        }
        return result;
    }

    // ═══════════════════════════════════════════════════
    //  UI RESULT
    // ═══════════════════════════════════════════════════

    private void showResult(int bpm) {
        tvBpm.setText(String.valueOf(bpm));

        // Categorize
        String category;
        int color;
        int bgColor;
        if (bpm < 60) {
            category = "Below Normal (Bradycardia)";
            color = 0xFF1565C0; bgColor = 0xFFE3F2FD;
        } else if (bpm <= 100) {
            category = "Normal Resting Heart Rate";
            color = 0xFF2E7D32; bgColor = 0xFFE8F5E9;
        } else if (bpm <= 130) {
            category = "Slightly Elevated";
            color = 0xFFFF8F00; bgColor = 0xFFFFF8E1;
        } else {
            category = "Elevated (Tachycardia)";
            color = 0xFFD32F2F; bgColor = 0xFFFFEBEE;
        }

        tvBpmCategory.setText(category);
        tvBpmCategory.setTextColor(color);
        tvBpmCategory.setBackgroundColor(bgColor);

        cardResult.setVisibility(View.VISIBLE);
        progressMeasure.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        llSignalQuality.setVisibility(View.GONE);
        signalDot.setVisibility(View.GONE);
        btnStartMeasure.setText("Measure Again");
        tvInstruction.setText("Measurement complete!");
        tvSubInstruction.setText("Tap below to measure again");
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        btnStartMeasure.setText("Try Again");
        progressMeasure.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        llSignalQuality.setVisibility(View.GONE);
        signalDot.setVisibility(View.GONE);
        tvInstruction.setText("Measurement failed");
        tvSubInstruction.setText(msg);
    }

    // ═══════════════════════════════════════════════════
    //  CLEANUP
    // ═══════════════════════════════════════════════════

    private void closeCamera() {
        try {
            if (captureSession != null) { captureSession.close(); captureSession = null; }
            if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
            if (imageReader != null) { imageReader.close(); imageReader = null; }
            if (cameraThread != null) { cameraThread.quitSafely(); cameraThread = null; }
        } catch (Exception e) {
            Log.e(TAG, "Error closing camera", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMeasuring) stopMeasurement();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
    }
}
