@echo off
echo ╔════════════════════════════════════════════╗
echo ║     GESTIONNAIRE DE RESEAU ELECTRIQUE     ║
echo ╚════════════════════════════════════════════╝
echo.
echo [1/2] Compilation en cours...
javac -encoding UTF-8 -d bin --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin src/up/mi/paa/util/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/io/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/cli/*.java src/up/mi/paa/ui/gui/components/*.java src/up/mi/paa/ui/gui/*.java src/up/mi/paa/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Echec de la compilation.
    pause
    exit /b 1
)
echo [OK] Compilation reussie !
echo.

:: Lancement
echo [2/2] Lancement du GUI...
java --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics --add-opens=java.base/java.lang=ALL-UNNAMED -cp bin up.mi.paa.Main --gui

pause
