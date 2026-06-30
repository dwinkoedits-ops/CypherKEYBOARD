package com.cipherkeyboard;

import android.util.Base64;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CipherUtils {

    // ── Mode constants ──────────────────────────────────────────────────────
    public static final String MODE_NORMAL       = "Normal";
    public static final String MODE_BASE64_ENC   = "Base64 ▲";
    public static final String MODE_BASE64_DEC   = "Base64 ▼";
    public static final String MODE_BASE32_ENC   = "Base32 ▲";
    public static final String MODE_BASE32_DEC   = "Base32 ▼";
    public static final String MODE_HEX_ENC      = "Hex ▲";
    public static final String MODE_HEX_DEC      = "Hex ▼";
    public static final String MODE_BINARY_ENC   = "Binaire ▲";
    public static final String MODE_BINARY_DEC   = "Binaire ▼";
    public static final String MODE_URL_ENC      = "URL ▲";
    public static final String MODE_URL_DEC      = "URL ▼";
    public static final String MODE_ASCII_CHR    = "ASCII→N°";
    public static final String MODE_ASCII_NUM    = "N°→ASCII";
    public static final String MODE_TEXT_NUM     = "Texte→N°";
    public static final String MODE_NUM_TEXT     = "N°→Texte";
    public static final String MODE_ROT13        = "ROT13";
    public static final String MODE_ROT47        = "ROT47";
    public static final String MODE_ROT5         = "ROT5";
    public static final String MODE_CAESAR3      = "César+3";
    public static final String MODE_MORSE_ENC    = "Morse ▲";
    public static final String MODE_MORSE_DEC    = "Morse ▼";
    public static final String MODE_NATO         = "NATO";
    public static final String MODE_ATBASH       = "Atbash";
    public static final String MODE_VIGENERE_ENC = "Vigenère ▲";
    public static final String MODE_VIGENERE_DEC = "Vigenère ▼";
    public static final String MODE_RAIL_ENC     = "Rail ▲";
    public static final String MODE_RAIL_DEC     = "Rail ▼";
    public static final String MODE_LEET         = "L33t";
    public static final String MODE_PIG_LATIN    = "Pig Latin";
    public static final String MODE_REVERSE      = "Inverser";
    public static final String MODE_REVERSE_WORDS = "Mots↩";
    public static final String MODE_TITLE_CASE   = "Titre";
    public static final String MODE_CAMEL_CASE   = "camelCase";
    public static final String MODE_SNAKE_CASE   = "snake_case";
    public static final String MODE_ALT_CASE     = "aLtErNe";
    public static final String MODE_UPPER        = "MAJUSC";
    public static final String MODE_LOWER        = "minusc";

    // ── All modes listed in display order ───────────────────────────────────
    public static final String[] ALL_MODES = {
        MODE_NORMAL,
        MODE_BASE64_ENC,   MODE_BASE64_DEC,
        MODE_BASE32_ENC,   MODE_BASE32_DEC,
        MODE_HEX_ENC,      MODE_HEX_DEC,
        MODE_BINARY_ENC,   MODE_BINARY_DEC,
        MODE_URL_ENC,      MODE_URL_DEC,
        MODE_ASCII_CHR,    MODE_ASCII_NUM,
        MODE_TEXT_NUM,     MODE_NUM_TEXT,
        MODE_ROT13,        MODE_ROT47,        MODE_ROT5,
        MODE_CAESAR3,
        MODE_MORSE_ENC,    MODE_MORSE_DEC,
        MODE_NATO,
        MODE_ATBASH,
        MODE_VIGENERE_ENC, MODE_VIGENERE_DEC,
        MODE_RAIL_ENC,     MODE_RAIL_DEC,
        MODE_LEET,
        MODE_PIG_LATIN,
        MODE_REVERSE,      MODE_REVERSE_WORDS,
        MODE_TITLE_CASE,   MODE_CAMEL_CASE,   MODE_SNAKE_CASE,
        MODE_ALT_CASE,     MODE_UPPER,        MODE_LOWER
    };

    // ── Morse tables ─────────────────────────────────────────────────────────
    private static final Map<Character, String> MORSE_ENC_MAP = new HashMap<>();
    private static final Map<String, Character> MORSE_DEC_MAP = new HashMap<>();

    static {
        String[][] table = {
            {"a",".-"},  {"b","-..."},{"c","-.-."},{"d","-.."},{"e","."},
            {"f","..-."},{"g","--."},{"h","...."},{"i",".."},{"j",".---"},
            {"k","-.-"},{"l",".-.."},{"m","--"},{"n","-."},{"o","---"},
            {"p",".--."},{"q","--.-"},{"r",".-."},{"s","..."},{"t","-"},
            {"u","..-"},{"v","...-"},{"w",".--"},{"x","-..-"},{"y","-.--"},
            {"z","--.."},
            {"0","-----"},{"1",".----"},{"2","..---"},{"3","...--"},
            {"4","....-"},{"5","....."},{"6","-...."},{"7","--..."},
            {"8","---.."},{"9","----."}
        };
        for (String[] p : table) {
            MORSE_ENC_MAP.put(p[0].charAt(0), p[1]);
            MORSE_DEC_MAP.put(p[1], p[0].charAt(0));
        }
    }

    // ── NATO phonetic alphabet ────────────────────────────────────────────────
    private static final Map<Character, String> NATO_MAP = new HashMap<>();
    static {
        String[][] nato = {
            {"a","Alpha"},{"b","Bravo"},{"c","Charlie"},{"d","Delta"},
            {"e","Echo"},{"f","Foxtrot"},{"g","Golf"},{"h","Hotel"},
            {"i","India"},{"j","Juliet"},{"k","Kilo"},{"l","Lima"},
            {"m","Mike"},{"n","November"},{"o","Oscar"},{"p","Papa"},
            {"q","Quebec"},{"r","Romeo"},{"s","Sierra"},{"t","Tango"},
            {"u","Uniform"},{"v","Victor"},{"w","Whiskey"},{"x","X-ray"},
            {"y","Yankee"},{"z","Zulu"},
            {"0","Zero"},{"1","One"},{"2","Two"},{"3","Three"},{"4","Four"},
            {"5","Five"},{"6","Six"},{"7","Seven"},{"8","Eight"},{"9","Nine"}
        };
        for (String[] p : nato) NATO_MAP.put(p[0].charAt(0), p[1]);
    }

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    // ── Main dispatcher ───────────────────────────────────────────────────────
    public static String convert(String input, String mode) {
        if (input == null || input.isEmpty()) return "";
        try {
            switch (mode) {
                case MODE_NORMAL:        return input;
                case MODE_BASE64_ENC:    return base64Encode(input);
                case MODE_BASE64_DEC:    return base64Decode(input);
                case MODE_BASE32_ENC:    return base32Encode(input);
                case MODE_BASE32_DEC:    return base32Decode(input);
                case MODE_HEX_ENC:       return hexEncode(input);
                case MODE_HEX_DEC:       return hexDecode(input);
                case MODE_BINARY_ENC:    return binaryEncode(input);
                case MODE_BINARY_DEC:    return binaryDecode(input);
                case MODE_URL_ENC:       return URLEncoder.encode(input, "UTF-8").replace("+", "%20");
                case MODE_URL_DEC:       return URLDecoder.decode(input, "UTF-8");
                case MODE_ASCII_CHR:     return asciiToNumbers(input);
                case MODE_ASCII_NUM:     return numbersToAscii(input);
                case MODE_TEXT_NUM:      return textToAlphaNumbers(input);
                case MODE_NUM_TEXT:      return alphaNumbersToText(input);
                case MODE_ROT13:         return rot13(input);
                case MODE_ROT47:         return rot47(input);
                case MODE_ROT5:          return rot5(input);
                case MODE_CAESAR3:       return caesar(input, 3);
                case MODE_MORSE_ENC:     return morseEncode(input);
                case MODE_MORSE_DEC:     return morseDecode(input);
                case MODE_NATO:          return natoEncode(input);
                case MODE_ATBASH:        return atbash(input);
                case MODE_VIGENERE_ENC:  return vigenere(input, true);
                case MODE_VIGENERE_DEC:  return vigenere(input, false);
                case MODE_RAIL_ENC:      return railFenceEncode(input, 3);
                case MODE_RAIL_DEC:      return railFenceDecode(input, 3);
                case MODE_LEET:          return leet(input);
                case MODE_PIG_LATIN:     return pigLatin(input);
                case MODE_REVERSE:       return new StringBuilder(input).reverse().toString();
                case MODE_REVERSE_WORDS: return reverseWords(input);
                case MODE_TITLE_CASE:    return titleCase(input);
                case MODE_CAMEL_CASE:    return camelCase(input);
                case MODE_SNAKE_CASE:    return snakeCase(input);
                case MODE_ALT_CASE:      return alternatingCase(input);
                case MODE_UPPER:         return input.toUpperCase();
                case MODE_LOWER:         return input.toLowerCase();
                default:                 return input;
            }
        } catch (Exception e) {
            return "[ERREUR: " + e.getMessage() + "]";
        }
    }

    // ── Base64 ───────────────────────────────────────────────────────────────
    private static String base64Encode(String s) {
        return Base64.encodeToString(s.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    private static String base64Decode(String s) {
        byte[] decoded = Base64.decode(s.trim(), Base64.NO_WRAP);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    // ── Base32 ───────────────────────────────────────────────────────────────
    private static String base32Encode(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int buf = 0, bitsLeft = 0;
        for (byte b : data) {
            buf = (buf << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_CHARS.charAt((buf >> bitsLeft) & 31));
            }
        }
        if (bitsLeft > 0) sb.append(BASE32_CHARS.charAt((buf << (5 - bitsLeft)) & 31));
        while (sb.length() % 8 != 0) sb.append('=');
        return sb.toString();
    }

    private static String base32Decode(String s) {
        s = s.toUpperCase().replace("=", "");
        // Max output size: 5 bits per char, 8 bits per byte → ceil(len*5/8)
        byte[] output = new byte[(s.length() * 5 + 7) / 8];
        int buf = 0, bitsLeft = 0, idx = 0;
        for (char c : s.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buf = (buf << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output[idx++] = (byte) ((buf >> bitsLeft) & 0xFF);
            }
        }
        return new String(output, 0, idx, StandardCharsets.UTF_8);
    }

    // ── Hex ──────────────────────────────────────────────────────────────────
    private static String hexEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8))
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private static String hexDecode(String s) {
        s = s.replaceAll("\\s", "");
        if (s.length() % 2 != 0) throw new IllegalArgumentException("Longueur hex invalide");
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ── Binary ────────────────────────────────────────────────────────────────
    private static String binaryEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }

    private static String binaryDecode(String s) {
        String[] parts = s.trim().split("\\s+");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++)
            bytes[i] = (byte) Integer.parseInt(parts[i], 2);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ── ASCII ─────────────────────────────────────────────────────────────────
    private static String asciiToNumbers(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append((int) c);
        }
        return sb.toString();
    }

    private static String numbersToAscii(String s) {
        StringBuilder sb = new StringBuilder();
        for (String token : s.trim().split("\\s+"))
            if (!token.isEmpty()) sb.append((char) Integer.parseInt(token));
        return sb.toString();
    }

    // ── Alpha numbers (a=1…z=26) ─────────────────────────────────────────────
    private static String textToAlphaNumbers(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                if (sb.length() > 0) sb.append('-');
                sb.append(c - 'a' + 1);
            } else if (c == ' ') {
                sb.append("/ ");
            }
        }
        return sb.toString();
    }

    private static String alphaNumbersToText(String s) {
        StringBuilder sb = new StringBuilder();
        for (String part : s.trim().split("-")) {
            part = part.trim();
            if (part.equals("/")) {
                sb.append(' ');
            } else if (!part.isEmpty()) {
                try {
                    int n = Integer.parseInt(part);
                    sb.append((n >= 1 && n <= 26) ? (char) ('a' + n - 1) : '?');
                } catch (NumberFormatException e) {
                    sb.append(part);
                }
            }
        }
        return sb.toString();
    }

    // ── ROT ciphers ───────────────────────────────────────────────────────────
    private static String rot13(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if      (c >= 'a' && c <= 'z') sb.append((char) ((c - 'a' + 13) % 26 + 'a'));
            else if (c >= 'A' && c <= 'Z') sb.append((char) ((c - 'A' + 13) % 26 + 'A'));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String rot47(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray())
            sb.append((c >= '!' && c <= '~') ? (char) ((c - '!' + 47) % 94 + '!') : c);
        return sb.toString();
    }

    // ROT5: rotates digits only (0-9 → 5-4)
    private static String rot5(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray())
            sb.append((c >= '0' && c <= '9') ? (char) ((c - '0' + 5) % 10 + '0') : c);
        return sb.toString();
    }

    private static String caesar(String s, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if      (c >= 'a' && c <= 'z') sb.append((char) ((c - 'a' + shift) % 26 + 'a'));
            else if (c >= 'A' && c <= 'Z') sb.append((char) ((c - 'A' + shift) % 26 + 'A'));
            else sb.append(c);
        }
        return sb.toString();
    }

    // ── Morse ─────────────────────────────────────────────────────────────────
    private static String morseEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            if (c == ' ') {
                sb.append("/ ");
            } else {
                String code = MORSE_ENC_MAP.get(c);
                if (code != null) {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') sb.append(' ');
                    sb.append(code);
                }
            }
        }
        return sb.toString().trim();
    }

    private static String morseDecode(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.trim().split(" / ")) {
            for (String symbol : word.trim().split("\\s+")) {
                if (!symbol.isEmpty()) {
                    Character c = MORSE_DEC_MAP.get(symbol);
                    sb.append(c != null ? c : '?');
                }
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    // ── NATO phonetic ─────────────────────────────────────────────────────────
    private static String natoEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toLowerCase().toCharArray()) {
            if (c == ' ') {
                sb.append("/ ");
            } else {
                String word = NATO_MAP.get(c);
                if (word != null) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(word);
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString().trim();
    }

    // ── Atbash ────────────────────────────────────────────────────────────────
    private static String atbash(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if      (c >= 'a' && c <= 'z') sb.append((char) ('z' - (c - 'a')));
            else if (c >= 'A' && c <= 'Z') sb.append((char) ('Z' - (c - 'A')));
            else sb.append(c);
        }
        return sb.toString();
    }

    // ── Vigenère (format: KEY|message) ────────────────────────────────────────
    private static String vigenere(String input, boolean encode) {
        int sep = input.indexOf('|');
        if (sep < 0) return "[Vigenère: tape CLE|message]";
        String key = input.substring(0, sep).toUpperCase().replaceAll("[^A-Z]", "");
        String text = input.substring(sep + 1);
        if (key.isEmpty()) return "[Vigenère: clé vide]";

        StringBuilder sb = new StringBuilder();
        int ki = 0;
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int shift = key.charAt(ki % key.length()) - 'A';
                sb.append((char) ((encode ? (c - 'a' + shift) : (c - 'a' - shift + 26)) % 26 + 'a'));
                ki++;
            } else if (c >= 'A' && c <= 'Z') {
                int shift = key.charAt(ki % key.length()) - 'A';
                sb.append((char) ((encode ? (c - 'A' + shift) : (c - 'A' - shift + 26)) % 26 + 'A'));
                ki++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ── Rail Fence (3 rails) ──────────────────────────────────────────────────
    private static String railFenceEncode(String s, int rails) {
        if (rails < 2 || s.length() < rails) return s;
        char[][] fence = new char[rails][s.length()];
        for (char[] row : fence) Arrays.fill(row, '\0');
        int rail = 0;
        boolean down = true;
        for (int i = 0; i < s.length(); i++) {
            fence[rail][i] = s.charAt(i);
            if (rail == rails - 1) down = false;
            else if (rail == 0)    down = true;
            rail += down ? 1 : -1;
        }
        StringBuilder sb = new StringBuilder();
        for (char[] row : fence)
            for (char c : row)
                if (c != '\0') sb.append(c);
        return sb.toString();
    }

    private static String railFenceDecode(String s, int rails) {
        if (rails < 2 || s.length() < rails) return s;
        int[] lengths = new int[rails];
        int rail = 0;
        boolean down = true;
        for (int i = 0; i < s.length(); i++) {
            lengths[rail]++;
            if (rail == rails - 1) down = false;
            else if (rail == 0)    down = true;
            rail += down ? 1 : -1;
        }
        String[] railStr = new String[rails];
        int pos = 0;
        for (int r = 0; r < rails; r++) {
            railStr[r] = s.substring(pos, pos + lengths[r]);
            pos += lengths[r];
        }
        int[] indices = new int[rails];
        StringBuilder sb = new StringBuilder();
        rail = 0;
        down = true;
        for (int i = 0; i < s.length(); i++) {
            sb.append(railStr[rail].charAt(indices[rail]++));
            if (rail == rails - 1) down = false;
            else if (rail == 0)    down = true;
            rail += down ? 1 : -1;
        }
        return sb.toString();
    }

    // ── Leet speak ────────────────────────────────────────────────────────────
    private static String leet(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (Character.toLowerCase(c)) {
                case 'a': sb.append('4');  break;
                case 'e': sb.append('3');  break;
                case 'i': sb.append('1');  break;
                case 'o': sb.append('0');  break;
                case 't': sb.append('7');  break;
                case 's': sb.append('5');  break;
                case 'b': sb.append('8');  break;
                case 'g': sb.append('9');  break;
                case 'l': sb.append('|');  break;
                case 'z': sb.append('2');  break;
                default:  sb.append(c);    break;
            }
        }
        return sb.toString();
    }

    // ── Pig Latin ─────────────────────────────────────────────────────────────
    private static String pigLatin(String s) {
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(pigLatinWord(word));
        }
        return sb.toString();
    }

    private static String pigLatinWord(String word) {
        if (word.isEmpty()) return word;
        String lower = word.toLowerCase();
        String vowels = "aeiou";
        if (vowels.indexOf(lower.charAt(0)) >= 0) {
            return word + "way";
        }
        int i = 0;
        while (i < lower.length() && vowels.indexOf(lower.charAt(i)) < 0) i++;
        return word.substring(i) + word.substring(0, i) + "ay";
    }

    // ── Case transforms ───────────────────────────────────────────────────────
    private static String reverseWords(String s) {
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(words[i]);
        }
        return sb.toString();
    }

    private static String titleCase(String s) {
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String camelCase(String s) {
        String[] words = s.trim().split("\\s+");
        if (words.length == 0) return "";
        StringBuilder sb = new StringBuilder(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            if (!words[i].isEmpty())
                sb.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String snakeCase(String s) {
        return s.toLowerCase().trim().replaceAll("\\s+", "_");
    }

    private static String alternatingCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = !upper;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
