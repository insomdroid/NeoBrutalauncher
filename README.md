# Neo Brutal Launcher

Neo Brutal Launcher is an open-source Android launcher with a bold neo-brutalist design, high-contrast tiles, and a straightforward app drawer.

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/license-MIT-111111)](LICENSE)
[![Build](https://img.shields.io/github/actions/workflow/status/insomdroid/NeoBrutalauncher/build.yml?branch=main&label=build)](https://github.com/insomdroid/NeoBrutalauncher/actions/workflows/build.yml)

## Screenshots

| Home | Apps |
|---|---|
| ![Home screen](docs/screenshots/1791727792265_100.PNG) | ![Apps screen](docs/screenshots/1791727792932_100.PNG) |

| Live | Settings |
|---|---|
| ![Live screen](docs/screenshots/1791727793765_100.PNG) | ![Settings screen](docs/screenshots/1791727795212_100.PNG) |

| Settings | Settings |
|---|---|
| ![Additional screenshot 1](docs/screenshots/1791727795860_100.PNG) | ![Additional screenshot 2](docs/screenshots/1791727895442_100.PNG) |

## Features

- **Home screen:** clock, date, quotes, app tiles, battery and network information, optional weather, and music controls.
- **Customizable tiles:** arrange app tiles on the Home screen and adjust their sizes.
- **Apps page:** search installed apps, browse alphabetically, and launch apps quickly.
- **Live page:** view useful live information, notification summaries, media controls, quotes, and optional weather.
- **Appearance:** light, dark, or system theme, with wallpaper and typography options.
- **Convenient gestures:** double-tap the Home clock to open Settings and long-press it to open Apps.

## Requirements and permissions

- Android 8.0 (API 26) or newer.
- Location permission is optional and used for weather.
- Notification access is optional and used by notification and media features.

## Build and download

Neo Brutal Launcher is built with Kotlin, Jetpack Compose, and Material 3.

- Minimum SDK: **26 (Android 8.0)**
- Compile/target SDK: **36**
- JDK: **17**

GitHub Actions builds the release APK when changes are pushed to `main`, when a pull request targets `main`, or when the workflow is started manually. The generated APK is checked and uploaded as an artifact named `NeoBrutalauncher-release`, available for 30 days.

- [Builds and APK artifacts](https://github.com/insomdroid/NeoBrutalauncher/actions)
- [Build workflow](.github/workflows/build.yml)

## Support the project

If you find Neo Brutal Launcher useful, you can support its development on Ko-fi.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/N4W528LMGI)

## Project structure

- `app/` — Android application source.
- `docs/screenshots/` — project screenshots displayed above.
- `.github/workflows/build.yml` — APK build workflow.

## License

MIT License. See [LICENSE](LICENSE).
