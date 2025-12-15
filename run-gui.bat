@echo off
echo [1/2] Compilation en cours...
javac -d bin -cp bin src/up/mi/paa/model/*.java
javac -d bin -cp bin src/up/mi/paa/exception/*.java
javac -d bin -cp bin src/up/mi/paa/service/*.java
javac -d bin -cp bin src/up/mi/paa/io/*.java
javac -d bin -cp bin src/up/mi/paa/ui/cli/*.java
javac -d bin --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin src/up/mi/paa/ui/gui/components/*.java
javac -d bin --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin src/up/mi/paa/ui/gui/*.java
javac -d bin --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin src/up/mi/paa/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Echec de la compilation.
    pause
    exit /b 1
)
echo [OK] Compilation reussie !
echo.

:: Lancement
echo [2/2] Lancement du GUI...
@echo off
java --module-path libs/windows/lib --add-modules javafx.controls,javafx.fxml -cp bin up.mi.paa.ui.gui.ReseauElectriqueUI
@echo off

pause
