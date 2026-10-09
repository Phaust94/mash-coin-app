package com.soapp.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soapp.wallet.AppContainer
import com.soapp.wallet.model.AddressBookEntry

private sealed interface Route {
    data object Import : Route
    data object Home : Route
    data object Send : Route
    data object AddressBook : Route
    data object ChainConfig : Route
}

@Composable
fun App(container: AppContainer) {
    val hasWallet = remember { container.walletRepository.hasWallet() }
    var route by remember { mutableStateOf<Route>(if (hasWallet) Route.Home else Route.Import) }
    var importSession by remember { mutableStateOf(0) }

    val sendViewModel: SendViewModel = viewModel(
        factory = SendViewModel.factory(
            container.walletRepository,
            container.chainConfigRepository,
            container.addressBookRepository,
            container.rpcFactory
        )
    )

    when (route) {
        Route.Import -> WalletImportScreen(
            viewModel = viewModel(
                key = "import-$importSession",
                factory = WalletSetupViewModel.factory(container.walletRepository)
            ),
            onDone = { route = Route.Home }
        )

        Route.Home -> HomeScreen(
            viewModel = viewModel(
                factory = HomeViewModel.factory(
                    container.walletRepository,
                    container.chainConfigRepository,
                    container.rpcFactory
                )
            ),
            onSend = { route = Route.Send },
            onAddressBook = { route = Route.AddressBook },
            onConfig = { route = Route.ChainConfig },
            onReimport = { importSession++; route = Route.Import }
        )

        Route.Send -> SendScreen(
            viewModel = sendViewModel,
            onBack = { route = Route.Home }
        )

        Route.AddressBook -> AddressBookScreen(
            viewModel = viewModel(
                factory = AddressBookViewModel.factory(container.addressBookRepository)
            ),
            onBack = { route = Route.Home },
            onSendTo = { entry ->
                sendViewModel.onPick(entry)
                route = Route.Send
            }
        )

        Route.ChainConfig -> ChainConfigScreen(
            viewModel = viewModel(
                factory = ChainConfigViewModel.factory(container.chainConfigRepository)
            ),
            onBack = { route = Route.Home }
        )
    }
}

@Composable
private fun Screen(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}

@Composable
private fun WalletImportScreen(viewModel: WalletSetupViewModel, onDone: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Screen(title = "Import wallet") {
        val derivedAddress = state.derivedAddress
        if (derivedAddress != null) {
            Text("Wallet created. Store this address:")
            Text(
                text = derivedAddress,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDone) { Text("Continue") }
            return@Screen
        }

        Text("Enter your 12- or 24-word recovery phrase. It is stored on this device.")
        OutlinedTextField(
            value = state.phrase,
            onValueChange = viewModel::onPhraseChange,
            label = { Text("Recovery phrase") },
            modifier = Modifier.fillMaxWidth()
        )
        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = viewModel::import, enabled = !state.working) {
            Text(if (state.working) "Importing..." else "Import")
        }
    }
}

@Composable
private fun HomeScreen(
    viewModel: HomeViewModel,
    onSend: () -> Unit,
    onAddressBook: () -> Unit,
    onConfig: () -> Unit,
    onReimport: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Re-fetch whenever the home screen opens (including after re-import or a
    // chain-config change while the ViewModel is still alive).
    LaunchedEffect(Unit) { viewModel.refresh() }

    Screen(title = "Wallet") {
        Text("Address", style = MaterialTheme.typography.labelMedium)
        Text(
            text = state.address.ifBlank { "-" },
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(8.dp))
        Text("Balance", style = MaterialTheme.typography.labelMedium)
        Text(
            text = "${state.balance ?: "--"} ${state.coinAlias}".trim(),
            style = MaterialTheme.typography.headlineMedium
        )
        if (state.loading) {
            CircularProgressIndicator()
        }
        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::refresh) { Text("Refresh") }
            Button(onClick = onSend, enabled = !state.needsConfig) { Text("Send coin") }
        }
        OutlinedButton(onClick = onConfig) { Text("Chain settings") }
        OutlinedButton(onClick = onAddressBook) { Text("Address book") }
        OutlinedButton(onClick = onReimport) { Text("Re-import wallet") }
    }
}

@Composable
private fun SendScreen(viewModel: SendViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    // The ViewModel outlives this screen, so reload the book on every open.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Screen(title = "Send ${state.coinAlias}".trim()) {
        if (state.successHash != null) {
            Text("Transaction sent.")
            Text(text = state.successHash!!, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { viewModel.consumeSuccess(); onBack() }) { Text("Back") }
            return@Screen
        }

        OutlinedTextField(
            value = state.amount,
            onValueChange = viewModel::onAmountChange,
            label = { Text("Amount (${state.coinAlias})") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.destination,
            onValueChange = viewModel::onDestinationChange,
            label = { Text("Destination address") },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.entries.isNotEmpty()) {
            Text("Address book", style = MaterialTheme.typography.labelMedium)
            state.entries.forEach { entry: AddressBookEntry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(entry.name)
                        Text(text = entry.address, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { viewModel.onPick(entry) }) { Text("Use this address") }
                    }
                }
            }
        }

        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = viewModel::send, enabled = !state.sending) {
            Text(if (state.sending) "Sending..." else "Send")
        }
        OutlinedButton(onClick = onBack) { Text("Cancel") }
    }
}

@Composable
private fun AddressBookScreen(
    viewModel: AddressBookViewModel,
    onBack: () -> Unit,
    onSendTo: (AddressBookEntry) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Screen(title = "Address book") {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.address,
            onValueChange = viewModel::onAddressChange,
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = viewModel::add) { Text("Save address") }

        Spacer(Modifier.height(8.dp))
        if (state.entries.isEmpty()) {
            Text("No saved addresses yet.")
        } else {
            state.entries.forEach { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.name)
                            Text(text = entry.address, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { onSendTo(entry) }) { Text("Send") }
                        TextButton(onClick = { viewModel.remove(entry.address) }) { Text("Delete") }
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun ChainConfigScreen(viewModel: ChainConfigViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Screen(title = "Chain settings") {
        OutlinedTextField(
            value = state.rpcUrl,
            onValueChange = viewModel::onRpcUrlChange,
            label = { Text("JSON-RPC URL") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.coinAlias,
            onValueChange = viewModel::onCoinAliasChange,
            label = { Text("Coin alias") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.chainId,
            onValueChange = viewModel::onChainIdChange,
            label = { Text("Chain ID") },
            modifier = Modifier.fillMaxWidth()
        )
        if (state.error != null) {
            Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
        }
        if (state.saved) {
            Text("Configuration saved.")
        }
        Button(onClick = viewModel::save) { Text("Save") }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
