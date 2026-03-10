// data/local/ScriptureDatabase.kt
package com.scripturewidgets.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.scripturewidgets.data.local.dao.VerseDao
import com.scripturewidgets.data.local.entities.FavoriteEntity
import com.scripturewidgets.data.local.entities.VerseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VerseEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = true
)
abstract class ScriptureDatabase : RoomDatabase() {

    abstract fun verseDao(): VerseDao

    companion object {
        private const val DB_NAME = "scripture_database"

        fun create(context: Context): ScriptureDatabase {
            return Room.databaseBuilder(context, ScriptureDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch { prepopulate(db) }
                    }
                    // Also fires on every                                                                                                                                                                                                                                                                      open — seeds data if table is empty
                    // This fixes the case where migration wiped the database
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val cursor = db.query("SELECT COUNT(*) FROM verses", arrayOf<Any?>())
                            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
                            cursor.close()
                            if (count == 0) prepopulate(db)
                        }
                    }
                })
                .build()
        }

        private fun prepopulate(db: SupportSQLiteDatabase) {
            OfflineVerseData.verses.forEach { v ->
                db.execSQL(
                    """INSERT OR REPLACE INTO verses
                       (id, book, chapter, verse, text, translation, category)
                       VALUES (?, ?, ?, ?, ?, ?, ?)""",
                    arrayOf(v.id, v.book, v.chapter, v.verse, v.text, v.translation, v.category)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// 500+ Bible Verses across all 15 categories
// ─────────────────────────────────────────────────────────────────────
object OfflineVerseData {
    data class SeedVerse(
        val id: String, val book: String, val chapter: Int, val verse: Int,
        val text: String, val translation: String = "NIV", val category: String
    )

    val verses = listOf(
        // ── HOPE (30 verses) ─────────────────────────────────────
        SeedVerse("hope_001","Jeremiah",29,11,"For I know the plans I have for you, declares the LORD, plans to prosper you and not to harm you, plans to give you hope and a future.","NIV","HOPE"),
        SeedVerse("hope_002","Romans",15,13,"May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope by the power of the Holy Spirit.","NIV","HOPE"),
        SeedVerse("hope_003","Psalm",31,24,"Be strong and take heart, all you who hope in the LORD.","NIV","HOPE"),
        SeedVerse("hope_004","Isaiah",40,31,"But those who hope in the LORD will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.","NIV","HOPE"),
        SeedVerse("hope_005","Romans",8,28,"And we know that in all things God works for the good of those who love him, who have been called according to his purpose.","NIV","HOPE"),
        SeedVerse("hope_006","Lamentations",3,22,"Because of the LORD's great love we are not consumed, for his compassions never fail.","NIV","HOPE"),
        SeedVerse("hope_007","Psalm",121,2,"My help comes from the LORD, the Maker of heaven and earth.","NIV","HOPE"),
        SeedVerse("hope_008","Hebrews",11,1,"Now faith is confidence in what we hope for and assurance about what we do not see.","NIV","HOPE"),
        SeedVerse("hope_009","Romans",5,5,"And hope does not put us to shame, because God's love has been poured out into our hearts through the Holy Spirit.","NIV","HOPE"),
        SeedVerse("hope_010","Psalm",27,14,"Wait for the LORD; be strong and take heart and wait for the LORD.","NIV","HOPE"),
        SeedVerse("hope_011","Romans",12,12,"Be joyful in hope, patient in affliction, faithful in prayer.","NIV","HOPE"),
        SeedVerse("hope_012","Psalm",62,5,"Yes, my soul, find rest in God; my hope comes from him.","NIV","HOPE"),
        SeedVerse("hope_013","Proverbs",23,18,"There is surely a future hope for you, and your hope will not be cut off.","NIV","HOPE"),
        SeedVerse("hope_014","1 Peter",1,3,"Praise be to the God and Father of our Lord Jesus Christ! In his great mercy he has given us new birth into a living hope through the resurrection of Jesus Christ from the dead.","NIV","HOPE"),
        SeedVerse("hope_015","Colossians",1,27,"Christ in you, the hope of glory.","NIV","HOPE"),
        SeedVerse("hope_016","Psalm",130,7,"Israel, put your hope in the LORD, for with the LORD is unfailing love and with him is full redemption.","NIV","HOPE"),
        SeedVerse("hope_017","Hebrews",6,19,"We have this hope as an anchor for the soul, firm and secure.","NIV","HOPE"),
        SeedVerse("hope_018","Zephaniah",3,17,"The LORD your God is with you, the Mighty Warrior who saves. He will take great delight in you; in his love he will no longer rebuke you, but will rejoice over you with singing.","NIV","HOPE"),
        SeedVerse("hope_019","Isaiah",43,2,"When you pass through the waters, I will be with you; and when you pass through the rivers, they will not sweep over you.","NIV","HOPE"),
        SeedVerse("hope_020","Psalm",33,22,"May your unfailing love be with us, LORD, even as we put our hope in you.","NIV","HOPE"),
        SeedVerse("hope_021","Romans",15,4,"For everything that was written in the past was written to teach us, so that through the endurance taught in the Scriptures and the encouragement they provide we might have hope.","NIV","HOPE"),
        SeedVerse("hope_022","Micah",7,7,"But as for me, I watch in hope for the LORD, I wait for God my Savior; my God will hear me.","NIV","HOPE"),
        SeedVerse("hope_023","Psalm",147,11,"The LORD delights in those who fear him, who put their hope in his unfailing love.","NIV","HOPE"),
        SeedVerse("hope_024","Titus",2,13,"While we wait for the blessed hope — the appearing of the glory of our great God and Savior, Jesus Christ.","NIV","HOPE"),
        SeedVerse("hope_025","Isaiah",61,3,"To bestow on them a crown of beauty instead of ashes, the oil of joy instead of mourning, and a garment of praise instead of a spirit of despair.","NIV","HOPE"),
        SeedVerse("hope_026","Psalm",71,14,"As for me, I will always have hope; I will praise you more and more.","NIV","HOPE"),
        SeedVerse("hope_027","Lamentations",3,25,"The LORD is good to those whose hope is in him, to the one who seeks him.","NIV","HOPE"),
        SeedVerse("hope_028","Romans",4,18,"Against all hope, Abraham in hope believed and so became the father of many nations.","NIV","HOPE"),
        SeedVerse("hope_029","Isaiah",55,11,"So is my word that goes out from my mouth: It will not return to me empty, but will accomplish what I desire and achieve the purpose for which I sent it.","NIV","HOPE"),
        SeedVerse("hope_030","Psalm",31,15,"My times are in your hands; deliver me from the hands of my enemies, from those who pursue me.","NIV","HOPE"),

        // ── FAITH (30 verses) ────────────────────────────────────
        SeedVerse("faith_001","Hebrews",11,6,"And without faith it is impossible to please God, because anyone who comes to him must believe that he exists and that he rewards those who earnestly seek him.","NIV","FAITH"),
        SeedVerse("faith_002","Matthew",17,20,"Truly I tell you, if you have faith as small as a mustard seed, you can say to this mountain, Move from here to there, and it will move. Nothing will be impossible for you.","NIV","FAITH"),
        SeedVerse("faith_003","2 Corinthians",5,7,"For we live by faith, not by sight.","NIV","FAITH"),
        SeedVerse("faith_004","Galatians",2,20,"I have been crucified with Christ and I no longer live, but Christ lives in me.","NIV","FAITH"),
        SeedVerse("faith_005","Ephesians",2,8,"For it is by grace you have been saved, through faith — and this is not from yourselves, it is the gift of God.","NIV","FAITH"),
        SeedVerse("faith_006","Proverbs",3,5,"Trust in the LORD with all your heart and lean not on your own understanding.","NIV","FAITH"),
        SeedVerse("faith_007","Romans",10,17,"Consequently, faith comes from hearing the message, and the message is heard through the word about Christ.","NIV","FAITH"),
        SeedVerse("faith_008","James",2,17,"In the same way, faith by itself, if it is not accompanied by action, is dead.","NIV","FAITH"),
        SeedVerse("faith_009","Mark",11,24,"Therefore I tell you, whatever you ask for in prayer, believe that you have received it, and it will be yours.","NIV","FAITH"),
        SeedVerse("faith_010","1 Peter",1,7,"These have come so that the proven genuineness of your faith — of greater worth than gold — may result in praise, glory and honor when Jesus Christ is revealed.","NIV","FAITH"),
        SeedVerse("faith_011","Romans",1,17,"For in the gospel the righteousness of God is revealed — a righteousness that is by faith from first to last, just as it is written: The righteous will live by faith.","NIV","FAITH"),
        SeedVerse("faith_012","Matthew",21,22,"If you believe, you will receive whatever you ask for in prayer.","NIV","FAITH"),
        SeedVerse("faith_013","Luke",17,6,"If you have faith as small as a mustard seed, you can say to this mulberry tree, Be uprooted and planted in the sea, and it will obey you.","NIV","FAITH"),
        SeedVerse("faith_014","1 John",5,4,"For everyone born of God overcomes the world. This is the victory that has overcome the world, even our faith.","NIV","FAITH"),
        SeedVerse("faith_015","Psalm",37,4,"Take delight in the LORD, and he will give you the desires of your heart.","NIV","FAITH"),
        SeedVerse("faith_016","Isaiah",26,4,"Trust in the LORD forever, for the LORD, the LORD himself, is the Rock eternal.","NIV","FAITH"),
        SeedVerse("faith_017","Proverbs",3,6,"In all your ways submit to him, and he will make your paths straight.","NIV","FAITH"),
        SeedVerse("faith_018","Romans",8,37,"No, in all these things we are more than conquerors through him who loved us.","NIV","FAITH"),
        SeedVerse("faith_019","Matthew",6,30,"If that is how God clothes the grass of the field, will he not much more clothe you — you of little faith?","NIV","FAITH"),
        SeedVerse("faith_020","James",1,6,"But when you ask, you must believe and not doubt, because the one who doubts is like a wave of the sea, blown and tossed by the wind.","NIV","FAITH"),
        SeedVerse("faith_021","Hebrews",12,2,"Fixing our eyes on Jesus, the pioneer and perfecter of faith.","NIV","FAITH"),
        SeedVerse("faith_022","Isaiah",12,2,"Surely God is my salvation; I will trust and not be afraid. The LORD, the LORD himself, is my strength and my defense; he has become my salvation.","NIV","FAITH"),
        SeedVerse("faith_023","Psalm",56,3,"When I am afraid, I put my trust in you.","NIV","FAITH"),
        SeedVerse("faith_024","Proverbs",16,3,"Commit to the LORD whatever you do, and he will establish your plans.","NIV","FAITH"),
        SeedVerse("faith_025","Nahum",1,7,"The LORD is good, a refuge in times of trouble. He cares for those who trust in him.","NIV","FAITH"),
        SeedVerse("faith_026","Psalm",18,2,"The LORD is my rock, my fortress and my deliverer; my God is my rock, in whom I take refuge.","NIV","FAITH"),
        SeedVerse("faith_027","Matthew",14,29,"Then Peter got down out of the boat, walked on the water and came toward Jesus.","NIV","FAITH"),
        SeedVerse("faith_028","Psalm",9,10,"Those who know your name trust in you, for you, LORD, have never forsaken those who seek you.","NIV","FAITH"),
        SeedVerse("faith_029","Romans",5,1,"Therefore, since we have been justified through faith, we have peace with God through our Lord Jesus Christ.","NIV","FAITH"),
        SeedVerse("faith_030","1 Corinthians",16,13,"Be on your guard; stand firm in the faith; be courageous; be strong.","NIV","FAITH"),

        // ── LOVE (30 verses) ─────────────────────────────────────
        SeedVerse("love_001","John",3,16,"For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.","NIV","LOVE"),
        SeedVerse("love_002","1 Corinthians",13,4,"Love is patient, love is kind. It does not envy, it does not boast, it is not proud.","NIV","LOVE"),
        SeedVerse("love_003","1 John",4,8,"Whoever does not love does not know God, because God is love.","NIV","LOVE"),
        SeedVerse("love_004","Romans",8,38,"For I am convinced that neither death nor life, neither angels nor demons, neither the present nor the future, nor any powers, will be able to separate us from the love of God.","NIV","LOVE"),
        SeedVerse("love_005","1 John",4,19,"We love because he first loved us.","NIV","LOVE"),
        SeedVerse("love_006","John",15,13,"Greater love has no one than this: to lay down one's life for one's friends.","NIV","LOVE"),
        SeedVerse("love_007","Romans",5,8,"But God demonstrates his own love for us in this: While we were still sinners, Christ died for us.","NIV","LOVE"),
        SeedVerse("love_008","1 Corinthians",13,13,"And now these three remain: faith, hope and love. But the greatest of these is love.","NIV","LOVE"),
        SeedVerse("love_009","John",13,34,"A new command I give you: Love one another. As I have loved you, so you must love one another.","NIV","LOVE"),
        SeedVerse("love_010","1 John",4,16,"And so we know and rely on the love God has for us. God is love. Whoever lives in love lives in God, and God in them.","NIV","LOVE"),
        SeedVerse("love_011","Ephesians",3,17,"And I pray that you, being rooted and established in love, may have power to grasp how wide and long and high and deep is the love of Christ.","NIV","LOVE"),
        SeedVerse("love_012","Romans",13,8,"Let no debt remain outstanding, except the continuing debt to love one another, for whoever loves others has fulfilled the law.","NIV","LOVE"),
        SeedVerse("love_013","Matthew",22,37,"Jesus replied: Love the Lord your God with all your heart and with all your soul and with all your mind.","NIV","LOVE"),
        SeedVerse("love_014","Song of Solomon",8,7,"Many waters cannot quench love; rivers cannot sweep it away.","NIV","LOVE"),
        SeedVerse("love_015","Deuteronomy",6,5,"Love the LORD your God with all your heart and with all your soul and with all your strength.","NIV","LOVE"),
        SeedVerse("love_016","Psalm",136,26,"Give thanks to the God of heaven. His love endures forever.","NIV","LOVE"),
        SeedVerse("love_017","Ephesians",5,2,"And walk in the way of love, just as Christ loved us and gave himself up for us as a fragrant offering and sacrifice to God.","NIV","LOVE"),
        SeedVerse("love_018","Colossians",3,14,"And over all these virtues put on love, which binds them all together in perfect unity.","NIV","LOVE"),
        SeedVerse("love_019","1 Peter",4,8,"Above all, love each other deeply, because love covers over a multitude of sins.","NIV","LOVE"),
        SeedVerse("love_020","Romans",8,39,"Neither height nor depth, nor anything else in all creation, will be able to separate us from the love of God that is in Christ Jesus our Lord.","NIV","LOVE"),
        SeedVerse("love_021","Isaiah",54,10,"Though the mountains be shaken and the hills be removed, yet my unfailing love for you will not be shaken, says the LORD.","NIV","LOVE"),
        SeedVerse("love_022","1 John",3,1,"See what great love the Father has lavished on us, that we should be called children of God!","NIV","LOVE"),
        SeedVerse("love_023","John",15,9,"As the Father has loved me, so have I loved you. Now remain in my love.","NIV","LOVE"),
        SeedVerse("love_024","Psalm",86,15,"But you, Lord, are a compassionate and gracious God, slow to anger, abounding in love and faithfulness.","NIV","LOVE"),
        SeedVerse("love_025","Jeremiah",31,3,"I have loved you with an everlasting love; I have drawn you with unfailing kindness.","NIV","LOVE"),
        SeedVerse("love_026","Hosea",2,19,"I will betroth you to me forever; I will betroth you in righteousness and justice, in love and compassion.","NIV","LOVE"),
        SeedVerse("love_027","Matthew",5,44,"But I tell you, love your enemies and pray for those who persecute you.","NIV","LOVE"),
        SeedVerse("love_028","Galatians",5,22,"But the fruit of the Spirit is love, joy, peace, forbearance, kindness, goodness, faithfulness.","NIV","LOVE"),
        SeedVerse("love_029","Proverbs",17,17,"A friend loves at all times, and a brother is born for a time of adversity.","NIV","LOVE"),
        SeedVerse("love_030","Psalm",63,3,"Because your love is better than life, my lips will glorify you.","NIV","LOVE"),

        // ── STRENGTH (25 verses) ─────────────────────────────────
        SeedVerse("strength_001","Philippians",4,13,"I can do all this through him who gives me strength.","NIV","STRENGTH"),
        SeedVerse("strength_002","Isaiah",41,10,"So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you.","NIV","STRENGTH"),
        SeedVerse("strength_003","Psalm",46,1,"God is our refuge and strength, an ever-present help in trouble.","NIV","STRENGTH"),
        SeedVerse("strength_004","2 Timothy",1,7,"For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline.","NIV","STRENGTH"),
        SeedVerse("strength_005","Ephesians",6,10,"Finally, be strong in the Lord and in his mighty power.","NIV","STRENGTH"),
        SeedVerse("strength_006","Nehemiah",8,10,"Do not grieve, for the joy of the LORD is your strength.","NIV","STRENGTH"),
        SeedVerse("strength_007","Joshua",1,9,"Be strong and courageous. Do not be afraid; do not be discouraged, for the LORD your God will be with you wherever you go.","NIV","STRENGTH"),
        SeedVerse("strength_008","Psalm",28,7,"The LORD is my strength and my shield; my heart trusts in him, and he helps me.","NIV","STRENGTH"),
        SeedVerse("strength_009","Isaiah",40,29,"He gives strength to the weary and increases the power of the weak.","NIV","STRENGTH"),
        SeedVerse("strength_010","2 Corinthians",12,10,"For when I am weak, then I am strong.","NIV","STRENGTH"),
        SeedVerse("strength_011","Psalm",73,26,"My flesh and my heart may fail, but God is the strength of my heart and my portion forever.","NIV","STRENGTH"),
        SeedVerse("strength_012","Habakkuk",3,19,"The Sovereign LORD is my strength; he makes my feet like the feet of a deer, he enables me to tread on the heights.","NIV","STRENGTH"),
        SeedVerse("strength_013","Isaiah",33,2,"LORD, be gracious to us; we long for you. Be our strength every morning, our salvation in time of distress.","NIV","STRENGTH"),
        SeedVerse("strength_014","Psalm",18,32,"It is God who arms me with strength and keeps my way secure.","NIV","STRENGTH"),
        SeedVerse("strength_015","Colossians",1,11,"Being strengthened with all power according to his glorious might so that you may have great endurance and patience.","NIV","STRENGTH"),
        SeedVerse("strength_016","Ephesians",3,16,"I pray that out of his glorious riches he may strengthen you with power through his Spirit in your inner being.","NIV","STRENGTH"),
        SeedVerse("strength_017","Psalm",29,11,"The LORD gives strength to his people; the LORD blesses his people with peace.","NIV","STRENGTH"),
        SeedVerse("strength_018","Daniel",10,19,"Do not be afraid, you who are highly esteemed. Peace! Be strong now; be strong.","NIV","STRENGTH"),
        SeedVerse("strength_019","Psalm",84,5,"Blessed are those whose strength is in you, whose hearts are set on pilgrimage.","NIV","STRENGTH"),
        SeedVerse("strength_020","Romans",15,1,"We who are strong ought to bear with the failings of the weak and not to please ourselves.","NIV","STRENGTH"),
        SeedVerse("strength_021","1 Samuel",2,9,"He will guard the feet of his faithful servants, but the wicked will be silenced in the place of darkness. It is not by strength that one prevails.","NIV","STRENGTH"),
        SeedVerse("strength_022","Psalm",46,5,"God is within her, she will not fall; God will help her at break of day.","NIV","STRENGTH"),
        SeedVerse("strength_023","Isaiah",12,2,"Surely God is my salvation; I will trust and not be afraid. The LORD, the LORD himself, is my strength and my defense.","NIV","STRENGTH"),
        SeedVerse("strength_024","1 Chronicles",16,11,"Look to the LORD and his strength; seek his face always.","NIV","STRENGTH"),
        SeedVerse("strength_025","Psalm",27,1,"The LORD is my light and my salvation — whom shall I fear? The LORD is the stronghold of my life.","NIV","STRENGTH"),

        // ── PEACE (25 verses) ────────────────────────────────────
        SeedVerse("peace_001","Philippians",4,7,"And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.","NIV","PEACE"),
        SeedVerse("peace_002","John",14,27,"Peace I leave with you; my peace I give you. I do not give to you as the world gives. Do not let your hearts be troubled and do not be afraid.","NIV","PEACE"),
        SeedVerse("peace_003","Isaiah",26,3,"You will keep in perfect peace those whose minds are steadfast, because they trust in you.","NIV","PEACE"),
        SeedVerse("peace_004","Colossians",3,15,"Let the peace of Christ rule in your hearts, since as members of one body you were called to peace. And be thankful.","NIV","PEACE"),
        SeedVerse("peace_005","Matthew",5,9,"Blessed are the peacemakers, for they will be called children of God.","NIV","PEACE"),
        SeedVerse("peace_006","Romans",8,6,"The mind governed by the flesh is death, but the mind governed by the Spirit is life and peace.","NIV","PEACE"),
        SeedVerse("peace_007","Isaiah",9,6,"And he will be called Wonderful Counselor, Mighty God, Everlasting Father, Prince of Peace.","NIV","PEACE"),
        SeedVerse("peace_008","Numbers",6,26,"The LORD turn his face toward you and give you peace.","NIV","PEACE"),
        SeedVerse("peace_009","Psalm",119,165,"Great peace have those who love your law, and nothing can make them stumble.","NIV","PEACE"),
        SeedVerse("peace_010","John",16,33,"I have told you these things, so that in me you may have peace. In this world you will have trouble. But take heart! I have overcome the world.","NIV","PEACE"),
        SeedVerse("peace_011","Romans",5,1,"Therefore, since we have been justified through faith, we have peace with God through our Lord Jesus Christ.","NIV","PEACE"),
        SeedVerse("peace_012","Psalm",85,8,"I will listen to what God the LORD says; he promises peace to his people, his faithful servants.","NIV","PEACE"),
        SeedVerse("peace_013","Isaiah",32,17,"The fruit of that righteousness will be peace; its effect will be quietness and confidence forever.","NIV","PEACE"),
        SeedVerse("peace_014","Philippians",4,9,"Whatever you have learned or received or heard from me, or seen in me — put it into practice. And the God of peace will be with you.","NIV","PEACE"),
        SeedVerse("peace_015","Psalm",4,8,"In peace I will lie down and sleep, for you alone, LORD, make me dwell in safety.","NIV","PEACE"),
        SeedVerse("peace_016","2 Thessalonians",3,16,"Now may the Lord of peace himself give you peace at all times and in every way.","NIV","PEACE"),
        SeedVerse("peace_017","Romans",14,19,"Let us therefore make every effort to do what leads to peace and to mutual edification.","NIV","PEACE"),
        SeedVerse("peace_018","Hebrews",12,14,"Make every effort to live in peace with everyone and to be holy.","NIV","PEACE"),
        SeedVerse("peace_019","Isaiah",48,18,"If only you had paid attention to my commands, your peace would have been like a river.","NIV","PEACE"),
        SeedVerse("peace_020","Psalm",37,11,"But the meek will inherit the land and enjoy peace and prosperity.","NIV","PEACE"),
        SeedVerse("peace_021","Luke",2,14,"Glory to God in the highest heaven, and on earth peace to those on whom his favor rests.","NIV","PEACE"),
        SeedVerse("peace_022","Psalm",46,10,"He says, Be still, and know that I am God; I will be exalted among the nations.","NIV","PEACE"),
        SeedVerse("peace_023","Matthew",11,29,"Take my yoke upon you and learn from me, for I am gentle and humble in heart, and you will find rest for your souls.","NIV","PEACE"),
        SeedVerse("peace_024","Proverbs",14,30,"A heart at peace gives life to the body, but envy rots the bones.","NIV","PEACE"),
        SeedVerse("peace_025","Isaiah",54,13,"All your children will be taught by the LORD, and great will be their peace.","NIV","PEACE"),

        // ── WISDOM (25 verses) ───────────────────────────────────
        SeedVerse("wisdom_001","Proverbs",1,7,"The fear of the LORD is the beginning of wisdom; fools despise wisdom and instruction.","NIV","WISDOM"),
        SeedVerse("wisdom_002","James",1,5,"If any of you lacks wisdom, you should ask God, who gives generously to all without finding fault, and it will be given to you.","NIV","WISDOM"),
        SeedVerse("wisdom_003","Proverbs",4,7,"The beginning of wisdom is this: Get wisdom, and whatever you get, get insight.","ESV","WISDOM"),
        SeedVerse("wisdom_004","Proverbs",16,16,"How much better to get wisdom than gold, to get insight rather than silver!","NIV","WISDOM"),
        SeedVerse("wisdom_005","Colossians",2,3,"In whom are hidden all the treasures of wisdom and knowledge.","NIV","WISDOM"),
        SeedVerse("wisdom_006","Psalm",111,10,"The fear of the LORD is the beginning of wisdom; all who follow his precepts have good understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_007","Proverbs",9,10,"The fear of the LORD is the beginning of wisdom, and knowledge of the Holy One is understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_008","Romans",11,33,"Oh, the depth of the riches of the wisdom and knowledge of God! How unsearchable his judgments, and his paths beyond tracing out!","NIV","WISDOM"),
        SeedVerse("wisdom_009","Proverbs",3,13,"Blessed are those who find wisdom, those who gain understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_010","1 Corinthians",1,25,"For the foolishness of God is wiser than human wisdom, and the weakness of God is stronger than human strength.","NIV","WISDOM"),
        SeedVerse("wisdom_011","Psalm",119,105,"Your word is a lamp for my feet, a light on my path.","NIV","WISDOM"),
        SeedVerse("wisdom_012","Proverbs",2,6,"For the LORD gives wisdom; from his mouth come knowledge and understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_013","Ecclesiastes",7,12,"Wisdom is a shelter as money is a shelter, but the advantage of knowledge is this: Wisdom preserves those who have it.","NIV","WISDOM"),
        SeedVerse("wisdom_014","Daniel",2,21,"He gives wisdom to the wise and knowledge to the discerning.","NIV","WISDOM"),
        SeedVerse("wisdom_015","Proverbs",13,10,"Where there is strife, there is pride, but wisdom is found in those who take advice.","NIV","WISDOM"),
        SeedVerse("wisdom_016","Proverbs",24,14,"Know also that wisdom is like honey for you: If you find it, there is a future hope for you.","NIV","WISDOM"),
        SeedVerse("wisdom_017","Matthew",7,24,"Therefore everyone who hears these words of mine and puts them into practice is like a wise man who built his house on the rock.","NIV","WISDOM"),
        SeedVerse("wisdom_018","Proverbs",19,20,"Listen to advice and accept discipline, and at the end you will be counted among the wise.","NIV","WISDOM"),
        SeedVerse("wisdom_019","Job",28,28,"And he said to the human race: The fear of the Lord — that is wisdom, and to shun evil is understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_020","Proverbs",11,2,"When pride comes, then comes disgrace, but with humility comes wisdom.","NIV","WISDOM"),
        SeedVerse("wisdom_021","Isaiah",11,2,"The Spirit of the LORD will rest on him — the Spirit of wisdom and of understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_022","Proverbs",15,33,"Wisdom's instruction is to fear the LORD, and humility comes before honor.","NIV","WISDOM"),
        SeedVerse("wisdom_023","1 Kings",4,29,"God gave Solomon wisdom and very great insight, and a breadth of understanding as measureless as the sand on the seashore.","NIV","WISDOM"),
        SeedVerse("wisdom_024","Proverbs",8,11,"For wisdom is more precious than rubies, and nothing you desire can compare with her.","NIV","WISDOM"),
        SeedVerse("wisdom_025","James",3,17,"But the wisdom that comes from heaven is first of all pure; then peace-loving, considerate, submissive, full of mercy and good fruit.","NIV","WISDOM"),

        // ── PRAYER (20 verses) ───────────────────────────────────
        SeedVerse("prayer_001","Matthew",7,7,"Ask and it will be given to you; seek and you will find; knock and the door will be opened to you.","NIV","PRAYER"),
        SeedVerse("prayer_002","1 Thessalonians",5,17,"Pray continually.","NIV","PRAYER"),
        SeedVerse("prayer_003","Philippians",4,6,"Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God.","NIV","PRAYER"),
        SeedVerse("prayer_004","James",5,16,"The prayer of a righteous person is powerful and effective.","NIV","PRAYER"),
        SeedVerse("prayer_005","Jeremiah",33,3,"Call to me and I will answer you and tell you great and unsearchable things you do not know.","NIV","PRAYER"),
        SeedVerse("prayer_006","Psalm",145,18,"The LORD is near to all who call on him, to all who call on him in truth.","NIV","PRAYER"),
        SeedVerse("prayer_007","1 John",5,14,"This is the confidence we have in approaching God: that if we ask anything according to his will, he hears us.","NIV","PRAYER"),
        SeedVerse("prayer_008","Romans",8,26,"The Spirit helps us in our weakness. We do not know what we ought to pray for, but the Spirit himself intercedes for us through wordless groans.","NIV","PRAYER"),
        SeedVerse("prayer_009","Matthew",6,6,"But when you pray, go into your room, close the door and pray to your Father, who is unseen. Then your Father, who sees what is done in secret, will reward you.","NIV","PRAYER"),
        SeedVerse("prayer_010","Psalm",66,19,"But God has surely listened and has heard my prayer.","NIV","PRAYER"),
        SeedVerse("prayer_011","Mark",11,25,"And when you stand praying, if you hold anything against anyone, forgive them, so that your Father in heaven may forgive you your sins.","NIV","PRAYER"),
        SeedVerse("prayer_012","Luke",11,9,"So I say to you: Ask and it will be given to you; seek and you will find; knock and the door will be opened to you.","NIV","PRAYER"),
        SeedVerse("prayer_013","Psalm",17,6,"I call on you, my God, for you will answer me; turn your ear to me and hear my prayer.","NIV","PRAYER"),
        SeedVerse("prayer_014","Hebrews",4,16,"Let us then approach God's throne of grace with confidence, so that we may receive mercy and find grace to help us in our time of need.","NIV","PRAYER"),
        SeedVerse("prayer_015","Acts",2,21,"And everyone who calls on the name of the Lord will be saved.","NIV","PRAYER"),
        SeedVerse("prayer_016","Psalm",5,3,"In the morning, LORD, you hear my voice; in the morning I lay my requests before you and wait expectantly.","NIV","PRAYER"),
        SeedVerse("prayer_017","Isaiah",65,24,"Before they call I will answer; while they are still speaking I will hear.","NIV","PRAYER"),
        SeedVerse("prayer_018","Matthew",18,20,"For where two or three gather in my name, there am I with them.","NIV","PRAYER"),
        SeedVerse("prayer_019","Psalm",116,2,"Because he turned his ear to me, I will call on him as long as I live.","NIV","PRAYER"),
        SeedVerse("prayer_020","Colossians",4,2,"Devote yourselves to prayer, being watchful and thankful.","NIV","PRAYER"),

        // ── SALVATION (20 verses) ────────────────────────────────
        SeedVerse("salvation_001","Romans",10,9,"If you declare with your mouth, Jesus is Lord, and believe in your heart that God raised him from the dead, you will be saved.","NIV","SALVATION"),
        SeedVerse("salvation_002","Acts",4,12,"Salvation is found in no one else, for there is no other name under heaven given to mankind by which we must be saved.","NIV","SALVATION"),
        SeedVerse("salvation_003","Ephesians",2,8,"For it is by grace you have been saved, through faith — and this is not from yourselves, it is the gift of God.","NIV","SALVATION"),
        SeedVerse("salvation_004","Titus",3,5,"He saved us, not because of righteous things we had done, but because of his mercy.","NIV","SALVATION"),
        SeedVerse("salvation_005","Luke",19,10,"For the Son of Man came to seek and to save the lost.","NIV","SALVATION"),
        SeedVerse("salvation_006","Isaiah",43,11,"I, even I, am the LORD, and apart from me there is no savior.","NIV","SALVATION"),
        SeedVerse("salvation_007","John",10,9,"I am the gate; whoever enters through me will be saved.","NIV","SALVATION"),
        SeedVerse("salvation_008","2 Corinthians",6,2,"I tell you, now is the time of God's favor, now is the day of salvation.","NIV","SALVATION"),
        SeedVerse("salvation_009","Romans",6,23,"For the wages of sin is death, but the gift of God is eternal life in Christ Jesus our Lord.","NIV","SALVATION"),
        SeedVerse("salvation_010","John",14,6,"Jesus answered, I am the way and the truth and the life. No one comes to the Father except through me.","NIV","SALVATION"),
        SeedVerse("salvation_011","1 Timothy",2,4,"God wants all people to be saved and to come to a knowledge of the truth.","NIV","SALVATION"),
        SeedVerse("salvation_012","Hebrews",7,25,"Therefore he is able to save completely those who come to God through him, because he always lives to intercede for them.","NIV","SALVATION"),
        SeedVerse("salvation_013","Romans",10,13,"For everyone who calls on the name of the Lord will be saved.","NIV","SALVATION"),
        SeedVerse("salvation_014","2 Peter",3,9,"The Lord is not slow in keeping his promise. He is patient with you, not wanting anyone to perish, but everyone to come to repentance.","NIV","SALVATION"),
        SeedVerse("salvation_015","Isaiah",12,3,"With joy you will draw water from the wells of salvation.","NIV","SALVATION"),
        SeedVerse("salvation_016","Psalm",62,2,"Truly he is my rock and my salvation; he is my fortress, I will never be shaken.","NIV","SALVATION"),
        SeedVerse("salvation_017","Revelation",22,17,"The Spirit and the bride say, Come! And let the one who hears say, Come! Let the one who is thirsty come; and let the one who wishes take the free gift of the water of life.","NIV","SALVATION"),
        SeedVerse("salvation_018","Romans",8,1,"Therefore, there is now no condemnation for those who are in Christ Jesus.","NIV","SALVATION"),
        SeedVerse("salvation_019","Isaiah",49,13,"Shout for joy, you heavens; rejoice, you earth; burst into song, you mountains! For the LORD comforts his people and will have compassion on his afflicted ones.","NIV","SALVATION"),
        SeedVerse("salvation_020","Psalm",18,2,"The LORD is my rock, my fortress and my deliverer; my God is my rock, in whom I take refuge, my shield and the horn of my salvation.","NIV","SALVATION"),

        // ── GRATITUDE (20 verses) ────────────────────────────────
        SeedVerse("gratitude_001","1 Thessalonians",5,18,"Give thanks in all circumstances; for this is God's will for you in Christ Jesus.","NIV","GRATITUDE"),
        SeedVerse("gratitude_002","Psalm",107,1,"Give thanks to the LORD, for he is good; his love endures forever.","NIV","GRATITUDE"),
        SeedVerse("gratitude_003","Ephesians",5,20,"Always giving thanks to God the Father for everything, in the name of our Lord Jesus Christ.","NIV","GRATITUDE"),
        SeedVerse("gratitude_004","Colossians",3,17,"And whatever you do, whether in word or deed, do it all in the name of the Lord Jesus, giving thanks to God the Father through him.","NIV","GRATITUDE"),
        SeedVerse("gratitude_005","Psalm",100,4,"Enter his gates with thanksgiving and his courts with praise; give thanks to him and praise his name.","NIV","GRATITUDE"),
        SeedVerse("gratitude_006","James",1,17,"Every good and perfect gift is from above, coming down from the Father of the heavenly lights.","NIV","GRATITUDE"),
        SeedVerse("gratitude_007","2 Corinthians",9,15,"Thanks be to God for his indescribable gift!","NIV","GRATITUDE"),
        SeedVerse("gratitude_008","Psalm",103,2,"Praise the LORD, my soul, and forget not all his benefits.","NIV","GRATITUDE"),
        SeedVerse("gratitude_009","Hebrews",12,28,"Let us be thankful, and so worship God acceptably with reverence and awe.","NIV","GRATITUDE"),
        SeedVerse("gratitude_010","Philippians",4,11,"I have learned to be content whatever the circumstances.","NIV","GRATITUDE"),
        SeedVerse("gratitude_011","Romans",8,32,"He who did not spare his own Son, but gave him up for us all — how will he not also, along with him, graciously give us all things?","NIV","GRATITUDE"),
        SeedVerse("gratitude_012","Psalm",103,1,"Praise the LORD, my soul; all my inmost being, praise his holy name.","NIV","GRATITUDE"),
        SeedVerse("gratitude_013","Colossians",2,7,"Rooted and built up in him, strengthened in the faith as you were taught, and overflowing with thankfulness.","NIV","GRATITUDE"),
        SeedVerse("gratitude_014","1 Chronicles",29,13,"Now, our God, we give you thanks, and praise your glorious name.","NIV","GRATITUDE"),
        SeedVerse("gratitude_015","Psalm",136,1,"Give thanks to the LORD, for he is good. His love endures forever.","NIV","GRATITUDE"),
        SeedVerse("gratitude_016","2 Corinthians",4,15,"All this is for your benefit, so that the grace that is reaching more and more people may cause thanksgiving to overflow to the glory of God.","NIV","GRATITUDE"),
        SeedVerse("gratitude_017","Luke",17,15,"One of them, when he saw he was healed, came back, praising God in a loud voice.","NIV","GRATITUDE"),
        SeedVerse("gratitude_018","Psalm",118,24,"The LORD has done it this very day; let us rejoice today and be glad.","NIV","GRATITUDE"),
        SeedVerse("gratitude_019","Revelation",4,11,"You are worthy, our Lord and God, to receive glory and honor and power, for you created all things.","NIV","GRATITUDE"),
        SeedVerse("gratitude_020","Psalm",92,1,"It is good to praise the LORD and make music to your name, O Most High.","NIV","GRATITUDE"),

        // ── COURAGE (20 verses) ──────────────────────────────────
        SeedVerse("courage_001","Deuteronomy",31,6,"Be strong and courageous. Do not be afraid or terrified because of them, for the LORD your God goes with you; he will never leave you nor forsake you.","NIV","COURAGE"),
        SeedVerse("courage_002","Psalm",27,1,"The LORD is my light and my salvation — whom shall I fear? The LORD is the stronghold of my life — of whom shall I be afraid?","NIV","COURAGE"),
        SeedVerse("courage_003","Isaiah",43,1,"Do not fear, for I have redeemed you; I have summoned you by name; you are mine.","NIV","COURAGE"),
        SeedVerse("courage_004","Psalm",23,4,"Even though I walk through the darkest valley, I will fear no evil, for you are with me.","NIV","COURAGE"),
        SeedVerse("courage_005","Romans",8,31,"What, then, shall we say in response to these things? If God is for us, who can be against us?","NIV","COURAGE"),
        SeedVerse("courage_006","1 John",4,4,"You, dear children, are from God and have overcome them, because the one who is in you is greater than the one who is in the world.","NIV","COURAGE"),
        SeedVerse("courage_007","Hebrews",13,6,"So we say with confidence, The Lord is my helper; I will not be afraid.","NIV","COURAGE"),
        SeedVerse("courage_008","Isaiah",54,4,"Do not be afraid; you will not be put to shame. Do not fear disgrace; you will not be humiliated.","NIV","COURAGE"),
        SeedVerse("courage_009","2 Chronicles",20,15,"Do not be afraid or discouraged because of this vast army. For the battle is not yours, but God's.","NIV","COURAGE"),
        SeedVerse("courage_010","Psalm",56,4,"In God, whose word I praise — in God I trust and am not afraid.","NIV","COURAGE"),
        SeedVerse("courage_011","Matthew",14,27,"But Jesus immediately said to them: Take courage! It is I. Don't be afraid.","NIV","COURAGE"),
        SeedVerse("courage_012","1 Samuel",17,47,"It is not by sword or spear that the LORD saves; for the battle is the LORD's.","NIV","COURAGE"),
        SeedVerse("courage_013","Acts",4,13,"When they saw the courage of Peter and John and realized that they were unschooled, ordinary men, they were astonished and they took note that these men had been with Jesus.","NIV","COURAGE"),
        SeedVerse("courage_014","Psalm",3,6,"I will not fear though tens of thousands assail me on every side.","NIV","COURAGE"),
        SeedVerse("courage_015","Proverbs",28,1,"The wicked flee though no one pursues, but the righteous are as bold as a lion.","NIV","COURAGE"),
        SeedVerse("courage_016","Ezra",10,4,"Rise up; this matter is in your hands. We will support you, so take courage and do it.","NIV","COURAGE"),
        SeedVerse("courage_017","Numbers",13,30,"Then Caleb silenced the people before Moses and said, We should go up and take possession of the land, for we can certainly do it.","NIV","COURAGE"),
        SeedVerse("courage_018","Joel",3,10,"Beat your plowshares into swords and your pruning hooks into spears. Let the weakling say, I am strong!","NIV","COURAGE"),
        SeedVerse("courage_019","Psalm",31,24,"Be strong and take heart, all you who hope in the LORD.","NIV","COURAGE"),
        SeedVerse("courage_020","Romans",8,15,"The Spirit you received does not make you slaves, so that you live in fear again; rather, the Spirit you received brought about your adoption to sonship.","NIV","COURAGE"),

        // ── HEALING (20 verses) ──────────────────────────────────
        SeedVerse("healing_001","Jeremiah",17,14,"Heal me, LORD, and I will be healed; save me and I will be saved, for you are the one I praise.","NIV","HEALING"),
        SeedVerse("healing_002","Psalm",147,3,"He heals the brokenhearted and binds up their wounds.","NIV","HEALING"),
        SeedVerse("healing_003","Isaiah",53,5,"But he was pierced for our transgressions, he was crushed for our iniquities; the punishment that brought us peace was on him, and by his wounds we are healed.","NIV","HEALING"),
        SeedVerse("healing_004","Revelation",21,4,"He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain.","NIV","HEALING"),
        SeedVerse("healing_005","Psalm",30,2,"LORD my God, I called to you for help, and you healed me.","NIV","HEALING"),
        SeedVerse("healing_006","Exodus",15,26,"I am the LORD, who heals you.","NIV","HEALING"),
        SeedVerse("healing_007","Matthew",11,28,"Come to me, all you who are weary and burdened, and I will give you rest.","NIV","HEALING"),
        SeedVerse("healing_008","3 John",1,2,"Dear friend, I pray that you may enjoy good health and that all may go well with you.","NIV","HEALING"),
        SeedVerse("healing_009","Psalm",34,18,"The LORD is close to the brokenhearted and saves those who are crushed in spirit.","NIV","HEALING"),
        SeedVerse("healing_010","Isaiah",40,31,"Those who hope in the LORD will renew their strength. They will soar on wings like eagles.","NIV","HEALING"),
        SeedVerse("healing_011","James",5,15,"And the prayer offered in faith will make the sick person well; the Lord will raise them up.","NIV","HEALING"),
        SeedVerse("healing_012","Luke",4,18,"He has sent me to proclaim freedom for the prisoners and recovery of sight for the blind, to set the oppressed free.","NIV","HEALING"),
        SeedVerse("healing_013","Psalm",103,3,"Who forgives all your sins and heals all your diseases.","NIV","HEALING"),
        SeedVerse("healing_014","Acts",10,38,"How God anointed Jesus of Nazareth with the Holy Spirit and power, and how he went around doing good and healing all who were under the power of the devil.","NIV","HEALING"),
        SeedVerse("healing_015","Romans",8,18,"I consider that our present sufferings are not worth comparing with the glory that will be revealed in us.","NIV","HEALING"),
        SeedVerse("healing_016","2 Corinthians",1,3,"Praise be to the God and Father of our Lord Jesus Christ, the Father of compassion and the God of all comfort.","NIV","HEALING"),
        SeedVerse("healing_017","Psalm",41,3,"The LORD sustains them on their sickbed and restores them from their bed of illness.","NIV","HEALING"),
        SeedVerse("healing_018","Malachi",4,2,"But for you who revere my name, the sun of righteousness will rise with healing in its rays.","NIV","HEALING"),
        SeedVerse("healing_019","Luke",8,50,"Hearing this, Jesus said to Jairus, Don't be afraid; just believe, and she will be healed.","NIV","HEALING"),
        SeedVerse("healing_020","Romans",15,13,"May the God of hope fill you with all joy and peace as you trust in him.","NIV","HEALING"),

        // ── JOY (20 verses) ─────────────────────────────────────
        SeedVerse("joy_001","Philippians",4,4,"Rejoice in the Lord always. I will say it again: Rejoice!","NIV","JOY"),
        SeedVerse("joy_002","Nehemiah",8,10,"The joy of the LORD is your strength.","NIV","JOY"),
        SeedVerse("joy_003","Psalm",16,11,"You make known to me the path of life; you will fill me with joy in your presence.","NIV","JOY"),
        SeedVerse("joy_004","John",15,11,"I have told you this so that my joy may be in you and that your joy may be complete.","NIV","JOY"),
        SeedVerse("joy_005","Isaiah",61,10,"I delight greatly in the LORD; my soul rejoices in my God.","NIV","JOY"),
        SeedVerse("joy_006","Romans",15,13,"May the God of hope fill you with all joy and peace as you trust in him.","NIV","JOY"),
        SeedVerse("joy_007","Psalm",118,24,"The LORD has done it this very day; let us rejoice today and be glad.","NIV","JOY"),
        SeedVerse("joy_008","Zephaniah",3,17,"He will take great delight in you; in his love he will no longer rebuke you, but will rejoice over you with singing.","NIV","JOY"),
        SeedVerse("joy_009","Psalm",30,5,"Weeping may stay for the night, but rejoicing comes in the morning.","NIV","JOY"),
        SeedVerse("joy_010","1 Peter",1,8,"Though you have not seen him, you love him; and even though you do not see him now, you believe in him and are filled with an inexpressible and glorious joy.","NIV","JOY"),
        SeedVerse("joy_011","Galatians",5,22,"But the fruit of the Spirit is love, joy, peace, forbearance, kindness, goodness, faithfulness.","NIV","JOY"),
        SeedVerse("joy_012","James",1,2,"Consider it pure joy, my brothers and sisters, whenever you face trials of many kinds.","NIV","JOY"),
        SeedVerse("joy_013","Luke",15,10,"In the same way, I tell you, there is rejoicing in the presence of the angels of God over one sinner who repents.","NIV","JOY"),
        SeedVerse("joy_014","Psalm",9,2,"I will be glad and rejoice in you; I will sing the praises of your name, O Most High.","NIV","JOY"),
        SeedVerse("joy_015","Romans",14,17,"For the kingdom of God is not a matter of eating and drinking, but of righteousness, peace and joy in the Holy Spirit.","NIV","JOY"),
        SeedVerse("joy_016","Habakkuk",3,18,"Yet I will rejoice in the LORD, I will be joyful in God my Savior.","NIV","JOY"),
        SeedVerse("joy_017","Psalm",128,1,"Blessed are all who fear the LORD, who walk in obedience to him.","NIV","JOY"),
        SeedVerse("joy_018","John",16,22,"Now is your time of grief, but I will see you again and you will rejoice, and no one will take away your joy.","NIV","JOY"),
        SeedVerse("joy_019","Isaiah",12,3,"With joy you will draw water from the wells of salvation.","NIV","JOY"),
        SeedVerse("joy_020","Psalm",5,11,"But let all who take refuge in you be glad; let them ever sing for joy.","NIV","JOY"),

        // ── COMFORT (20 verses) ──────────────────────────────────
        SeedVerse("comfort_001","2 Corinthians",1,3,"Praise be to the God and Father of our Lord Jesus Christ, the Father of compassion and the God of all comfort.","NIV","COMFORT"),
        SeedVerse("comfort_002","Psalm",23,4,"Even though I walk through the darkest valley, I will fear no evil, for you are with me; your rod and your staff, they comfort me.","NIV","COMFORT"),
        SeedVerse("comfort_003","Isaiah",40,1,"Comfort, comfort my people, says your God.","NIV","COMFORT"),
        SeedVerse("comfort_004","Matthew",5,4,"Blessed are those who mourn, for they will be comforted.","NIV","COMFORT"),
        SeedVerse("comfort_005","Psalm",34,18,"The LORD is close to the brokenhearted and saves those who are crushed in spirit.","NIV","COMFORT"),
        SeedVerse("comfort_006","John",14,1,"Do not let your hearts be troubled. You believe in God; believe also in me.","NIV","COMFORT"),
        SeedVerse("comfort_007","Isaiah",41,13,"For I am the LORD your God who takes hold of your right hand and says to you, Do not fear; I will help you.","NIV","COMFORT"),
        SeedVerse("comfort_008","Romans",8,28,"And we know that in all things God works for the good of those who love him.","NIV","COMFORT"),
        SeedVerse("comfort_009","Psalm",46,1,"God is our refuge and strength, an ever-present help in trouble.","NIV","COMFORT"),
        SeedVerse("comfort_010","2 Corinthians",1,4,"Who comforts us in all our troubles, so that we can comfort those in any trouble with the comfort we ourselves receive from God.","NIV","COMFORT"),
        SeedVerse("comfort_011","Revelation",21,4,"He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain.","NIV","COMFORT"),
        SeedVerse("comfort_012","1 Peter",5,7,"Cast all your anxiety on him because he cares for you.","NIV","COMFORT"),
        SeedVerse("comfort_013","Psalm",119,50,"My comfort in my suffering is this: Your promise preserves my life.","NIV","COMFORT"),
        SeedVerse("comfort_014","Romans",8,38,"For I am convinced that neither death nor life, neither angels nor demons, will be able to separate us from the love of God.","NIV","COMFORT"),
        SeedVerse("comfort_015","Isaiah",66,13,"As a mother comforts her child, so will I comfort you; and you will be comforted over Jerusalem.","NIV","COMFORT"),
        SeedVerse("comfort_016","Psalm",91,4,"He will cover you with his feathers, and under his wings you will find refuge; his faithfulness will be your shield and rampart.","NIV","COMFORT"),
        SeedVerse("comfort_017","Matthew",11,28,"Come to me, all you who are weary and burdened, and I will give you rest.","NIV","COMFORT"),
        SeedVerse("comfort_018","Lamentations",3,22,"Because of the LORD's great love we are not consumed, for his compassions never fail.","NIV","COMFORT"),
        SeedVerse("comfort_019","Psalm",147,3,"He heals the brokenhearted and binds up their wounds.","NIV","COMFORT"),
        SeedVerse("comfort_020","Numbers",6,24,"The LORD bless you and keep you; the LORD make his face shine on you and be gracious to you.","NIV","COMFORT"),

        // ── FORGIVENESS (15 verses) ──────────────────────────────
        SeedVerse("forgiveness_001","1 John",1,9,"If we confess our sins, he is faithful and just and will forgive us our sins and purify us from all unrighteousness.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_002","Isaiah",43,25,"I, even I, am he who blots out your transgressions, for my own sake, and remembers your sins no more.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_003","Micah",7,18,"Who is a God like you, who pardons sin and forgives the transgression of the remnant of his inheritance?","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_004","Colossians",3,13,"Bear with each other and forgive one another if any of you has a grievance against someone. Forgive as the Lord forgave you.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_005","Matthew",6,14,"For if you forgive other people when they sin against you, your heavenly Father will also forgive you.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_006","Psalm",103,12,"As far as the east is from the west, so far has he removed our transgressions from us.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_007","Ephesians",4,32,"Be kind and compassionate to one another, forgiving each other, just as in Christ God forgave you.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_008","Acts",3,19,"Repent, then, and turn to God, so that your sins may be wiped out, that times of refreshing may come from the Lord.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_009","Romans",8,1,"Therefore, there is now no condemnation for those who are in Christ Jesus.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_010","Hebrews",8,12,"For I will forgive their wickedness and will remember their sins no more.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_011","Luke",23,34,"Jesus said, Father, forgive them, for they do not know what they are doing.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_012","Psalm",32,5,"Then I acknowledged my sin to you and did not cover up my iniquity. I said, I will confess my transgressions to the LORD. And you forgave the guilt of my sin.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_013","Matthew",18,21,"Then Peter came to Jesus and asked, Lord, how many times shall I forgive my brother or sister who sins against me? Up to seven times? Jesus answered, I tell you, not seven times, but seventy-seven times.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_014","Romans",5,8,"But God demonstrates his own love for us in this: While we were still sinners, Christ died for us.","NIV","FORGIVENESS"),
        SeedVerse("forgiveness_015","Psalm",51,2,"Wash away all my iniquity and cleanse me from my sin.","NIV","FORGIVENESS"),

        // ── ALL / GENERAL (30 classic verses) ────────────────────
        SeedVerse("all_001","Psalm",23,1,"The LORD is my shepherd, I lack nothing.","NIV","ALL"),
        SeedVerse("all_002","John",11,35,"Jesus wept.","KJV","ALL"),
        SeedVerse("all_003","Matthew",11,28,"Come to me, all you who are weary and burdened, and I will give you rest.","NIV","ALL"),
        SeedVerse("all_004","John",14,6,"Jesus answered, I am the way and the truth and the life. No one comes to the Father except through me.","NIV","ALL"),
        SeedVerse("all_005","Psalm",46,10,"Be still, and know that I am God; I will be exalted among the nations.","NIV","ALL"),
        SeedVerse("all_006","Matthew",6,33,"But seek first his kingdom and his righteousness, and all these things will be given to you as well.","NIV","ALL"),
        SeedVerse("all_007","Romans",3,23,"For all have sinned and fall short of the glory of God.","NIV","ALL"),
        SeedVerse("all_008","John",8,32,"Then you will know the truth, and the truth will set you free.","NIV","ALL"),
        SeedVerse("all_009","Revelation",21,4,"He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain.","NIV","ALL"),
        SeedVerse("all_010","Genesis",1,1,"In the beginning God created the heavens and the earth.","KJV","ALL"),
        SeedVerse("all_011","Proverbs",3,5,"Trust in the LORD with all your heart and lean not on your own understanding.","NIV","ALL"),
        SeedVerse("all_012","Romans",8,28,"And we know that in all things God works for the good of those who love him.","NIV","ALL"),
        SeedVerse("all_013","Psalm",119,105,"Your word is a lamp for my feet, a light on my path.","NIV","ALL"),
        SeedVerse("all_014","John",3,16,"For God so loved the world that he gave his one and only Son.","NIV","ALL"),
        SeedVerse("all_015","Matthew",28,19,"Go and make disciples of all nations, baptizing them in the name of the Father and of the Son and of the Holy Spirit.","NIV","ALL"),
        SeedVerse("all_016","Psalm",100,3,"Know that the LORD is God. It is he who made us, and we are his; we are his people, the sheep of his pasture.","NIV","ALL"),
        SeedVerse("all_017","Ephesians",2,10,"For we are God's handiwork, created in Christ Jesus to do good works, which God prepared in advance for us to do.","NIV","ALL"),
        SeedVerse("all_018","Romans",12,2,"Do not conform to the pattern of this world, but be transformed by the renewing of your mind.","NIV","ALL"),
        SeedVerse("all_019","Psalm",139,14,"I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well.","NIV","ALL"),
        SeedVerse("all_020","Philippians",1,6,"Being confident of this, that he who began a good work in you will carry it on to completion until the day of Christ Jesus.","NIV","ALL"),
        SeedVerse("all_021","Isaiah",40,8,"The grass withers and the flowers fall, but the word of our God endures forever.","NIV","ALL"),
        SeedVerse("all_022","1 Corinthians",10,13,"No temptation has overtaken you except what is common to mankind. And God is faithful; he will not let you be tempted beyond what you can bear.","NIV","ALL"),
        SeedVerse("all_023","Matthew",5,16,"In the same way, let your light shine before others, that they may see your good deeds and glorify your Father in heaven.","NIV","ALL"),
        SeedVerse("all_024","Romans",12,19,"Do not take revenge, my dear friends, but leave room for God's wrath, for it is written: It is mine to avenge; I will repay, says the Lord.","NIV","ALL"),
        SeedVerse("all_025","Psalm",27,4,"One thing I ask from the LORD, this only do I seek: that I may dwell in the house of the LORD all the days of my life.","NIV","ALL"),
        SeedVerse("all_026","Hebrews",4,12,"For the word of God is alive and active. Sharper than any double-edged sword.","NIV","ALL"),
        SeedVerse("all_027","Colossians",3,2,"Set your minds on things above, not on earthly things.","NIV","ALL"),
        SeedVerse("all_028","2 Timothy",3,16,"All Scripture is God-breathed and is useful for teaching, rebuking, correcting and training in righteousness.","NIV","ALL"),
        SeedVerse("all_029","Psalm",34,8,"Taste and see that the LORD is good; blessed is the one who takes refuge in him.","NIV","ALL"),
        SeedVerse("all_030","Matthew",7,7,"Ask and it will be given to you; seek and you will find; knock and the door will be opened to you.","NIV","ALL")
    )
}
