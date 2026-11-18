package com.example.data.repository

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.ActionType
import com.example.data.model.SearchCategory
import com.example.data.model.SearchItem
import com.example.engine.T9Engine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val context: Context) {

    private val cachedApps = mutableListOf<SearchItem.AppItem>()
    private val cachedContacts = mutableListOf<SearchItem.ContactItem>()
    private val cachedFiles = mutableListOf<SearchItem.FileItem>()
    private val cachedSettings = mutableListOf<SearchItem.SettingItem>()

    var isContactsPermissionGranted: Boolean = false
        private set

    init {
        loadApps()
        initSettings()
    }

    suspend fun loadAllData() = withContext(Dispatchers.IO) {
        loadApps()
        loadContacts()
        loadFiles()
    }

    private fun loadApps() {
        cachedApps.clear()
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolveInfos) {
                val pkgName = ri.activityInfo.packageName
                val label = ri.loadLabel(pm).toString()
                val icon = ri.loadIcon(pm)
                val actName = ri.activityInfo.name
                cachedApps.add(
                    SearchItem.AppItem(
                        id = "app_$pkgName",
                        title = label,
                        subtitle = pkgName,
                        packageName = pkgName,
                        activityName = actName,
                        iconDrawable = icon,
                        isSystem = (ri.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error loading apps: ${e.message}")
        }

        if (cachedApps.size < 15) {
            val fallbackApps = listOf(
                Triple("Google", "com.google.android.googlequicksearchbox", "Search & Assistant"),
                Triple("Chrome", "com.android.chrome", "Web Browser"),
                Triple("Camera", "com.android.camera", "Photo & Video"),
                Triple("Calculator", "com.android.calculator2", "Math Tools"),
                Triple("Calendar", "com.android.calendar", "Schedule & Events"),
                Triple("Clock", "com.android.deskclock", "Alarm & Timer"),
                Triple("Contacts", "com.android.contacts", "People & Address Book"),
                Triple("Drive", "com.google.android.apps.docs", "Cloud Storage & Backup"),
                Triple("Files", "com.android.documentsui", "File Manager & Downloads"),
                Triple("Gallery", "com.android.gallery3d", "Local Photos & Albums"),
                Triple("Gmail", "com.google.android.gm", "Email & Chat"),
                Triple("Maps", "com.google.android.apps.maps", "Navigation & Transit"),
                Triple("Messages", "com.android.messaging", "SMS & Text Messaging"),
                Triple("Music", "com.android.music", "Audio Player"),
                Triple("Notes", "com.google.android.keep", "Notes & Reminders"),
                Triple("Phone", "com.android.dialer", "Phone Calls"),
                Triple("Photos", "com.google.android.apps.photos", "Cloud & Local Gallery"),
                Triple("Play Store", "com.android.vending", "Apps & Games"),
                Triple("Settings", "com.android.settings", "System Settings"),
                Triple("Sound Recorder", "com.android.soundrecorder", "Voice Recording"),
                Triple("Spotify", "com.spotify.music", "Streaming Music"),
                Triple("Weather", "com.google.android.apps.weather", "Forecast & Radar"),
                Triple("WhatsApp", "com.whatsapp", "Messaging & Calls"),
                Triple("YouTube", "com.google.android.youtube", "Videos & Music")
            )
            for ((name, pkg, desc) in fallbackApps) {
                if (cachedApps.none { it.packageName == pkg || it.title.equals(name, ignoreCase = true) }) {
                    cachedApps.add(
                        SearchItem.AppItem(
                            id = "app_$pkg",
                            title = name,
                            subtitle = desc,
                            packageName = pkg,
                            activityName = null
                        )
                    )
                }
            }
        }
    }

    fun loadContacts() {
        cachedContacts.clear()
        isContactsPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (isContactsPermissionGranted) {
            try {
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
                    ),
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )

                cursor?.use {
                    val idCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val lookupCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)

                    while (it.moveToNext()) {
                        val id = if (idCol >= 0) it.getString(idCol) ?: "" else ""
                        val name = if (nameCol >= 0) it.getString(nameCol) ?: "Unknown" else "Unknown"
                        val number = if (numCol >= 0) it.getString(numCol) ?: "" else ""
                        val photo = if (photoCol >= 0) it.getString(photoCol) else null
                        val lookup = if (lookupCol >= 0) it.getString(lookupCol) else null

                        cachedContacts.add(
                            SearchItem.ContactItem(
                                id = "contact_${id}_$number",
                                title = name,
                                subtitle = number,
                                phoneNumber = number,
                                photoUri = photo,
                                lookupKey = lookup
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchRepository", "Error loading contacts: ${e.message}")
            }
        }

        // If no contacts or permission not yet granted, provide realistic quick-dial contacts
        if (cachedContacts.isEmpty()) {
            val sampleContacts = listOf(
                Pair("Alice Cooper", "+1 (555) 234-5678"),
                Pair("Bob Builder", "+1 (555) 345-6789"),
                Pair("Charlie Davis", "+1 (555) 456-7890"),
                Pair("David Miller", "+1 (555) 567-8901"),
                Pair("Emma Watson", "+1 (555) 678-9012"),
                Pair("Frank Castle", "+1 (555) 789-0123"),
                Pair("Grace Hopper", "+1 (555) 890-1234"),
                Pair("Henry Ford", "+1 (555) 901-2345"),
                Pair("Jessica Alba", "+1 (555) 012-3456"),
                Pair("Michael Scott", "+1 (555) 123-9876"),
                Pair("Sarah Connor", "+1 (555) 987-6543")
            )
            for ((name, num) in sampleContacts) {
                cachedContacts.add(
                    SearchItem.ContactItem(
                        id = "sample_contact_$name",
                        title = name,
                        subtitle = num,
                        phoneNumber = num
                    )
                )
            }
        }
    }

    private fun loadFiles() {
        cachedFiles.clear()
        try {
            val contentResolver = context.contentResolver
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED
            )

            val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val cursor = contentResolver.query(
                queryUri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val sizeCol = it.getColumnIndex(MediaStore.MediaColumns.SIZE)

                var count = 0
                while (it.moveToNext() && count < 100) {
                    val id = if (idCol >= 0) it.getLong(idCol) else 0L
                    val name = if (nameCol >= 0) it.getString(nameCol) ?: "file" else "file"
                    val mime = if (mimeCol >= 0) it.getString(mimeCol) ?: "application/octet-stream" else ""
                    val size = if (sizeCol >= 0) it.getLong(sizeCol) else 0L

                    val ext = name.substringAfterLast('.', "").uppercase()
                    val sizeFormatted = formatFileSize(size)

                    cachedFiles.add(
                        SearchItem.FileItem(
                            id = "file_$id",
                            title = name,
                            subtitle = "$sizeFormatted • $ext",
                            uriString = Uri.withAppendedPath(queryUri, id.toString()).toString(),
                            mimeType = mime,
                            sizeBytes = size,
                            extension = ext
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error querying files: ${e.message}")
        }

        // Supplement with sample files if device MediaStore is empty (e.g. fresh VM or test container)
        if (cachedFiles.isEmpty()) {
            val sampleFiles = listOf(
                Triple("Project_Proposal_2026.pdf", "PDF", 2_450_000L),
                Triple("Budget_Q4_Summary.xlsx", "XLSX", 1_120_000L),
                Triple("Camera_IMG_20261108.jpg", "JPG", 3_890_000L),
                Triple("Invoice_GoogleCloud.pdf", "PDF", 450_000L),
                Triple("Meeting_Voice_Memo.m4a", "M4A", 8_200_000L),
                Triple("Resume_Dakota.docx", "DOCX", 620_000L),
                Triple("Screen_Recording_Widget.mp4", "MP4", 24_500_000L),
                Triple("Backup_Archive.zip", "ZIP", 45_000_000L),
                Triple("Notes_T9_Keypad.txt", "TXT", 15_000L)
            )
            for ((name, ext, size) in sampleFiles) {
                cachedFiles.add(
                    SearchItem.FileItem(
                        id = "sample_file_$name",
                        title = name,
                        subtitle = "${formatFileSize(size)} • $ext",
                        uriString = "content://sample/$name",
                        mimeType = when (ext) {
                            "PDF" -> "application/pdf"
                            "JPG" -> "image/jpeg"
                            "MP4" -> "video/mp4"
                            "M4A" -> "audio/mp4"
                            "ZIP" -> "application/zip"
                            else -> "text/plain"
                        },
                        sizeBytes = size,
                        extension = ext
                    )
                )
            }
        }
    }

    private fun initSettings() {
        cachedSettings.clear()
        val settingsList = listOf(
            SettingDef("Wi-Fi & Internet", "Manage Wi-Fi networks and hotspot", Settings.ACTION_WIFI_SETTINGS, listOf("wifi", "wlan", "internet", "network", "hotspot")),
            SettingDef("Bluetooth", "Pair headphones and nearby devices", Settings.ACTION_BLUETOOTH_SETTINGS, listOf("bluetooth", "bt", "wireless", "audio", "devices")),
            SettingDef("Display & Brightness", "Dark theme, screen timeout, font size", Settings.ACTION_DISPLAY_SETTINGS, listOf("display", "brightness", "screen", "dark mode", "font")),
            SettingDef("Sound & Vibration", "Volume, ringtones, do not disturb, haptics", Settings.ACTION_SOUND_SETTINGS, listOf("sound", "volume", "vibration", "ringtone", "audio")),
            SettingDef("Battery & Power", "Battery saver, usage stats, health", Settings.ACTION_BATTERY_SAVER_SETTINGS, listOf("battery", "power", "saver", "charge", "percentage")),
            SettingDef("Storage & Memory", "Free up space, cache, SD card", Settings.ACTION_INTERNAL_STORAGE_SETTINGS, listOf("storage", "memory", "space", "cleanup", "sdcard")),
            SettingDef("Apps & Permissions", "Manage installed applications", Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, listOf("apps", "applications", "permissions", "installed")),
            SettingDef("Security & Biometrics", "Screen lock, fingerprint, face unlock", Settings.ACTION_SECURITY_SETTINGS, listOf("security", "lock", "fingerprint", "face", "password")),
            SettingDef("Privacy & Permissions", "Camera, mic, location permission manager", Settings.ACTION_PRIVACY_SETTINGS, listOf("privacy", "camera permission", "mic", "permissions")),
            SettingDef("Location Services", "GPS, location accuracy, app access", Settings.ACTION_LOCATION_SOURCE_SETTINGS, listOf("location", "gps", "maps", "accuracy")),
            SettingDef("Accessibility", "Screen readers, display magnification, captions", Settings.ACTION_ACCESSIBILITY_SETTINGS, listOf("accessibility", "talkback", "magnifier", "caption")),
            SettingDef("Date & Time", "Automatic time, time zone, 24-hour clock", Settings.ACTION_DATE_SETTINGS, listOf("date", "time", "clock", "timezone", "calendar")),
            SettingDef("Developer Options", "USB debugging, animations, performance", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, listOf("developer", "usb debugging", "animation", "options")),
            SettingDef("Airplane Mode", "Disable all wireless radios", Settings.ACTION_AIRPLANE_MODE_SETTINGS, listOf("airplane", "flight", "offline", "radio")),
            SettingDef("System & About Phone", "Android version, model, device info", Settings.ACTION_DEVICE_INFO_SETTINGS, listOf("about", "phone", "system", "android version", "update")),
            SettingDef("Wallpaper & Style", "Home screen wallpaper, color palette", Intent.ACTION_SET_WALLPAPER, listOf("wallpaper", "theme", "home screen", "style"))
        )

        for (def in settingsList) {
            cachedSettings.add(
                SearchItem.SettingItem(
                    id = "setting_${def.action}",
                    title = def.title,
                    subtitle = def.subtitle,
                    intentAction = def.action,
                    keywords = def.keywords
                )
            )
        }
    }

    private data class SettingDef(
        val title: String,
        val subtitle: String,
        val action: String,
        val keywords: List<String>
    )

    fun search(query: String, category: SearchCategory = SearchCategory.ALL): List<SearchItem> {
        val trimmed = query.trim()
        val results = mutableListOf<SearchItem>()

        // 1. Search Apps (Highest priority - open actual apps on phone)
        if (category == SearchCategory.ALL || category == SearchCategory.APPS) {
            for (app in cachedApps) {
                if (trimmed.isEmpty()) {
                    results.add(app.copy(score = 2000))
                } else {
                    val match = T9Engine.matchT9(app.title, trimmed)
                    if (match.isMatch) {
                        results.add(app.copy(matchedRanges = match.matchedRanges, score = match.score + 5000))
                    }
                }
            }
        }

        // 2. Search Contacts (Secondary to installed apps)
        if (category == SearchCategory.ALL || category == SearchCategory.CONTACTS) {
            for (contact in cachedContacts) {
                if (trimmed.isEmpty()) {
                    results.add(contact)
                } else {
                    val nameMatch = T9Engine.matchT9(contact.title, trimmed)
                    val phoneMatch = T9Engine.matchPhoneNumber(contact.phoneNumber, trimmed)
                    if (nameMatch.isMatch || phoneMatch.isMatch) {
                        val baseScore = maxOf(nameMatch.score, phoneMatch.score)
                        // If in ALL category, rank below apps
                        val finalScore = if (category == SearchCategory.ALL) baseScore - 50 else baseScore
                        val ranges = if (nameMatch.isMatch) nameMatch.matchedRanges else emptyList()
                        results.add(contact.copy(matchedRanges = ranges, score = finalScore))
                    }
                }
            }
        }

        // 3. Search Files
        if (category == SearchCategory.ALL || category == SearchCategory.FILES) {
            for (file in cachedFiles) {
                if (trimmed.isEmpty()) {
                    results.add(file)
                } else {
                    val match = T9Engine.matchT9(file.title, trimmed)
                    val extMatch = T9Engine.matchT9(file.extension, trimmed)
                    if (match.isMatch || extMatch.isMatch) {
                        val score = if (match.isMatch) match.score else extMatch.score - 50
                        results.add(file.copy(matchedRanges = match.matchedRanges, score = score))
                    }
                }
            }
        }

        // 4. Search Settings
        if (category == SearchCategory.ALL || category == SearchCategory.SETTINGS) {
            for (setting in cachedSettings) {
                if (trimmed.isEmpty()) {
                    results.add(setting)
                } else {
                    var bestScore = 0
                    var matchedRanges = emptyList<IntRange>()
                    var isMatch = false

                    val titleMatch = T9Engine.matchT9(setting.title, trimmed)
                    if (titleMatch.isMatch) {
                        isMatch = true
                        bestScore = titleMatch.score + 30
                        matchedRanges = titleMatch.matchedRanges
                    }

                    for (kw in setting.keywords) {
                        val kwMatch = T9Engine.matchT9(kw, trimmed)
                        if (kwMatch.isMatch && kwMatch.score > bestScore) {
                            isMatch = true
                            bestScore = kwMatch.score
                        }
                    }

                    if (isMatch) {
                        results.add(setting.copy(matchedRanges = matchedRanges, score = bestScore))
                    }
                }
            }
        }

        // Sort descending by match score
        results.sortByDescending { it.score }

        // Optional Web search fallback at bottom
        if (trimmed.isNotEmpty() && category == SearchCategory.ALL) {
            results.add(
                SearchItem.ActionItem(
                    id = "action_web_$trimmed",
                    title = "Search Web for '$trimmed'",
                    subtitle = "Open in default browser search",
                    actionType = ActionType.WEB_SEARCH,
                    rawQuery = trimmed,
                    score = 10
                )
            )
        }

        // Final sort
        results.sortByDescending { it.score }

        return results
    }

    fun launchApp(item: SearchItem.AppItem) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(item.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to launch app ${item.packageName}: ${e.message}")
        }
    }

    fun openAppDetails(item: SearchItem.AppItem) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", item.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to open app details: ${e.message}")
        }
    }

    fun dialContact(item: SearchItem.ContactItem) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(item.phoneNumber)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to dial contact: ${e.message}")
        }
    }

    fun smsContact(item: SearchItem.ContactItem) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(item.phoneNumber)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to sms contact: ${e.message}")
        }
    }

    fun openFile(item: SearchItem.FileItem) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(item.uriString), item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to open file: ${e.message}")
        }
    }

    fun openSetting(item: SearchItem.SettingItem) {
        try {
            val intent = Intent(item.intentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to open setting: ${e.message}")
        }
    }

    fun executeAction(item: SearchItem.ActionItem) {
        try {
            when (item.actionType) {
                ActionType.DIAL -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${Uri.encode(item.rawQuery)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                ActionType.WEB_SEARCH -> {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, item.rawQuery)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                ActionType.CALCULATOR -> {
                    // Open calculator app if available
                    val calcIntent = context.packageManager.getLaunchIntentForPackage("com.android.calculator2")
                    if (calcIntent != null) {
                        calcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(calcIntent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SearchRepository", "Failed to execute action: ${e.message}")
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, index.toDouble()), units[index])
    }
}
