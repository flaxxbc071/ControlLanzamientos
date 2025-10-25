package com.company.lanzamientos.data
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Seller::class, Client::class, Product::class, Assignment::class, ClientProductState::class, ClientProductWeekly::class, ImportBatch::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sellerDao(): SellerDao
    abstract fun clientDao(): ClientDao
    abstract fun productDao(): ProductDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun stateDao(): StateDao
    abstract fun batchDao(): BatchDao
}
