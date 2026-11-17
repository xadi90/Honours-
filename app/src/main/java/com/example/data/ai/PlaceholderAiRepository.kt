package com.example.data.ai

import com.example.data.model.AcademicYearLevel
import com.example.data.model.AiQuickTool
import com.example.data.model.AnswerLengthOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Sample/placeholder implementation used while Firebase AI Logic is not provisioned.
 * Every response is clearly labeled as a preview sample and never fabricates scholarly citations.
 */
class PlaceholderAiRepository : AiRepository {

    override val isLiveGeminiConnected: Boolean = false

    override val noticeBannerBn: String =
        "AI প্রিভিউ মোড: লাইভ Gemini AI-এর জন্য Firebase (Firebase AI Logic) সক্রিয় করা প্রয়োজন। বর্তমানে নমুনা একাডেমিক কাঠামো প্রদর্শিত হচ্ছে।"

    override fun solveQuestionStream(request: AiSolveRequest): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Analyzing)
        delay(350)

        // Validate image clarity if images were attached without text
        if (request.images.isNotEmpty()) {
            val tinyOrBlank = request.images.any { it.width < 120 || it.height < 120 }
            if (tinyOrBlank) {
                emit(
                    AiStreamEvent.Error(
                        message = "ছবিটি অস্পষ্ট বা অত্যন্ত ছোট। অনুগ্রহ করে আরও স্পষ্ট ছবি তুলুন অথবা ক্রপ করে আপলোড করুন।",
                        isUnreadableImage = true
                    )
                )
                return@flow
            }
        }

        val yearLabel = AcademicYearLevel.fromYear(request.academicYear).titleBn
        val effectiveQuestion = when {
            request.questionText.isNotBlank() -> request.questionText.trim()
            request.images.isNotEmpty() ->
                "[ছবি থেকে শনাক্তকৃত নমুনা প্রশ্ন — ${request.images.size}টি ছবি বিশ্লেষিত]: '${request.paperTitle}' বিষয়ের প্রদত্ত প্রশ্নের একাডেমিক সমাধান"
            else -> ""
        }

        if (effectiveQuestion.isBlank()) {
            emit(AiStreamEvent.Error("অনুগ্রহ করে প্রশ্ন লিখুন অথবা প্রশ্নের ছবি যুক্ত করুন।"))
            return@flow
        }

        val isFollowUp = request.conversationHistory.isNotEmpty()
        val responseBody = buildStructuredSampleAnswer(
            question = effectiveQuestion,
            yearLabel = yearLabel,
            paperTitle = request.paperTitle,
            answerLength = request.answerLength,
            hasImages = request.images.isNotEmpty(),
            isFollowUp = isFollowUp
        )

        // Stream word-by-word chunks smoothly for realistic TTFT UX
        val chunks = responseBody.chunked(36)
        val builder = StringBuilder()
        for (chunk in chunks) {
            builder.append(chunk)
            emit(AiStreamEvent.Streaming(builder.toString()))
            delay(28)
        }
        emit(
            AiStreamEvent.Completed(
                fullText = builder.toString(),
                detectedQuestionText = if (request.questionText.isBlank() && request.images.isNotEmpty()) effectiveQuestion else null
            )
        )
    }

    override fun applyQuickToolStream(
        previousAnswer: String,
        originalQuestion: String,
        tool: AiQuickTool,
        academicYear: Int,
        paperTitle: String
    ): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Analyzing)
        delay(300)

        val result = when (tool) {
            AiQuickTool.SIMPLIFY -> """
                [নমুনা AI টুল: সহজ ভাষায় ব্যাখ্যা]
                প্রশ্ন: $originalQuestion
                
                সহজ সারসংক্ষেপ ($paperTitle):
                • মূল বিষয়টি হলো রাষ্ট্র ও রাজনৈতিক প্রতিষ্ঠানগুলো কীভাবে জনগণের কল্যাণে নিয়মতান্ত্রিকভাবে ক্ষমতা প্রয়োগ করে।
                • কঠিন তাত্ত্বিক পরিভাষা বাদ দিয়ে মনে রাখবেন—প্রতিটি রাজনৈতিক ধারণার পেছনে দুটি দিক থাকে: (১) তাত্ত্বিক আদর্শ এবং (২) বাস্তব রাষ্ট্রীয় প্রয়োগ।
                • পরীক্ষায় প্রথমে সহজ সংজ্ঞা লিখে ৩–৪টি বাস্তব উদাহরণ দিলে পূর্ণ নম্বর পাওয়া সহজ হয়।
                
                (নোট: এটি একটি নমুনা উত্তর। লাইভ Gemini অনুবাদ/ব্যাখ্যার জন্য Firebase AI Logic সক্রিয় করুন।)
            """.trimIndent()

            AiQuickTool.EXPLAIN_DETAIL -> """
                [নমুনা AI টুল: বিস্তারিত একাডেমিক ব্যাখ্যা]
                $previousAnswer
                
                অতিরিক্ত তাত্ত্বিক গভীরতা:
                সমসাময়িক রাষ্ট্রবিজ্ঞানে বিষয়টি কেবল প্রতিষ্ঠানিক কাঠামোর মধ্যে সীমাবদ্ধ নয়, বরং আচরণবাদী (Behavioral) ও উত্তর-আচরণবাদী (Post-Behavioral) দৃষ্টিভঙ্গি থেকেও বিশ্লেষিত হয়। জাতীয় বিশ্ববিদ্যালয়ের রচনামূলক উত্তরে তুলনামূলক বিশ্লেষণ ও বাংলাদেশ প্রেক্ষাপট সংযুক্ত করলে উত্তরটি আরও সমৃদ্ধ হবে।
            """.trimIndent()

            AiQuickTool.WITH_EXAMPLES -> """
                [নমুনা AI টুল: বাস্তব উদাহরণসহ ব্যাখ্যা]
                প্রশ্ন: $originalQuestion
                
                প্রাসঙ্গিক উদাহরণসমূহ:
                ১. বাংলাদেশ প্রেক্ষাপট: ১৯৭২ সালের সংবিধানের মৌলিক অধিকার ও সংসদীয় দায়বদ্ধতার কাঠামো।
                ২. তুলনামূলক উদাহরণ: যুক্তরাজ্যের ওয়েস্টমিনিস্টার সংসদীয় প্রথা বনাম যুক্তরাষ্ট্রের ক্ষমতা বিভাজন (Checks and Balances)।
                ৩. আন্তর্জাতিক প্রেক্ষাপট: জাতিসংঘ ও আঞ্চলিক জোটগুলোর বহুপাক্ষিক কূটনীতি।
                
                (নমুনা প্রিভিউ — লাইভ AI উত্তরের জন্য Firebase প্রয়োজন।)
            """.trimIndent()

            AiQuickTool.TRANSLATE_EN -> """
                [Sample AI Tool: English Translation]
                Topic: $originalQuestion ($paperTitle)
                
                Academic Overview:
                1. Introduction: In Political Science, this concept examines the institutional foundations, distribution of authority, and constitutional accountability within the modern state.
                2. Core Analysis: Scholars evaluate both normative political theory and empirical governance practices in Bangladesh and comparative political systems.
                3. Conclusion: Effective governance requires balancing constitutional principles with democratic participation.
                
                (Preview Notice: Enable Firebase AI Logic for full live Gemini translation.)
            """.trimIndent()

            AiQuickTool.TRANSLATE_BN -> """
                [নমুনা AI টুল: বাংলায় অনুবাদ ও পরিমার্জন]
                প্রশ্ন: $originalQuestion
                
                প্রাঞ্জল বাংলা সংস্করণ:
                রাষ্ট্রবিজ্ঞানের মৌলিক আলোচনায় সংশ্লিষ্ট বিষয়টি রাষ্ট্র, সরকার ও নাগরিক অধিকারের পারস্পরিক সম্পর্ককে স্পষ্ট করে। বাংলাদেশের সাংবিধানিক ও প্রশাসনিক প্রেক্ষাপটে এর প্রয়োগ অত্যন্ত তাৎপর্যপূর্ণ।
                
                (নমুনা প্রিভিউ — পূর্ণাঙ্গ অনুবাদের জন্য Firebase AI Logic প্রয়োজন।)
            """.trimIndent()

            AiQuickTool.KEY_POINTS -> """
                [নমুনা AI টুল: পরীক্ষার গুরুত্বপূর্ণ পয়েন্ট]
                প্রশ্ন: $originalQuestion
                
                দ্রুত মনে রাখার বুলেট পয়েন্ট:
                ✓ ১. মূল সংজ্ঞা ও প্রবক্তা (সঠিক ইংরেজি পরিভাষাসহ)
                ✓ ২. প্রধান ৩–৪টি বৈশিষ্ট্য ও শ্রেণিবিভাগ
                ✓ ৩. সাংবিধানিক বা প্রাতিষ্ঠানিক ভিত্তি
                ✓ ৪. বাংলাদেশের বাস্তব প্রেক্ষাপটে প্রাসঙ্গিক উদাহরণ
                ✓ ৫. সীমাবদ্ধতা ও সমালোচনামূলক মূল্যায়ন
                ✓ ৬. সংক্ষিপ্ত ও ভারসাম্যপূর্ণ উপসংহার
            """.trimIndent()

            AiQuickTool.GENERATE_MCQ -> """
                [নমুনা AI টুল: অনুশীলনের জন্য ৩টি MCQ]
                বিষয়: $paperTitle
                
                ১. প্রশ্ন: আধুনিক রাষ্ট্রবিজ্ঞানে সার্বভৌমত্বের কয়টি দিক রয়েছে?
                (ক) ১টি  (খ) ২টি  (গ) ৩টি  (ঘ) ৪টি
                উত্তর: (খ) ২টি (অভ্যন্তরীণ ও বাহ্যিক সার্বভৌমত্ব)।
                
                ২. প্রশ্ন: কোন পদ্ধতিতে তুলনামূলক রাজনীতি সবচেয়ে কার্যকরভাবে বিশ্লেষিত হয়?
                (ক) কেবল বর্ণনামূলক  (খ) প্রাতিষ্ঠানিক ও আচরণবাদী সমন্বয়  (গ) কাল্পনিক  (ঘ) কোনোটিই নয়
                উত্তর: (খ) প্রাতিষ্ঠানিক ও আচরণবাদী সমন্বয়।
                
                (নমুনা প্রিভিউ — কাস্টম AI MCQ তৈরির জন্য Firebase AI Logic সক্রিয় করুন।)
            """.trimIndent()

            AiQuickTool.EXTRA_QUESTIONS -> """
                [নমুনা AI টুল: সম্পর্কিত অতিরিক্ত সম্ভাব্য প্রশ্ন]
                বিষয়: $paperTitle
                
                ১. অতি সংক্ষিপ্ত: আলোচ্য বিষয়টির মূল ইংরেজি পরিভাষা ও প্রবক্তা কে?
                ২. সংক্ষিপ্ত: বর্তমান বাংলাদেশের শাসনব্যবস্থায় এই ধারণার দুটি প্রভাব লেখো।
                ৩. রচনামূলক: "$originalQuestion"—প্রসঙ্গটির ঐতিহাসিক বিবর্তন ও সমসাময়িক তাৎপর্য মূল্যায়ন করো।
            """.trimIndent()

            AiQuickTool.SHORTEN -> """
                [নমুনা AI টুল: সংক্ষিপ্ত সংস্করণ]
                প্রশ্ন: $originalQuestion
                
                সংক্ষিপ্ত উত্তর:
                $paperTitle-এর আলোকে বলা যায়, প্রদত্ত বিষয়টি রাষ্ট্রের সাংগঠনিক কাঠামো, ক্ষমতা বণ্টন ও নাগরিক দায়বদ্ধতার সাথে সরাসরি সম্পৃক্ত। পরীক্ষার খাতায় ১টি প্রামাণ্য সংজ্ঞা ও ৪টি মূল পয়েন্ট উপস্থাপন করলেই পূর্ণ সংক্ষিপ্ত উত্তর সম্পন্ন হয়।
            """.trimIndent()

            AiQuickTool.EXPAND -> """
                [নমুনা AI টুল: বর্ধিত রচনামূলক সংস্করণ]
                $previousAnswer
                
                [বর্ধিত বিশ্লেষণ অংশ]:
                তাত্ত্বিক ও ব্যবহারিক উভয় দৃষ্টিকোণ থেকে বিষয়টি মূল্যায়ন করলে দেখা যায়, উন্নয়নশীল গণতান্ত্রিক রাষ্ট্রগুলোতে প্রাতিষ্ঠানিক দুর্বলতা কাটিয়ে উঠতে স্বচ্ছতা (Transparency), জবাবদিহিতা (Accountability) ও আইনের শাসন (Rule of Law) অপরিহার্য।
            """.trimIndent()
        }

        val chunks = result.chunked(32)
        val sb = StringBuilder()
        for (c in chunks) {
            sb.append(c)
            emit(AiStreamEvent.Streaming(sb.toString()))
            delay(22)
        }
        emit(AiStreamEvent.Completed(sb.toString()))
    }

    private fun buildStructuredSampleAnswer(
        question: String,
        yearLabel: String,
        paperTitle: String,
        answerLength: AnswerLengthOption,
        hasImages: Boolean,
        isFollowUp: Boolean
    ): String {
        val header = buildString {
            appendLine("⚠️ [নমুনা AI উত্তর — Firebase সংযোগবিহীন প্রিভিউ মোড]")
            appendLine("স্তর: $yearLabel | পত্র: $paperTitle | দৈর্ঘ্য: ${answerLength.labelBn}")
            if (hasImages) appendLine("OCR ইনপুট: আপলোডকৃত ছবি থেকে প্রশ্ন শনাক্তকরণ সম্পন্ন")
            if (isFollowUp) appendLine("ফলো-আপ প্রেক্ষাপট: পূর্ববর্তী আলোচনার ধারাবাহিকতায় উত্তর প্রদান করা হয়েছে")
            appendLine()
        }

        val body = when (answerLength) {
            AnswerLengthOption.VERY_SHORT -> """
                উত্তর (অতি সংক্ষিপ্ত):
                "$question" — রাষ্ট্রবিজ্ঞানের দৃষ্টিতে এটি একটি মৌলিক রাজনৈতিক ধারণা যা রাষ্ট্র, সংবিধান ও শাসনপ্রক্রিয়ার নিয়মতান্ত্রিক সম্পর্ককে নির্দেশ করে। (নির্ভুল উদ্ধৃতি ও তথ্য যাচাইয়ের জন্য পাঠ্যবই ও অধ্যায়ভিত্তিক নোট দেখুন।)
            """.trimIndent()

            AnswerLengthOption.SHORT -> """
                সংজ্ঞা ও মূল বক্তব্য:
                "$question" বিষয়টি $paperTitle কোর্সের একটি গুরুত্বপূর্ণ অংশ। সংক্ষেপে এর প্রধান দিকসমূহ হলো:
                ১. প্রাতিষ্ঠানিক ভিত্তি: সাংবিধানিক ও আইনগত কাঠামোর আওতায় ক্ষমতা প্রয়োগ।
                ২. রাজনৈতিক তাৎপর্য: নাগরিক অংশগ্রহণ ও রাষ্ট্রীয় জবাবদিহিতা নিশ্চিতকরণ।
                ৩. বাস্তব প্রয়োগ: বাংলাদেশের শাসনব্যবস্থা ও তুলনামূলক রাজনীতিতে এর সুস্পষ্ট প্রভাব।
            """.trimIndent()

            AnswerLengthOption.MEDIUM,
            AnswerLengthOption.DETAILED,
            AnswerLengthOption.EXAM_READY -> """
                ১. ভূমিকা:
                জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান ($yearLabel — $paperTitle) পাঠ্যক্রমে "$question" একটি অত্যন্ত গুরুত্বপূর্ণ ও পরীক্ষামুখী বিষয়। আধুনিক রাষ্ট্রব্যবস্থা, রাজনৈতিক প্রতিষ্ঠান ও শাসন প্রক্রিয়া অনুধাবনের জন্য এই ধারণাটির স্বচ্ছ জ্ঞান অপরিহার্য।

                ২. সংজ্ঞা:
                রাষ্ট্রবিজ্ঞানের প্রচলিত একাডেমিক পরিভাষায় এই ধারণাটি এমন একটি সুসংগঠিত রাজনৈতিক ও সামাজিক প্রক্রিয়াকে নির্দেশ করে, যার মাধ্যমে রাষ্ট্রের নীতি নির্ধারণ, ক্ষমতা বণ্টন এবং নাগরিক অধিকারের সুরক্ষা পরিচালিত হয়।

                ৩. মূল আলোচনা:
                বিষয়টিকে দুটি প্রধান দৃষ্টিভঙ্গি থেকে বিশ্লেষণ করা যায়—(ক) ঐতিহ্যগত প্রাতিষ্ঠানিক দৃষ্টিভঙ্গি (Traditional Institutional Approach) এবং (খ) আধুনিক আচরণবাদী ও ব্যবস্থাগত দৃষ্টিভঙ্গি (Behavioral & Systems Approach)।

                ৪. বৈশিষ্ট্য:
                • সুনির্দিষ্ট সাংবিধানিক বা প্রাতিষ্ঠানিক কাঠামো।
                • ক্ষমতা ও দায়িত্বের ভারসাম্যপূর্ণ বণ্টন।
                • সমাজ, অর্থনীতি ও রাজনৈতিক সংস্কৃতির সাথে পারস্পরিক মিথস্ক্রিয়া।
                • সময় ও পরিবেশের চাহিদা অনুযায়ী বিবর্তনশীলতা।

                ৫. প্রয়োজনীয় ব্যাখ্যা:
                তৃতীয় বিশ্বের উন্নয়নশীল রাষ্ট্র, বিশেষত বাংলাদেশের প্রেক্ষাপটে প্রতিষ্ঠানগুলোর কার্যকারিতা কেবল লিখিত আইনের ওপর নির্ভর করে না; বরং রাজনৈতিক সদিচ্ছা, প্রশাসনিক দক্ষতা এবং সচেতন নাগরিক সমাজের ওপরও নির্ভরশীল।

                ৬. উদাহরণ:
                তুলনামূলক উদাহরণ হিসেবে যুক্তরাজ্যের সংসদীয় প্রথা, যুক্তরাষ্ট্রের ফেডারেল ক্ষমতা বিভাজন এবং বাংলাদেশের ১৯৭২ সালের সংবিধানের মৌলিক কাঠামো উল্লেখযোগ্য।

                ৭. বিশ্লেষণ:
                একাডেমিক মূল্যায়নে দেখা যায়, তাত্ত্বিক আদর্শ এবং বাস্তব প্রয়োগের মধ্যে সর্বদা কিছু ব্যবধান থাকে। এই ব্যবধান কমাতে স্বচ্ছ নির্বাচন, স্বাধীন বিচার বিভাগ ও জবাবদিহিমূলক প্রশাসন অপরিহার্য। (বি.দ্র.: এটি একটি নমুনা উত্তর; লাইভ Gemini থেকে প্রকৃত অধ্যায়ভিত্তিক উদ্ধৃতি ও বিশ্লেষণ পেতে Firebase AI Logic চালু করুন।)

                ৮. উপসংহার:
                পরিশেষে বলা যায়, "$question" সম্পর্কিত ধারণাটি রাষ্ট্রবিজ্ঞান শিক্ষার্থীদের জন্য একটি মৌলিক ভিত্তি রচনা করে, যা রাষ্ট্রীয় নীতি ও গণতান্ত্রিক চর্চাকে আরও সুসংহত করতে সহায়তা করে।
            """.trimIndent()
        }

        return header + body
    }
}
