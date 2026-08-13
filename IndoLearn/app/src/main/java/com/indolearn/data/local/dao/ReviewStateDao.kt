package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.ReviewStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewStateDao {

    @Query("SELECT * FROM review_states WHERE languageCode = :langCode")
    fun getAllForLanguage(langCode: String): Flow<List<ReviewStateEntity>>

    @Query("SELECT * FROM review_states WHERE languageCode = :langCode")
    suspend fun getAllForLanguageOnce(langCode: String): List<ReviewStateEntity>

    @Query(
        "SELECT * FROM review_states " +
            "WHERE itemId = :itemId AND kind = :kind AND languageCode = :langCode"
    )
    suspend fun find(itemId: Int, kind: String, langCode: String): ReviewStateEntity?

    @Query(
        "SELECT * FROM review_states " +
            "WHERE languageCode = :langCode AND dueAt <= :now " +
            "ORDER BY dueAt LIMIT :limit"
    )
    suspend fun getDue(langCode: String, now: Long, limit: Int): List<ReviewStateEntity>

    @Query("SELECT COUNT(*) FROM review_states WHERE languageCode = :langCode AND dueAt <= :now")
    suspend fun countDue(langCode: String, now: Long): Int

    /**
     * عدد العناصر التي "تعلّمها" المستخدم فعلاً.
     * التعريف: أُجيب عليها بنجاح مرة واحدة على الأقل — لا مجرد عرضها على الشاشة.
     */
    @Query("SELECT COUNT(*) FROM review_states WHERE languageCode = :langCode AND correctReviews > 0")
    suspend fun countLearned(langCode: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ReviewStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<ReviewStateEntity>)
}
