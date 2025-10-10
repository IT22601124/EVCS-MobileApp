package com.example.evcs_mobileapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface AuthResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(auth: AuthResponseEntity)

    @Query("SELECT * FROM auth_response LIMIT 1")
    suspend fun getUser(): AuthResponseEntity?

    @Query("DELETE FROM auth_response")
    suspend fun clear()
}

