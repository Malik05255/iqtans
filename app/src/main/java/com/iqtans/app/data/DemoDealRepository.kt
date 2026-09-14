package com.iqtans.app.data

import com.iqtans.app.domain.DealOffer
import com.iqtans.app.domain.HotelDeal
import com.iqtans.app.domain.PriceTrust
import com.iqtans.app.domain.ReviewSource
import com.iqtans.app.domain.SearchRequest

/**
 * Demo-only repository used until live supplier/affiliate credentials are connected.
 * Prices in this object MUST NOT be presented as live or verified market prices.
 */
object DemoDealRepository : DealRepository {
    override val isLive: Boolean = false

    override fun cities() = listOf(
        "جدة",
        "مكة المكرمة",
        "جازان",
        "أبها",
        "محايل عسير"
    )

    private val hotels = listOf(
        HotelDeal(
            id = "jeddah-sea",
            name = "فندق الواجهة البحرية",
            city = "جدة",
            area = "الكورنيش",
            stars = 5,
            rating = 9.1,
            ratingCount = 4280,
            distanceLabel = "على الواجهة البحرية",
            reviewSources = listOf(
                ReviewSource("Booking", 9.0, 10, 2380),
                ReviewSource("Google", 4.6, 5, 1900)
            ),
            insight = "إنشاء عضوية مجانية في الموقع الرسمي يجعل سعر الأعضاء أقل من السعر الظاهر للزائر.",
            flexibilitySaving = 118,
            flexibleDateLabel = "19–22 سبتمبر",
            offers = listOf(
                DealOffer(
                    id = "j1-best",
                    source = "تطبيق سفر + بطاقة مؤهلة",
                    finalPrice = 376,
                    referencePrice = 487,
                    title = "أفضل اقتناص",
                    method = "سعر التطبيق + كود خصم + بطاقة Visa Signature",
                    trust = PriceTrust.DEMO,
                    matchPercent = 100,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني حتى اليوم السابق",
                    meal = "بدون إفطار",
                    cardRequirement = "Visa Signature من بنك مشارك",
                    conditions = listOf("التطبيق فقط", "مرة واحدة لكل بطاقة", "الحد الأدنى للحجز متحقق"),
                    steps = listOf(
                        "افتح تطبيق منصة الحجز.",
                        "سجّل الدخول أو أنشئ حسابًا مجانيًا.",
                        "اختر نفس الفندق والتواريخ والغرفة.",
                        "طبّق كود العرض الظاهر في صفحة الحملة.",
                        "ادفع بالبطاقة المؤهلة وتأكد أن الإجمالي 376 ر.س قبل الدفع."
                    ),
                    breakdown = listOf(
                        "السعر قبل الخصومات" to 487,
                        "خصم التطبيق" to -31,
                        "كود العرض" to -40,
                        "خصم البطاقة" to -40
                    )
                ),
                DealOffer(
                    id = "j1-member",
                    source = "الموقع الرسمي",
                    finalPrice = 419,
                    referencePrice = 487,
                    title = "سعر أعضاء",
                    method = "اشترك مجانًا في برنامج الفندق",
                    trust = PriceTrust.DEMO,
                    matchPercent = 100,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني",
                    meal = "بدون إفطار",
                    memberRequirement = "عضوية مجانية",
                    steps = listOf("أنشئ عضوية مجانية.", "سجّل الدخول.", "اختر Member Rate لنفس الغرفة.")
                ),
                DealOffer(
                    id = "j1-public",
                    source = "منصة حجز عامة",
                    finalPrice = 487,
                    referencePrice = 487,
                    title = "السعر العام",
                    method = "بدون شروط إضافية",
                    trust = PriceTrust.DEMO,
                    matchPercent = 100,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني",
                    meal = "بدون إفطار"
                )
            )
        ),
        HotelDeal(
            id = "makkah-view",
            name = "فندق أفق مكة",
            city = "مكة المكرمة",
            area = "أجياد",
            stars = 5,
            rating = 8.9,
            ratingCount = 7130,
            distanceLabel = "650 م عن المسجد الحرام",
            reviewSources = listOf(
                ReviewSource("Booking", 8.8, 10, 4130),
                ReviewSource("Google", 4.5, 5, 3000)
            ),
            insight = "السعر داخل التطبيق أقل من الموقع، ويوجد مسار آخر بسعر أعضاء في الموقع الرسمي.",
            flexibilitySaving = 210,
            flexibleDateLabel = "21–24 سبتمبر",
            offers = listOf(
                DealOffer(
                    id = "m1-best",
                    source = "تطبيق حجز دولي",
                    finalPrice = 1097,
                    referencePrice = 1420,
                    title = "أفضل اقتناص",
                    method = "سعر تطبيق + عضوية مجانية + عرض بطاقة",
                    trust = PriceTrust.DEMO,
                    matchPercent = 100,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني حتى 48 ساعة",
                    meal = "إفطار لشخصين",
                    cardRequirement = "Mastercard World أو أعلى",
                    conditions = listOf("الحجز عبر التطبيق", "العرض لا يجتمع مع كاش باك خارجي"),
                    steps = listOf(
                        "نزّل تطبيق منصة الحجز أو افتحه.",
                        "سجّل الدخول للحصول على سعر الأعضاء.",
                        "اختر الغرفة المطابقة مع الإفطار والإلغاء المجاني.",
                        "ادفع ببطاقة Mastercard World المؤهلة.",
                        "تحقق من الإجمالي النهائي قبل تأكيد الحجز."
                    ),
                    breakdown = listOf(
                        "أفضل سعر عام مطابق" to 1420,
                        "سعر الأعضاء" to -137,
                        "سعر التطبيق" to -93,
                        "عرض البطاقة" to -93
                    )
                ),
                DealOffer(
                    id = "m1-official",
                    source = "الموقع الرسمي",
                    finalPrice = 1190,
                    referencePrice = 1420,
                    title = "عضوية الفندق",
                    method = "عضوية مجانية في برنامج الولاء",
                    trust = PriceTrust.DEMO,
                    matchPercent = 100,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني حتى 48 ساعة",
                    meal = "إفطار لشخصين"
                )
            )
        ),
        HotelDeal(
            id = "abha-cloud",
            name = "منتجع سحاب أبها",
            city = "أبها",
            area = "السودة",
            stars = 4,
            rating = 9.3,
            ratingCount = 840,
            distanceLabel = "12 دقيقة من وسط أبها",
            reviewSources = listOf(
                ReviewSource("Google", 4.7, 5, 620),
                ReviewSource("منصة محلية", 9.1, 10, 220)
            ),
            insight = "الحجز المباشر أغلى ظاهريًا، لكن كود الحملة يجعل الغرفة الأفضل أرخص من الغرفة القياسية.",
            flexibilitySaving = 75,
            flexibleDateLabel = "19–22 سبتمبر",
            offers = listOf(
                DealOffer(
                    id = "a1-best",
                    source = "الموقع الرسمي + كود",
                    finalPrice = 612,
                    referencePrice = 760,
                    title = "غرفة أفضل بسعر أقل",
                    method = "كود حملة موسمية على فئة الغرف الأعلى",
                    trust = PriceTrust.DEMO,
                    matchPercent = 96,
                    lastChecked = "بيانات توضيحية",
                    cancellation = "إلغاء مجاني",
                    meal = "إفطار",
                    conditions = listOf("نوع الغرفة أعلى من طلبك", "نفس عدد الليالي")
                )
            )
        )
    )

    override fun search(request: SearchRequest): List<HotelDeal> {
        val cityMatches = hotels.filter { it.city == request.city }
        val pool = if (cityMatches.isNotEmpty()) cityMatches else hotels
        if (request.hotelQuery.isBlank()) return pool
        return pool.filter { it.name.contains(request.hotelQuery, ignoreCase = true) }
            .ifEmpty { pool }
    }

    override fun getHotel(id: String): HotelDeal? = hotels.firstOrNull { it.id == id }
}
