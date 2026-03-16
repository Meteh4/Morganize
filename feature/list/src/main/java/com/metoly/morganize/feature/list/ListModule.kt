package com.metoly.morganize.feature.list

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val listModule = module {
    viewModel { ListViewModel(noteRepository = get(), categoryRepository = get()) }
}
