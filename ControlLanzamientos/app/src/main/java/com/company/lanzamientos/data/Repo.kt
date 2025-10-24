package com.company.lanzamientos.data

import android.content.Context
import androidx.room.Room
import java.util.UUID

interface Repo {
    fun upsertSellerByName(name: String): String
    fun upsertProduct(code: String, name: String): String
    fun upsertClient(id: String, name: String, locality: String, zone: String?): String
    fun ensureAssignment(clientId: String, sellerId: String)
    fun upsertClientProductStateByCode(clientId: String, productCode: String, state: Incorporation, updatedAt: Long)
    fun upsertClientProductWeeklyByCode(clientId: String, productCode: String, yearWeek: String, state: Incorporation)
    fun getAllClientIds(): List<String>
    fun getAllProductCodes(): List<String>
    fun insertImportBatch(importedAt: Long, filename: String, sheets: Int, products: Int, clients: Int)
}

class RepoImpl private constructor(private val db: AppDatabase) : Repo {

    companion object {
        fun create(context: Context): RepoImpl =
            RepoImpl(Room.databaseBuilder(context, AppDatabase::class.java, "launches.db").build())
    }

    override fun upsertSellerByName(name: String): String {
        val id = name.trim()
        db.sellerDao().insert(Seller(id, name))
        return id
    }

    override fun upsertProduct(code: String, name: String): String {
        db.productDao().insert(Product(code, name))
        return code
    }

    override fun upsertClient(id: String, name: String, locality: String, zone: String?): String {
        db.clientDao().insert(Client(id, name, locality, zone))
        return id
    }

    override fun ensureAssignment(clientId: String, sellerId: String) {
        db.assignmentDao().insert(Assignment(clientId, sellerId))
    }

    override fun upsertClientProductStateByCode(clientId: String, productCode: String, state: Incorporation, updatedAt: Long) {
        db.stateDao().upsert(ClientProductState(clientId, productCode, state, updatedAt))
    }

    override fun upsertClientProductWeeklyByCode(clientId: String, productCode: String, yearWeek: String, state: Incorporation) {
        db.stateDao().upsertWeekly(ClientProductWeekly(clientId, productCode, yearWeek, state))
    }

    override fun getAllClientIds(): List<String> = db.clientDao().allIds()
    override fun getAllProductCodes(): List<String> = db.productDao().allCodes()

    override fun insertImportBatch(importedAt: Long, filename: String, sheets: Int, products: Int, clients: Int) {
        db.batchDao().insert(ImportBatch(UUID.randomUUID().toString(), importedAt, filename, sheets, products, clients))
    }

    // Exponemos algunas consultas para el dashboard
    fun totalPairs(): Int = db.stateDao().totalPairs()
    fun totalIncorporated(): Int = db.stateDao().totalIncorporated()
    fun perSeller(): List<KpiSeller> = db.stateDao().kpiPerSeller()
    fun lastBatch(): ImportBatch? = db.batchDao().lastBatch()

}
