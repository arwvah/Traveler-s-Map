package com.travelersmap.data.seed

import com.travelersmap.domain.model.Difficulty
import com.travelersmap.domain.model.PlaceCategory
import com.travelersmap.domain.model.TouristPlace

/**
 * Famous Uzbekistan destinations for offline MVP.
 * Photos use Wikimedia Commons stable URLs (network when available; UI degrades offline).
 */
object UzbekistanSeedData {

    private const val UZ = "UZ"
    private const val GOLD = 0xFFC9A227

    val places: List<TouristPlace> = listOf(
        place(
            id = "registan",
            name = "Registan Square",
            city = "Samarkand",
            category = PlaceCategory.UNESCO,
            short = "Iconic heart of Samarkand with three grand madrasahs.",
            desc = "The Registan is the most famous public square in Central Asia, framed by the Ulugh Beg, Sher-Dor, and Tilya-Kori madrasahs.",
            history = "Developed from the 15th to 17th centuries as the commercial and social center of Timurid Samarkand. A UNESCO World Heritage site within the Historic Centre of Samarkand.",
            lat = 39.6542, lng = 66.9757,
            hours = "Daily 08:00–19:00",
            ticket = "≈ 50,000 UZS",
            visit = 120, season = "Apr–Jun, Sep–Oct",
            difficulty = Difficulty.EASY, family = true, rating = 4.9f,
            nearby = listOf("gur_emir", "shahi_zinda", "bibi_khanym"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Registan_square_Samarkand.jpg/1280px-Registan_square_Samarkand.jpg"
            )
        ),
        place(
            id = "gur_emir",
            name = "Gur-e-Amir Mausoleum",
            city = "Samarkand",
            category = PlaceCategory.HISTORICAL,
            short = "Timur’s turquoise-domed mausoleum.",
            desc = "Burial place of Timur (Tamerlane) and his family; blueprint for later Mughal architecture.",
            history = "Built in the early 15th century. Its ribbed azure dome influenced the design of the Taj Mahal.",
            lat = 39.6484, lng = 66.9680,
            hours = "Daily 09:00–18:00",
            ticket = "≈ 30,000 UZS",
            visit = 60, season = "Spring, Autumn",
            difficulty = Difficulty.EASY, family = true, rating = 4.8f,
            nearby = listOf("registan", "bibi_khanym"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Gur-e_Amir_Mausoleum.jpg/1280px-Gur-e_Amir_Mausoleum.jpg"
            )
        ),
        place(
            id = "shahi_zinda",
            name = "Shah-i-Zinda",
            city = "Samarkand",
            category = PlaceCategory.MONUMENT,
            short = "Avenue of dazzling Timurid mausoleums.",
            desc = "A necropolis of richly tiled tombs climbing a hillside north of the old city.",
            history = "Associated with Qutham ibn Abbas; major expansions under Timur and Ulugh Beg.",
            lat = 39.6631, lng = 66.9880,
            hours = "Daily 08:00–19:00",
            ticket = "≈ 40,000 UZS",
            visit = 90, season = "Apr–Oct",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.8f,
            nearby = listOf("registan", "bibi_khanym"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Shah-i-Zinda.jpg/1280px-Shah-i-Zinda.jpg"
            )
        ),
        place(
            id = "bibi_khanym",
            name = "Bibi-Khanym Mosque",
            city = "Samarkand",
            category = PlaceCategory.MOSQUE,
            short = "Monumental mosque built after Timur’s India campaign.",
            desc = "Once one of the largest mosques in the Islamic world, with a vast courtyard and towering portal.",
            history = "Commissioned by Timur around 1399–1404; extensively restored in the modern era.",
            lat = 39.6607, lng = 66.9793,
            hours = "Daily 08:00–18:00",
            ticket = "≈ 30,000 UZS",
            visit = 45, season = "Spring, Autumn",
            difficulty = Difficulty.EASY, family = true, rating = 4.7f,
            nearby = listOf("registan", "shahi_zinda"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Bibi-Khanym_Mosque.jpg/1280px-Bibi-Khanym_Mosque.jpg"
            )
        ),
        place(
            id = "po_i_kalyan",
            name = "Po-i-Kalyan Complex",
            city = "Bukhara",
            category = PlaceCategory.UNESCO,
            short = "Kalyan Minaret, mosque, and madrasah ensemble.",
            desc = "The spiritual center of Bukhara, dominated by the 12th-century Kalyan Minaret.",
            history = "Minaret completed 1127; mosque and Mir-i-Arab Madrasah later completed the complex.",
            lat = 39.7756, lng = 64.4220,
            hours = "Daily 08:00–19:00",
            ticket = "≈ 45,000 UZS",
            visit = 100, season = "Apr–Jun, Sep–Oct",
            difficulty = Difficulty.EASY, family = true, rating = 4.9f,
            nearby = listOf("ark_bukhara", "lyabi_hauz", "samanid_mausoleum"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Kalyan_Minaret.jpg/1280px-Kalyan_Minaret.jpg"
            )
        ),
        place(
            id = "ark_bukhara",
            name = "Ark of Bukhara",
            city = "Bukhara",
            category = PlaceCategory.FORTRESS,
            short = "Ancient citadel of Bukharan emirs.",
            desc = "Massive mud-brick fortress with museums inside the reconstructed royal complex.",
            history = "Occupied for over a millennium; largely destroyed in 1920 and partially rebuilt.",
            lat = 39.7778, lng = 64.4108,
            hours = "Wed–Mon 09:00–17:00",
            ticket = "≈ 40,000 UZS",
            visit = 90, season = "Spring, Autumn",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.6f,
            nearby = listOf("po_i_kalyan", "lyabi_hauz"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Ark_of_Bukhara.jpg/1280px-Ark_of_Bukhara.jpg"
            )
        ),
        place(
            id = "lyabi_hauz",
            name = "Lyabi-Hauz",
            city = "Bukhara",
            category = PlaceCategory.HISTORICAL,
            short = "Plaza around a historic pool with madrasahs.",
            desc = "A lively square with tea houses, crafts, and the Nadir Divan-Begi ensemble.",
            history = "Created in the 17th century as a social hub of old Bukhara.",
            lat = 39.7740, lng = 64.4205,
            hours = "Open 24h (plaza)",
            ticket = "Free (plaza)",
            visit = 60, season = "Year-round evenings",
            difficulty = Difficulty.EASY, family = true, rating = 4.7f,
            nearby = listOf("po_i_kalyan", "ark_bukhara"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Lyab-i_Hauz.jpg/1280px-Lyab-i_Hauz.jpg"
            )
        ),
        place(
            id = "samanid_mausoleum",
            name = "Samanid Mausoleum",
            city = "Bukhara",
            category = PlaceCategory.MONUMENT,
            short = "Pearl of early Islamic brick architecture.",
            desc = "Perfectly proportioned 10th-century mausoleum of baked brick.",
            history = "Built for Ismail Samani; survived Mongol invasions under layers of silt.",
            lat = 39.7770, lng = 64.4015,
            hours = "Daily 08:00–18:00",
            ticket = "≈ 15,000 UZS",
            visit = 30, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.7f,
            nearby = listOf("ark_bukhara", "po_i_kalyan"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Samanid_Mausoleum.jpg/1280px-Samanid_Mausoleum.jpg"
            )
        ),
        place(
            id = "itchan_kala",
            name = "Itchan Kala",
            city = "Khiva",
            category = PlaceCategory.UNESCO,
            short = "Walled inner city of Khiva — open-air museum.",
            desc = "Dense historic core with minarets, madrasahs, and caravanserais inside mud-brick walls.",
            history = "UNESCO World Heritage site; capital of the Khivan Khanate.",
            lat = 41.3783, lng = 60.3639,
            hours = "Daily 08:00–18:00 (sites vary)",
            ticket = "≈ 100,000 UZS (combined)",
            visit = 240, season = "Apr–May, Sep–Oct",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.9f,
            nearby = listOf("kalta_minor", "juma_mosque_khiva"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Itchan_Kala.jpg/1280px-Itchan_Kala.jpg"
            )
        ),
        place(
            id = "kalta_minor",
            name = "Kalta Minor Minaret",
            city = "Khiva",
            category = PlaceCategory.MONUMENT,
            short = "Unfinished turquoise minaret of Khiva.",
            desc = "Stunning truncated minaret covered in glazed tiles.",
            history = "Begun in 1851 by Muhammad Amin Khan; left unfinished after his death.",
            lat = 41.3789, lng = 60.3605,
            hours = "Daily with Itchan Kala ticket",
            ticket = "Included in city ticket",
            visit = 30, season = "Spring, Autumn",
            difficulty = Difficulty.EASY, family = true, rating = 4.8f,
            nearby = listOf("itchan_kala", "juma_mosque_khiva"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Kalta_Minor.jpg/1280px-Kalta_Minor.jpg"
            )
        ),
        place(
            id = "juma_mosque_khiva",
            name = "Juma Mosque (Khiva)",
            city = "Khiva",
            category = PlaceCategory.MOSQUE,
            short = "Hypostyle mosque with 200+ wooden columns.",
            desc = "Atmospheric hall of carved wooden pillars, some centuries old.",
            history = "Rebuilt in the 18th century on much older foundations.",
            lat = 41.3775, lng = 60.3618,
            hours = "Daily 09:00–17:00",
            ticket = "Included / small fee",
            visit = 40, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.6f,
            nearby = listOf("itchan_kala", "kalta_minor"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Juma_Mosque_Khiva.jpg/1280px-Juma_Mosque_Khiva.jpg"
            )
        ),
        place(
            id = "ak_saray",
            name = "Ak-Saray Palace",
            city = "Shahrisabz",
            category = PlaceCategory.PALACE,
            short = "Ruins of Timur’s White Palace portal.",
            desc = "Colossal gateway remains with stunning tilework — Timur’s birthplace city.",
            history = "Built late 14th century; once the largest portal in Central Asia.",
            lat = 39.0575, lng = 66.8300,
            hours = "Daily 09:00–18:00",
            ticket = "≈ 25,000 UZS",
            visit = 60, season = "Spring, Autumn",
            difficulty = Difficulty.EASY, family = true, rating = 4.5f,
            nearby = listOf("dorut_tilavat"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Ak-Saray_Palace.jpg/1280px-Ak-Saray_Palace.jpg"
            )
        ),
        place(
            id = "dorut_tilavat",
            name = "Dorut Tilavat Complex",
            city = "Shahrisabz",
            category = PlaceCategory.HISTORICAL,
            short = "Timurid memorial complex with Kok Gumbaz.",
            desc = "Mosques and mausoleums honoring Timur’s family and teachers.",
            history = "15th-century complex, UNESCO-listed with Historic Centre of Shakhrisyabz.",
            lat = 39.0520, lng = 66.8340,
            hours = "Daily 09:00–18:00",
            ticket = "≈ 20,000 UZS",
            visit = 45, season = "Spring, Autumn",
            difficulty = Difficulty.EASY, family = true, rating = 4.4f,
            nearby = listOf("ak_saray"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Kok_Gumbaz_Mosque.jpg/1280px-Kok_Gumbaz_Mosque.jpg"
            )
        ),
        place(
            id = "savitsky",
            name = "Savitsky Karakalpakstan Art Museum",
            city = "Nukus",
            category = PlaceCategory.MUSEUM,
            short = "World-class avant-garde art in the desert capital.",
            desc = "One of the largest collections of Russian avant-garde outside Moscow.",
            history = "Assembled by Igor Savitsky under Soviet rule; now a cultural landmark of Karakalpakstan.",
            lat = 42.4619, lng = 59.6167,
            hours = "Tue–Sun 09:00–13:00, 14:00–17:00",
            ticket = "≈ 50,000 UZS",
            visit = 120, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.8f,
            nearby = listOf(),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Nukus_museum.jpg/1280px-Nukus_museum.jpg"
            )
        ),
        place(
            id = "fayaz_tepe",
            name = "Fayaz Tepe",
            city = "Termez",
            category = PlaceCategory.ANCIENT_CITY,
            short = "Buddhist monastery ruins near the Afghan border.",
            desc = "Archaeological complex from the Kushan period on the Silk Road.",
            history = "Flourished in the 1st–3rd centuries CE; key site of Buddhist Central Asia.",
            lat = 37.2860, lng = 67.1870,
            hours = "Daily 09:00–17:00",
            ticket = "≈ 20,000 UZS",
            visit = 90, season = "Oct–Apr (cooler)",
            difficulty = Difficulty.MODERATE, family = false, rating = 4.3f,
            nearby = listOf("sultan_saodat"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e2/Fayaz_Tepe.jpg/1280px-Fayaz_Tepe.jpg"
            )
        ),
        place(
            id = "sultan_saodat",
            name = "Sultan Saodat Complex",
            city = "Termez",
            category = PlaceCategory.HISTORICAL,
            short = "Mausoleums of Termez Sayyids.",
            desc = "Elegant ensemble of domed tombs south of modern Termez.",
            history = "Dating from the 10th–17th centuries; resting place of local nobility.",
            lat = 37.2290, lng = 67.2780,
            hours = "Daily 08:00–18:00",
            ticket = "Free / donation",
            visit = 40, season = "Autumn–Spring",
            difficulty = Difficulty.EASY, family = true, rating = 4.2f,
            nearby = listOf("fayaz_tepe"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Sultan_Saodat.jpg/1280px-Sultan_Saodat.jpg"
            )
        ),
        place(
            id = "zaamin",
            name = "Zaamin National Park",
            city = "Zaamin",
            category = PlaceCategory.NATIONAL_PARK,
            short = "Alpine meadows and juniper forests of Jizzakh.",
            desc = "Mountain national park ideal for hiking, fresh air, and family nature trips.",
            history = "Protected since the Soviet era; one of Uzbekistan’s oldest nature reserves.",
            lat = 39.7160, lng = 68.4000,
            hours = "Daylight hours",
            ticket = "Park entry fee varies",
            visit = 300, season = "May–Sep",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.6f,
            nearby = listOf(),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Zaamin_mountains.jpg/1280px-Zaamin_mountains.jpg"
            )
        ),
        place(
            id = "chimgan",
            name = "Chimgan Mountains",
            city = "Chimgan",
            category = PlaceCategory.MOUNTAIN,
            short = "Tashkent’s mountain playground — hiking & skiing.",
            desc = "Popular alpine resort area in the western Tian Shan.",
            history = "Long-standing recreation zone for Tashkent residents; developed in Soviet times.",
            lat = 41.5000, lng = 70.0167,
            hours = "Daylight / resort hours",
            ticket = "Activity-dependent",
            visit = 360, season = "Jun–Sep (hike), Dec–Mar (ski)",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.7f,
            nearby = listOf("charvak"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Chimgan.jpg/1280px-Chimgan.jpg"
            )
        ),
        place(
            id = "charvak",
            name = "Charvak Reservoir",
            city = "Charvak",
            category = PlaceCategory.LAKE,
            short = "Turquoise mountain reservoir near Tashkent.",
            desc = "Scenic lake for swimming, camping, and weekend escapes.",
            history = "Created by a hydroelectric dam in the 1970s on the Chirchiq River.",
            lat = 41.6167, lng = 70.0333,
            hours = "Open access",
            ticket = "Beach/club fees vary",
            visit = 240, season = "Jun–Aug",
            difficulty = Difficulty.EASY, family = true, rating = 4.6f,
            nearby = listOf("chimgan"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Charvak_Reservoir.jpg/1280px-Charvak_Reservoir.jpg"
            )
        ),
        place(
            id = "nuratau",
            name = "Nuratau Mountains",
            city = "Nuratau",
            category = PlaceCategory.NATURE_RESERVE,
            short = "Community eco-tourism and wild walnut forests.",
            desc = "Scenic range between desert and steppe; yurt stays and hiking.",
            history = "Home to traditional villages and a nature reserve protecting wildlife corridors.",
            lat = 40.5000, lng = 66.7000,
            hours = "Daylight",
            ticket = "Homestay packages",
            visit = 480, season = "Apr–Jun, Sep–Oct",
            difficulty = Difficulty.MODERATE, family = true, rating = 4.5f,
            nearby = listOf("aydarkul"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Nuratau.jpg/1280px-Nuratau.jpg"
            )
        ),
        place(
            id = "aydarkul",
            name = "Aydarkul Lake",
            city = "Aydarkul",
            category = PlaceCategory.LAKE,
            short = "Vast desert lake with yurt camps.",
            desc = "Artificial desert lake perfect for stargazing and camel treks.",
            history = "Formed by Syr Darya floodwaters diverted into the Arnasay lowland in the 1960s.",
            lat = 40.9000, lng = 66.8000,
            hours = "Camp-dependent",
            ticket = "Camp packages",
            visit = 600, season = "May–Sep",
            difficulty = Difficulty.EASY, family = true, rating = 4.4f,
            nearby = listOf("nuratau"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Aydar_Lake.jpg/1280px-Aydar_Lake.jpg"
            )
        ),
        place(
            id = "hazrati_imam",
            name = "Hazrati Imam Complex",
            city = "Tashkent",
            category = PlaceCategory.MOSQUE,
            short = "Spiritual center of Tashkent; home of the Uthman Quran.",
            desc = "Ensemble of mosques and madrasahs with a library holding a famous Quran manuscript.",
            history = "Named after a 10th-century scholar; modern complex rebuilt in the 2000s.",
            lat = 41.3386, lng = 69.2380,
            hours = "Daily 08:00–19:00",
            ticket = "Free / library fee",
            visit = 75, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.7f,
            nearby = listOf("chorsu", "amir_temur_square"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Hazrati_Imam.jpg/1280px-Hazrati_Imam.jpg"
            )
        ),
        place(
            id = "chorsu",
            name = "Chorsu Bazaar",
            city = "Tashkent",
            category = PlaceCategory.HISTORICAL,
            short = "Iconic blue-domed central market.",
            desc = "Vibrant bazaar for spices, bread, produce, and local life.",
            history = "Trading site for centuries; current dome structure from the Soviet period.",
            lat = 41.3267, lng = 69.2350,
            hours = "Daily 06:00–19:00",
            ticket = "Free",
            visit = 90, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.6f,
            nearby = listOf("hazrati_imam"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Chorsu_Bazaar.jpg/1280px-Chorsu_Bazaar.jpg"
            )
        ),
        place(
            id = "amir_temur_square",
            name = "Amir Temur Square",
            city = "Tashkent",
            category = PlaceCategory.MONUMENT,
            short = "Central square with Timur equestrian statue.",
            desc = "Green urban square surrounded by museums, hotels, and avenues.",
            history = "Laid out in the Russian imperial era; renamed and re-centered on Timur after independence.",
            lat = 41.3111, lng = 69.2797,
            hours = "Open 24h",
            ticket = "Free",
            visit = 30, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.5f,
            nearby = listOf("history_museum_tashkent"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a8/Amir_Temur_Square.jpg/1280px-Amir_Temur_Square.jpg"
            )
        ),
        place(
            id = "history_museum_tashkent",
            name = "State Museum of History of Uzbekistan",
            city = "Tashkent",
            category = PlaceCategory.MUSEUM,
            short = "National history from ancient times to independence.",
            desc = "Comprehensive museum near Amir Temur Square covering archaeology and culture.",
            history = "One of the oldest museums in Central Asia, founded in 1876.",
            lat = 41.3125, lng = 69.2780,
            hours = "Tue–Sun 10:00–17:00",
            ticket = "≈ 30,000 UZS",
            visit = 100, season = "Year-round",
            difficulty = Difficulty.EASY, family = true, rating = 4.4f,
            nearby = listOf("amir_temur_square"),
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/History_Museum_Tashkent.jpg/1280px-History_Museum_Tashkent.jpg"
            )
        )
    )

    private fun place(
        id: String,
        name: String,
        city: String,
        category: PlaceCategory,
        short: String,
        desc: String,
        history: String,
        lat: Double,
        lng: Double,
        hours: String,
        ticket: String,
        visit: Int,
        season: String,
        difficulty: Difficulty,
        family: Boolean,
        rating: Float,
        nearby: List<String>,
        photos: List<String>
    ) = TouristPlace(
        id = id,
        countryCode = UZ,
        name = name,
        city = city,
        category = category,
        shortDescription = short,
        description = desc,
        history = history,
        latitude = lat,
        longitude = lng,
        photoUrls = photos,
        openingHours = hours,
        ticketPrice = ticket,
        estimatedVisitMinutes = visit,
        bestSeason = season,
        difficulty = difficulty,
        familyFriendly = family,
        rating = rating,
        nearbyIds = nearby,
        accentColorHex = GOLD
    )
}
