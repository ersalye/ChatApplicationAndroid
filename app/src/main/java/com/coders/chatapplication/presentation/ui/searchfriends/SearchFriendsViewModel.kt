package com.coders.chatapplication.presentation.ui.searchfriends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.coders.chatapplication.domain.model.UserModel
import com.coders.chatapplication.domain.usecase.users.SearchUsersUseCase
import com.coders.chatapplication.presentation.commons.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchFriendsViewModel(
	private val searchUsersUseCase: SearchUsersUseCase
) : BaseViewModel() {

	val searchedUsers = MutableLiveData<List<UserModel>>()

	private val searchChannelFlow = Channel<String>(CONFLATED)

	init {
		searchChannelFlow.consumeAsFlow()
			.debounce(500)
			.onEach {
				searchUsers(it)
			}.launchIn(viewModelScope)
	}

	fun search(queryString: String) {
		viewModelScope.launch(Dispatchers.IO) {
			searchChannelFlow.send(queryString)
		}
	}

	private fun searchUsers(queryString: String) {
		viewModelScope.launch(Dispatchers.IO) {
			searchUsersUseCase(this, queryString) {
				it.either(::handleFailure, ::handleSuccess)
			}
		}
	}

	private fun handleSuccess(users: List<UserModel>) {
		searchedUsers.postValue(users)
	}
}