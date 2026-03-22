package com.marsxz.marsxzmedia

object MediaFileNaming {

    data class TrackInfo(
        val originalLabel: String?,
        val languageDisplay: String?,
        val languageCode: String?,
        val isOriginal: Boolean,
        val isDub: Boolean
    )

    fun buildFinalTitle(
        originalTitle: String,
        selectedAudioTrackLabel: String?,
        translatedTitle: String? = null
    ): String {
        val cleanOriginal = sanitizeTitle(originalTitle)
        val track = parseTrackInfo(selectedAudioTrackLabel)

        if (track.isOriginal || !track.isDub) {
            return cleanOriginal
        }

        val baseTitle = translatedTitle?.trim().takeUnless { it.isNullOrBlank() } ?: cleanOriginal
        val suffix = buildDubSuffix(track)

        return if (suffix.isBlank()) {
            baseTitle
        } else {
            "$baseTitle $suffix"
        }
    }

    fun parseTrackInfo(label: String?): TrackInfo {
        if (label.isNullOrBlank()) {
            return TrackInfo(
                originalLabel = label,
                languageDisplay = null,
                languageCode = null,
                isOriginal = false,
                isDub = false
            )
        }

        val s = label.trim()
        val lower = s.lowercase()

        val isOriginal = "оригинал" in lower || "original" in lower
        val isDub = "дубляж" in lower || "dub" in lower || "dubbed" in lower

        val pair = when {
            "рус" in lower || "russian" in lower -> "Русский" to "ru"
            "англ" in lower || "english" in lower -> "Английский" to "en"
            "каз" in lower || "kazakh" in lower -> "Казахский" to "kk"
            "укр" in lower || "ukrainian" in lower -> "Украинский" to "uk"
            "испан" in lower || "spanish" in lower -> "Испанский" to "es"
            "нем" in lower || "german" in lower -> "Немецкий" to "de"
            "франц" in lower || "french" in lower -> "Французский" to "fr"
            "тур" in lower || "turkish" in lower -> "Турецкий" to "tr"
            "араб" in lower || "arabic" in lower -> "Арабский" to "ar"
            "хинди" in lower || "hindi" in lower -> "Хинди" to "hi"
            "португ" in lower || "portuguese" in lower -> "Португальский" to "pt"
            "итал" in lower || "italian" in lower -> "Итальянский" to "it"
            "поль" in lower || "polish" in lower -> "Польский" to "pl"
            "япон" in lower || "japanese" in lower -> "Японский" to "ja"
            "корей" in lower || "korean" in lower -> "Корейский" to "ko"
            "китай" in lower || "chinese" in lower || "mandarin" in lower -> "Китайский" to "zh-CN"
            "нидерл" in lower || "dutch" in lower -> "Нидерландский" to "nl"
            "чеш" in lower || "czech" in lower -> "Чешский" to "cs"
            "румын" in lower || "romanian" in lower -> "Румынский" to "ro"
            "венгр" in lower || "hungarian" in lower -> "Венгерский" to "hu"
            "швед" in lower || "swedish" in lower -> "Шведский" to "sv"
            "норв" in lower || "norwegian" in lower -> "Норвежский" to "no"
            "дат" in lower || "danish" in lower -> "Датский" to "da"
            "фин" in lower || "finnish" in lower -> "Финский" to "fi"
            "греч" in lower || "greek" in lower -> "Греческий" to "el"
            "иврит" in lower || "hebrew" in lower -> "Иврит" to "he"
            "тай" in lower || "thai" in lower -> "Тайский" to "th"
            "вьет" in lower || "vietnamese" in lower -> "Вьетнамский" to "vi"
            "индон" in lower || "indonesian" in lower -> "Индонезийский" to "id"
            "малай" in lower || "malay" in lower -> "Малайский" to "ms"
            "бенгал" in lower || "bengali" in lower -> "Бенгальский" to "bn"
            "урду" in lower || "urdu" in lower -> "Урду" to "ur"
            "тамил" in lower || "tamil" in lower -> "Тамильский" to "ta"
            "телугу" in lower || "telugu" in lower -> "Телугу" to "te"
            "армян" in lower || "armenian" in lower -> "Армянский" to "hy"
            "азерб" in lower || "azerbaijani" in lower -> "Азербайджанский" to "az"
            "груз" in lower || "georgian" in lower -> "Грузинский" to "ka"
            "узбек" in lower || "uzbek" in lower -> "Узбекский" to "uz"
            "белорус" in lower || "belarusian" in lower -> "Белорусский" to "be"
            "болг" in lower || "bulgarian" in lower -> "Болгарский" to "bg"
            "серб" in lower || "serbian" in lower -> "Сербский" to "sr"
            "хорв" in lower || "croatian" in lower -> "Хорватский" to "hr"
            "словак" in lower || "slovak" in lower -> "Словацкий" to "sk"
            "словен" in lower || "slovenian" in lower -> "Словенский" to "sl"
            "эстон" in lower || "estonian" in lower -> "Эстонский" to "et"
            "латыш" in lower || "latvian" in lower -> "Латышский" to "lv"
            "литов" in lower || "lithuanian" in lower -> "Литовский" to "lt"
            "перс" in lower || "persian" in lower || "farsi" in lower -> "Персидский" to "fa"
            "филип" in lower || "tagalog" in lower || "filipino" in lower -> "Филиппинский" to "tl"
            "монгол" in lower || "mongolian" in lower -> "Монгольский" to "mn"

            // --- ЕЩЕ БОЛЬШЕ ЯЗЫКОВ ---
            // Африка
            "африкаанс" in lower || "afrikaans" in lower -> "Африкаанс" to "af"
            "амхар" in lower || "amharic" in lower -> "Амхарский" to "am"
            "зулу" in lower || "zulu" in lower -> "Зулу" to "zu"
            "суахили" in lower || "swahili" in lower -> "Суахили" to "sw"
            "йоруба" in lower || "yoruba" in lower -> "Йоруба" to "yo"
            "сомали" in lower || "somali" in lower -> "Сомалийский" to "so"
            "хауса" in lower || "hausa" in lower -> "Хауса" to "ha"
            "игбо" in lower || "igbo" in lower -> "Игбо" to "ig"

            // Европа (доп)
            "албан" in lower || "albanian" in lower -> "Албанский" to "sq"
            "баск" in lower || "basque" in lower -> "Баскский" to "eu"
            "валлий" in lower || "welsh" in lower -> "Валлийский" to "cy"
            "ирланд" in lower || "irish" in lower -> "Ирландский" to "ga"
            "исланд" in lower || "icelandic" in lower -> "Исландский" to "is"
            "каталан" in lower || "catalan" in lower -> "Каталанский" to "ca"
            "мальтий" in lower || "maltese" in lower -> "Мальтийский" to "mt"
            "македон" in lower || "macedonian" in lower -> "Македонский" to "mk"
            "латин" in lower || "latin" in lower -> "Латынь" to "la"
            "гаэль" in lower || "gaelic" in lower -> "Гаэльский" to "gd"

            // Азия и Океания (доп)
            "непал" in lower || "nepali" in lower -> "Непальский" to "ne"
            "сингаль" in lower || "sinhala" in lower -> "Сингальский" to "si"
            "бирман" in lower || "burmese" in lower -> "Бирманский" to "my"
            "кхмер" in lower || "khmer" in lower -> "Кхмерский" to "km"
            "лаос" in lower || "lao" in lower -> "Лаосский" to "lo"
            "яван" in lower || "javanese" in lower -> "Яванский" to "jv"
            "маори" in lower || "maori" in lower -> "Маори" to "mi"
            "самоа" in lower || "samoan" in lower -> "Самоанский" to "sm"
            "тибет" in lower || "tibetan" in lower -> "Тибетский" to "bo"

            // Кавказ и Средняя Азия (доп)
            "тадж" in lower || "tajik" in lower -> "Таджикский" to "tg"
            "туркмен" in lower || "turkmen" in lower -> "Туркменский" to "tk"
            "кирг" in lower || "kyrgyz" in lower -> "Киргизский" to "ky"
            "татар" in lower || "tatar" in lower -> "Татарский" to "tt"
            "башк" in lower || "bashkir" in lower -> "Башкирский" to "ba"
            "чуваш" in lower || "chuvash" in lower -> "Чувашский" to "cv"

            // Индия и окрестности
            "маратх" in lower || "marathi" in lower -> "Маратхи" to "mr"
            "панджаб" in lower || "punjabi" in lower -> "Панджаби" to "pa"
            "гуджарат" in lower || "gujarati" in lower -> "Гуджарати" to "gu"
            "каннада" in lower || "kannada" in lower -> "Каннада" to "kn"
            "малаялам" in lower || "malayalam" in lower -> "Малаялам" to "ml"
            "пушту" in lower || "pashto" in lower -> "Пушту" to "ps"

            // Другие
            "эсперанто" in lower || "esperanto" in lower -> "Эсперанто" to "eo"
            "идиш" in lower || "yiddish" in lower -> "Идиш" to "yi"
            "малагас" in lower || "malagasy" in lower -> "Малагасийский" to "mg"

            else -> null to null
        }

        return TrackInfo(
            originalLabel = s,
            languageDisplay = pair.first,
            languageCode = pair.second,
            isOriginal = isOriginal,
            isDub = isDub
        )
    }

    fun shouldTranslateTitle(selectedAudioTrackLabel: String?): Boolean {
        val info = parseTrackInfo(selectedAudioTrackLabel)
        return info.isDub && !info.languageCode.isNullOrBlank()
    }

    private fun buildDubSuffix(track: TrackInfo): String {
        val lang = track.languageDisplay ?: return "[Дубляж]"
        return "[$lang дубляж]"
    }

    private fun sanitizeTitle(title: String): String {
        return title
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}