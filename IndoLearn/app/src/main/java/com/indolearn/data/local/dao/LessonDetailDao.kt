package com.indolearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.indolearn.data.local.entity.LessonDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDetailDao {

    /**
     * تفاصيل درس واحد.
     *
     * الاستعلام على `id` وليس على `unitId` — انظر شرح العطل في [LessonDetailEntity].
     * `id` هو المفتاح الأساسي، لذا يعيد صفاً واحداً حتماً (لا التباس).
     */
    @Query("SELECT * FROM lesson_details WHERE id = :lessonId")
    suspend fun getLessonDetail(lessonId: Int): LessonDetailEntity?

    /** كل تفاصيل دروس وحدة معينة. */
    @Query("SELECT * FROM lesson_details WHERE unitId = :unitId ORDER BY id")
    fun getDetailsForUnit(unitId: Int): Flow<List<LessonDetailEntity>>

    @Query("SELECT COUNT(*) FROM lesson_details")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<LessonDetailEntity>)
}
