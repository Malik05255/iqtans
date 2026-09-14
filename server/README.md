# Iqtans Engine

خادم الاقتناص. جميع مفاتيح مزودي السفر والبحث تحفظ هنا ولا توضع داخل تطبيق Android.

## ما ينفذه
- Booking.com Demand API v3.2: بحث حي بالتاريخ والنزلاء.
- Expedia Rapid: بنية Typeahead/Geography/Shopping مع توقيع SHA-512.
- Brave Search: اكتشاف عروض عامة ومحتوى X/Instagram/TikTok المفهرس؛ النتائج تبقى `DISCOVERED` ولا تخفض السعر تلقائيًا.
- Promotion rules: تطبيق عروض بطاقات/أكواد **موثقة** فقط على المستخدم المؤهل.
- Optimizer: يحسب أرخص تكلفة نهائية من العروض المتاحة ويقارنها بأرخص سعر STANDARD مطابق، لا بأعلى سعر تسويقي.

## التشغيل
```bash
cd server
npm install
npm run build
npm test
BOOKING_API_TOKEN=... BOOKING_AFFILIATE_ID=... npm start
```

`POST /v1/search`
```json
{
  "city": "جدة",
  "checkIn": "2026-10-01",
  "checkOut": "2026-10-03",
  "adults": 2,
  "rooms": 1,
  "currency": "SAR",
  "cards": [{"bank":"SNB","network":"Visa","tier":"Signature"}]
}
```

## قاعدة النزاهة
`DISCOVERED` = وجدنا ذكر العرض. `LIVE_VERIFIED` = السعر جاء من مزود حي لنفس معايير البحث. لا يتحول ذكر على السوشال إلى «وفرت» إلا بعد تحويله لقاعدة عرض موثقة مع مصدر وشروط ثم إعادة التحقق من سعر الحجز.
