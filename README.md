# AURA TV

Base nativa para Android y Android TV. Incluye Inicio, TV en vivo, Películas, Series, Favoritos y Buscar; navegación con control remoto; superficie de reproducción a pantalla completa; y un punto de extensión para el historial de progreso.

## Protección de contenido

La aplicación ahora carga la fuente M3U HTTPS confirmada por el titular: `https://iptv-org.github.io/iptv/index.m3u`. Los enlaces de reproducción se aceptan únicamente si usan HTTPS; los demás se descartan para mantener el tráfico cifrado. Solo conectá una fuente cuando tengas autorización expresa para usar y distribuir su contenido.

## Ejecutar

1. Abrí esta carpeta con Android Studio.
2. Instalá/seleccioná JDK 17 y Android SDK 35.
3. Esperá la sincronización y ejecutá el módulo `app` en un emulador o dispositivo Android TV.

## Publicar en GitHub

Esta sesión no tiene una cuenta de GitHub autenticada, por lo que no se creó un remoto ni se pudo verificar si ya existe `aura-tv` en tu cuenta. Cuando tengas la URL del repositorio confirmado:

```powershell
git init
git add .
git commit -m "Initial AURA TV foundation"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/aura-tv.git
git push -u origin main
```

## Próxima integración

Las tarjetas de TV en vivo se crean desde la lista M3U y abren `FullScreenPlayer` a pantalla completa. El reproductor usa Media3/ExoPlayer con soporte HLS y muestra un aviso si una transmisión deja de estar disponible. La siguiente iteración puede añadir filtros por país/categoría, favoritos persistentes y el historial de progreso.
