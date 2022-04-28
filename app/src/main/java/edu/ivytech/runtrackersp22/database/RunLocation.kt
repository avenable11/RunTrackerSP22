package edu.ivytech.runtrackersp22.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class RunLocation(@PrimaryKey var date: Date = Date(), var latitude : Double, var longitude : Double)
