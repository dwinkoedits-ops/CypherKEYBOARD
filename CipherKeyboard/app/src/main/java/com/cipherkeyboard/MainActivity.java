package com.cipherkeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnEnable = new Button(this);
        btnEnable.setText("1. Activer CipherKeyboard dans les paramètres");
        btnEnable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

        Button btnSelect = new Button(this);
        btnSelect.setText("2. Sélectionner CipherKeyboard comme clavier");
        btnSelect.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
        });

        TextView info = new TextView(this);
        info.setText(
            "\nCipherKeyboard — Modes disponibles:\n\n" +
            "• Normal — frappe standard\n" +
            "• Base64 ▲/▼ — encoder/décoder\n" +
            "• Base32 ▲/▼ — encoder/décoder\n" +
            "• Hex ▲/▼ — hexadécimal\n" +
            "• Binaire ▲/▼ — 01010101\n" +
            "• ASCII→N° — 'A' → 65\n" +
            "• N°→ASCII — 65 → 'A'\n" +
            "• Texte→N° — 'oo' → 15-15\n" +
            "• N°→Texte — 15-15 → 'oo'\n" +
            "• ROT13 / ROT47\n" +
            "• César+3\n" +
            "• Morse ▲/▼\n" +
            "• Atbash\n" +
            "• Inverser / MAJUSC / minusc\n\n" +
            "Comment utiliser:\n" +
            "1. Tape ton texte\n" +
            "2. Choisis le mode en haut\n" +
            "3. Appuie sur ENVOYER → le texte converti est inséré"
        );
        info.setPadding(24, 24, 24, 24);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 64, 32, 32);
        layout.addView(btnEnable);
        layout.addView(btnSelect);
        layout.addView(info);

        setContentView(layout);
    }
}
