package com.cipherkeyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class CipherKeyboardService extends InputMethodService implements View.OnClickListener {

    private TextView tvBuffer;
    private LinearLayout llModes;
    private View keyboardRootView;

    private final StringBuilder buffer = new StringBuilder();
    private String currentMode = CipherUtils.MODE_NORMAL;
    private boolean capsOn = false;

    private static final int[] NUM_IDS = {
        R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5,
        R.id.k6, R.id.k7, R.id.k8, R.id.k9, R.id.k0
    };
    private static final char[] DIGITS  = {'1','2','3','4','5','6','7','8','9','0'};
    private static final char[] SYMBOLS = {'!','@','#','$','%','^','&','*','(',')' };

    private static final String[] SPECIAL_CHARS = {
        "!", "?", "#", "$", "%", "^", "&", "*", "(", ")",
        "[", "]", "{", "}", "<", ">", "/", "\\", "+", "=",
        "~", "`", "'", "\"", ";", ":", "_", ",", "€", "£"
    };

    // Accent variants for each letter (French + common European)
    private static final Map<Character, String[]> ACCENTS = new HashMap<>();
    static {
        ACCENTS.put('a', new String[]{"à", "â", "ä", "á", "å", "ã", "æ"});
        ACCENTS.put('e', new String[]{"é", "è", "ê", "ë", "œ"});
        ACCENTS.put('i', new String[]{"î", "ï", "í", "ì"});
        ACCENTS.put('o', new String[]{"ô", "ö", "ò", "ó", "õ", "ø", "œ"});
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

        tvBuffer = keyboardRootView.findViewById(R.id.tv_buffer);
        llModes  = keyboardRootView.findViewById(R.id.ll_modes);

        buildModeBar();

        // Letter keys: tap = type, long press = show accents if available
        for (int i = 0; i < LETTER_IDS.length; i++) {
            View btn = keyboardRootView.findViewById(LETTER_IDS[i]);
            btn.setOnClickListener(this);
            final char letter = LETTER_CHARS[i];
            if (ACCENTS.containsKey(letter)) {
                btn.setOnLongClickListener(v -> {
                    showAccentPopup(v, letter);
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

        // Space: tap = space, long press = switch keyboard/language
        View spaceBtn = keyboardRootView.findViewById(R.id.kspace);
        spaceBtn.setOnClickListener(this);
        spaceBtn.setOnLongClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
            return true;
        });

        // Special chars button: opens popup only — does NOT type anything
        View symBtn = keyboardRootView.findViewById(R.id.ksymbol);
        symBtn.setOnClickListener(v -> showSpecialCharsPopup(v));

        // Backspace: tap = delete one, long press = delete all
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

    // ── Type a character ──────────────────────────────────────────────────────
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

    // Type a full string (for accented chars which are multi-char in Java)
    private void typeString(String s) {
        if (currentMode.equals(CipherUtils.MODE_NORMAL)) {
            String out = (capsOn) ? s.toUpperCase() : s;
            getCurrentInputConnection().commitText(out, 1);
        } else {
            buffer.append(capsOn ? s.toUpperCase() : s);
            updateBufferDisplay();
        }
    }

    // ── Accent popup (long-press on a letter) ─────────────────────────────────
    private void showAccentPopup(View anchor, char letter) {
        String[] variants = ACCENTS.get(letter);
        if (variants == null || variants.length == 0) return;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.popup_bg);
        row.setPadding(10, 10, 10, 10);

        final PopupWindow[] popupRef = {null};

        int keySize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());

        for (String accent : variants) {
            Button btn = new Button(this);
            btn.setText(accent);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundResource(R.drawable.accent_key_bg);
            btn.setStateListAnimator(null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(keySize, keySize);
            lp.setMargins(5, 0, 5, 0);
            btn.setLayoutParams(lp);
            btn.setPadding(0, 0, 0, 0);
            btn.setOnClickListener(vv -> {
                if (popupRef[0] != null) popupRef[0].dismiss();
                typeString(accent);
            });
            row.addView(btn);
        }

        row.measure(
            View.MeasureSpec.makeMeasureSpec(anchor.getRootView().getWidth(), View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        PopupWindow popup = new PopupWindow(
            row,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        popupRef[0] = popup;

        int yOffset = -(row.getMeasuredHeight() + anchor.getHeight() + 12);
        popup.showAsDropDown(anchor, 0, yOffset, Gravity.START);
    }

    // ── Special chars popup (via !# button) ───────────────────────────────────
    private void showSpecialCharsPopup(View anchor) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(10);
        grid.setBackgroundColor(Color.parseColor("#EE111113"));
        grid.setPadding(8, 10, 8, 10);

        final PopupWindow[] popupRef = {null};

        for (String ch : SPECIAL_CHARS) {
            Button btn = new Button(this);
            btn.setText(ch);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(16f);
            btn.setTypeface(null, Typeface.BOLD);
            btn.setBackgroundColor(Color.parseColor("#FF2C2C2E"));
            btn.setStateListAnimator(null);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width  = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(4, 5, 4, 5);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(vv -> {
                // Dismiss first, then type — prevents touch-through to keyboard
                if (popupRef[0] != null) popupRef[0].dismiss();
                if (ch.length() == 1) typeChar(ch.charAt(0));
                else typeString(ch);
            });
            grid.addView(btn);
        }

        grid.measure(
            View.MeasureSpec.makeMeasureSpec(anchor.getRootView().getWidth(), View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        PopupWindow popup = new PopupWindow(
            grid,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        popupRef[0] = popup;

        int yOffset = -(grid.getMeasuredHeight() + anchor.getHeight() + 8);
        popup.showAsDropDown(anchor, 0, yOffset);
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
        if (tvBuffer != null) {
            tvBuffer.setText("");
            updateBufferDisplay();
        }
    }
}
