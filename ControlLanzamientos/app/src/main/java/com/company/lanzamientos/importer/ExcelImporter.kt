package com.company.lanzamientos.importer

import android.content.Context
import android.net.Uri
import com.company.lanzamientos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate
import java.time.temporal.IsoFields

object ExcelImporter {
    private fun clean(s: String?): String = s?.trim() ?: ""

    suspend fun importWorkbook(context: Context, uri: Uri, repo: Repo) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { stream ->
            val wb = XSSFWorkbook(stream)
            val now = System.currentTimeMillis()
            val today = LocalDate.now()
            val yw = "${today.get(IsoFields.WEEK_BASED_YEAR)}-W${today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"

            val purchased = mutableSetOf<Pair<String,String>>()  // (clientId, productCode)
            val clients = mutableSetOf<String>()
            val products = mutableSetOf<String>()

            wb.forEach { sheet ->
                val headerRow = (0..sheet.lastRowNum).mapNotNull(sheet::getRow).firstOrNull() ?: return@forEach
                val headers = (0 until headerRow.lastCellNum).map { idx -> clean(headerRow.getCell(idx)?.stringCellValue) }

                val isVendorSheet = headers.any { it.equals("Cliente2", true) || it.equals("Cliente", true) } &&
                                    headers.any { it.equals("Localidad", true) }
                val looksProductSheet = headers.firstOrNull()?.equals("COD", true) == true &&
                                        sheet.sheetName.any { it.isDigit() }

                if (isVendorSheet) {
                    val sellerName = sheet.sheetName.trim()
                    val sellerId = repo.upsertSellerByName(sellerName)

                    val idxCOD = headers.indexOfFirst { it.equals("COD", true) }.takeIf { it >= 0 } ?: 0
                    val idxCliente = headers.indexOfFirst { it.equals("Cliente2", true) || it.equals("Cliente", true) }.takeIf { it >= 0 } ?: 1
                    val idxLocalidad = headers.indexOfFirst { it.equals("Localidad", true) }.takeIf { it >= 0 } ?: 2
                    val idxZona = headers.indexOfFirst { it.equals("Zona", true) }.takeIf { it >= 0 }

                    val skip = setOf(idxCOD, idxCliente, idxLocalidad, idxZona ?: -1)
                    val productCols = headers.mapIndexedNotNull { i, h ->
                        if (i in skip || h.isBlank()) null else {
                            val parts = h.split("-", limit = 2).map { it.trim() }
                            val code = parts.firstOrNull()?.takeWhile { !it.isWhitespace() }?.ifBlank { h } ?: h
                            val name = parts.getOrNull(1) ?: h
                            repo.upsertProduct(code, name)
                            products += code
                            i to code
                        }
                    }

                    (1..sheet.lastRowNum).forEach { r ->
                        val row = sheet.getRow(r) ?: return@forEach
                        val cod = clean(row.getCell(idxCOD)?.toString())
                        if (cod.isBlank()) return@forEach
                        val cliente = clean(row.getCell(idxCliente)?.stringCellValue ?: row.getCell(idxCliente)?.toString())
                        val localidad = clean(row.getCell(idxLocalidad)?.stringCellValue ?: row.getCell(idxLocalidad)?.toString())
                        val zona = idxZona?.let { clean(row.getCell(it)?.stringCellValue ?: row.getCell(it)?.toString()) }

                        repo.upsertClient(id = cod, name = if (cliente.isBlank()) cod else cliente, locality = localidad, zone = zona)
                        repo.ensureAssignment(cod, sellerId)
                        clients += cod

                        productCols.forEach { (colIdx, pCode) ->
                            val cell = row.getCell(colIdx)
                            val isOne = when (cell?.cellType) {
                                CellType.STRING -> cell.stringCellValue.trim() == "1"
                                CellType.NUMERIC -> cell.numericCellValue == 1.0
                                else -> false
                            }
                            if (isOne) purchased += cod to pCode
                        }
                    }
                } else if (looksProductSheet) {
                    val productCode = sheet.sheetName.trim()
                    repo.upsertProduct(productCode, productCode)
                    products += productCode

                    val idxCOD = headers.indexOfFirst { it.equals("COD", true) }.takeIf { it >= 0 } ?: 0
                    val idxFlag = (0 until headers.size).firstOrNull { it != idxCOD } ?: 1

                    (1..sheet.lastRowNum).forEach { r ->
                        val row = sheet.getRow(r) ?: return@forEach
                        val cod = clean(row.getCell(idxCOD)?.toString())
                        if (cod.isBlank()) return@forEach
                        val flag = clean(row.getCell(idxFlag)?.toString())
                        if (flag.isNotBlank()) {
                            purchased += cod to productCode
                            clients += cod
                        }
                    }
                }
            }

            val allClients = (repo.getAllClientIds().toSet() + clients).toSet()
            val allProducts = (repo.getAllProductCodes().toSet() + products).toSet()

            allClients.forEach { clientId ->
                allProducts.forEach { pCode ->
                    val state = if ((clientId to pCode) in purchased) Incorporation.INCORPORATED else Incorporation.PENDING
                    repo.upsertClientProductStateByCode(clientId, pCode, state, now)
                    repo.upsertClientProductWeeklyByCode(clientId, pCode, yw, state)
                }
            }

            repo.insertImportBatch(now, "import.xlsx", wb.numberOfSheets, allProducts.size, allClients.size)
        }
    }
}
