package de.rogallab.mobile.data.local

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface IBaseDao<Dto> {

   // COMMANDS --------------------------------------------
   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insert(dto: Dto)

   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insert(dtos: List<Dto>)

   @Update
   suspend fun update(dto: Dto)

   @Delete
   suspend fun remove(dto: Dto)
}