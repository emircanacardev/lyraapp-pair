# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi

- Seçim*: **Hilt**

- Son Güncelleme Tarihi*: 04.06.2026

- Alternatifler: **Koin**

- Sebep: **Opsiyonel**


### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 09.06.2026

- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5** (version catalog: `navigationCompose`).

- Uygulama: Tek `NavHost` (`ui/navigation/LyraNavHost.kt`) Auth grafiğini barındırır (başlangıç
  hedefi Login). Navigasyon MVI ile uyumlu kurulur: ViewModel'de navigasyon API'si yoktur
  (bkz. [architecture/mvi-viewmodel-rules.md](architecture/mvi-viewmodel-rules.md) §6); navigasyon
  `Intent → Effect` üzerinden akar, `Route` Effect'i tüketip `NavHost`'tan gelen lambda'ları çağırır.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 09.06.2026

- Sürümler: Hilt **2.59.2**, KSP **2.2.10-2.0.2** (Kotlin 2.2.10 ile birebir uyumlu).

- Compose'da ViewModel: `androidx.hilt:hilt-lifecycle-viewmodel-compose` (`hiltViewModel()`).
  Compose Navigation henüz kurulmadığından navigation-compose bağımlılığı eklenmemiştir.

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.2 ile uyumludur.


### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunludur.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak
  olmadan derlemeyi kırar. Bayrak deneysel (experimental) olarak işaretlidir ancak gereklidir.


### Alt Gezinme Çubuğu (Bottom Navigation Bar)

- Seçim: **Material 3 `NavigationBar`** — tek `NavHost` + iskelet seviyesinde tek dış `Scaffold`.

- Son Güncelleme Tarihi: 11.06.2026

- Uygulama: `ui/navigation/LyraBottomBar.kt` (bileşen + `LyraBottomBarTab` sekme tanımları) ve
  `ui/navigation/LyraNavHost.kt` (Scaffold `bottomBar` entegrasyonu). Çubuk yalnızca üst düzey
  sekme rotalarında görünür (Auth ekranlarında gizli); böylece her ana sayfanın altında otomatik
  yer alır. Sekme geçişi standart desenle yapılır: `popUpTo(Home) { saveState = true }` +
  `launchSingleTop` + `restoreState`. Dış Scaffold'ın `contentWindowInsets`'i sıfırdır; sistem
  çubuğu boşluklarını ekranlar kendisi yönetir, içerik dolgusu yalnızca alt çubuk yüksekliğini taşır.

- MVI kapsamı: BNB navigasyon iskeletidir (chrome), feature ekranı değildir; State/Intent/Effect
  sözleşmesi yoktur. Seçili sekme `currentBackStackEntryAsState()` ile nav back stack'ten türetilir
  (tek doğruluk kaynağı back stack'tir). Sekme ekranları MVI ile yazıldığında yalnızca
  `LyraNavHost` içindeki geçici placeholder rotaları gerçek `Route`'lara bağlanacaktır.

- Sebep: Tek doğruluk kaynağı (back stack) ile durum tekrarına yer bırakmaz; sekme başına ayrı
  `NavHost`/ViewModel karmaşıklığından kaçınılır; mevcut Auth grafiği değişmeden korunur.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API
  geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.


### Kütüphane Ekranı

- Seçim: Tam MVI implementasyonu (Contract + ViewModel + Route/Screen); backend bağlantısı yok.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama: `ui/library/` — `LibraryContract.kt`, `LibraryViewModel.kt`, `LibraryScreen.kt`.
  Static playlist listesi ViewModel içinde private fonksiyon (`staticPlaylists()`) olarak tutulur;
  bu ekran UI-only gereksinim olduğundan stub repository eklenmedi. Filtre seçimi (`LibraryFilter`)
  `LibraryUiState` içinde tutulur; `LibraryIntent.FilterSelected` ile güncellenir. Thumbnail rengi
  `PlaylistItem.thumbnailColor: Long` (ARGB hex) alanında saklanır, composable katmanında
  `Color(long)` ile dönüştürülür. `LyraNavHost.kt` içindeki `PlaceholderScreen("Kütüphane")`
  kaldırılarak `LibraryRoute()` ile değiştirildi. `LyraIcons.kt`'ye 6 yeni ikon eklendi:
  `Check`, `Add`, `MoreVert`, `PushPin`, `SwapVert`, `GridView`.

- Sebep: Bottom navigation bar'daki Kütüphane sekmesinin tasarım implementasyonu.


### Playlist Detail ve Yeni Çalma Listesi Ekranları

- Seçim: Tam MVI implementasyonu; her biri Contract + ViewModel + Route/Screen; backend bağlantısı yok.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama:
  - `ui/library/detail/` — PlaylistDetail ekranı. `PlaylistDetailViewModel`, `SavedStateHandle["playlistId"]`
    ile navigation argümanını alır; `staticDetail(playlistId)` fonksiyonu eşleşen playlist verisini döner.
    Rota: `LyraDestination.PlaylistDetail("library/detail/{playlistId}")`; Compose Navigation `navArgument`
    ile `NavType.StringType` olarak tanımlanır. `playlistDetailRoute(id)` yardımcı fonksiyonu navigate çağrısı
    için rota string'ini üretir.
  - `ui/library/create/` — CreatePlaylist ekranı. `isSaveEnabled` türetilmiş alan; yalnızca `name` boş
    değilse `true`. Şarkı seçimi `Set<String>` üzerinden toggle edilir. `Switch` ile gizlilik ayarı.
    `BasicTextField` + `HorizontalDivider` ile alt çizgili text alanları.
  - `LibraryRoute`'a `onOpenPlaylist` ve `onCreatePlaylist` lambda'ları eklendi;
    `LibraryEffect.NavigateToCreatePlaylist` effect'i de `LibraryContract`'a eklendi.
  - `LyraIcons.kt`'ye 6 yeni ikon eklendi: `PlayArrow`, `Download`, `Shuffle`, `Edit`, `Public`, `Close`.

- Sebep: Kütüphane ekranından playlist detayına ve yeni liste oluşturma akışına tasarım implementasyonu.


### Beğenilen Şarkılar Varyantı — PlaylistDetail Ekranı

- Karar: `PlaylistDetailUiState`'e `isLikedSongs: Boolean = false` alanı eklendi; header composable'ı
  koşullu hale getirildi: `isLikedSongs = true` ise `LikedSongsHeader`, değilse mevcut `PlaylistHeader`.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama: `LikedSongsHeader` — 120dp sol hizalı thumbnail (pembe bg + kalp ikonu) + sağında başlık/meta.
  `LikedSongsActionRow` — tam genişlik hap şeklinde "Çal" butonu (primary bg) + 48dp kare Shuffle +
  Download icon button'ları (surfaceContainerHigh bg). Tüm şarkılar `isLiked = true`. Statik veri:
  playlistId "1", coverColor 0xFFE91E8CL, 5 şarkı, `isLikedSongs = true`.

- Sebep: Beğenilen Şarkılar özel bir sistem listesidir; farklı görsel hiyerarşi (küçük sol thumbnail +
  geniş aksiyon butonu) gerektirdiğinden ayrı composable olarak modellendi.


### Ana Sayfa Gerçek API Entegrasyonu

- Karar: `DefaultHomeRepository` tüm statik/mock veri kaldırılarak gerçek API'lere bağlandı.

- Son Güncelleme Tarihi: 23.06.2026

- Uygulama:
  - `songs` → `GET /api/v1/songs` (önceden de bağlıydı)
  - `recentlyPlayed` → `GET /api/v1/me/recently-played`
  - `quickPicks` → `GET /api/v1/me/recommendations`
  - `playlistsForYou` → `GET /api/v1/me/for-you`
  - `userInitials` → `GET /api/v1/me`
  - Tüm 5 çağrı `coroutineScope { async {} }` ile paralel atılır.
  - Auth gerektiren 4 çağrı başarısız olursa (401 — token yok) boş liste döner; yalnızca
    `GET /api/v1/songs` başarısız olursa tüm feed `Result.failure` döner.
  - Artwork renkleri API'de bulunmadığından `artworkColorsFor(id)` HSL hash fonksiyonu korundu.
  - `MockHomeRepository.kt` silindi.

- Sebep: Ana sayfa kullanıcıya özel ve güncel içerik göstermeli; statik veri üretim ortamında
  kabul edilemez.


### Token Saklama ve `/api/v1/me` Profil Entegrasyonu

- Karar: `TokenStorage` (DataStore) + `AuthInterceptor` (OkHttp) + `UserRepository` mimarisi.

- Son Güncelleme Tarihi: 23.06.2026

- Uygulama:
  - `data/auth/TokenStorage.kt` — `getAccessToken / saveAccessToken / clearTokens` sözleşmesi.
  - `data/auth/TokenStorageImpl.kt` — `DataStore<Preferences>` (`lyra_preferences`, anahtar `access_token`).
  - `data/network/AuthInterceptor.kt` — OkHttp `Interceptor`; token varsa `Authorization: Bearer` header ekler,
    yoksa isteği olduğu gibi iletir. `runBlocking` kullanılır (interceptor zaten arka plan thread'inde çalışır).
  - `NetworkModule.provideOkHttpClient` — `AuthInterceptor` logging interceptor'dan önce eklendi.
  - `di/AuthModule.kt` — `TokenStorage → TokenStorageImpl` Hilt bağlaması eklendi.
  - `data/user/UserDto.kt` + `MeApi.kt` + `UserRepository.kt` + `DefaultUserRepository.kt` — `GET /api/v1/me`
    veri katmanı; hata `runCatching` ile `Result.failure` olarak sarılır.
  - `di/UserModule.kt` — `MeApi` ve `UserRepository` Hilt bağlamaları.
  - `ProfileViewModel` — `UserRepository` inject edilir; `init {}` içinde `loadProfile()` çağrılır.
    Başarıda `name`, `initials`, `handle` API'den türetilir; hata durumunda `isLoading = false` ile
    varsayılan değerler korunur (tasarım bozulmaz).

- `GET /api/v1/me` dışındaki endpoint'ler (SongsApi vb.) `AuthInterceptor` üzerinden geçer;
  token null ise header eklenmez ve public endpoint'ler etkilenmez.

- Token henüz yazılmadıysa (OTP login akışı henüz gerçek API ile bağlı değil) `loadProfile()`
  çağrısı 401 döner, `Result.failure` yakalanır ve ekran statik varsayılanlarla gösterilir.
- 23.06.2026 güncellemesi: Tüm `/api/v1/me` endpoint'leri (plays, recently-played, for-you,
  recommendations, playlists CRUD, track ekleme/kaldırma) `AuthApi` + `AuthRepository` +
  `DefaultAuthRepository` üçlüsüne eklendi. `PlaylistDto` `AuthDtos.kt` içinde tanımlandı;
  şarkı listesi döndüren endpoint'ler mevcut `data.songs.SongDto`'yu yeniden kullanır.


### Offline Muzik Indirme

- Karar: Room veritabani + OkHttp akisi + yerel dosya oncelikli oynatma.

- Son Guncelleme Tarihi: 25.06.2026

- Uygulama:
  - `data/download/DownloadedSong.kt` — Room `@Entity` (songId, title, artist, localFilePath, downloadedAt),
    `@Dao` (getDownload, insertDownload, deleteDownload, observeAll) ve `@Database` (`LyraDatabase`) tek
    dosyada tanimlanir.
  - `data/download/DownloadRepository.kt` — `downloadSong`, `isDownloaded`, `getLocalFilePath`,
    `deleteDownload`, `observeDownloads` sozlesmesi.
  - `data/download/DefaultDownloadRepository.kt` — `PlayerRepository.getStreamUrl()` ile imzali URL
    alinir; `OkHttpClient` ile ses dosyasi `getExternalFilesDir(DIRECTORY_MUSIC)` altina indirilir;
    indirme basarili olursa `DownloadedSongDao.insertDownload()` ile meta-veri kayit edilir.
    Tum IO islemi `withContext(Dispatchers.IO)` icinde calisir.
  - `di/DownloadModule.kt` — Hilt `@Singleton`: `LyraDatabase`, `DownloadedSongDao` ve
    `DownloadRepository → DefaultDownloadRepository` baglama.
  - `ui/player/PlayerContract.kt` — `PlayerUiState`'e `isDownloaded: Boolean` ve `isDownloading: Boolean`
    alanlari eklendi; `PlayerIntent.DownloadSong` intenti ve `PlayerEffect.ShowMessage` effecti eklendi.
  - `ui/player/PlayerViewModel.kt` — `DownloadRepository` inject edilir. `init` bloğunda
    `checkDownloadStatus()` cagrilir. `loadAndPlay()` once `getLocalFilePath()` ile yerel dosyayi
    kontrol eder; varsa `Uri.fromFile()` ile offline oynatir, yoksa stream URL'ye duser.
    `DownloadSong` intentinde `downloadSong()` coroutine'i baslatilir; sonuc `PlayerEffect.ShowMessage`
    ile kullaniciya bildirilir.
  - `ui/player/PlayerScreen.kt` — Baslik satirinin sag tarafina indir butonu eklendi: indirme
    baslamadan once beyaz ikon, indirilirken `CircularProgressIndicator`, indirildikten sonra
    pembe renkte tamamlanmis ikon. `PlayerEffect` `LaunchedEffect` icinde dinlenir ve `SnackbarHost`
    ile gosterilir.

- Sebep: Kullanici, onceden indirdigi sarkiyi internet baglantisi olmadan cihazin kendi depolamasindan
  calar. Yaklasim secimlerinin gerekceleri:
  - **OkHttp** (yeni bagimliliksiz): Proje zaten OkHttp kullaniyor; `DownloadManager` sistemi
    daha az kontrol saglar ve progress takibi icin ek etkilesim gerektirir.
  - **Room** (yeni bagimlililik: 2.7.1): Indirilen sarkilarin meta-verisini (yol, baslik) kalici
    tutmak icin gereklidir; uygulama yeniden baslatildiginda dosya varligi dogrulanir.
  - **app-specific external storage** (`getExternalFilesDir`): API 19+'ta ek izin gerektirmez;
    uygulama kaldirildiginda dosyalar otomatik silinir.
  - **Yerel dosya onceligi**: `PlayerViewModel.loadAndPlay()` once `getLocalFilePath()` sorgular;
    Stream URL yalnizca yerel dosya yoksa istenir. Bu sayede offline oynatma seffaftir ve ekstra
    bir UI dallanmasi gerektirmez.

### Bildirim Paneli Medya Mini Player

- Karar: `Service` + `Notification.MediaStyle` + `MediaSession` — MVI dışı bileşen.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama:
  - `service/LyraMediaService.kt` — `Service` subclass; `onCreate`'de `MediaSession` oluşturur
    (sahte metadata: "Neon Sokaklar / Şehir Işıkları / Gece Vardiyası"), `onStartCommand`'da
    `startForeground()` çağırır.
  - `service/LyraNotificationHelper.kt` — `NotificationChannel` oluşturma + `Notification.MediaStyle`
    nesnesi; sahte albüm kapağı (`LinearGradient` bitmap) ve 4 aksiyon butonu (Beğen, Önceki,
    Duraklat, Sonraki) içerir. Aksiyon `PendingIntent`'leri `NOOP` broadcast'e bağlıdır.
  - `AndroidManifest.xml`'e `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`,
    `POST_NOTIFICATIONS` izinleri ve `mediaPlayback` tipinde service kaydı eklendi.
  - `PlayerScreen.kt` — "Arkaplan" Row'u `onBackgroundClicked` lambdası ile tıklanabilir hale
    getirildi; `PlayerRoute` içinde `LocalContext` üzerinden `LyraMediaService.start()` tetiklenir.

- MVI kapsamı: `LyraMediaService` bir `Service`'dir, sunum katmanı değildir; MVI
  State/Intent/Effect sözleşmesi dışındadır. Bu, `agents.md §2.4`'ün bilinçli bir istisnasıdır.

- Sebep: Android bildirim panelinde medya kontrolü göstermek için sistem `Notification.MediaStyle`
  API'si zorunludur; arka plan ses servisi olmadan modern gradient görünümü (Android 12+)
  `MediaSession` aktif token'ı gerektirir. Backend yoktur, tüm veri statiktir.