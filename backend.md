# Wordify — Backend Development Prompt

## Genel Bakış

Wordify, İngilizce-Türkçe kelime öğrenme uygulamasıdır. Bu belge, uygulamanın Java Spring Boot + PostgreSQL backend'ini geliştirmek için gereken tüm gereksinimleri tanımlamaktadır.

---

## Teknoloji Stack

- **Java 21** (veya 17+)
- **Spring Boot 3.x**
    - Spring Web (REST API)
    - Spring Data JPA
    - Spring Validation
- **PostgreSQL 15+**

---

## Veritabanı Şeması

### Tablo: `words`

Kullanıcının kelime listesini tutar.

```sql
CREATE TABLE words (
    id            BIGSERIAL PRIMARY KEY,
    english       VARCHAR(255) NOT NULL,
    turkish       TEXT NOT NULL,
    level         VARCHAR(10),                       -- A1, A2, B1, B2, C1, C2
    success_rate  INTEGER DEFAULT 0,                 -- 0-100 arası başarı oranı
    quiz_count    INTEGER DEFAULT 0,                 -- Kaç kez soruldu (toplam)
    correct_count INTEGER DEFAULT 0,                 -- Kaç kez doğru yanıtlandı
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_asked_at TIMESTAMPTZ,                       -- Son sorulma tarihi
    last_wrong_at TIMESTAMPTZ                        -- Son yanlış yapılma tarihi
);
```

### Tablo: `word_examples`

Bir kelimeye ait örnek cümleleri tutar.

```sql
CREATE TABLE word_examples (
    id      BIGSERIAL PRIMARY KEY,
    word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    sentence TEXT NOT NULL
);
```

### Tablo: `quizzes`

Her tamamlanan quiz oturumunu tutar.

```sql
CREATE TABLE quizzes (
    id              BIGSERIAL PRIMARY KEY,
    quiz_type       VARCHAR(20) NOT NULL,            -- LAST_WRONG | RECENT | RANDOM
    quiz_direction  VARCHAR(20) NOT NULL DEFAULT 'TR_TO_EN', -- TR_TO_EN | EN_TO_TR
    total_count     INTEGER NOT NULL,                -- Toplam soru sayısı
    correct_count   INTEGER NOT NULL,                -- Doğru cevap sayısı
    wrong_count     INTEGER NOT NULL,                -- Yanlış cevap sayısı
    score_percent   INTEGER NOT NULL,                -- correct/total * 100
    quiz_date       TIMESTAMPTZ NOT NULL DEFAULT NOW() -- Quiz tamamlanma tarihi
);
```

### Tablo: `quiz_wrong_words` (ilişki tablosu)

Bir quiz'de yanlış yapılan kelimeleri tutar.

```sql
CREATE TABLE quiz_wrong_words (
    quiz_id  BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    word_id  BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    user_answer TEXT,                                -- Kullanıcının verdiği yanlış cevap
    PRIMARY KEY (quiz_id, word_id)
);
```

---

## REST API Endpoints

### Words API — `/api/words`

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/words` | Tüm kelimeleri listele (sayfalama + arama desteği) |
| `GET` | `/api/words/{id}` | Tek kelime getir |
| `POST` | `/api/words` | Yeni kelime ekle |
| `PUT` | `/api/words/{id}` | Kelime güncelle |
| `DELETE` | `/api/words/{id}` | Kelime sil |
| `GET` | `/api/words/quiz?type=RECENT&count=10` | Quiz için kelime havuzu getir |

#### `GET /api/words` Query Parameters

| Parametre | Tip | Açıklama |
|-----------|-----|----------|
| `q` | String | İngilizce veya Türkçe kelimede arama |
| `level` | String | Seviye filtresi (A1-C2) |
| `page` | Integer | Sayfa no (default 0) |
| `size` | Integer | Sayfa boyutu (default 20) |
| `sort` | String | Sıralama alanı (created_at, success_rate, english) |

#### `GET /api/words/quiz` Query Parameters

| Parametre | Tip | Açıklama |
|-----------|-----|----------|
| `type` | String | `LAST_WRONG` \| `RECENT` \| `RANDOM` |
| `count` | Integer | Kaç kelime döndürülsün (default 10, max 50) |

**`LAST_WRONG` davranışı:** `last_wrong_at IS NOT NULL ORDER BY last_wrong_at DESC`  
**`RECENT` davranışı:** `ORDER BY created_at DESC LIMIT count`  
**`RANDOM` davranışı:** `ORDER BY RANDOM() LIMIT count`

#### Request Body — `POST /api/words`

```json
{
  "english": "Resilient",
  "turkish": "Dirençli, Dayanıklı",
  "level": "B2",
  "examples": [
    "She is a resilient person.",
    "Children are surprisingly resilient."
  ]
}
```

#### Response — Word

```json
{
  "id": 1,
  "english": "Resilient",
  "turkish": "Dirençli, Dayanıklı",
  "level": "B2",
  "examples": [
    { "id": 10, "sentence": "She is a resilient person." },
    { "id": 11, "sentence": "Children are surprisingly resilient." }
  ],
  "successRate": 72,
  "quizCount": 9,
  "correctCount": 6,
  "createdAt": "2024-01-15T10:30:00Z",
  "lastAskedAt": "2024-03-01T18:00:00Z",
  "lastWrongAt": "2024-02-28T09:15:00Z"
}
```

---

### Quizzes API — `/api/quizzes`

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `POST` | `/api/quizzes` | Tamamlanan quiz kaydını oluştur |
| `GET` | `/api/quizzes` | Quiz geçmişini listele (sayfalama) |
| `GET` | `/api/quizzes/{id}` | Tek quiz detayını getir (yanlış kelimelerle birlikte) |
| `GET` | `/api/quizzes/stats` | Genel istatistikler |

#### Request Body — `POST /api/quizzes`

Quiz tamamlandığında frontend tek bir istek ile quiz sonucunu ve yanlış kelimeleri kaydeder.

```json
{
  "quizType": "RECENT",
  "quizDirection": "TR_TO_EN",
  "totalCount": 10,
  "correctCount": 8,
  "wrongCount": 2,
  "wrongWords": [
    {
      "wordId": 6,
      "userAnswer": "Short-lived"
    },
    {
      "wordId": 7,
      "userAnswer": "Slow"
    }
  ]
}
```

Bu endpoint aynı zamanda:
- `words` tablosunda ilgili kelimelerin `quiz_count`, `correct_count`, `success_rate`, `last_asked_at` alanlarını günceller
- Yanlış yapılan kelimeler için `last_wrong_at` alanını günceller

#### Response — Quiz

```json
{
  "id": 42,
  "quizType": "RECENT",
  "quizDirection": "TR_TO_EN",
  "totalCount": 10,
  "correctCount": 8,
  "wrongCount": 2,
  "scorePercent": 80,
  "quizDate": "2024-03-01T18:05:30Z",
  "wrongWords": [
    {
      "wordId": 6,
      "english": "Ephemeral",
      "turkish": "Geçici, Kısa Ömürlü",
      "userAnswer": "Short-lived"
    }
  ]
}
```

#### Response — `GET /api/quizzes/stats`

```json
{
  "totalWords": 124,
  "totalQuizzes": 56,
  "averageScore": 73,
  "totalQuestionsAnswered": 560,
  "overallSuccessRate": 68
}
```

---

## İş Kuralları

1. **`success_rate` hesaplama:** Her quiz sonrası `ROUND(correct_count::numeric / quiz_count * 100)` ile güncellenir.
2. **`LAST_WRONG` quiz tipi:** `last_wrong_at IS NOT NULL` olan kelimelerden oluşur; hiç yanlış yoksa random döner.
3. **Soft delete yok:** Silme işlemi kalıcıdır; `quiz_wrong_words`'deki kayıtlar CASCADE ile silinir.
4. **Validasyon:**
    - `english` ve `turkish` alanları zorunlu ve boş olamaz.
    - `level` alanı: `A1 | A2 | B1 | B2 | C1 | C2` veya null.
    - `count` parametresi max 50 olabilir.
5. **Quiz kaydı:** Frontend quiz sırasında word state'i takip eder; sona erince tek toplu istek gönderir.

---

## Paket Yapısı (Önerilen)

```
com.wordify
├── controller
│   ├── WordController.java
│   └── QuizController.java
├── service
│   ├── WordService.java
│   └── QuizService.java
├── repository
│   ├── WordRepository.java
│   ├── WordExampleRepository.java
│   └── QuizRepository.java
├── entity
│   ├── Word.java
│   ├── WordExample.java
│   ├── Quiz.java
│   └── QuizWrongWord.java
├── dto
│   ├── WordRequest.java
│   ├── WordResponse.java
│   ├── QuizRequest.java
│   ├── QuizResponse.java
│   └── StatsResponse.java
└── exception
    └── ResourceNotFoundException.java
```

---

## CORS & Frontend Entegrasyonu

Frontend `http://localhost:5173` adresinde çalışmaktadır. Backend'de bu origin'e CORS izni verilmeli. Cors izni her yer vere public olsun. (***)

Frontend entegrasyon noktaları (`AppContext.jsx`):
- `startQuiz()` → `GET /api/words/quiz?type=...&count=10`
- `answerQuestion()` sonrası quiz bitince → `POST /api/quizzes`
- `addWord()` → `POST /api/words`
- `deleteWord()` → `DELETE /api/words/{id}`
- Word management sayfası → `GET /api/words?q=...&page=...`
- Home stats → `GET /api/quizzes/stats`
