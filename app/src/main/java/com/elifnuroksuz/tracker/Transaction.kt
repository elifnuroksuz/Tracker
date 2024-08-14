package com.elifnuroksuz.tracker

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val amount: Double,
    val description: String
) : Parcelable











/*
Transaction sınıfı, uygulamanızda işlemleri temsil eden bir veri modelidir.
Room veritabanında bir tablo olarak saklanır ve Parcelable arayüzü sayesinde diğer
etkinliklere veri aktarmak için kullanılabilir. Sınıf, işlemlerin kimliğini, etiketini,
miktarını ve açıklamasını tutar ve bu verileri yönetmek için kullanılır.

Room Veritabanı:
Bu sınıf, Room veritabanında bir tablo olarak kullanılır. Transaction nesneleri veritabanında saklanabilir
ve sorgulanabilir.

Parcelable:
Parcelable arayüzünü implement ederek, Transaction nesnelerini Intent ile bir etkinlikten diğerine
geçirebilir veya bir Bundle içinde saklayabilirsiniz. Bu, Android'de veri aktarımını basitleştirir.
 */