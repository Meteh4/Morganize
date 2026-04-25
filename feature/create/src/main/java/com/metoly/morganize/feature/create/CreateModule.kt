package com.metoly.morganize.feature.create

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val createModule = module {
    viewModel { CreateViewModel(
        noteRepository = get(),
        categoryRepository = get(),
        encryptionManager = get(),
        keyManager = get()
    ) }
}
