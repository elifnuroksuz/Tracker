package com.elifnuroksuz.tracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}


/* BU CLASS NE İŞE YARAR?
AppDatabase sınıfı, Room veritabanı oluşturulurken kullanılan temel yapı taşlarından biridir.
Bu sınıfın görevi, veritabanı nesnesini yapılandırmak ve uygulama içinde veritabanına erişim sağlamak için
DAO'ları sunmaktır. TransactionDao aracılığıyla Transaction tablosu üzerinde veri ekleme,
güncelleme, silme ve sorgulama işlemleri gerçekleştirilir.
 */