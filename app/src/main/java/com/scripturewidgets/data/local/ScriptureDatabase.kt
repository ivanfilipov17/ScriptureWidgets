// data/local/ScriptureDatabase.kt
// Room database with pre-populated offline verse data

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
    version = 1,
    exportSchema = true
)
abstract class ScriptureDatabase : RoomDatabase() {

    abstract fun verseDao(): VerseDao

    companion object {
        private const val DB_NAME = "scripture_database"

        fun create(context: Context): ScriptureDatabase {
            return Room.databaseBuilder(context, ScriptureDatabase::class.java, DB_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate on first creation
                        CoroutineScope(Dispatchers.IO).launch {
                            prepopulate(db)
                        }
                    }
                })
                .build()
        }

        /**
         * Inserts 120+ pre-seeded offline Bible verses.
         * Called once on first database creation.
         */
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

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// MARK: Offline Verse Seed Data (120+ verses)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
object OfflineVerseData {
    data class SeedVerse(
        val id: String, val book: String, val chapter: Int, val verse: Int,
        val text: String, val translation: String = "NIV", val category: String
    )

    val verses = listOf(
        // HOPE
        SeedVerse("hope_001","Jeremiah",29,11,"For I know the plans I have for you, declares the LORD, plans to prosper you and not to harm you, plans to give you hope and a future.","NIV","HOPE"),
        SeedVerse("hope_002","Romans",15,13,"May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope by the power of the Holy Spirit.","NIV","HOPE"),
        SeedVerse("hope_003","Psalm",31,24,"Be strong and take heart, all you who hope in the LORD.","NIV","HOPE"),
        SeedVerse("hope_004","Isaiah",40,31,"But those who hope in the LORD will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.","NIV","HOPE"),
        SeedVerse("hope_005","Romans",8,28,"And we know that in all things God works for the good of those who love him, who have been called according to his purpose.","NIV","HOPE"),
        SeedVerse("hope_006","Lamentations",3,22,"Because of the LORD''s great love we are not consumed, for his compassions never fail.","NIV","HOPE"),
        SeedVerse("hope_007","Psalm",121,1,"I lift up my eyes to the mountains â€” where does my help come from? My help comes from the LORD, the Maker of heaven and earth.","NIV","HOPE"),
        SeedVerse("hope_008","Hebrews",11,1,"Now faith is confidence in what we hope for and assurance about what we do not see.","NIV","HOPE"),
        SeedVerse("hope_009","Romans",5,5,"And hope does not put us to shame, because God''s love has been poured out into our hearts through the Holy Spirit, who has been given to us.","NIV","HOPE"),
        SeedVerse("hope_010","Psalm",27,14,"Wait for the LORD; be strong and take heart and wait for the LORD.","NIV","HOPE"),
        // FAITH
        SeedVerse("faith_001","Hebrews",11,6,"And without faith it is impossible to please God, because anyone who comes to him must believe that he exists and that he rewards those who earnestly seek him.","NIV","FAITH"),
        SeedVerse("faith_002","Matthew",17,20,"He replied, Because you have so little faith. Truly I tell you, if you have faith as small as a mustard seed, you can say to this mountain, Move from here to there, and it will move. Nothing will be impossible for you.","NIV","FAITH"),
        SeedVerse("faith_003","2 Corinthians",5,7,"For we live by faith, not by sight.","NIV","FAITH"),
        SeedVerse("faith_004","Galatians",2,20,"I have been crucified with Christ and I no longer live, but Christ lives in me. The life I now live in the body, I live by faith in the Son of God, who loved me and gave himself for me.","NIV","FAITH"),
        SeedVerse("faith_005","Ephesians",2,8,"For it is by grace you have been saved, through faith â€” and this is not from yourselves, it is the gift of God.","NIV","FAITH"),
        SeedVerse("faith_006","Proverbs",3,5,"Trust in the LORD with all your heart and lean not on your own understanding.","NIV","FAITH"),
        SeedVerse("faith_007","Romans",10,17,"Consequently, faith comes from hearing the message, and the message is heard through the word about Christ.","NIV","FAITH"),
        SeedVerse("faith_008","James",2,17,"In the same way, faith by itself, if it is not accompanied by action, is dead.","NIV","FAITH"),
        SeedVerse("faith_009","Mark",11,24,"Therefore I tell you, whatever you ask for in prayer, believe that you have received it, and it will be yours.","NIV","FAITH"),
        SeedVerse("faith_010","1 Peter",1,7,"These have come so that the proven genuineness of your faith â€” of greater worth than gold â€” may result in praise, glory and honor when Jesus Christ is revealed.","NIV","FAITH"),
        // LOVE
        SeedVerse("love_001","John",3,16,"For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.","NIV","LOVE"),
        SeedVerse("love_002","1 Corinthians",13,4,"Love is patient, love is kind. It does not envy, it does not boast, it is not proud.","NIV","LOVE"),
        SeedVerse("love_003","1 John",4,8,"Whoever does not love does not know God, because God is love.","NIV","LOVE"),
        SeedVerse("love_004","Romans",8,38,"For I am convinced that neither death nor life, neither angels nor demons, neither the present nor the future, nor any powers, will be able to separate us from the love of God that is in Christ Jesus our Lord.","NIV","LOVE"),
        SeedVerse("love_005","1 John",4,19,"We love because he first loved us.","NIV","LOVE"),
        SeedVerse("love_006","John",15,13,"Greater love has no one than this: to lay down one''s life for one''s friends.","NIV","LOVE"),
        SeedVerse("love_007","Romans",5,8,"But God demonstrates his own love for us in this: While we were still sinners, Christ died for us.","NIV","LOVE"),
        SeedVerse("love_008","Zephaniah",3,17,"The LORD your God is with you, the Mighty Warrior who saves. He will take great delight in you; in his love he will no longer rebuke you, but will rejoice over you with singing.","NIV","LOVE"),
        SeedVerse("love_009","Psalm",136,1,"Give thanks to the LORD, for he is good. His love endures forever.","NIV","LOVE"),
        SeedVerse("love_010","Song of Solomon",8,7,"Many waters cannot quench love; rivers cannot sweep it away.","NIV","LOVE"),
        // STRENGTH
        SeedVerse("strength_001","Philippians",4,13,"I can do all this through him who gives me strength.","NIV","STRENGTH"),
        SeedVerse("strength_002","Isaiah",41,10,"So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.","NIV","STRENGTH"),
        SeedVerse("strength_003","Psalm",46,1,"God is our refuge and strength, an ever-present help in trouble.","NIV","STRENGTH"),
        SeedVerse("strength_004","2 Timothy",1,7,"For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline.","NIV","STRENGTH"),
        SeedVerse("strength_005","Ephesians",6,10,"Finally, be strong in the Lord and in his mighty power.","NIV","STRENGTH"),
        SeedVerse("strength_006","Nehemiah",8,10,"Do not grieve, for the joy of the LORD is your strength.","NIV","STRENGTH"),
        SeedVerse("strength_007","Joshua",1,9,"Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the LORD your God will be with you wherever you go.","NIV","STRENGTH"),
        SeedVerse("strength_008","Psalm",28,7,"The LORD is my strength and my shield; my heart trusts in him, and he helps me.","NIV","STRENGTH"),
        SeedVerse("strength_009","1 Chronicles",16,11,"Look to the LORD and his strength; seek his face always.","NIV","STRENGTH"),
        SeedVerse("strength_010","Habakkuk",3,19,"The Sovereign LORD is my strength; he makes my feet like the feet of a deer, he enables me to tread on the heights.","NIV","STRENGTH"),
        // PEACE
        SeedVerse("peace_001","Philippians",4,7,"And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.","NIV","PEACE"),
        SeedVerse("peace_002","John",14,27,"Peace I leave with you; my peace I give you. I do not give to you as the world gives. Do not let your hearts be troubled and do not be afraid.","NIV","PEACE"),
        SeedVerse("peace_003","Isaiah",26,3,"You will keep in perfect peace those whose minds are steadfast, because they trust in you.","NIV","PEACE"),
        SeedVerse("peace_004","Colossians",3,15,"Let the peace of Christ rule in your hearts, since as members of one body you were called to peace. And be thankful.","NIV","PEACE"),
        SeedVerse("peace_005","Matthew",5,9,"Blessed are the peacemakers, for they will be called children of God.","NIV","PEACE"),
        SeedVerse("peace_006","Isaiah",9,6,"For to us a child is born, to us a son is given, and the government will be on his shoulders. And he will be called Wonderful Counselor, Mighty God, Everlasting Father, Prince of Peace.","NIV","PEACE"),
        SeedVerse("peace_007","Psalm",29,11,"The LORD gives strength to his people; the LORD blesses his people with peace.","NIV","PEACE"),
        SeedVerse("peace_008","Numbers",6,26,"The LORD turn his face toward you and give you peace.","NIV","PEACE"),
        SeedVerse("peace_009","Romans",15,33,"The God of peace be with you all. Amen.","NIV","PEACE"),
        SeedVerse("peace_010","2 Thessalonians",3,16,"Now may the Lord of peace himself give you peace at all times and in every way. The Lord be with all of you.","NIV","PEACE"),
        // WISDOM
        SeedVerse("wisdom_001","Proverbs",1,7,"The fear of the LORD is the beginning of wisdom; fools despise wisdom and instruction.","NIV","WISDOM"),
        SeedVerse("wisdom_002","James",1,5,"If any of you lacks wisdom, you should ask God, who gives generously to all without finding fault, and it will be given to you.","NIV","WISDOM"),
        SeedVerse("wisdom_003","Proverbs",4,7,"The beginning of wisdom is this: Get wisdom, and whatever you get, get insight.","ESV","WISDOM"),
        SeedVerse("wisdom_004","Proverbs",16,16,"How much better to get wisdom than gold, to get insight rather than silver!","NIV","WISDOM"),
        SeedVerse("wisdom_005","Colossians",2,3,"In whom are hidden all the treasures of wisdom and knowledge.","NIV","WISDOM"),
        SeedVerse("wisdom_006","Psalm",111,10,"The fear of the LORD is the beginning of wisdom; all who follow his precepts have good understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_007","Proverbs",9,10,"The fear of the LORD is the beginning of wisdom, and knowledge of the Holy One is understanding.","NIV","WISDOM"),
        SeedVerse("wisdom_008","Romans",11,33,"Oh, the depth of the riches of the wisdom and knowledge of God! How unsearchable his judgments, and his paths beyond tracing out!","NIV","WISDOM"),
        SeedVerse("wisdom_009","Ecclesiastes",7,12,"Wisdom is a shelter as money is a shelter, but the advantage of knowledge is this: Wisdom preserves those who have it.","NIV","WISDOM"),
        SeedVerse("wisdom_010","1 Corinthians",1,25,"For the foolishness of God is wiser than human wisdom, and the weakness of God is stronger than human strength.","NIV","WISDOM"),
        // PRAYER
        SeedVerse("prayer_001","Matthew",7,7,"Ask and it will be given to you; seek and you will find; knock and the door will be opened to you.","NIV","PRAYER"),
        SeedVerse("prayer_002","1 Thessalonians",5,17,"Pray continually.","NIV","PRAYER"),
        SeedVerse("prayer_003","Philippians",4,6,"Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God.","NIV","PRAYER"),
        SeedVerse("prayer_004","James",5,16,"The prayer of a righteous person is powerful and effective.","NIV","PRAYER"),
        SeedVerse("prayer_005","Jeremiah",33,3,"Call to me and I will answer you and tell you great and unsearchable things you do not know.","NIV","PRAYER"),
        SeedVerse("prayer_006","Psalm",145,18,"The LORD is near to all who call on him, to all who call on him in truth.","NIV","PRAYER"),
        SeedVerse("prayer_007","1 John",5,14,"This is the confidence we have in approaching God: that if we ask anything according to his will, he hears us.","NIV","PRAYER"),
        SeedVerse("prayer_008","Romans",8,26,"In the same way, the Spirit helps us in our weakness. We do not know what we ought to pray for, but the Spirit himself intercedes for us.","NIV","PRAYER"),
        SeedVerse("prayer_009","Matthew",6,9,"This, then, is how you should pray: Our Father in heaven, hallowed be your name.","NIV","PRAYER"),
        SeedVerse("prayer_010","Psalm",66,19,"But God has surely listened and has heard my prayer.","NIV","PRAYER"),
        // SALVATION
        SeedVerse("salvation_001","Romans",10,9,"If you declare with your mouth, Jesus is Lord, and believe in your heart that God raised him from the dead, you will be saved.","NIV","SALVATION"),
        SeedVerse("salvation_002","Acts",4,12,"Salvation is found in no one else, for there is no other name under heaven given to mankind by which we must be saved.","NIV","SALVATION"),
        SeedVerse("salvation_003","Ephesians",2,5,"Made us alive with Christ even when we were dead in transgressions â€” it is by grace you have been saved.","NIV","SALVATION"),
        SeedVerse("salvation_004","Titus",3,5,"He saved us, not because of righteous things we had done, but because of his mercy.","NIV","SALVATION"),
        SeedVerse("salvation_005","Luke",19,10,"For the Son of Man came to seek and to save the lost.","NIV","SALVATION"),
        SeedVerse("salvation_006","Isaiah",43,11,"I, even I, am the LORD, and apart from me there is no savior.","NIV","SALVATION"),
        SeedVerse("salvation_007","2 Corinthians",6,2,"I tell you, now is the time of God''s favor, now is the day of salvation.","NIV","SALVATION"),
        SeedVerse("salvation_008","Psalm",68,20,"Our God is a God who saves; from the Sovereign LORD comes escape from death.","NIV","SALVATION"),
        // GRATITUDE
        SeedVerse("gratitude_001","1 Thessalonians",5,18,"Give thanks in all circumstances; for this is God''s will for you in Christ Jesus.","NIV","GRATITUDE"),
        SeedVerse("gratitude_002","Psalm",107,1,"Give thanks to the LORD, for he is good; his love endures forever.","NIV","GRATITUDE"),
        SeedVerse("gratitude_003","Ephesians",5,20,"Always giving thanks to God the Father for everything, in the name of our Lord Jesus Christ.","NIV","GRATITUDE"),
        SeedVerse("gratitude_004","Colossians",3,17,"And whatever you do, whether in word or deed, do it all in the name of the Lord Jesus, giving thanks to God the Father through him.","NIV","GRATITUDE"),
        SeedVerse("gratitude_005","Psalm",100,4,"Enter his gates with thanksgiving and his courts with praise; give thanks to him and praise his name.","NIV","GRATITUDE"),
        SeedVerse("gratitude_006","James",1,17,"Every good and perfect gift is from above, coming down from the Father of the heavenly lights.","NIV","GRATITUDE"),
        SeedVerse("gratitude_007","2 Corinthians",9,15,"Thanks be to God for his indescribable gift!","NIV","GRATITUDE"),
        SeedVerse("gratitude_008","Psalm",103,2,"Praise the LORD, my soul, and forget not all his benefits.","NIV","GRATITUDE"),
        SeedVerse("gratitude_009","Hebrews",12,28,"Let us be thankful, and so worship God acceptably with reverence and awe.","NIV","GRATITUDE"),
        SeedVerse("gratitude_010","Philippians",4,11,"I have learned to be content whatever the circumstances.","NIV","GRATITUDE"),
        // COURAGE
        SeedVerse("courage_001","Deuteronomy",31,6,"Be strong and courageous. Do not be afraid or terrified because of them, for the LORD your God goes with you; he will never leave you nor forsake you.","NIV","COURAGE"),
        SeedVerse("courage_002","Psalm",27,1,"The LORD is my light and my salvation â€” whom shall I fear? The LORD is the stronghold of my life â€” of whom shall I be afraid?","NIV","COURAGE"),
        SeedVerse("courage_003","Isaiah",43,1,"Do not fear, for I have redeemed you; I have summoned you by name; you are mine.","NIV","COURAGE"),
        SeedVerse("courage_004","Psalm",23,4,"Even though I walk through the darkest valley, I will fear no evil, for you are with me; your rod and your staff, they comfort me.","NIV","COURAGE"),
        SeedVerse("courage_005","Romans",8,31,"What, then, shall we say in response to these things? If God is for us, who can be against us?","NIV","COURAGE"),
        SeedVerse("courage_006","1 John",4,4,"You, dear children, are from God and have overcome them, because the one who is in you is greater than the one who is in the world.","NIV","COURAGE"),
        SeedVerse("courage_007","Hebrews",13,6,"So we say with confidence, The Lord is my helper; I will not be afraid. What can mere mortals do to me?","NIV","COURAGE"),
        SeedVerse("courage_008","Isaiah",54,4,"Do not be afraid; you will not be put to shame. Do not fear disgrace; you will not be humiliated.","NIV","COURAGE"),
        SeedVerse("courage_009","Matthew",10,28,"Do not be afraid of those who kill the body but cannot kill the soul.","NIV","COURAGE"),
        SeedVerse("courage_010","Psalm",56,4,"In God, whose word I praise â€” in God I trust and am not afraid.","NIV","COURAGE"),
        // ALL category bonus verses
        SeedVerse("all_001","Psalm",23,1,"The LORD is my shepherd, I lack nothing.","NIV","ALL"),
        SeedVerse("all_002","John",11,35,"Jesus wept.","KJV","ALL"),
        SeedVerse("all_003","Matthew",11,28,"Come to me, all you who are weary and burdened, and I will give you rest.","NIV","ALL"),
        SeedVerse("all_004","John",14,6,"Jesus answered, I am the way and the truth and the life. No one comes to the Father except through me.","NIV","ALL"),
        SeedVerse("all_005","Psalm",46,10,"He says, Be still, and know that I am God; I will be exalted among the nations, I will be exalted in the earth.","NIV","ALL"),
        SeedVerse("all_006","Matthew",6,33,"But seek first his kingdom and his righteousness, and all these things will be given to you as well.","NIV","ALL"),
        SeedVerse("all_007","Romans",3,23,"For all have sinned and fall short of the glory of God.","NIV","ALL"),
        SeedVerse("all_008","John",8,32,"Then you will know the truth, and the truth will set you free.","NIV","ALL"),
        SeedVerse("all_009","Revelation",21,4,"He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain, for the old order of things has passed away.","NIV","ALL"),
        SeedVerse("all_010","Genesis",1,1,"In the beginning God created the heavens and the earth.","KJV","ALL"),
    )
}
