package com.indolearn.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.indolearn.data.local.riyada.RiyadaDataResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiyadaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var riyadaDataCache: RiyadaDataResponse? = null

    fun getRiyadaData(): RiyadaDataResponse {
        if (riyadaDataCache != null) return riyadaDataCache!!
        
        val inputStream = context.assets.open("riyada_data.json")
        val reader = InputStreamReader(inputStream, "UTF-8")
        val gson = Gson()
        val type = object : TypeToken<RiyadaDataResponse>() {}.type
        
        riyadaDataCache = gson.fromJson(reader, type)
        reader.close()
        
        return riyadaDataCache!!
    }
}
