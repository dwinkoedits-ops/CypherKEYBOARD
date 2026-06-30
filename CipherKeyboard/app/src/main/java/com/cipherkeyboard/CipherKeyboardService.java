package com.cipherkeyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class CipherKeyboardService extends InputMethodService implements View.OnClickListener {

    private TextView tvBuffer;
    private LinearLayout llModes;
    private View keyboardRootView;

    // In-keyboard overlay panels (no separate windows → no keyboard dismissal)
    private View panelDismiss;
    private LinearLayout accentPanel;
    private LinearLayout symbolPanel;
    private GridLayout symbolGrid;

    private final StringBuilder buffer = new StringBuilder();
    private String currentMode = CipherUtils.MODE_NORMAL;
    private boolean capsOn = false;
    private boolean symbolsBuilt = false;

    private static final int[] NUM_IDS = {
        R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5,
        R.id.k6, R.id.k7, R.id.k8, R.id.k9, R.id.k0
    };
    private static final char[] SYMBOLS = {'!','@','#','$','%','^','&','*','(',')' };

    private static final String[] SPECIAL_CHARS = {
        "!", "?", "#", "$", "%", "^", "&", "*", "(", ")",
        "[", "]", "{", "}", "<", ">", "/", "\\", "+", "=",
        "~", "`", "'", "\"", ";", ":", "_", ",", "€", "£"
    };

    // Accent variants per letter
    private static final Map<Character, String[]> ACCENTS = new HashMap<>();
    static {
        ACCENTS.put('a', new String[]{"à", "â", "ä", "á", "å", "ã", "æ"});
        ACCENTS.put('e', new String[]{"é", "è", "ê", "ë", "œ"});
        ACCENTS.put('i', new String[]{"î", "ï", "í", "ì"});
        ACCENTS.put('o', new String[]{"ô", "ö", "ò", "ó", "õ", "ø"});
        ACCENTS.put('u', new String[]{"ù", "û", "ü", "ú"});
        ACCENTS.put('c', new String[]{"ç", "ć", "č"});
        ACCENTS.put('n', new String[]{"ñ", "ń"});
        ACCENTS.put('y', new String[]{"ÿ", "ý"});
        ACCENTS.put('s', new String[]{"ş", "š", "ś"});
        ACCENTS.put('z', new String[]{"ž", "ź", "ż"});
    }

    private static final int[] LETTER_IDS = {
        R.id.ka, R.id.kz, R.id.ke, R.id.kr, R.id.kt,
        R.id.ky, R.id.ku, R.id.ki, R.id.ko, R.id.kp,
        R.id.kq, R.id.ks, R.id.kd, R.id.kf, R.id.kg,
        R.id.kh, R.id.kj, R.id.kk, R.id.kl, R.id.km,
        R.id.kw, R.id.kx, R.id.kc, R.id.kv, R.id.kb, R.id.kn
    };

    private static final char[] LETTER_CHARS = {
        'a','z','e','r','t','y','u','i','o','p',
        'q','s','d','f','g','h','j','k','l','m',
        'w','x','c','v','b','n'
    };

    @Override
    public View onCreateInputView() {
        keyboardRootView = LayoutInflater.from(this).inflate(R.layout.keyboard, null);

        tvBuffer    = keyboardRootView.findViewById(R.id.tv_buffer);
        llModes     = keyboardRootView.findViewById(R.id.ll_modes);
        panelDismiss = keyboardRootView.findViewById(R.id.panel_dismiss);
        accentPanel  = keyboardRootView.findViewById(R.id.accent_panel);
        symbolPanel  = keyboardRootView.findViewById(R.id.symbol_panel);
        symbolGrid   = keyboardRootView.findViewById(R.id.symbol_grid);

        buildModeBar();
        buildSymbolGrid();

        // Tap on dim overlay → close any panel
        panelDismiss.setOnClickListener(v -> closePanels());

        // Letter keys
        for (int i = 0; i < LETTER_IDS.length; i++) {
            View btn = keyboardRootView.findViewById(LETTER_IDS[i]);
            btn.setOnClickListener(this);
            final char letter = LETTER_CHARS[i];
            if (ACCENTS.containsKey(letter)) {
                btn.setOnLongClickListener(v -> {
                    showAccentPanel(letter);
                    return true;
                });
            }
        }

        // Number keys: tap = digit, long press = symbol
        for (int i = 0; i < NUM_IDS.length; i++) {
            View btn = keyboardRootView.findViewById(NUM_IDS[i]);
            btn.setOnClickListener(this);
            final char sym = SYMBOLS[i];
            btn.setOnLongClickListener(v -> {
                typeChar(sym);
                return true;
            });
        }

        keyboardRootView.findViewById(R.id.kdash).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kdot).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kat).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kpipe).setOnClickListener(this);

        // Space: tap = space, long press = language picker
        View spaceBtn = keyboardRootView.findViewById(R.id.kspace);
        spaceBtn.setOnClickListener(this);
        spaceBtn.setOnLongClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
            return true;
        });

        // !# button: toggle symbol panel — does NOT type anything
        keyboardRootView.findViewById(R.id.ksymbol).setOnClickListener(v -> toggleSymbolPanel());

        // Backspace
        View backspaceBtn = keyboardRootView.findViewById(R.id.kbackspace);
        backspaceBtn.setOnClickListener(this);
        backspaceBtn.setOnLongClickListener(v -> {
            if (buffer.length() > 0) {
                buffer.setLength(0);
                updateBufferDisplay();
                Toast.makeText(this, "Buffer effacé", Toast.LENGTH_SHORT).show();
            } else {
                getCurrentInputConnection().performContextMenuAction(android.R.id.selectAll);
                getCurrentInputConnection().commitText("", 1);
                Toast.makeText(this, "Champ effacé", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        keyboardRootView.findViewById(R.id.kshift).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kclear).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.ksend).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kcopy).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kpaste).setOnClickListener(this);
        keyboardRootView.findViewById(R.id.kpastesend).setOnClickListener(this);

        return keyboardRootView;
    }

    // ── Build the symbol grid once ────────────────────────────────────────────
    private void buildSymbolGrid() {
        if (symbolsBuilt) return;
        symbolsBuilt = true;
        symbolGrid.removeAllViews();
        for (String ch : SPECIAL_CHARS) {
            Button btn = new Button(this);
            btn.setText(ch);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundColor(Color.parseColor("#FF2C2C2E"));
            btn.setStateListAnimator(null);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width  = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(4, 5, 4, 5);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> {
                closePanels();
                if (ch.length() == 1) typeChar(ch.charAt(0));
                else typeString(ch);
            });
            symbolGrid.addView(btn);
        }
    }

    // ── Show / hide panels ────────────────────────────────────────────────────
    private void showAccentPanel(char letter) {
        String[] variants = ACCENTS.get(letter);
        if (variants == null) return;

        accentPanel.removeAllViews();
        int keySize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());

        for (String accent : variants) {
            Button btn = new Button(this);
            btn.setText(accent);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundResource(R.drawable.accent_key_bg);
            btn.setStateListAnimator(null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(keySize, keySize);
            lp.setMargins(5, 0, 5, 0);
            btn.setLayoutParams(lp);
            btn.setPadding(0, 0, 0, 0);
            btn.setOnClickListener(v -> {
                closePanels();
                typeString(accent);
            });
            accentPanel.addView(btn);
        }

        symbolPanel.setVisibility(View.GONE);
        accentPanel.setVisibility(View.VISIBLE);
        panelDismiss.setVisibility(View.VISIBLE);
    }

    private void toggleSymbolPanel() {
        if (symbolPanel.getVisibility() == View.VISIBLE) {
            closePanels();
        } else {
            accentPanel.setVisibility(View.GONE);
            symbolPanel.setVisibility(View.VISIBLE);
            panelDismiss.setVisibility(View.VISIBLE);
        }
    }

    private void closePanels() {
        accentPanel.setVisibility(View.GONE);
        symbolPanel.setVisibility(View.GONE);
        panelDismiss.setVisibility(View.GONE);
    }

    // ── Mode bar ──────────────────────────────────────────────────────────────
    private void buildModeBar() {
        llModes.removeAllViews();
        for (String mode : CipherUtils.ALL_MODES) {
            Button btn = new Button(this);
            btn.setText(mode);
            btn.setTextSize(10f);
            btn.setTextColor(Color.WHITE);
            btn.setTypeface(null, mode.equals(currentMode) ? Typeface.BOLD : Typeface.NORMAL);
            btn.setBackgroundColor(mode.equals(currentMode)
                    ? Color.parseColor("#FF007AFF") : Color.parseColor("#FF1E2D42"));
            btn.setPadding(20, 0, 20, 0);
            btn.setStateListAnimator(null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(3, 4, 3, 4);
            btn.setLayoutParams(lp);
            String modeRef = mode;
            btn.setOnClickListener(v -> {
                currentMode = modeRef;
                buildModeBar();
                updateBufferDisplay();
            });
            llModes.addView(btn);
        }
    }

    // ── Click handler ─────────────────────────────────────────────────────────
    @Override
    public void onClick(View v) {
        // Close any open panel when user taps a key
        closePanels();

        int id = v.getId();

        if (id == R.id.kbackspace) {
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            } else {
                getCurrentInputConnection().deleteSurroundingText(1, 0);
            }
            updateBufferDisplay();
            return;
        }
        if (id == R.id.kclear) {
            buffer.setLength(0);
            updateBufferDisplay();
            return;
        }
        if (id == R.id.kshift) {
            capsOn = !capsOn;
            Toast.makeText(this, capsOn ? "MAJUSC ON" : "minusc", Toast.LENGTH_SHORT).show();
            return;
        }
        if (id == R.id.kspace) {
            typeChar(' ');
            return;
        }
        if (id == R.id.ksend) {
            if (buffer.length() > 0) {
                String result = CipherUtils.convert(buffer.toString(), currentMode);
                getCurrentInputConnection().commitText(result, 1);
                buffer.setLength(0);
                updateBufferDisplay();
            } else {
                getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_SEND);
            }
            return;
        }
        if (id == R.id.kcopy) {
            if (buffer.length() > 0) {
                String result = CipherUtils.convert(buffer.toString(), currentMode);
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("CipherKeyboard", result));
                Toast.makeText(this, "✅ Copié : " + result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Buffer vide !", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (id == R.id.kpaste) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm.hasPrimaryClip() && cm.getPrimaryClip() != null) {
                CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
                if (text != null) {
                    buffer.append(text);
                    updateBufferDisplay();
                    Toast.makeText(this, "📥 Collé dans le buffer", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Presse-papier vide !", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (id == R.id.kpastesend) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm.hasPrimaryClip() && cm.getPrimaryClip() != null) {
                CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
                if (text != null) {
                    String converted = CipherUtils.convert(text.toString(), currentMode);
                    getCurrentInputConnection().commitText(converted, 1);
                    Toast.makeText(this, "✅ Converti et envoyé", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Presse-papier vide !", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Generic single-character key
        String text = ((Button) v).getText().toString();
        if (text.length() == 1) {
            typeChar(text.charAt(0));
        }
    }

    // ── Type helpers ──────────────────────────────────────────────────────────
    private void typeChar(char c) {
        if (currentMode.equals(CipherUtils.MODE_NORMAL)) {
            char out = (capsOn && c >= 'a' && c <= 'z') ? (char)(c - 32) : c;
            getCurrentInputConnection().commitText(String.valueOf(out), 1);
        } else {
            char typed = (capsOn && c >= 'a' && c <= 'z') ? (char)(c - 32) : c;
            buffer.append(typed);
            updateBufferDisplay();
        }
    }

    private void typeString(String s) {
        if (currentMode.equals(CipherUtils.MODE_NORMAL)) {
            String out = capsOn ? s.toUpperCase() : s;
            getCurrentInputConnection().commitText(out, 1);
        } else {
            buffer.append(capsOn ? s.toUpperCase() : s);
            updateBufferDisplay();
        }
    }

    // ── Buffer display ────────────────────────────────────────────────────────
    private void updateBufferDisplay() {
        if (tvBuffer == null) return;
        if (buffer.length() == 0) {
            tvBuffer.setText("");
            tvBuffer.setHint(currentMode.contains("Vigenère")
                    ? "Tape CLE|message  ex: SECRET|bonjour"
                    : currentMode.contains("Rail")
                        ? "Rail Fence (3 rails)"
                        : "Tape ici...");
            return;
        }
        String preview = CipherUtils.convert(buffer.toString(), currentMode);
        tvBuffer.setText(buffer + "  →  " + preview);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        buffer.setLength(0);
        closePanels();
        if (tvBuffer != null) {
            tvBuffer.setText("");
            updateBufferDisplay();
        }
    }
}
