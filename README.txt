ACCURACY IMPROVER v1.0.0
Pendamping posisi GNSS: membaca NMEA chip, lalu menyuntikkan posisi lewat mock location
agar aplikasi peta (mis. CarryMap) memakainya.

BUILD RELEASE (GitHub Actions)
1. Unggah SELURUH isi folder ini ke repo (termasuk folder .github).
2. Tab Actions > "Release APK" > Run workflow
   (atau buat tag, mis. v1.0.0, agar APK juga masuk ke halaman Releases).
3. Unduh artifact "AccuracyImprover-apk" (zip berisi AccuracyImprover.apk), pasang di HP.

PASANG DI HP
1. Izinkan instal dari sumber ini. Jika Play Protect memperingatkan, pilih instal saja.
2. Buka aplikasi, izinkan Lokasi dengan opsi PRESISI.
3. Developer Options > Select mock location app > Accuracy Improver.
4. Tekan MULAI di tempat terbuka, lalu buka aplikasi peta.

KUNCI PENANDATANGAN TETAP (disarankan, agar APK baru bisa menimpa yang lama)
Tanpa langkah ini setiap build memakai kunci sementara: update harus uninstall dulu.
Gunakan repo PRIVAT bila bisa; artifact repo publik dapat diunduh pengguna GitHub lain.

1. Repo > Settings > Secrets and variables > Actions > New repository secret:
     KEYSTORE_PASSWORD = minimal 16 karakter (dipakai untuk keystore dan key)
     KEY_ALIAS         = accuracy-improver   (3-40 huruf/angka/_/-)
2. Actions > "Create Accuracy Improver Release Keystore (RUN ONCE ONLY)" > Run workflow.
3. Buka run yang hijau, unduh artifact "accuracy-improver-keystore-ONE-TIME-CONFIDENTIAL"
   (kedaluwarsa 1 hari), ekstrak sehingga didapat accuracy-improver-release.jks.
   Simpan cadangannya di tempat aman (mis. password manager). Jangan diunggah ke repo.
4. Ubah ke base64 lalu simpan sebagai secret KEYSTORE_BASE64:
     Linux/macOS : base64 -w0 accuracy-improver-release.jks
     PowerShell  : [Convert]::ToBase64String([IO.File]::ReadAllBytes("accuracy-improver-release.jks"))
5. Jalankan "Release APK". Mulai saat ini semua build memakai kunci tetap itu.

Workflow pembuat keystore menolak jalan lagi jika KEYSTORE_BASE64 sudah ada.
Kehilangan keystore + password berarti aplikasi terpasang tidak bisa diperbarui.
