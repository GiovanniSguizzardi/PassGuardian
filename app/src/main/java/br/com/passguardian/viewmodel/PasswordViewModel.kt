package br.com.passguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.passguardian.data.model.PasswordItem
import br.com.passguardian.data.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(
    private val repo: PasswordRepository = PasswordRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<PasswordItem>>(emptyList())
    val items: StateFlow<List<PasswordItem>> = _items

    private val _current = MutableStateFlow<PasswordItem?>(null)
    val current: StateFlow<PasswordItem?> = _current

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadList() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _error.value = null
                _items.value = repo.list()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar senhas"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadOne(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _error.value = null
                _current.value = repo.get(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar senha"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearCurrent() {
        _current.value = null
    }

    fun add(title: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _error.value = null
                repo.add(title.trim(), password)
                loadList()
                onDone()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao salvar"
            } finally {
                _loading.value = false
            }
        }
    }

    fun update(id: String, title: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _error.value = null
                repo.update(id, title.trim(), password)
                loadList()
                onDone()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao atualizar"
            } finally {
                _loading.value = false
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _error.value = null
                repo.delete(id)
                loadList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao excluir"
            } finally {
                _loading.value = false
            }
        }
    }
}