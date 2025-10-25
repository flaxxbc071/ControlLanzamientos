package com.company.lanzamientos.data

import androidx.room.*

@Dao
interface SellerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(seller: Seller)
}

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(client: Client)
    @Query("SELECT id FROM Client") fun allIds(): List<String>
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(product: Product)
    @Query("SELECT code FROM Product") fun allCodes(): List<String>
}

@Dao
interface AssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(a: Assignment)
}

@Dao
interface StateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun upsert(state: ClientProductState)
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun upsertWeekly(state: ClientProductWeekly)

    @Query("SELECT COUNT(*) FROM ClientProductState") fun totalPairs(): Int
    @Query("SELECT COUNT(*) FROM ClientProductState WHERE state='INCORPORATED'") fun totalIncorporated(): Int

    @Query("""
        SELECT s.name AS seller,
               SUM(CASE WHEN cps.state='INCORPORATED' THEN 1 ELSE 0 END) AS inc,
               SUM(CASE WHEN cps.state='PENDING' THEN 1 ELSE 0 END) AS pend
        FROM Seller s
        LEFT JOIN Assignment a ON a.sellerId=s.id
        LEFT JOIN ClientProductState cps ON cps.clientId=a.clientId
        GROUP BY s.id
        ORDER BY inc DESC
    """)
    fun kpiPerSeller(): List<KpiSeller>
}

data class KpiSeller(val seller: String, val inc: Int, val pend: Int)

@Dao
interface BatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(b: ImportBatch)
}
@Query("SELECT * FROM ImportBatch ORDER BY importedAt DESC LIMIT 1")
fun lastBatch(): ImportBatch?
