package com.elifnuroksuz.tracker

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * from transactions")
    fun getAll(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Transaction

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)
}





/*
TransactionDao, Room veritabanında Transaction nesneleri üzerinde CRUD işlemleri yapmak için
kullanılan bir DAO arayüzüdür. @Dao anotasyonu ile tanımlanır ve çeşitli Room anotasyonları
(@Query, @Insert, @Delete, @Update) ile veritabanı işlemlerini belirtir. Bu DAO, genellikle MVVM
veya MVC mimarilerinde Model katmanı olarak kullanılır ve veri erişim katmanını yönetir.
*/