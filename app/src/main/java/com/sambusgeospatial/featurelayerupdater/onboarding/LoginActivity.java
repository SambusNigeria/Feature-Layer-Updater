package com.sambusgeospatial.featurelayerupdater.onboarding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.google.android.material.button.MaterialButton;
import com.sambusgeospatial.featurelayerupdater.R;
import com.sambusgeospatial.featurelayerupdater.handlers.SessionManager;
import com.sambusgeospatial.featurelayerupdater.models.SambusUser;
import com.sambusgeospatial.featurelayerupdater.ui.home.MainActivity;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {
    SessionManager sessionManager;
    Button loginButton, logoutButton;
    ImageView logo;
    TextView welcome;
    String encodedThumbnail;
    DefaultAuthenticationChallengeHandler handler;
    final Portal portal = new Portal("https://www.arcgis.com", true);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
        handler = new DefaultAuthenticationChallengeHandler(this);
        AuthenticationManager.setAuthenticationChallengeHandler(handler);
        // Create a Portal object, indicate authentication is required

        loginButton = findViewById(R.id.loginBtn);
        logoutButton = findViewById(R.id.logoutBtn);
        welcome = findViewById(R.id.welcome);
        logo = findViewById(R.id.logo);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            logoutButton.setVisibility(View.VISIBLE);
            SambusUser user = sessionManager.getUserDetails();
            final String welcomeMessage = "Welcome back, " + user.getUserFullName() + "\nPlease login to continue.";
            // fetch the thumbnail
            byte[] imageAsBytes = Base64.decode(user.getUserThumbnail(), Base64.DEFAULT);
            logo.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
            welcome.setText(welcomeMessage);
        }
        loginButton.setOnClickListener( v-> attemptLogin());
        logoutButton.setOnClickListener( v-> {
            sessionManager.logoutUser();
            recreate();
        });
    }
    private void attemptLogin(){
        if (!sessionManager.isLoggedIn()) {
            portal.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if (portal.getLoadStatus() == LoadStatus.LOADED) {
                        PortalUser user = portal.getUser();
                        // check if user profile thumbnail exists
                        if (user.getThumbnailFileName() == null) {
                            return;
                        }
                        // fetch the thumbnail
                        final ListenableFuture<byte[]> thumbnailFuture = user.fetchThumbnailAsync();
                        thumbnailFuture.addDoneListener(() -> {
                            // get the thumbnail image data
                            byte[] itemThumbnailData;
                            try {
                                itemThumbnailData = thumbnailFuture.get();

                                if ((itemThumbnailData != null) && (itemThumbnailData.length > 0)) {
                                    // create a Bitmap to use as required
                                    Bitmap bitmap = BitmapFactory
                                            .decodeByteArray(itemThumbnailData, 0, itemThumbnailData.length);
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                    sessionManager.loginUser(user.getFullName(), user.getEmail(), user.getUsername(), user.getUserDescription(), encoded);

                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                Log.e(LoginActivity.class.getSimpleName(), e.toString());
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                }
            });
            portal.loadAsync();
        } else {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
