@echo off
echo ╔════════════════════════════════════════════╗
echo ║     GESTIONNAIRE DE RESEAU ELECTRIQUE     ║
echo ╚════════════════════════════════════════════╝
echo.
echo [1/2] Compilation en cours...
javac -encoding UTF-8 -d bin -cp bin src/up/mi/paa/util/*.java src/up/mi/paa/model/*.java src/up/mi/paa/exception/*.java src/up/mi/paa/io/*.java src/up/mi/paa/service/*.java src/up/mi/paa/ui/cli/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Echec de la compilation.
    pause
    exit /b 1
)
echo [OK] Compilation reussie !
echo.

:: Lancement
echo [2/2] Lancement du CLI...
echo.

:: Vérifier si un fichier est passé en argument
if "%~1"=="" (
    java -cp bin up.mi.paa.Main --cli
) else (
    java -cp bin up.mi.paa.Main %*
)

pause
