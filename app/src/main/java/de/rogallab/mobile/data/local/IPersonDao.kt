package de.rogallab.mobile.data.local

import androidx.room.Dao
import androidx.room.Query
import de.rogallab.mobile.data.dtos.PersonDto
import kotlinx.coroutines.flow.Flow

@Dao
interface IPersonDao: IBaseDao<PersonDto> {
   // QUERIES ---------------------------------------------
   @Query("SELECT * FROM Person")
   fun selectAll(): Flow<List<PersonDto>>

   @Query("SELECT * FROM Person WHERE id = :personId")
   suspend fun findById(personId: String): PersonDto?

   @Query("SELECT COUNT(*) FROM Person")
   suspend fun count(): Int

   // COMMANDS --------------------------------------------
 }