package com.company.lanzamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity data class Seller(@PrimaryKey val id: String, val name: String)
@Entity data class Client(@PrimaryKey val id: String, val name: String, val locality: String, val zone: String? = null)
@Entity data class Product(@PrimaryKey val code: String, val name: String)

@Entity(primaryKeys = ["clientId","sellerId"])
data class Assignment(val clientId: String, val sellerId: String)

enum class Incorporation { INCORPORATED, PENDING }

@Entity(primaryKeys = ["clientId","productCode"])
data class ClientProductState(
    val clientId: String,
    val productCode: String,
    val state: Incorporation,
    val updatedAt: Long
)

@Entity(primaryKeys = ["clientId","productCode","yearWeek"])
data class ClientProductWeekly(
    val clientId: String,
    val productCode: String,
    val yearWeek: String,
    val state: Incorporation
)

@Entity
data class ImportBatch(
    @PrimaryKey val id: String,
    val importedAt: Long,
    val filename: String,
    val sheets: Int,
    val products: Int,
    val clients: Int
)
