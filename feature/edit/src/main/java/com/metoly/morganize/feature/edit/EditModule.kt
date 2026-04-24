package com.metoly.morganize.feature.edit

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val editModule = module {
    viewModel { parameters ->
        EditViewModel(
            noteId = parameters.get(),
            noteRepository = get(),
            categoryRepository = get(),
            encryptionManager = get(),
            keyManager = get()
        )
    }
}
