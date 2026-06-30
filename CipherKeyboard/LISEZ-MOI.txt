========================================
 CipherKeyboard — Installation en 3 étapes
========================================

ETAPE 1 — Ouvrir dans Android Studio
  - Télécharge Android Studio gratuitement : https://developer.android.com/studio
  - Ouvre Android Studio → "Open" → sélectionne ce dossier CipherKeyboard
  - Attends que Gradle finisse de sync (2-3 min, barre en bas)

ETAPE 2 — Construire l'APK
  - Menu : Build → Build Bundle(s)/APK(s) → Build APK(s)
  - Attends ~1 minute
  - Clique "locate" dans la notif en bas → ton APK est là !
  - L'APK se trouve dans : app/build/outputs/apk/debug/app-debug.apk

ETAPE 3 — Installer sur le téléphone
  - Transfère app-debug.apk sur ton téléphone (USB ou Drive)
  - Sur le téléphone : Paramètres → "Sources inconnues" → Activer
  - Ouvre le fichier APK et installe
  - Lance l'app CipherKeyboard → suis les instructions à l'écran

========================================
 MODES DISPONIBLES
========================================

Normal       — frappe classique
Base64 ▲    — encoder en Base64
Base64 ▼    — décoder depuis Base64
Base32 ▲    — encoder en Base32
Base32 ▼    — décoder depuis Base32
Hex ▲       — encoder en hexadécimal
Hex ▼       — décoder depuis hex
Binaire ▲   — convertir en binaire (01001000...)
Binaire ▼   — décoder depuis binaire
ASCII→N°    — 'A' → 65  (char vers nombre ASCII)
N°→ASCII    — 65 → 'A'  (nombre vers char)
Texte→N°    — 'oo' → 15-15  (a=1, b=2 ... z=26)
N°→Texte    — 15-15 → 'oo'
ROT13       — chiffrement ROT13
ROT47       — chiffrement ROT47
César+3     — chiffrement de César décalage 3
Morse ▲     — texte vers morse (... --- ...)
Morse ▼     — morse vers texte
Atbash      — a↔z, b↔y, etc.
Inverser    — "hello" → "olleh"
MAJUSC      — tout en majuscules
minusc      — tout en minuscules

========================================
 COMMENT UTILISER
========================================

1. Dans n'importe quelle app (WhatsApp, SMS, etc.)
2. Tape ton texte dans la zone verte en haut
3. Choisis le mode (barre bleue)
4. Tu vois la conversion en temps réel dans la zone verte
5. Appuie sur ENVOYER → le texte converti est inséré !

========================================
