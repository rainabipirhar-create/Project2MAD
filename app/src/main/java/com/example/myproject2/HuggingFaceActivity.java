package com.example.myproject2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject2.databinding.ActivityHuggingFaceBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HuggingFaceActivity extends AppCompatActivity {

    private static final String TAG = "HuggingFaceActivity";
    private ActivityHuggingFaceBinding binding;

    // --- Hugging Face API Configuration for Image Classification ---
    private static final String HF_API_URL = "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224";
    private static final String HF_API_KEY = "YOUR API KEY";
    public static final MediaType IMAGE = MediaType.get("image/jpeg");
    private final OkHttpClient client = new OkHttpClient();

    // --- Activity Result Launcher for Image Selection ---
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        binding.imageView.setImageBitmap(bitmap);
                        classifyImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading image", e);
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHuggingFaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- 1. Toolbar Setup ---
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- 2. Handle Button Click ---
        binding.selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void classifyImage(Bitmap bitmap) {
        binding.resultTextView.setText("Classifying...");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        RequestBody requestBody = RequestBody.create(imageBytes, IMAGE);

        Request request = new Request.Builder()
                .url(HF_API_URL)
                .header("Authorization", "Bearer " + HF_API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> binding.resultTextView.setText("Failed to connect. Check internet."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {
                            JSONObject firstResult = jsonArray.getJSONObject(0);
                            String label = firstResult.getString("label");
                            double score = firstResult.getDouble("score");
                            String resultText = String.format("%s (Score: %.2f)", label, score);
                            runOnUiThread(() -> binding.resultTextView.setText(resultText));
                        } else {
                            runOnUiThread(() -> binding.resultTextView.setText("No classification found."));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        runOnUiThread(() -> binding.resultTextView.setText("Failed to parse response."));
                    }
                } else {
                    final String errorText = "API Error: " + response.code();
                    runOnUiThread(() -> binding.resultTextView.setText(errorText));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
