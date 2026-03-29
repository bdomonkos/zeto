package com.zeto.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.zeto.app.service.DeviceMonitorService;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

/**
 * Single activity of the app. Loads the UI from assets into a WebView,
 * asks for runtime permissions, and starts {@link DeviceMonitorService}.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean locationGranted = Boolean.TRUE.equals(
                                result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                        boolean notificationGranted = Boolean.TRUE.equals(
                                result.get(Manifest.permission.POST_NOTIFICATIONS));

                        Timber.d("Location granted: %b, Notification granted: %b",
                                locationGranted, notificationGranted);

                        startMonitoringService();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        setupWebView();

        requestPermissions();
    }

    /** Enables JS/DOM storage/file access and loads {@code index.html} from assets. */
    private void setupWebView() {
        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("file:///android_asset/index.html");
        webView.bringToFront();
    }

    /** Starts the service right away if permissions are already granted, otherwise shows the dialog. */
    private void requestPermissions() {
        boolean locationGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        if (locationGranted && notificationGranted) {
            Timber.d("All permissions already granted");
            startMonitoringService();
        } else {
            Timber.d("Requesting permissions...");
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            });
        }
    }

    /** Starts {@link DeviceMonitorService} with the current permission flags as extras. */
    private void startMonitoringService() {
        boolean hasLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean hasNotification = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        Intent serviceIntent = new Intent(this, DeviceMonitorService.class);
        serviceIntent.putExtra("hasLocation", hasLocation);
        serviceIntent.putExtra("hasNotification", hasNotification);

        startForegroundService(serviceIntent);

        Timber.d("DeviceMonitorService started");
    }
}
