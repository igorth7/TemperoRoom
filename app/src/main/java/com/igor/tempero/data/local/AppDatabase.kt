package com.igor.tempero.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

/** O banco de dados do app, com as 2 entidades (tabelas). */
@Database(entities = [Receita::class, ItemCompra::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun receitaDao(): ReceitaDao
    abstract fun itemCompraDao(): ItemCompraDao

    companion object {
        // @Volatile: todas as threads enxergam sempre o valor mais novo da variável
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton: só existe UMA instância do banco no app inteiro
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tempero_room_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
