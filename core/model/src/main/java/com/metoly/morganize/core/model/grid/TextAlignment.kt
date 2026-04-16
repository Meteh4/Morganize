package com.metoly.morganize.core.model.grid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TextAlignment {
    @SerialName("Start")
    Start,
    
    @SerialName("Center")
    Center,
    
    @SerialName("End")
    End
}
