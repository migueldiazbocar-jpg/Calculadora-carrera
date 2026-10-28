# Calculadora de carrera

App Android (Kotlin + Jetpack Compose + Material 3, minSdk 26). Introduce dos de los tres datos (distancia, tiempo, ritmo) y calcula el que falta. Incluye distancias predefinidas (5K, 10K, media maratón y maratón), velocidad media y tiempos estimados.

## Estructura

```
app/src/main/java/com/calculadoracarrera/app/
├── MainActivity.kt
├── domain/
│   ├── RaceCalculator.kt   ← lógica pura (cálculo, validación, formatos)
│   └── InputRules.kt       ← reglas de las casillas
└── ui/
    ├── RaceCalculatorViewModel.kt
    ├── RaceCalculatorScreen.kt
    └── theme/Theme.kt
app/src/test/...            ← tests unitarios (JUnit 4)
```

## Abrir y ejecutar en el móvil

1. Instala Android Studio (versión Ladybug o posterior).
2. Descomprime el ZIP y en Android Studio elige **File › Open** y selecciona la carpeta `CalculadoraCarrera`. La primera vez descargará Gradle y las dependencias (unos minutos).
3. En el móvil, activa las **Opciones de desarrollador** (Ajustes › Información del teléfono › pulsa 7 veces en «Número de compilación») y dentro de ellas la **Depuración USB**.
4. Conecta el móvil por USB, acepta el aviso de depuración y pulsa **Run ▶** en Android Studio.

## Pasar los tests

En Android Studio: clic derecho sobre `app/src/test` › **Run 'Tests in…'**. O en la terminal:

```
./gradlew testDebugUnitTest
```

## Generar el APK para instalar

- Rápido (APK de depuración): **Build › Build App Bundle(s) / APK(s) › Build APK(s)**. Quedará en `app/build/outputs/apk/debug/app-debug.apk`. Pásalo al móvil y ábrelo (tendrás que permitir «instalar apps de origen desconocido»).
- Definitivo (firmado): **Build › Generate Signed App Bundle or APK › APK**, crea un *keystore* nuevo, elige `release` y guarda bien el keystore y sus contraseñas.

## Sin Android Studio: GitHub Actions

Si subes la carpeta a un repositorio de GitHub, el flujo `.github/workflows/build.yml` compila, pasa los tests y deja el APK descargable en la pestaña **Actions** › la ejecución › *Artifacts*.
