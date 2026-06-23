package com.turkcell.lyraapp.data.auth

/**
 * Kimlik doğrulama işlemlerinin tek soyutlama noktası.
 *
 * Backend ekibinin REST API'si henüz tanımlı olmadığından, şu an yalnızca sahte bir
 * implementasyon ([FakeAuthRepository]) ile sağlanır. Gerçek API geldiğinde yalnızca
 * implementasyon değişir; çağıran katmanlar (ör. LoginViewModel) etkilenmez.
 */
interface AuthRepository {

    /**
     * Telefon numarasına OTP doğrulama kodu gönderilmesini talep eder.
     *
     * @param phone E.164 formatında telefon numarası.
     * @return OTP talep sonucu.
     */
    suspend fun requestOtp(phone: String): Result<OtpResponseDto>

    /**
     * Alınan OTP doğrulama kodunu onaylayarak kullanıcı oturumu başlatır.
     *
     * @param phone E.164 formatında telefon numarası.
     * @param code Kullanıcıya gönderilen doğrulama kodu.
     * @return Kullanıcı oturum bilgileri.
     */
    suspend fun verifyOtp(phone: String, code: String): Result<AuthSessionDto>

    /**
     * Refresh token kullanarak yeni access ve refresh token çifti alır.
     *
     * @param refreshToken Mevcut geçerli refresh token.
     * @return Yeni token bilgileri.
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthTokensDto>

    /**
     * Kullanıcı oturumunu sonlandırır ve refresh token'ı iptal eder.
     *
     * @param refreshToken İptal edilecek refresh token.
     * @return İşlem başarısı.
     */
    suspend fun logout(refreshToken: String): Result<Boolean>

    /**
     * Kullanıcı profil bilgilerini tamamlar veya günceller.
     *
     * @param firstName Kullanıcının ilk ismi.
     * @param lastName Kullanıcının soyismi.
     * @param birthDate Kullanıcının doğum tarihi (YYYY-MM-DD formatında).
     * @return Güncellenmiş kullanıcı profili.
     */
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        birthDate: String
    ): Result<UserDto>

    /**
     * Oturum açmış olan kullanıcının profil bilgilerini getirir.
     *
     * @return Kullanıcı bilgileri.
     */
    suspend fun getCurrentUser(): Result<UserDto>

    /**
     * Kullanıcının bir şarkıyı çaldığını kaydeder. Son çalınanlar ve öneri motorunu besler.
     *
     * @param songId Çalınan şarkının id'si.
     * @return Kayıt başarılıysa true.
     */
    suspend fun recordPlay(songId: String): Result<Boolean>

    /**
     * Kullanıcının en son çaldığı şarkıları döner (distinct, en yeni önce).
     *
     * @param limit Maksimum şarkı sayısı (varsayılan 20, max 100).
     */
    suspend fun getRecentlyPlayed(limit: Int? = null): Result<List<com.turkcell.lyraapp.data.songs.SongDto>>

    /**
     * Kullanıcıya özel karışık şarkı listesi döner. Çalma geçmişi yoksa en yeni katalog kullanılır.
     *
     * @param limit Maksimum şarkı sayısı.
     */
    suspend fun getForYou(limit: Int? = null): Result<List<com.turkcell.lyraapp.data.songs.SongDto>>

    /**
     * Kullanıcının en çok çaldığı sanatçılardan henüz çalmadığı şarkıları döner.
     *
     * @param limit Maksimum şarkı sayısı.
     */
    suspend fun getRecommendations(limit: Int? = null): Result<List<com.turkcell.lyraapp.data.songs.SongDto>>

    /**
     * Kullanıcının kendi oluşturduğu çalma listelerini döner.
     */
    suspend fun getUserPlaylists(): Result<List<PlaylistDto>>

    /**
     * Yeni bir çalma listesi oluşturur.
     *
     * @param name Liste adı (max 120 karakter).
     * @param description İsteğe bağlı açıklama (max 500 karakter).
     */
    suspend fun createPlaylist(name: String, description: String? = null): Result<PlaylistDto>

    /**
     * Kullanıcının bir çalma listesine şarkı ekler.
     *
     * @param playlistId Hedef çalma listesinin id'si.
     * @param songId Eklenecek şarkının id'si.
     * @return Ekleme başarılıysa true.
     */
    suspend fun addTrackToPlaylist(playlistId: String, songId: String): Result<Boolean>

    /**
     * Kullanıcının bir çalma listesinden şarkı kaldırır.
     *
     * @param playlistId Hedef çalma listesinin id'si.
     * @param songId Kaldırılacak şarkının id'si.
     * @return Kaldırma başarılıysa true.
     */
    suspend fun removeTrackFromPlaylist(playlistId: String, songId: String): Result<Boolean>

    /**
     * Verilen telefon numarası ve şifreyle giriş dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    @Deprecated("Yeni OTP tabanlı giriş akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    suspend fun login(phoneNumber: String, password: String): Result<Unit>

    /**
     * Verilen kullanıcı bilgileriyle yeni bir hesap oluşturmayı dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    @Deprecated("Yeni OTP tabanlı kayıt akışına geçildiğinden bu metot kullanımdan kaldırılmıştır.")
    suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit>
}

