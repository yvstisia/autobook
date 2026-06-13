package com.autobook.app.data.repository

import com.autobook.app.data.local.dao.WorkshopDao
import com.autobook.app.data.local.entity.Workshop
import kotlinx.coroutines.flow.Flow

class WorkshopRepository(private val workshopDao: WorkshopDao) {

    fun getAllWorkshops(): Flow<List<Workshop>> = workshopDao.getAllWorkshops()

    suspend fun getWorkshopById(id: Int): Workshop? = workshopDao.getWorkshopById(id)

    suspend fun insertWorkshop(workshop: Workshop): Long = workshopDao.insertWorkshop(workshop)

    suspend fun updateWorkshop(workshop: Workshop) = workshopDao.updateWorkshop(workshop)

    suspend fun deleteWorkshop(workshop: Workshop) = workshopDao.deleteWorkshop(workshop)
}
