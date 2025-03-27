package de.joancode.bloomassist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import de.joancode.bloomassist.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private boolean isLoginMode = true;
    private AuthService authService;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupLanguageSelector();
        loadLanguagePreference();

        GetConfig configFetcher = new GetConfig();
        configFetcher.fetchConfig(config -> {
            if (config != null) {
                String apiUrl = config.getAuthApiUrl();
                authService = new AuthService(apiUrl, this);
                runOnUiThread(() -> {
                    setupClickListeners();
                    binding.authButton.setEnabled(true);
                });
                Log.d("AuthActivity", "Config loaded successfully: " + apiUrl);
            } else {
                runOnUiThread(() -> showError(getString(R.string.config_load_error)));
                Log.e("AuthActivity", "Error loading config: " + getString(R.string.config_load_error));
            }
        });

        // Initially disable the auth button until config is loaded
        binding.authButton.setEnabled(false);
    }

    private void setupLanguageSelector() {
        String[] languages = {"English", "Deutsch"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.languageSpinner.setAdapter(adapter);

        binding.languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = position == 0 ? "english" : "german";
                saveLanguagePreference(selectedLanguage);
                updateUILanguage(selectedLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadLanguagePreference() {
        String savedLanguage = getSharedPreferences("AuthAppPrefs", Context.MODE_PRIVATE)
                .getString("selectedLanguage", "english");
        binding.languageSpinner.setSelection(savedLanguage.equals("english") ? 0 : 1);
        updateUILanguage(savedLanguage);
    }

    private void saveLanguagePreference(String language) {
        getSharedPreferences("AuthAppPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("selectedLanguage", language)
                .apply();
    }

    private void updateUILanguage(String language) {
        if ("german".equals(language)) {
            if (isLoginMode) {
                binding.authTitle.setText(R.string.login_de);
                binding.authButton.setText(R.string.login_de);
                binding.switchAuthMode.setText(R.string.no_account_signup_de);
                binding.emailLayout.setHint(getString(R.string.email_de));
                binding.passwordLayout.setHint(getString(R.string.password_de));
            } else {
                binding.authTitle.setText(R.string.signup_de);
                binding.authButton.setText(R.string.signup_de);
                binding.switchAuthMode.setText(R.string.already_have_account_de);
                binding.emailLayout.setHint(getString(R.string.email_de));
                binding.passwordLayout.setHint(getString(R.string.password_de));
            }
        } else {
            if (isLoginMode) {
                binding.authTitle.setText(R.string.login);
                binding.authButton.setText(R.string.login);
                binding.switchAuthMode.setText(R.string.no_account_signup);
                binding.emailLayout.setHint(getString(R.string.email));
                binding.passwordLayout.setHint(getString(R.string.password));
            } else {
                binding.authTitle.setText(R.string.signup);
                binding.authButton.setText(R.string.signup);
                binding.switchAuthMode.setText(R.string.already_have_account);
                binding.emailLayout.setHint(getString(R.string.email));
                binding.passwordLayout.setHint(getString(R.string.password));
            }
        }
    }

    private void setupClickListeners() {
        binding.switchAuthMode.setOnClickListener(v -> {
            if (!isLoading) {
                isLoginMode = !isLoginMode;
                String currentLanguage = binding.languageSpinner.getSelectedItemPosition() == 0 ?
                        "english" : "german";
                updateUILanguage(currentLanguage);
                binding.authError.setVisibility(View.GONE);
                clearInputs();
            }
        });

        binding.authButton.setOnClickListener(v -> {
            if (isLoading) return;

            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showError(getString(R.string.fill_all_fields));
                return;
            }

            setLoading(true);
            if (isLoginMode) {
                handleLogin(email, password);
            } else {
                handleSignup(email, password);
            }
        });
    }

    private void handleLogin(String email, String password) {
        authService.login(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthService.AuthResponse response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.getSuccess()) {
                        saveAuthData(response.getToken(), response.getEmail());
                        Toast.makeText(AuthActivity.this, response.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        showError(response.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(message);
                });
            }
        });
    }

    private void handleSignup(String email, String password) {
        authService.signup(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthService.AuthResponse response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.getSuccess()) {
                        saveAuthData(response.getToken(), response.getEmail());
                        Toast.makeText(AuthActivity.this, response.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        showError(response.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(message);
                });
            }
        });
    }

    private void saveAuthData(String token, String email) {
        getSharedPreferences("AuthAppPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("auth_token", token)
                .putString("user_email", email)
                .apply();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        binding.authButton.setEnabled(!loading);
        binding.switchAuthMode.setEnabled(!loading);
        binding.emailInput.setEnabled(!loading);
        binding.passwordInput.setEnabled(!loading);
        binding.languageSpinner.setEnabled(!loading);
    }

    private void showError(String message) {
        binding.authError.setText(message);
        binding.authError.setVisibility(View.VISIBLE);
    }

    private void clearInputs() {
        binding.emailInput.setText("");
        binding.passwordInput.setText("");
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}