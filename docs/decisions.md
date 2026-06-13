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